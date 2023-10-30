package com.pfortbe22bgrupo2.architectapp.utilities

import android.util.Log
import com.google.ar.core.PointCloud
import com.pfortbe22bgrupo2.architectapp.types.Floor
import com.pfortbe22bgrupo2.architectapp.types.Int3
import com.pfortbe22bgrupo2.architectapp.types.ModelPoint
import com.pfortbe22bgrupo2.architectapp.types.Outline
import com.pfortbe22bgrupo2.architectapp.types.Point
import com.pfortbe22bgrupo2.architectapp.types.PosDir
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.arcore.ArFrame
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.toVector3
import io.github.sceneview.node.Node
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.math.abs
import kotlin.math.sign


private const val CONFIRMED_POINT_MODEL = "models/square.glb"
private const val OUTLINE_POINT_MODEL = "models/outline_square.glb"
private const val ORANGE_POINT_MODEL = "models/orangeSquare.glb"
private const val PINK_POINT_MODEL = "models/pinkSquare.glb"

private const val ONE_POINT_PER_CELL = true // Points that fall into an occupied cell are discarded (Confirmed points will replace unconfirmed points)

// After this many non-confirmed points are registered, they are all purged
// 3000 when 1 point/cell, 5000 otherwise (Kotlin doesn't allow preprocessing branches for constants)
private const val MAX_EXCESS_POINTS = ONE_POINT_PER_CELL.compareTo(false) * 
                                            3000 + 
                                        (1 - ONE_POINT_PER_CELL.compareTo(false)) * 
                                            5000 

private const val EXCESS_POINTS_CLEAN_UP_STEP = 1000 // After this many non-confirmed points are registered, a clean up ocurrs
private const val CONFIRMED_POINTS_CLEAN_UP_STEP = 500 // After this many confirmed points are registered, a clean up ocurrs
private const val CLEAN_UP_CELL_MIN_POINTS = 2 // Cells with less than this many points will be emptied
private const val CLEAN_UP_CELL_MAX_POINTS = 4 // Cells with more than this many points will be culled

private const val MIN_CONFIDENCE = 0.5 // How confident the sensors have to be about a point to not discard it (0 - 1)
private const val MAX_CELL_DISTANCE = 1 // The radius of cells to check for neighbors
private const val MAX_DISTANCE = MAX_CELL_DISTANCE.toFloat() / DICT_COORD_ZOOM.toFloat() // MAX_CELL_DISTANCE but in distance units
private const val MIN_NEIGHBORS = 3 // The amount of neighbors needed for a point to be confirmed

private const val FLOOR_HEIGHT_CELL_LEEWAY = 1 // Make use of points that aren't at the floor height, but are close enough
private const val FLOOR_LEEWAY_MIN_NEIGHBORS = 5 // The amount of neighbors that the points given leeway must have to be used

private const val MIN_FLOOR_POINTS = 20 // A height must have at least this many points to be considered a valid floor
private const val CONFIRMED_POINTS_FLOOR_CHECK_STEP = 50 // After this many confirmed points are registered, a floor check ocurrs

private const val DELAY_MULTIPLIER: Long = 10 // Multiplies most async delays

class DefaultARTracking : ARTracking {
    private var lastExcessCleanUpStep = 0
    private var lastConfirmedCleanUpStep = 0
    private var lastConfirmedFloorCheckStep = 0

    private var useFloorHeight = false // Changes point verification after a floor height has been discovered
    private var floorHeight = Int.MAX_VALUE // The cell height of the floor
    private var onFloorDetectedFunction: () -> Unit // A function to run when a floor height is discovered

    private var detectedFloor: Floor = Floor() // A grid representing the current confirmed floor area
    private var coloredFloor: Map<Int, Map<Int, Floor.CellState>>? = null // A grid representing the current colored floor area

    /** Sets up the AR state. */
    constructor(checksPerSecond: Int, sceneView: ArSceneView, onFloorDetectedFunction: () -> Unit = fun(){}) : super(checksPerSecond, sceneView) {
        setup(
            this@DefaultARTracking::pointScanning,
            this@DefaultARTracking::onConfirmedPoint
        )
        this.onFloorDetectedFunction = onFloorDetectedFunction
    }

    /** Resets the AR state. */
    override fun reset() {
        lastExcessCleanUpStep = 0
        lastConfirmedCleanUpStep = 0
        lastConfirmedFloorCheckStep = 0
        useFloorHeight = false
        floorHeight = Int.MAX_VALUE
        super.reset()
    }

    /** Confirm the current scan and start the conversion to a Floor, or update a previous Floor with the scan. */
    fun confirm() {
        CoroutineScope(Dispatchers.IO).launch {
            paused = true
            val outlineAsync = async{ getFloorOutline() }
            val outlines: Outline = outlineAsync.await()
            delay(10 * DELAY_MULTIPLIER)

            var floor: Floor = if (detectedFloor.height != Int.MAX_VALUE) detectedFloor else Floor(mutableMapOf(), floorHeight)

            for (s in outlines.segments) {
                for (p in s) {
                    val rightToInside: Int3 = -p.prevDir
                    val angle1: Float = MathUtils.angleOf2DVector(rightToInside.x.toFloat(), rightToInside.z.toFloat())
                    val leftToInside: Int3 = p.nextDir
                    val angle2: Float = MathUtils.angleOf2DVector(leftToInside.x.toFloat(), leftToInside.z.toFloat()) - 360
                    floor.addPoint(convertAxisToIndex(p.position.x), convertAxisToIndex(p.position.z), emitEdge= (angle1 != 0f && angle2 != 0f && angle1 - angle2 < 360))
                }
            }

            delay(10 * DELAY_MULTIPLIER)

            val floorAsync = async{ floor.fillFloor() }
            floorAsync.await()

            delay(10 * DELAY_MULTIPLIER)
            detectedFloor = floor
            callMainFunction = {
                paintFloor()
                paused = false
            }
        }
    }

    /** Process each AR frame.
     *
     * Discard repeated and not confident points. Classify points with enough neighbors as confirmed points.
     *
     * If a floor height has been found, use it to further filter the points that were detected. */
    fun pointScanning(arFrame: ArFrame) {
        val pointCloud: PointCloud = arFrame.frame.acquirePointCloud()
        if (pointCloud.ids != null && pointCloud.timestamp != lastFrame?.frame?.timestamp) {
            lastFrame = arFrame

            val pointCount: Int = pointCloud.ids.limit()
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
                        if (!useFloorHeight || convertAxisToIndex(position.y) == floorHeight) {
                            val point = Point(pointCloud.ids[i], position)
                            if (addPoint(point) >= MIN_NEIGHBORS) {
                                onConfirmedPointFunction(point)
                            }
                        }
                        // Not at floor height
                        else {
                            // Within leeway range
                            if (abs(convertAxisToIndex(position.y) - floorHeight).toFloat() < FLOOR_HEIGHT_CELL_LEEWAY) {
                                position.y = floorHeight.toFloat() * DICT_COORD_UNZOOM
                                val point = Point(pointCloud.ids[i], position)
                                if (addPoint(point, leeway = true) >= FLOOR_LEEWAY_MIN_NEIGHBORS) {
                                    onConfirmedPointFunction(point)
                                }
                                else {
                                    val keys = convertPosToIndexes(position)

                                    clearExcessPoints(keys.x, keys.y, keys.z)

                                    if ((points.get(keys.x)?.get(keys.y)?.get(keys.z)?.size ?: 0) == 0) {
                                        points.get(keys.x)?.get(keys.y)?.remove(keys.z)
                                        if ((points.get(keys.x)?.get(keys.y)?.size ?: 0) == 0) {
                                            points.get(keys.x)?.remove(keys.y)
                                            if ((points.get(keys.x)?.size ?: 0) == 0) {
                                                points.remove(keys.x)
                                            }
                                        }
                                    }
                                }
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
                    cleanUpCells(this@DefaultARTracking::cleanUpExcessPoints)
                    if (pointIds.size - confirmedPoints.size > lastExcessCleanUpStep + EXCESS_POINTS_CLEAN_UP_STEP) {
                        lastExcessCleanUpStep += EXCESS_POINTS_CLEAN_UP_STEP
                    }
                }

                if (pointIds.size - confirmedPoints.size > MAX_EXCESS_POINTS) {
                    cleanUpCells(this@DefaultARTracking::clearExcessPoints)
                }
            }
        }
    }

    /** When a verified point is found, add it to the confirmed points list and render the cell's tile. */
    fun onConfirmedPoint(point: Point) {
        confirmedPoints += ModelPoint(
            renderer.render(
                modelPath = CONFIRMED_POINT_MODEL,
                position = convertPosToIndexes(point.position).toFloat3() * DICT_COORD_UNZOOM,
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
                onFloorDetectedFunction()
                paused = false
                Log.w("ARTracking", "Floor Height: ${floorHeight}")
            }
        }
    }

    /** When a point is found, add it to the list and calculate its neighbor count. */
    fun addPoint(point: Point, leeway: Boolean = false, skipNeighbors: Boolean = false): Int {
        if (point.id != Int.MIN_VALUE) {
            pointIds += point.id
        }
        val position = point.position
        val pointIndex = convertPosToIndexes(position)
        val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

        // If each cell is limited to 1 point and the cell for this point already contains a point
        if (ONE_POINT_PER_CELL && (cellPoints?.size?: 0) > 0) {
            val cellPointsId = cellPoints?.elementAtOrNull(0)?.id
            // If the point in the cell is not a confirmed point
            if (cellPointsId != Int.MIN_VALUE && confirmedPoints.find { it.id == cellPointsId } == null) {
                // Remove the point in the cell
                pointIds.remove(cellPointsId)
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

        if (skipNeighbors) {
            return -1
        }

        var neighborCount = -1 // Start at -1 since the point itself will be counted
        for (p in cellPoints?: emptyList()) {
            if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                neighborCount++
                if ((leeway && neighborCount > FLOOR_LEEWAY_MIN_NEIGHBORS) || neighborCount >= MIN_NEIGHBORS) return neighborCount
            }
        }

        for (scanX in convertAxisToIndex(position.x - MAX_DISTANCE)..convertAxisToIndex(position.x + MAX_DISTANCE)) {
            for (scanY in convertAxisToIndex(position.y - MAX_DISTANCE)..convertAxisToIndex(position.y + MAX_DISTANCE)) {
                for (scanZ in convertAxisToIndex(position.z - MAX_DISTANCE)..convertAxisToIndex(position.z + MAX_DISTANCE)) {
                    for (p in cellPoints?: emptyList()) {
                        if ((p.position.xyz - position.xyz).toVector3().length() < MAX_DISTANCE) {
                            neighborCount++
                            if ((leeway && neighborCount > FLOOR_LEEWAY_MIN_NEIGHBORS) || neighborCount >= MIN_NEIGHBORS) return neighborCount
                        }
                    }
                }
            }
        }
        return neighborCount
    }

    /** Clean up a cell too many points, or clear one with too few. */
    fun cleanUpExcessPoints(xKey: Int, yKey: Int, zKey: Int): Int {
        var cleanUpCount = 0

        val cellPoints = points.get(xKey)?.get(yKey)?.get(zKey)
        // If this cell has too many or too few points
        if (cellPoints?.size != null && cellPoints.size !in CLEAN_UP_CELL_MIN_POINTS..CLEAN_UP_CELL_MAX_POINTS) {
            for (p in (cellPoints.lastIndex?: -1)downTo 0) {
                val cellPointsId = cellPoints.get(p).id
                // If this point is not a confirmed one
                if (cellPointsId != Int.MIN_VALUE && confirmedPoints.find { it.id == cellPointsId } == null) {
                    // Remove the point
                    pointIds.remove(cellPointsId)
                    points.get(xKey)?.get(yKey)?.get(zKey)?.removeAt(p)
                    cleanUpCount++
                }
            }
        }

        return cleanUpCount
    }

    /** Clear all non-confirmed points. */
    fun clearExcessPoints(xKey: Int, yKey: Int, zKey: Int): Int {
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

    /** Clean up cells with more than 1 confirmed point. */
    fun cleanUpConfirmedPoints(): Int {
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
                    val cellPointsId = confirmedPoints.get(point).id
                    if (cellPointsId != Int.MIN_VALUE) {
                        pointIds.remove(cellPointsId)
                    }
                    points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAll { it.id == cellPointsId }
                    confirmedPoints.get(point).model.destroy()
                    confirmedPoints.removeAt(point)
                    cleanUpCount++
                }
            }
        }

        if (confirmedPoints.size > lastConfirmedCleanUpStep + CONFIRMED_POINTS_CLEAN_UP_STEP) {
            lastConfirmedCleanUpStep += CONFIRMED_POINTS_CLEAN_UP_STEP
        }
        return cleanUpCount
    }

    /** Clear all points not at floor height. */
    fun clearNonFloorPoints(): Int {
        if (!paused) {
            paused = true
            runBlocking {
                delay(10 * DELAY_MULTIPLIER)
            }
        }

        var cleanUpCount = cleanUpCells(this@DefaultARTracking::clearExcessPoints)

        runBlocking {
            delay(10 * DELAY_MULTIPLIER)
        }

        // Loop through every confirmed point
        for (point in confirmedPoints.lastIndex downTo 0) {
            val position = confirmedPoints[point].position
            val pointIndex = convertPosToIndexes(position)
            val cellPoints = points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)

            // If this point is not at floorHeight
            if (pointIndex.y != floorHeight) {
                for (p in (cellPoints?.lastIndex?: -1)downTo 0) {
                    val cellPointsId = confirmedPoints[point].id
                    if (cellPointsId == cellPoints?.get(p)?.id) {
                        // Remove the point
                        if (cellPointsId != Int.MIN_VALUE) {
                            pointIds.remove(cellPointsId)
                        }
                        points.get(pointIndex.x)?.get(pointIndex.y)?.get(pointIndex.z)?.removeAt(p)
                        confirmedPoints.get(point).model.destroy()
                        confirmedPoints.removeAt(point)
                        cleanUpCount++
                        break
                    }
                    runBlocking {
                        delay(1)
                    }
                }
            }
        }

        runBlocking {
            delay(10 * DELAY_MULTIPLIER)
        }

        // Remove all cells with Y different from the floor height
        for (xi in (points.keys.size?: 0) -1 downTo 0) {
            val xKey = points.keys.elementAt(xi)

            for (yi in (points.get(xKey)?.keys?.size?: 0) -1 downTo 0) {
                val yKey = points.get(xKey)?.keys?.elementAt(yi)

                if (yKey != floorHeight) {
                    for (zi in (points.get(xKey)?.get(yKey)?.keys?.size?: 0) -1 downTo 0) {
                        val zKey = points.get(xKey)?.get(yKey)?.keys?.elementAt(zi)
                        if ((points.get(xKey)?.get(yKey)?.get(zKey)?.size?: 0) -1 == 0) {
                            points.get(xKey)?.get(yKey)?.remove(zKey)
                        }
                    }

                    if ((points.get(xKey)?.get(yKey)?.size ?: 0) == 0) {
                        points.get(xKey)?.remove(yKey)
                    }
                }
            }

            if ((points.get(xKey)?.size ?: 0) == 0) {
                points.remove(xKey)
            }
        }

        return cleanUpCount
    }

    /** Get the floor height.
     *
     * Calculates the amount of confirmed points at each height.
     *
     * Returns a pair containing (Height with the most points; Amount of points at that height). */
    fun calculateFloorHeight(): Pair<Int,Int> {
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

    /** Given a height, calculates how many confirmed points are in cells at that height. */
    fun confirmedPointsAtHeight(height: Int): Int {
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

    /** Calculates 4 partial/completed outlines, each starting from a different extreme of the grid.
     *
     * Completed outlines have the same first and last point. */
    suspend fun getFloorOutline(): Outline {
        if (!useFloorHeight || floorHeight == Int.MAX_VALUE || confirmedPoints.size < MIN_FLOOR_POINTS) {
            Log.e("ARTracking", "Floor outline requires the floor height to be active")
            return Outline(emptyList())
        }

        clearNonFloorPoints()
        delay(10 * DELAY_MULTIPLIER)

        val minX = Int3(
            points.keys.min(),
            floorHeight,
            points.get(points.keys.min())?.get(floorHeight)?.keys?.first() ?: Int.MAX_VALUE
        )
        val maxX = Int3(
            points.keys.max(),
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

        delay(10 * DELAY_MULTIPLIER)

        // Using the furthest point on each end of the X and Z axis
        //      find as many contiguous points as possible
        //      If the points all connect, then that's a completed outline

        // Minimum X point, moving towards -Z
        val pointList1: List<PosDir> = findConnectedPoints(
            minX,
            Int3(0,0,-1)
        )
        delay(10 * DELAY_MULTIPLIER)

        // Maximum X point, moving towards +Z
        val pointList2: List<PosDir> = findConnectedPoints(
            maxX,
            Int3(0,0,+1)
        )
        delay(10 * DELAY_MULTIPLIER)

        // Minimum Z point, moving towards -X
        val pointList3: List<PosDir> = findConnectedPoints(
            minZ,
            Int3(-1,0,0)
        )
        delay(10 * DELAY_MULTIPLIER)

        // Maximum Z point, moving towards +X
        val pointList4: List<PosDir> = findConnectedPoints(
            maxZ,
            Int3(+1,0,0)
        )

        return Outline(listOf(pointList1, pointList2, pointList3, pointList4))
    }

    /** From a cell and a starting direction, tries to find the longest connected series of points.
     *
     * The algorithm always tries to keep the interior towards the left-hand side, opting to go away from it whenever possible.
     *
     * Returns a list of all the points that were found, along with the direction before and after the point. */
    private fun findConnectedPoints(startingCell: Int3, startingDirection: Int3): List<PosDir> {
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

    /** Colors the cell tile that contains the given position.
     *
     * If the cell has no points, adds a new confirmed point (ID: INT_MIN) there. */
    fun colorPoint(point: Position, state: Floor.CellState) {
        val confirmedIndex: Int? = findFirstConfirmedPointInCell(convertPosToIndexes(point))

        if (confirmedIndex != null) {
            val newModel: Node = renderer.render(
                //modelPath = OUTLINE_POINT_MODEL, // Regular
                modelPath = if (state === Floor.CellState.FILLED) OUTLINE_POINT_MODEL
                    else (if (state === Floor.CellState.EMIT_EDGE) PINK_POINT_MODEL
                    else ORANGE_POINT_MODEL), // For painting cells according to their floor cell states, for debugging
                position = confirmedPoints.get(confirmedIndex).model.position,
                rotation = confirmedPoints.get(confirmedIndex).model.rotation
            )
            confirmedPoints.get(confirmedIndex).model.destroy()
            confirmedPoints.get(confirmedIndex).model = newModel
        }
        // If there are no confirmed points in the given cell
        else {
            // Creates a new confirmed point
            val newPoint: Point = Point(Int.MIN_VALUE, point)
            addPoint(newPoint, skipNeighbors = true)

            confirmedPoints += ModelPoint(
                renderer.render(
                    //modelPath = OUTLINE_POINT_MODEL, // Regular
                    modelPath = if (state === Floor.CellState.FILLED) OUTLINE_POINT_MODEL
                        else (if (state === Floor.CellState.EMIT_EDGE) PINK_POINT_MODEL
                        else ORANGE_POINT_MODEL), // For painting cells according to their floor cell states, for debugging
                    position = convertPosToIndexes(point).toFloat3() * DICT_COORD_UNZOOM,
                    rotation = Rotation()
                ),
                Int.MIN_VALUE,
                point
            )
        }
    }

    /** Colors all the cell tiles of a detected Floor. */
    fun paintFloor() {
        for (x in detectedFloor.grid) {
            for (z in x.value) {
                if (coloredFloor == null
                    //|| (coloredFloor?.get(x.key)?.get(z.key)?: Floor.CellState.UNKNOWN) == Floor.CellState.UNKNOWN // Regular
                    || (coloredFloor?.get(x.key)?.get(z.key)?: Floor.CellState.UNKNOWN) != z.value // For painting cells according to their floor cell state, for debugging purposes
                ) {
                    colorPoint(Int3(x.key, floorHeight, z.key).toFloat3() * DICT_COORD_UNZOOM, z.value)
                }
            }
        }
        coloredFloor = detectedFloor.getGridCopy()
    }
}