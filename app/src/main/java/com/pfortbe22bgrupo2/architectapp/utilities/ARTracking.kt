package com.pfortbe22bgrupo2.architectapp.utilities

import android.util.Log
import com.google.ar.core.Config
import com.google.ar.core.PointCloud
import dev.romainguy.kotlin.math.Float3
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.math.Position
import io.github.sceneview.math.toVector3

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
private const val ONE_POINT_PER_CELL = false // Points that fall into an occupied cell are discarded (Confirmed points will replace unconfirmed points)

class Point(
    val id: Int,
    val position: Position
)

class Outline(
    val points: List<Position>
)

class ARTracking {
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
    private val confirmedPoints = mutableListOf<Point>()

    constructor(checksPerSecond: Int) {
        ChecksPerSecond = Math.min(checksPerSecond, MAX_CHECKS_PER_SECOND)
    }

    fun setup(
        sceneView: ArSceneView,
        frameUpdateFunction: (ArFrame) -> Unit,
        onConfirmedPointFunction: (Point) -> Unit
    ) {
        this.frameUpdateFunction = frameUpdateFunction
        this.onConfirmedPointFunction = onConfirmedPointFunction

        sceneView.apply {
            lightEstimationMode = Config.LightEstimationMode.DISABLED
            depthEnabled = true
            instantPlacementEnabled = true
            planeRenderer.isEnabled = false
            environment = null
            isDepthOcclusionEnabled = true
            depthMode = Config.DepthMode.AUTOMATIC
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            onArFrame = this@ARTracking::onArFrame
            onArTrackingFailureChanged = { reason ->
                Log.w("ARTracking", reason.toString())
            }
        }

        setup = true
    }

    private fun onArFrame(arFrame: ArFrame) {
        if (arFrame.fps(lastFrame) < ChecksPerSecond) {
            Log.d("ARTracking Stats", "Points: ${pointIds.size}}")
            Log.d("ARTracking Stats", "Confirmed Points: ${confirmedPoints.size}}")

            frameUpdateFunction(arFrame)
        }
    }

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
                            if (!useFloorHeight || (position.y * DICT_COORD_ZOOM).toInt() == floorHeight) {
                                confirmedPoints += point

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
                cleanUpExcessPoints()

                if (pointIds.size - confirmedPoints.size > MAX_EXCESS_POINTS) {
                    clearExcessPoints()
                }
            }
        }
    }

    fun onConfirmedPoint(point: Point) {
        Log.w("ARTracking Stats", "Confirmed Points: ${point.position}")

        if (confirmedPoints.size > lastConfirmedFloorCheckStep + CONFIRMED_POINTS_FLOOR_CHECK_STEP) {
            lastConfirmedFloorCheckStep += CONFIRMED_POINTS_FLOOR_CHECK_STEP
            val floor = calculateFloorHeight()
            if (floor.second > MIN_FLOOR_POINTS) {
                floorHeight = floor.first
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
        val x = (position.x * DICT_COORD_ZOOM).toInt()
        val y = (position.y * DICT_COORD_ZOOM).toInt()
        val z = (position.z * DICT_COORD_ZOOM).toInt()
        val cellPoints = points.get(x)?.get(y)?.get(z)

        // If each cell is limited to 1 point and the cell for this point already contains a point
        if (ONE_POINT_PER_CELL && (cellPoints?.size?: 0) > 0) {
            // If the point in the cell is not a confirmed point
            if (!pointIds.contains(cellPoints?.get(0)?.id)) {
                // Remove the point in the cell
                pointIds.remove(cellPoints?.get(0)?.id)
                points.get(x)?.get(y)?.get(z)?.removeAt(0)
            }
            // If the point in the cell is a confirmed point
            else {
                // Discard this new point
                return -1
            }
        }

        points.getOrPut(x) { mutableMapOf() }
            .getOrPut(y) { mutableMapOf() }
            .getOrPut(z) { mutableListOf() }
            .add(point)

        var neighborCount = -1 // Start at -1 since the point itself will be counted
        for (p in cellPoints?: emptyList()) {
            if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                neighborCount++
                if (neighborCount >= MIN_NEIGHBORS) return neighborCount
            }
        }

        for (scanX in ((position.x - MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()..((position.x + MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()) {
            for (scanY in ((position.y - MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()..((position.y + MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()) {
                for (scanZ in ((position.z - MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()..((position.z + MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()) {
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

    fun cleanUpExcessPoints(): Int {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return -1
        }

        if (ONE_POINT_PER_CELL) return 0

        var cleanUpCount = 0
        // Iterate through every cell in the dictionary
        for (xKey in points.keys) {
            for (yKey in points.get(xKey)?.keys?: mutableSetOf()) {
                for (zKey in points.get(xKey)?.get(yKey)?.keys?: mutableSetOf()) {
                    val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
                    // If this cell has too many or too few points
                    if ((cellPoints?.size?: 0) !in CLEAN_UP_CELL_MIN_POINTS..CLEAN_UP_CELL_MAX_POINTS
                    ) {
                        for (p in (cellPoints?.size?: 0)downTo 0) {
                            // If this point is not a confirmed one
                            if (confirmedPoints.firstOrNull { it.id == cellPoints?.get(p)?.id } == null) {
                                // Remove the point
                                pointIds.remove(cellPoints?.get(p)?.id)
                                points.get(xKey)?.get(yKey)?.get(zKey)?.removeAt(p)
                                cleanUpCount++
                            }
                        }
                    }
                }
            }
        }

        Log.d("ARTracking Stats", "Points Culled: ${cleanUpCount}}")
        if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {
            lastExcessCleanUpStep += EXCESS_POINTS_CLEAN_UP_STEP
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
            val x = (position.x * DICT_COORD_ZOOM).toInt()
            val y = (position.y * DICT_COORD_ZOOM).toInt()
            val z = (position.z * DICT_COORD_ZOOM).toInt()
            val cellPoints = points.get(x)?.get(y)?.get(z)
            // If its cell contains more points
            if ((cellPoints?.size?: 0) > 1) {
                for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
                    // If this point is a different confirmed point
                    if (cellPoints?.get(p)?.id != confirmedPoints[point].id && confirmedPoints.firstOrNull { it.id == cellPoints?.get(p)?.id } == null) {
                        // Remove the point
                        pointIds.remove(cellPoints?.get(p)?.id)
                        points.get(x)?.get(y)?.get(z)?.removeAt(p)
                        confirmedPoints.removeAt(point)
                        cleanUpCount++
                        break
                    }
                }
            }
        }

        Log.d("ARTracking Stats", "Confirmed Points Culled: ${cleanUpCount}}")
        if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
            lastConfirmedCleanUpStep += CONFIRMED_POINTS_CLEAN_UP_STEP
        }
        return cleanUpCount
    }

    fun clearExcessPoints(): Int {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return -1
        }

        var cleanUpCount = 0
        // Iterate through every cell in the dictionary
        for (xKey in points.keys) {
            for (yKey in points.get(xKey)?.keys?: mutableSetOf()) {
                for (zKey in points.get(xKey)?.get(yKey)?.keys?: mutableSetOf()) {
                    val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
                    for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
                        // If this point is not a confirmed one
                        if (confirmedPoints.firstOrNull { it.id == cellPoints?.get(p)?.id } == null) {
                            // Remove the point
                            pointIds.remove(cellPoints?.get(p)?.id)
                            points.get(xKey)?.get(yKey)?.get(zKey)?.removeAt(p)
                            cleanUpCount++
                        }
                    }
                }
            }
        }

        Log.d("ARTracking Stats", "Points Purged: ${cleanUpCount}}")
        lastExcessCleanUpStep = 0
        return cleanUpCount
    }

    fun calculateFloorHeight(): Pair<Int,Int> {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return Pair(-1, -1)
        }

        val pointsPerHeight = mutableMapOf<Int,Int>()
        for (p in confirmedPoints) {
            val y = (p.position.y * DICT_COORD_ZOOM).toInt()
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
            if ((confirmedPoints[point].position.y * DICT_COORD_ZOOM).toInt() == height) {
                floorChance++
            }
        }
        return floorChance
    }

    fun enableFloorHeight() {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return
        }

        useFloorHeight = true
        clearExcessPoints()

        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            val position = confirmedPoints[point].position
            val x = (position.x * DICT_COORD_ZOOM).toInt()
            val y = (position.y * DICT_COORD_ZOOM).toInt()
            val z = (position.z * DICT_COORD_ZOOM).toInt()
            val cellPoints = points.get(x)?.get(y)?.get(z)

            // If this point is not at floorHeight
            if (y != floorHeight) {
                for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
                    if (confirmedPoints[point].id == cellPoints?.get(p)?.id) {
                        // Remove the point
                        pointIds.remove(confirmedPoints[point].id)
                        points.get(x)?.get(y)?.get(z)?.removeAt(p)
                        confirmedPoints.removeAt(point)
                        break
                    }
                }
            }
        }
    }

    fun getFloorOutline(): Outline {
        if (!setup) {
            Log.e("ARTracking", "ARTracking not set up correctly")
            return Outline(emptyList())
        }

        if (!useFloorHeight || floorHeight == Int.MAX_VALUE || confirmedPoints.size < MIN_FLOOR_POINTS) {
            Log.e("ARTracking", "Floor outline requires the floor height to be active")
            return Outline(emptyList())
        }

        clearExcessPoints()

        val minMaxX = Pair(points.keys.min(), points.keys.max())
        val minMaxZ = fun(): Pair<Int, Int> {
            var min: Int = Int.MAX_VALUE
            var max: Int = Int.MIN_VALUE
            for (xKey in points.keys) {
                if ((points.get(xKey)?.get(floorHeight)?.keys?.min()?: min) < min) {
                    min = points.get(xKey)?.get(floorHeight)?.keys?.min()?: min
                }
                if ((points.get(xKey)?.get(floorHeight)?.keys?.max()?: max) > max) {
                    max = points.get(xKey)?.get(floorHeight)?.keys?.max()?: max
                }
            }

            return Pair(min, max)
        }

        // Using the furthest point on each end of the X and Z axis
        //      find as many contiguous points as possible
        //      If the points all connect, then that's an outline
        TODO("Search for consecutive points from all 4 extremes to show outline progress better")

        // Minimum X point, moving towards -Z
        val zPos: Int? = points.get(minMaxX.first)?.get(floorHeight)?.keys?.first()
        if (zPos != null) {
            val pointList = findConnectedPoints(
                Triple(minMaxX.first, floorHeight, zPos),
                Float3(0.0f,0.0f,-1.0f)
            )

            if (pointList.size > 1 && pointList.first() == pointList.last()) {
                // Looped
                return Outline(pointList)
            }
        }

        return Outline(emptyList())
    }

    private fun findConnectedPoints(startingCell: Triple<Int, Int, Int>, startingDirection: Float3): List<Position> {
        val startPoint: Point? = points.get(startingCell.first)?.get(startingCell.second)?.get(startingCell.third)?.get(0)
        if (startPoint != null) {
            val pointList = mutableListOf<Position>()

            pointList.add(startPoint.position)
            val startX = (startPoint.position.x * DICT_COORD_ZOOM).toInt()
            val startZ = (startPoint.position.z * DICT_COORD_ZOOM).toInt()

            var direction: Float3 = Float3(0.0f,0.0f,-1.0f)
            var prevPoint: Point? = startPoint
            var nextPoint: Point? = nextConnectedPoint(prevPoint, direction)

            while (nextPoint != null && prevPoint != null) {
                pointList.add(nextPoint.position)
                if ((nextPoint.position.x * DICT_COORD_ZOOM).toInt() == startX && (nextPoint.position.z * DICT_COORD_ZOOM).toInt() == startZ) {
                    // Looped around back to the starting point
                    break
                }

                direction = nextPoint.position - prevPoint.position
                prevPoint = nextPoint
                nextPoint = nextConnectedPoint(prevPoint, direction)
            }

            return pointList
        }
        return emptyList()
    }

    private fun nextConnectedPoint(point: Point?, direction: Float3): Point? {
        // Using the given point, find a point in an adjacent cell
        //      First check away from the center respective to the direction
        //      Then intermediate cells until reaching the one along the direction
        //      Finally intermediate cells until reaching the opposite of the direction
        // Once a point has been found, return it
        // If no point is found, return null
        TODO("Implement logic for getting the next connected point")

        return null
    }
}