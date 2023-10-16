package com.pfortbe22bgrupo2.architectapp.utilities

import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.PointCloud
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.toVector3
import io.github.sceneview.node.Node
import kotlin.math.abs
import kotlin.math.sign

private const val CONFIRMED_POINT_MODEL = "models/square.glb"

private const val MAX_CHECKS_PER_SECOND = 5
private const val MAX_EXCESS_POINTS = 5000
private const val EXCESS_POINTS_CLEAN_UP_STEP = 1000
private const val CONFIRMED_POINTS_CLEAN_UP_STEP = 500
private const val MIN_CONFIDENCE = 0.5
private const val DICT_COORD_ZOOM = 10
private const val MAX_DISTANCE = 0.1f
private const val MIN_NEIGHBORS = 3
private const val CLEAN_UP_CELL_MIN_POINTS = 2 // Cells with less than this many points will be emptied
private const val CLEAN_UP_CELL_MAX_POINTS = 4 // Cells with more than this many points will be culled
private const val MIN_FLOOR_POINTS = 20
private const val CONFIRMED_POINTS_FLOOR_CHECK_STEP = 50
private const val ONE_POINT_PER_CELL = true // Points that fall into an occupied cell are discarded (Confirmed points will replace unconfirmed points)

open class Point(
    val id: Int,
    val position: Position
)

class ModelPoint(
    val model: Node,
    id: Int,
    position: Position
): Point(id, position)

class Int3(
    val x: Int,
    val y: Int,
    val z: Int,
) {
    operator fun plus(other: Int3): Int3 { return Int3(x + other.x, y + other.y, z + other.z) }
    fun toFloat3(): Float3 { return Float3(x.toFloat(), y.toFloat(), z.toFloat()) }
}

class Outline(
    val points: List<List<Position>>
)

class Floor(
    // Boolean is temporary, there should be a more useful value to store per floor cell
    val grid: MutableMap<Int, MutableMap<Int, Boolean>>,
    val height: Int
)

class ARTracking {
    private val renderer: Render3D
    private val sceneView: ArSceneView

    private val ChecksPerSecond: Int
    private var lastFrame: ArFrame? = null
    private var lastExcessCleanUpStep = 0
    private var lastConfirmedCleanUpStep = 0
    private var lastConfirmedFloorCheckStep = 0

    private var setup: Boolean = false
    private var useFloorHeight = false
    private var floorHeight = Int.MAX_VALUE
    private lateinit var onConfirmedPointFunction: (Point) -> Unit
    private lateinit var frameUpdateFunction: (ArFrame) -> Unit

    private val pointIds = mutableListOf<Int>()
    private val points = mutableMapOf<Int,MutableMap<Int,MutableMap<Int,MutableList<Point>>>>()
    // Currently new points that have enough neighbors are marked as confirmed
    // Points that get neighbors added afterwards are not added
    private val confirmedPoints = mutableListOf<ModelPoint>()

    constructor(checksPerSecond: Int, sceneView: ArSceneView) {
        ChecksPerSecond = Math.min(checksPerSecond, MAX_CHECKS_PER_SECOND)
        this.sceneView = sceneView
        renderer = Render3D(sceneView)
    }

    fun setup(
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

    fun convertAxisToIndex(axis: Float): Int {
        return (axis * DICT_COORD_ZOOM).toInt()
    }

    fun convertPosToIndexes(pos: Float3): Int3 {
        return Int3(
            convertAxisToIndex(pos.x),
            convertAxisToIndex(pos.y),
            convertAxisToIndex(pos.z)
        )
    }

    private fun onArFrame(arFrame: ArFrame) {
        if (arFrame.fps(lastFrame) < ChecksPerSecond) {
            Log.d("ARTracking Stats", "Points: ${pointIds.size}}")
            Log.d("ARTracking Stats", "Confirmed Points: ${confirmedPoints.size}}")

            frameUpdateFunction(arFrame)
        }
    }

    /*
    /** Displays the depth texture of the camera to an ImageView */
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

    fun pointScanning(arFrame: ArFrame) {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return
        }

        val pointCloud: PointCloud = arFrame.frame.acquirePointCloud()
        if (pointCloud.ids != null && pointCloud.timestamp != lastFrame?.frame?.timestamp) {
            lastFrame = arFrame

            val pointCount = pointCloud.ids.limit()
            for (i in 0 until pointCount) {
                if (!pointIds.contains(pointCloud.ids[i])) {
                    val index = i * 4
                    val position = Position(
                        pointCloud.points[index],
                        pointCloud.points[index + 1],
                        pointCloud.points[index + 2]
                    )
                    val confidence = pointCloud.points[index + 3]

                    if (confidence > MIN_CONFIDENCE) {
                        pointIds += pointCloud.ids[i]
                        val point = Point(pointCloud.ids[i], position)
                        if (addPoint(point) >= MIN_NEIGHBORS) {
                            if (!useFloorHeight || convertAxisToIndex(position.y) == floorHeight) {
                                onConfirmedPointFunction(point)
                            }
                        }
                    }
                }
            }

            if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
                cleanUpConfirmedPoints()
            }

            if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {

                if (!ONE_POINT_PER_CELL) {
                    cleanUpCells(this@ARTracking::cleanUpExcessPoints)
                }

                if (pointIds.size - confirmedPoints.size > MAX_EXCESS_POINTS) {
                    cleanUpCells(this@ARTracking::clearExcessPoints)
                }
            }
        }
    }

    fun onConfirmedPoint(point: Point) {
        Log.w("ARTracking Stats", "Confirmed Points: ${point.position}")

        confirmedPoints += ModelPoint(
            renderer.render(
                modelPath = CONFIRMED_POINT_MODEL,
                position = convertPosToIndexes(point.position).toFloat3() / DICT_COORD_ZOOM.toFloat(),
                rotation = Rotation()
            ),
            point.id,
            point.position
        )

        if (confirmedPoints.size > lastConfirmedFloorCheckStep + CONFIRMED_POINTS_FLOOR_CHECK_STEP) {
            lastConfirmedFloorCheckStep += CONFIRMED_POINTS_FLOOR_CHECK_STEP
            val floor = calculateFloorHeight()
            if (floor.second > MIN_FLOOR_POINTS) {
                floorHeight = floor.first
                useFloorHeight = true
                clearNonFloorPoints()
                Log.w("ARTracking Stats", "Floor Height: ${floorHeight}")
            }
        }
    }

    fun addPoint(point: Point): Int {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return -1
        }

        val position = point.position
        val pointIndex = convertPosToIndexes(position)
        val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

        // If each cell is limited to 1 point and the cell for this point already contains a point
        if (ONE_POINT_PER_CELL && (cellPoints?.size?: 0) > 0) {
            // If the point in the cell is not a confirmed point
            if (confirmedPoints.find { it.id == cellPoints?.get(0)?.id } == null) {
                // Remove the point in the cell
                pointIds.remove(cellPoints?.get(0)?.id)
                points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAt(0)
            }
            // If the point in the cell is a confirmed point
            else {
                // Discard this new point
                return -1
            }
        }

        points.getOrPut(pointIndex.x) { mutableMapOf() }
            .getOrPut(pointIndex.y) { mutableMapOf() }
            .getOrPut(pointIndex.z) { mutableListOf() }
            .add(point)

        var neighborCount = -1 // Start at -1 since the point itself will be counted
        for (p in cellPoints?: emptyList()) {
            if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                neighborCount++
                if (neighborCount >= MIN_NEIGHBORS) return neighborCount
            }
        }

        for (scanX in convertAxisToIndex(position.x - MAX_DISTANCE)..convertAxisToIndex(position.x + MAX_DISTANCE)) {
            for (scanY in convertAxisToIndex(position.y - MAX_DISTANCE)..convertAxisToIndex(position.y + MAX_DISTANCE)) {
                for (scanZ in convertAxisToIndex(position.z - MAX_DISTANCE)..convertAxisToIndex(position.z + MAX_DISTANCE)) {
                    for (p in cellPoints?: emptyList()) {
                        if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                            neighborCount++
                            if (neighborCount >= MIN_NEIGHBORS) return neighborCount
                        }
                    }
                }
            }
        }
        return neighborCount
    }

    fun cleanUpCells(pointDelete: (Int,Int,Int) -> Int): Int {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return -1
        }

        var cleanUpCount = 0
        // Iterate through every cell in the dictionary
        for (xKey in points.keys) {
            for (yKey in points.get(xKey)?.keys?: mutableSetOf()) {
                val yDict = points.get(xKey)?.get(yKey)

                for (zKey in yDict?.keys?: mutableSetOf()) {
                    cleanUpCount += pointDelete(xKey, yKey, zKey)
                }

                for (zKey in (yDict?.keys?.size?: -1)downTo 0) {
                    if ((yDict?.get(zKey)?.size ?: 0) == 0) {
                        points.get(xKey)?.get(yKey)?.remove(zKey)
                    }
                }
            }

            for (yKey in (points.get(xKey)?.keys?.size?: -1)downTo 0) {
                if ((points.get(xKey)?.get(yKey)?.size ?: 0) == 0) {
                    points.get(xKey)?.remove(yKey)
                }
            }
        }

        for (xKey in points.keys.size downTo 0) {
            if ((points.get(xKey)?.size ?: 0) == 0) {
                points.remove(xKey)
            }
        }

        Log.d("ARTracking Stats", "Points Culled: ${cleanUpCount}}")
        if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {
            lastExcessCleanUpStep += EXCESS_POINTS_CLEAN_UP_STEP
        }
        return cleanUpCount
    }

    fun cleanUpExcessPoints(xKey: Int, yKey: Int, zKey: Int): Int {
        var cleanUpCount = 0

        val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
        // If this cell has too many or too few points
        if (cellPoints?.size != null && cellPoints.size !in CLEAN_UP_CELL_MIN_POINTS..CLEAN_UP_CELL_MAX_POINTS) {
            for (p in cellPoints.size downTo 0) {
                // If this point is not a confirmed one
                if (confirmedPoints.find { it.id == cellPoints.get(p).id } == null) {
                    // Remove the point
                    pointIds.remove(cellPoints.get(p).id)
                    points.get(xKey)?.get(yKey)?.get(zKey)?.removeAt(p)
                    cleanUpCount++
                }
            }
        }

        return cleanUpCount
    }

    fun clearExcessPoints(xKey: Int, yKey: Int, zKey: Int): Int {
        var cleanUpCount = 0

        val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
        for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
            // If this point is not a confirmed one
            if (confirmedPoints.find { it.id == cellPoints?.get(p)?.id } == null) {
                // Remove the point
                pointIds.remove(cellPoints?.get(p)?.id)
                points.get(xKey)?.get(yKey)?.get(zKey)?.removeAt(p)
                cleanUpCount++
            }
        }

        return cleanUpCount
    }

    fun cleanUpConfirmedPoints(): Int {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return -1
        }

        if (ONE_POINT_PER_CELL) return 0

        var cleanUpCount = 0
        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            val position = confirmedPoints[point].position
            val pointIndex = convertPosToIndexes(position)
            val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

            // If its cell contains more points
            if (cellPoints != null && cellPoints.size > 1) {
                // If there is a previous confirmed point in that cell
                if (findFirstConfirmedPointInCell(pointIndex) != point) {
                    // Remove the point
                    pointIds.remove(confirmedPoints.get(point).id)
                    points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAll { it.id == confirmedPoints.get(point).id }
                    confirmedPoints.get(point).model.destroy()
                    confirmedPoints.removeAt(point)
                    cleanUpCount++
                    break
                }
            }
        }

        for (xKey in points.keys.size downTo 0) {
            if ((points.get(xKey)?.size ?: 0) == 0) {
                points.remove(xKey)
            }
        }

        Log.d("ARTracking Stats", "Confirmed Points Culled: ${cleanUpCount}}")
        if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
            lastConfirmedCleanUpStep += CONFIRMED_POINTS_CLEAN_UP_STEP
        }
        return cleanUpCount
    }

    fun clearNonFloorPoints(): Int {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return -1
        }

        var cleanUpCount = cleanUpCells(this@ARTracking::clearExcessPoints)

        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            val position = confirmedPoints[point].position
            val pointIndex = convertPosToIndexes(position)
            val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

            // If this point is not at floorHeight
            if (pointIndex.y != floorHeight) {
                for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
                    if (confirmedPoints[point].id == cellPoints?.get(p)?.id) {
                        // Remove the point
                        pointIds.remove(confirmedPoints[point].id)
                        points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAt(p)
                        confirmedPoints.get(point).model.destroy()
                        confirmedPoints.removeAt(point)
                        cleanUpCount++
                        break
                    }
                }
            }
        }

        // Remove all cells with Y different from the floor height, and all floor height cells that ended up empty
        for (xKey in points.keys) {
            for (yKey in (points.get(xKey)?.keys?.size?: -1)downTo 0) {
                if (yKey != floorHeight) {
                    points.get(xKey)?.remove(yKey)
                }
                else {
                    for (zKey in (points.get(xKey)?.get(floorHeight)?.keys?.size?: -1)downTo 0) {
                        if ((points.get(xKey)?.get(floorHeight)?.get(zKey)?.size ?: 0) == 0) {
                            points.get(xKey)?.get(floorHeight)?.remove(zKey)
                        }
                    }
                }
            }
        }

        for (xKey in points.keys.size downTo 0) {
            if ((points.get(xKey)?.size ?: 0) == 0) {
                points.remove(xKey)
            }
        }

        Log.d("ARTracking Stats", "Non Floor Points Purged: ${cleanUpCount}}")
        return cleanUpCount
    }

    fun calculateFloorHeight(): Pair<Int,Int> {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return Pair(-1, -1)
        }

        val pointsPerHeight = mutableMapOf<Int,Int>()
        for (p in confirmedPoints) {
            val y = convertAxisToIndex(p.position.y)
            if (!pointsPerHeight.containsKey(y)) {
                pointsPerHeight[y] = confirmedPointsAtHeight(y)
            }
        }

        var curMax: Int = -1
        var curHeight: Int = Int.MAX_VALUE
        for (p in pointsPerHeight) {
            if (p.value > curMax) {
                curMax = p.value
                curHeight = p.key
            }
            else if (p.value == curMax && p.key < curHeight) {
                curHeight = p.key
            }
        }

        return Pair(curHeight, curMax)
    }

    fun confirmedPointsAtHeight(height: Int): Int {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return -1
        }

        var floorChance = 0
        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            // If the point's height is the same as the provided height
            if (convertAxisToIndex(confirmedPoints[point].position.y) == height) {
                floorChance++
            }
        }
        return floorChance
    }

    /** Returns either a completed outline, or 4 partial ones */
    fun getFloorOutline(): Outline {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return Outline(emptyList())
        }

        if (!useFloorHeight || floorHeight == Int.MAX_VALUE || confirmedPoints.size < MIN_FLOOR_POINTS) {
            Log.e("ARTracking", "Floor outline requires the floor height to be active")
            return Outline(emptyList())
        }

        clearNonFloorPoints()

        val minX = Int3(
            points.keys.min(),
            floorHeight,
            points.get(points.keys.min())?.get(floorHeight)?.keys?.first() ?: Int.MAX_VALUE
        )
        val maxX = Int3(
            points.keys.min(),
            floorHeight,
            points.get(points.keys.max())?.get(floorHeight)?.keys?.first() ?: Int.MAX_VALUE
        )

        var minZ: Int3 = Int3(0, floorHeight, Int.MAX_VALUE)
        var maxZ: Int3 = Int3(0, floorHeight, Int.MIN_VALUE)
        for (xKey in points.keys) {
            if ((points.get(xKey)?.get(floorHeight)?.keys?.min() ?: minZ.z) < minZ.z) {
                minZ = Int3(
                    xKey,
                    floorHeight,
                    points.get(xKey)?.get(floorHeight)?.keys?.min() ?: minZ.z
                )
            }
            if ((points.get(xKey)?.get(floorHeight)?.keys?.max() ?: maxZ.z) > maxZ.z) {
                maxZ = Int3(
                    xKey,
                    floorHeight,
                    points.get(xKey)?.get(floorHeight)?.keys?.max() ?: maxZ.z
                )
            }
        }

        // Using the furthest point on each end of the X and Z axis
        //      find as many contiguous points as possible
        //      If the points all connect, then that's a completed outline

        // Minimum X point, moving towards -Z
        val pointList1: List<Position> = findConnectedPoints(
            minX,
            Int3(0,0,-1)
        )
        if (pointList1.size > 1 && pointList1.first() == pointList1.last()) {
            // Looped
            return Outline(listOf(pointList1))
        }

        // Maximum X point, moving towards +Z
        val pointList2: List<Position> = findConnectedPoints(
            maxX,
            Int3(0,0,+1)
        )

        // Minimum Z point, moving towards -X
        val pointList3: List<Position> = findConnectedPoints(
            minZ,
            Int3(-1,0,0)
        )

        // Maximum Z point, moving towards +X
        val pointList4: List<Position> = findConnectedPoints(
            maxZ,
            Int3(+1,0,0)
        )

        return Outline(listOf(pointList1, pointList2, pointList3, pointList4))
    }

    private fun findConnectedPoints(startingCell: Int3, startingDirection: Int3): List<Position> {
        val startPoint: Point? = points.get(startingCell.x)?.get(startingCell.y)?.get(startingCell.z)?.get(0)
        if (startPoint != null) {
            val pointList = mutableListOf<Position>()

            pointList.add(startPoint.position)
            val startIndex = convertPosToIndexes(startPoint.position)

            var direction: Int3 = startingDirection
            var prevPoint: Point = startPoint
            var nextPoint: Point? = nextConnectedPoint(prevPoint, direction)

            while (nextPoint != null) {
                pointList.add(nextPoint.position)
                if (convertAxisToIndex(nextPoint.position.x) == startIndex.x && convertAxisToIndex(nextPoint.position.z) == startIndex.z) {
                    // Looped around back to the starting point
                    break
                }
                val dir = nextPoint.position - prevPoint.position
                direction = Int3(sign(dir.x).toInt(), 0, sign(dir.y).toInt())
                prevPoint = nextPoint
                nextPoint = nextConnectedPoint(prevPoint, direction)
            }

            return pointList
        }
        return emptyList()
    }

    private fun nextConnectedPoint(point: Point, direction: Int3): Point? {
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

    fun calculateFloorGrid(outline: Outline): Floor {
        var grid = mutableMapOf<Int, MutableMap<Int, Boolean>>()
        for (l in outline.points) {
            for (p in l) {
                grid.getOrPut(convertAxisToIndex(p.x)) {mutableMapOf()}
                    .getOrPut(convertAxisToIndex(p.z)) {true}
            }
        }

        var recursiveFill: (Int,Int) -> Unit = { x: Int, y: Int -> }
        recursiveFill = { x: Int, z: Int ->
            if (grid.get(x)?.get(z) == null) {
                grid.getOrPut(x) {mutableMapOf()}
                    .getOrPut(z) {true}
                recursiveFill(x + 1, z)
                recursiveFill(x - 1, z)
                recursiveFill(x, z + 1)
                recursiveFill(x, z - 1)
            }
        }

        val firstCell: Position = outline.points.get(0).get(0)
        val firstDir: Position = outline.points.get(0).get(1) - firstCell
        val left: Int3 = Int3(-sign(firstDir.z).toInt(), 0, sign(firstDir.x).toInt())
        val startCell: Int3 = convertPosToIndexes(firstCell) + left
        recursiveFill(startCell.x, startCell.z)

        return Floor(grid, floorHeight)
    }
}