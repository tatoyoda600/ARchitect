package com.pfortbe22bgrupo2.architectapp.utilities

import android.util.Log
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.ar.core.Config
import com.pfortbe22bgrupo2.architectapp.types.Int3
import com.pfortbe22bgrupo2.architectapp.types.ModelPoint
import com.pfortbe22bgrupo2.architectapp.types.Point
import com.pfortbe22bgrupo2.architectapp.types.PosDir
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.sign

internal const val MAX_CHECKS_PER_SECOND = 5 // Limit how often frames are checked
internal const val DICT_COORD_ZOOM = 10 // Multiplies points before truncation to increase accuracy (1u ~= 1m, so 10x zoom makes each cell equivalent to ~10cm³; 100x ~= 1cm³/cell)
internal const val DICT_COORD_UNZOOM = 1.0f / DICT_COORD_ZOOM.toFloat() // Reverses the zoom in order to calculate the world position of a cell
private const val STARTUP_DELAY = 2f // Amount of seconds to wait before starting the AR scanning (To allow the camera to focus)

//     -z (Away from user)
//      |
//-x ---+--- +x (Right of user)
//      |
//     +z (Towards user)

abstract class ARTracking(
    checksPerSecond: Int,
    internal val sceneView: ArSceneView,
    private val progressBar: CircularProgressIndicator
) {
    internal val renderer: Render3D

    private val checksPerSecond: Int // How many frames will be analyzed every second
    internal var lastFrame: ArFrame? = null // The previous AR frame, to prevent reanalyzing frames

    @Volatile
    private var paused: Boolean = false // Prevents update function from running when set

    internal val pointIds = mutableListOf<Int>(Int.MIN_VALUE) // List of detected point IDs, in order to avoid repeated points
    internal val points = mutableMapOf<Int,MutableMap<Int,MutableMap<Int,MutableList<Point>>>>() // 3D grid of world cells, containing the detected points located in each cell (Empty cells should not be registered)
    internal val confirmedPoints = mutableListOf<ModelPoint>() // List of confirmed points

    companion object {
        /** Converts an axis (X, Y, or Z) to its cell index version. */
        fun convertAxisToIndex(axis: Float): Int {
            // Log.d("FunctionNames", "convertAxisToIndex")
            return floor(axis * DICT_COORD_ZOOM).toInt()
        }

        /** Converts a position to a set of cell indices. */
        fun convertPosToIndexes(pos: Float3): Int3 {
            // Log.d("FunctionNames", "convertPosToIndexes")
            return Int3(
                floor(pos.x * DICT_COORD_ZOOM).toInt(),
                floor(pos.y * DICT_COORD_ZOOM).toInt(),
                floor(pos.z * DICT_COORD_ZOOM).toInt()
            )
        }

        /** Converts a position to a set of cell indices. */
        fun convertIndexesToCellCenter(pos: Int3): Float3 {
            // Log.d("FunctionNames", "convertPosToIndexes")
            return Float3(
                ((pos.x + 0.5f) * DICT_COORD_UNZOOM),
                ((pos.y + 0.5f) * DICT_COORD_UNZOOM),
                ((pos.z + 0.5f) * DICT_COORD_UNZOOM)
            )
        }
    }

    init {
        // Log.d("FunctionNames", "init")
        setPaused(true)
        this.checksPerSecond = Math.min(checksPerSecond, MAX_CHECKS_PER_SECOND)
        renderer = Render3D(sceneView, progressBar)
        setup()
    }

    /** Function run on every AR frame update */
    internal abstract fun frameUpdate(arFrame: ArFrame)

    /** Function to run when a confirmed point is found */
    internal abstract fun onConfirmedPoint(point: Point)

    /** Takes care of setting up the AR scene. */
    internal open fun setup() {
        // Log.d("FunctionNames", "setup")
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

        CoroutineScope(Dispatchers.IO).launch {
            delay((STARTUP_DELAY * 1000).toLong())
            setPaused(false)
            setLoading(true)
        }
    }

    /** Resets the AR state. */
    open fun reset() {
        // Log.d("FunctionNames", "reset")
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
    internal fun findFirstConfirmedPointInCell(cellIndex: Int3): Int? {
        // Log.d("FunctionNames", "findFirstConfirmedPointInCell")
        val cellPoints = points.get(cellIndex.x)?.get(cellIndex.y)?.get(cellIndex.z)

        if (cellPoints != null && cellPoints.size > 0) {
            for (i in 0..confirmedPoints.lastIndex) {
                //// Log.d("FunctionNames", "iterateConfirmedPoints ${i} / ${confirmedPoints.lastIndex} (${confirmedPoints.size})")
                val point = confirmedPoints.get(i)
                // Log.d("FunctionNames", "point: ${point}")
                if (convertAxisToIndex(point.position.x) == cellIndex.x
                    && convertAxisToIndex(point.position.y) == cellIndex.y
                    && convertAxisToIndex(point.position.z) == cellIndex.z
                ) {
                    //// Log.d("FunctionNames", "foundConfirmedPoint ${i} / ${confirmedPoints.lastIndex} (${confirmedPoints.size})")
                    return i
                }
            }
        }

        return null
    }

    /** Handles AR frame updates, executing the 'frameUpdateFunction' when applicable. */
    private fun onArFrame(arFrame: ArFrame) {
        // Log.d("FunctionNames", "onArFrame")
        // 'Frame.fps(Frame)' gets the time in seconds between the 2 frames, then divides 1 by that, giving you the current fps, if using consecutive frames
        // Here frames are discarded, without saving them, until one gives an fps under the desired amount, effectively limiting the fps
        if (arFrame.fps(lastFrame) < checksPerSecond && !paused) {
            frameUpdate(arFrame)
        }
    }

    /** Scans the grid of cells and removes any cells with no points in them. */
    private fun deleteEmptyCells() {
        // Log.d("FunctionNames", "deleteEmptyCells")
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
    internal fun cleanUpCells(pointDelete: (Int,Int,Int) -> Int): Int {
        // Log.d("FunctionNames", "cleanUpCells")
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

    /** Clear all non-confirmed points. */
    internal open fun clearExcessPoints(xKey: Int, yKey: Int, zKey: Int): Int {
        // Log.d("FunctionNames", "clearExcessPoints")
        var cleanUpCount = 0

        val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
        for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
            val cellPointsId = cellPoints?.get(p)?.id
            // If this point is not a confirmed one
            if (cellPointsId != Int.MIN_VALUE && confirmedPoints.find { it.id == cellPointsId } == null) {
                // Remove the point
                pointIds.remove(cellPointsId)
                points.get(xKey)?.get(yKey)?.get(zKey)?.removeAt(p)
                cleanUpCount++
            }
        }

        return cleanUpCount
    }

    /** Pauses / Unpauses the AR. */
    internal fun setPaused(value: Boolean) {
        // Log.d("FunctionNames", "setPaused")
        paused = value
        setLoading(value)
    }

    /** Shows / Hides a spinning circle for when something is loading. */
    internal fun setLoading(value: Boolean) {
        // Log.d("FunctionNames", "setLoading")
        CoroutineScope(Dispatchers.Main).launch {
            progressBar.isVisible = value
        }
    }

    internal fun renderFirebaseModel(modelCategory: String, modelName: String, scale: Float, position: Position) {
        Log.d("FunctionNames", "renderFirebaseModel")
        renderer.renderFromFirebase(
            modelCategory,
            modelName,
            position,
            Rotation(),
            Scale(scale),
            onSuccess = { node: Node ->
                Log.e("AR","Spawned object from Firebase")
            },
            onFailure = {
                Log.e("AR","ERROR: Failed to spawn object from Firebase")
            }
        )
    }

    /** From a cell and a starting direction, tries to find the longest connected series of points.
     *
     * The algorithm always tries to keep the interior towards the left-hand side, opting to go away from it whenever possible.
     *
     * Returns a list of all the points that were found, along with the direction before and after the point. */
    internal fun findConnectedPoints(startingCell: Int3, startingDirection: Int3): List<PosDir> {
        // Log.d("FunctionNames", "findConnectedPoints")
        val startPoint: Point? = points.get(startingCell.x)?.get(startingCell.y)?.get(startingCell.z)?.first()
        if (startPoint != null) {
            val pointList = mutableListOf<PosDir>()

            val startIndex = convertPosToIndexes(startPoint.position)

            var direction: Int3 = startingDirection
            var prevPoint: Point = startPoint
            var nextPoint: Point? = nextConnectedPoint(prevPoint, direction)

            while (nextPoint != null) {
                val prevDir: Int3 = direction
                val dir = convertPosToIndexes(nextPoint.position) - convertPosToIndexes(prevPoint.position)
                direction = Int3(dir.x.sign, 0, dir.z.sign)

                pointList.add(PosDir(prevPoint.position,  prevDir, direction))

                if (pointList.size > 1 && convertAxisToIndex(prevPoint.position.x) == startIndex.x && convertAxisToIndex(prevPoint.position.z) == startIndex.z) {
                    // Looped around back to the starting point
                    break
                }

                prevPoint = nextPoint
                nextPoint = nextConnectedPoint(prevPoint, direction)
            }

            return pointList
        }
        return emptyList()
    }

    /** Given a point and a direction, returns the following connected point.
     *
     * Always tries to go towards the right from the starting direction, heading away from the left.
     *
     * Never goes backwards or back right, in order to avoid loops when called multiple times. */
    private fun nextConnectedPoint(point: Point, direction: Int3): Point? {
        // Log.d("FunctionNames", "nextConnectedPoint")
        // Using the given point, find a point in an adjacent cell
        //      First check away from the center respective to the direction
        //      Then intermediate cells until reaching the one along the direction
        //      Finally intermediate cells until reaching the opposite of the direction
        // Once a point has been found, return it
        // If no point is found, return null

        val startCellX: (Int, Int) -> Int = {x: Int, z: Int -> z}
        val startCellZ: (Int, Int) -> Int = {x: Int, z: Int -> -x}

        // Functions to get the next cell offset in the cycle
        //      Sign inputs and outputs (Values always 1, 0, or -1)
        val nextCellX: (Int, Int) -> Int = {x: Int, z: Int -> ((x - z) * (1 + abs(x + z)) * 0.5f).toInt()}
        val nextCellZ: (Int, Int) -> Int = {x: Int, z: Int -> nextCellX(x, -z)}

        val pointIndex = convertPosToIndexes(point.position)
        var checkCell: Int3 = Int3(startCellX(direction.x, direction.z), 0, startCellZ(direction.x, direction.z))

        for (i in 1..6) {
            val cellIndex: Int3 = Int3(pointIndex.x + checkCell.x, pointIndex.y, pointIndex.z + checkCell.z)
            val confirmedIndex: Int? = findFirstConfirmedPointInCell(cellIndex)

            if (confirmedIndex != null) {
                return confirmedPoints[confirmedIndex]
            }

            checkCell = Int3(nextCellX(checkCell.x, checkCell.z), 0, nextCellZ(checkCell.x, checkCell.z))
        }

        return null
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