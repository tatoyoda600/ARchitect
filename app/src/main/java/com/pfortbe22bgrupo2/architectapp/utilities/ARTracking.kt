package com.pfortbe22bgrupo2.architectapp.utilities

import android.util.Log
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.ar.core.Config
import com.pfortbe22bgrupo2.architectapp.types.Int3
import com.pfortbe22bgrupo2.architectapp.types.ModelPoint
import com.pfortbe22bgrupo2.architectapp.types.Point
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

internal const val MAX_CHECKS_PER_SECOND = 5 // Limit how often frames are checked
internal const val DICT_COORD_ZOOM = 10 // Multiplies points before truncation to increase accuracy (1u ~= 1m, so 10x zoom makes each cell equivalent to ~10cm³; 100x ~= 1cm³/cell)
internal const val DICT_COORD_UNZOOM = 1.0f / DICT_COORD_ZOOM.toFloat() // Reverses the zoom in order to calculate the world position of a cell

//     -z (Away from user)
//      |
//-x ---+--- +x (Right of user)
//      |
//     +z (Towards user)

abstract class ARTracking {
    internal val renderer: Render3D
    private val sceneView: ArSceneView
    private val progressBar: CircularProgressIndicator

    private val checksPerSecond: Int // How many frames will be analyzed every second
    internal var lastFrame: ArFrame? = null // The previous AR frame, to prevent reanalyzing frames

    private var setup: Boolean = false // Prevents update function from running if setup hasn't been completed
    internal var paused: Boolean = false // Prevents update function from running when set
    internal lateinit var frameUpdateFunction: (ArFrame) -> Unit // Function run on every AR frame update
    internal lateinit var onConfirmedPointFunction: (Point) -> Unit // Function to run when a confirmed point is found

    internal val pointIds = mutableListOf<Int>(Int.MIN_VALUE) // List of detected point IDs, in order to avoid repeated points
    internal val points = mutableMapOf<Int,MutableMap<Int,MutableMap<Int,MutableList<Point>>>>() // 3D grid of world cells, containing the detected points located in each cell (Empty cells should not be registered)
    internal val confirmedPoints = mutableListOf<ModelPoint>() // List of confirmed points

    constructor(checksPerSecond: Int, sceneView: ArSceneView, progressBar: CircularProgressIndicator) {
        this.checksPerSecond = Math.min(checksPerSecond, MAX_CHECKS_PER_SECOND)
        this.sceneView = sceneView
        this.progressBar = progressBar
        renderer = Render3D(sceneView, progressBar)
    }

    /** Takes care of setting up the AR scene. */
    internal fun setup(
        frameUpdateFunction: (ArFrame) -> Unit,
        onConfirmedPointFunction: (Point) -> Unit
    ) {
        this.frameUpdateFunction = frameUpdateFunction
        this.onConfirmedPointFunction = onConfirmedPointFunction
        val supportsDepth = sceneView.arSession != null && sceneView.arSession!!.isDepthModeSupported(Config.DepthMode.AUTOMATIC)

        sceneView.apply {
            lightEstimationMode = Config.LightEstimationMode.DISABLED
            depthEnabled = true
            instantPlacementEnabled = true
            planeRenderer.isEnabled = false
            environment = null
            isDepthOcclusionEnabled = supportsDepth
            depthMode = if (supportsDepth) Config.DepthMode.AUTOMATIC else Config.DepthMode.DISABLED
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL
            onArFrame = this@ARTracking::onArFrame
            onArTrackingFailureChanged = { reason ->
                Log.w("ARTracking", reason.toString())
            }
        }

        setup = true
    }

    /** Resets the AR state. */
    open fun reset() {
        paused = false
        pointIds.clear()
        pointIds += Int.MIN_VALUE
        points.clear()
        for (p in confirmedPoints) {
            p.model.destroy()
        }
        confirmedPoints.clear()
    }

    /** Given a cell, finds the first confirmed point that's located in that cell. */
    fun findFirstConfirmedPointInCell(cellIndex: Int3): Int? {
        val cellPoints = points.get(cellIndex.x)?.get(cellIndex.y)?.get(cellIndex.z)

        if (cellPoints != null && cellPoints.size > 0) {
            for (i in 0..confirmedPoints.lastIndex) {
                if (convertAxisToIndex(confirmedPoints[i].position.x) == cellIndex.x
                    && convertAxisToIndex(confirmedPoints[i].position.y) == cellIndex.y
                    && convertAxisToIndex(confirmedPoints[i].position.z) == cellIndex.z
                ) {
                    return i
                }
            }
        }

        return null
    }

    /** Converts an axis (X, Y, or Z) to its cell index version. */
    fun convertAxisToIndex(axis: Float): Int {
        return (axis * DICT_COORD_ZOOM).toInt()
    }

    /** Converts a position to a set of cell indices. */
    fun convertPosToIndexes(pos: Float3): Int3 {
        return Int3(
            convertAxisToIndex(pos.x),
            convertAxisToIndex(pos.y),
            convertAxisToIndex(pos.z)
        )
    }

    /** Handles AR frame updates, executing the 'frameUpdateFunction' when applicable. */
    private fun onArFrame(arFrame: ArFrame) {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return
        }

        // 'Frame.fps(Frame)' gets the time in seconds between the 2 frames, then divides 1 by that, giving you the current fps, if using consecutive frames
        // Here frames are discarded, without saving them, until one gives an fps under the desired amount, effectively limiting the fps
        if (arFrame.fps(lastFrame) < checksPerSecond && !paused) {
            frameUpdateFunction(arFrame)
        }
    }

    /** Scans the grid of cells and removes any cells with no points in them. */
    fun deleteEmptyCells() {
        for (xi in (points.keys.size?: 0) - 1 downTo 0) {
            val xKey = points.keys.elementAt(xi)

            for (yi in (points.get(xKey)?.keys?.size?: 0) - 1 downTo 0) {
                val yKey = points.get(xKey)?.keys?.elementAt(yi)

                for (zi in (points.get(xKey)?.get(yKey)?.keys?.size?: 0) - 1 downTo 0) {
                    val zKey = points.get(xKey)?.get(yKey)?.keys?.elementAt(zi)
                    if ((points.get(xKey)?.get(yKey)?.get(zKey)?.size?: 0) == 0) {
                        points.get(xKey)?.get(yKey)?.remove(zKey)
                    }
                }

                if ((points.get(xKey)?.get(yKey)?.size ?: 0) == 0) {
                    points.get(xKey)?.remove(yKey)
                }
            }

            if ((points.get(xKey)?.size ?: 0) == 0) {
                points.remove(xKey)
            }
        }
    }

    /** Goes through every cell in the grid and applies a given deletion function.
     *
     * Afterwards makes sure to clean up empty cells. */
    fun cleanUpCells(pointDelete: (Int,Int,Int) -> Int): Int {
        var cleanUpCount = 0
        // Iterate through every cell in the dictionary
        for (xKey in points.keys) {
            for (yKey in points.get(xKey)?.keys?: mutableSetOf()) {
                for (zKey in points.get(xKey)?.get(yKey)?.keys?: mutableSetOf()) {
                    cleanUpCount += pointDelete(xKey, yKey, zKey)
                }
            }
        }

        deleteEmptyCells()

        return cleanUpCount
    }

    /** Pauses / Unpauses the AR. */
    fun setPaused(value: Boolean) {
        paused = value
        setLoading(value)
    }

    /** Shows / Hides a spinning circle for when something is loading. */
    fun setLoading(value: Boolean) {
        CoroutineScope(Dispatchers.Main).launch {
            progressBar.isVisible = value
        }
    }

    /*
    /** A frame update function that displays the depth texture of the camera to an ImageView */
    fun rawDepth(arFrame: ArFrame) {
        if (curImage != null)
            curImage?.close()

        try {
            // Depth image is in uint16, at GPU aspect ratio, in native orientation.
            arFrame.frame.acquireRawDepthImage16Bits().use { rawDepth ->
                curImage = rawDepth

                // Confidence image is in uint8, matching the depth image size.
                arFrame.frame.acquireRawDepthConfidenceImage().use { rawDepthConfidence ->
                    // Compare timestamps to determine whether depth is is based on new
                    // depth data, or is a reprojection based on device movement.
                    val thisFrameHasNewDepthData = arFrame.frame.timestamp == rawDepth.timestamp
                    if (thisFrameHasNewDepthData) {
                        val depthData = rawDepth.planes[0].buffer
                        val confidenceData = rawDepthConfidence.planes[0].buffer
                        val width = rawDepth.width
                        val height = rawDepth.height

                        val buffer: ByteBuffer = depthData

                        if (imageView != null) {
                            var bytes = ByteArray(buffer.remaining())
                            buffer.get(bytes)
                            val myBitmap: Bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.size,null);
                            imageView?.setImageBitmap(myBitmap)
                        }
                    }
                }
            }
        } catch (e: NotYetAvailableException) {
            // Depth image is not (yet) available.
        }
    }
    */
}