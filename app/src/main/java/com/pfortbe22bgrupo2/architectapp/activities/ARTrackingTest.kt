package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.google.ar.core.PointCloud
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
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

class ARTrackingTest: AppCompatActivity() {
    class Point(
        val id: Int,
        val position: Position
    )

    lateinit var sceneView: ArSceneView
    lateinit var binding: ActivityArtrackingTestBinding
    private var lastFrame: ArFrame? = null
    private var lastExcessCleanUpStep = 0
    private var lastConfirmedCleanUpStep = 0
    private var lastConfirmedFloorCheckStep = 0
    private val pointIds = mutableListOf<Int>()
    private val points = mutableMapOf<Int,MutableMap<Int,MutableMap<Int,MutableList<Point>>>>()
    // Currently new points that have enough neighbors are marked as confirmed
    // Points that get neighbors added afterwards are not added
    private val confirmedPoints = mutableListOf<Point>()

    private var useFloorHeight = false
    private var floorHeight = Int.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sceneView = binding.sceneView.apply {
            lightEstimationMode = Config.LightEstimationMode.DISABLED
            depthEnabled = true
            instantPlacementEnabled = true
            planeRenderer.isEnabled = false
            environment = null
            isDepthOcclusionEnabled = true
            depthMode = Config.DepthMode.AUTOMATIC
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            onArFrame = this@ARTrackingTest::onArFrame
            onArTrackingFailureChanged = { reason ->
                Log.w("TAG", reason.toString())
            }
        }
    }

    private fun onArFrame(arFrame: ArFrame) {
        if (arFrame.fps(lastFrame) < MAX_CHECKS_PER_SECOND) {
            Log.d("Stats", "Points: ${pointIds.size}}")
            Log.d("Stats", "Confirmed Points: ${confirmedPoints.size}}")

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
                                    Log.w("Confirmed Points: ", position.toString())

                                    if (confirmedPoints.size > lastConfirmedFloorCheckStep + CONFIRMED_POINTS_FLOOR_CHECK_STEP) {
                                        lastConfirmedFloorCheckStep += CONFIRMED_POINTS_FLOOR_CHECK_STEP
                                        var floor = calculateFloorHeight()
                                        if (floor.second > MIN_FLOOR_POINTS) {
                                            floorHeight = floor.first
                                            Log.w("Floor Height: ", floorHeight.toString())
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
                    val count = cleanUpConfirmedPoints()
                    Log.d("Cleaning", "Confirmed Points Culled: ${count}}")
                    if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
                        lastConfirmedCleanUpStep += CONFIRMED_POINTS_CLEAN_UP_STEP
                    }
                }

                if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {
                    val count = cleanUpExcessPoints()
                    Log.d("Cleaning", "Points Culled: ${count}}")
                    if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {
                        lastExcessCleanUpStep += EXCESS_POINTS_CLEAN_UP_STEP
                    }
                    if (pointIds.size - confirmedPoints.size > MAX_EXCESS_POINTS) {
                        val count2 = clearExcessPoints()
                        Log.d("Cleaning", "Points Purged: ${count2}}")
                        lastExcessCleanUpStep = 0
                    }
                }
            }
        }
    }

    private fun addPoint(point: Point): Int {
        val position = point.position
        val x = (position.x * DICT_COORD_ZOOM).toInt()
        val y = (position.y * DICT_COORD_ZOOM).toInt()
        val z = (position.z * DICT_COORD_ZOOM).toInt()
        val cellPoints = points.get(x)?.get(y)?.get(z)

        // If each cell is limited to 1 point and the cell for this point already contains a point
        if (ONE_POINT_PER_CELL && cellPoints?.size?:0 > 0) {
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
        for (p in cellPoints?: mutableListOf()) {
            if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                neighborCount++
                if (neighborCount >= MIN_NEIGHBORS) return neighborCount
            }
        }

        for (scanX in ((position.x - MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()..((position.x + MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()) {
            for (scanY in ((position.y - MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()..((position.y + MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()) {
                for (scanZ in ((position.z - MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()..((position.z + MAX_DISTANCE) * DICT_COORD_ZOOM).toInt()) {
                    for (p in cellPoints?: mutableListOf()) {
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

    private fun cleanUpExcessPoints(): Int {
        if (ONE_POINT_PER_CELL) return 0

        var cleanUpCount = 0
        // Iterate through every cell in the dictionary
        for (xKey in points.keys) {
            for (yKey in points.get(xKey)?.keys?: mutableSetOf()) {
                for (zKey in points.get(xKey)?.get(yKey)?.keys?: mutableSetOf()) {
                    val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
                    // If this cell has too many or too few points
                    if (cellPoints?.size?:0 !in CLEAN_UP_CELL_MIN_POINTS..CLEAN_UP_CELL_MAX_POINTS) {
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
        return cleanUpCount
    }

    private fun cleanUpConfirmedPoints(): Int {
        if (ONE_POINT_PER_CELL) return 0

        var cleanUpCount = 0
        // Loop through every confirmed point
        for (point in (confirmedPoints.lastIndex?: -1) downTo 0) {
            val position = confirmedPoints[point].position
            val x = (position.x * DICT_COORD_ZOOM).toInt()
            val y = (position.y * DICT_COORD_ZOOM).toInt()
            val z = (position.z * DICT_COORD_ZOOM).toInt()
            val cellPoints = points.get(x)?.get(y)?.get(z)
            // If its cell contains more points
            if (cellPoints?.size?:0 > 1) {
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
        return cleanUpCount
    }

    private fun clearExcessPoints(): Int {
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
        return cleanUpCount
    }

    private fun calculateFloorHeight(): Pair<Int,Int> {
        var pointsPerHeight = mutableMapOf<Int,Int>()
        for (p in confirmedPoints) {
            val y = (p.position.y * DICT_COORD_ZOOM).toInt()
            if (!pointsPerHeight.containsKey(y)) {
                pointsPerHeight.put(y, confirmedPointsAtHeight(y))
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

    private fun confirmedPointsAtHeight(height: Int): Int {
        var floorChance = 0
        // Loop through every confirmed point
        for (point in (confirmedPoints.lastIndex?: -1) downTo 0) {
            // If the point's height is the same as the provided height
            if ((confirmedPoints[point].position.y * DICT_COORD_ZOOM).toInt() == height) {
                floorChance++
            }
        }
        return floorChance
    }

    private fun enableFloorHeight() {
        useFloorHeight = true
        clearExcessPoints()

        // Loop through every confirmed point
        for (point in (confirmedPoints.lastIndex?: -1) downTo 0) {
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
}