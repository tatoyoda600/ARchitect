package com.pfortbe22bgrupo2.architectapp.types

import android.util.Log
import kotlinx.coroutines.delay

class Floor(
    val grid: MutableMap<Int, MutableMap<Int, CellState>>,
    val height: Int
) {
    constructor(): this(mutableMapOf(), Int.MAX_VALUE)

    constructor(grid: MutableMap<Int, MutableMap<Int, CellState>>): this(grid, Int.MAX_VALUE)

    enum class CellState {
        UNKNOWN,
        EMIT_EDGE,
        RECEIVE_EDGE,
        FILLED
    }

    /** Adds a point to the grid, updating cells that get blocked.
     *
     * By default adds a receiving edge, but can add an emitting edge. */
    fun addPoint(x: Int, z: Int, emitEdge: Boolean = false) {
        Log.e("POINT", "ADD POINT")
        grid.getOrPut(x) {mutableMapOf()}
            .getOrPut(z) { if (emitEdge) CellState.EMIT_EDGE else CellState.RECEIVE_EDGE }

        if (grid.get(x - 1)?.get(z) != CellState.FILLED && grid.get(x - 1)?.get(z) != CellState.UNKNOWN) {
            grid.get(x - 1)?.put(z, CellState.FILLED)
        }
    }

    /** Analyzes the registered emitting and receiving edges to try and fill in gaps in the grid. */
    suspend fun fillFloor() {
        Log.e("FILL", "Started floor algorithm")
        val maxX = grid.keys.max()
        val xKeys = grid.keys.toSet()
        for (x in xKeys) {
            val zKeys = grid.get(x)?.keys?.toSet()
            for (z in zKeys?: mutableSetOf()) {
                val state: CellState? = grid.get(x)?.get(z)
                if (state == CellState.EMIT_EDGE || state == CellState.RECEIVE_EDGE) {
                    Log.e("FILL", "Start at (${x}, ${z})")
                    if (recursiveFill(x + 1, z, maxX, state)) {
                        grid.get(x)?.put(z, CellState.FILLED)
                    }
                }
                delay(10)
            }
        }
    }

    /** Scans a line going in the +X direction from a receiving or emitting edge, filling in the points if it turns out being valid.
     *
     * Lines starting from emitting edges are valid if they collide with a filled, receiving, or emitting cell.
     *
     * Lines starting from receiving edges are only valid if they collide with another receiving edge. */
    private suspend fun recursiveFill(x: Int, z: Int, maxX: Int, startState: CellState): Boolean {
        delay(10)
        if (x > maxX) {
            Log.d("RECURSIVE", "Past max X")
            return false
        }

        val state: CellState? = grid.get(x)?.get(z)
        if (state == CellState.RECEIVE_EDGE
            || (startState != CellState.RECEIVE_EDGE
                && (state == CellState.EMIT_EDGE
                    || state == CellState.FILLED
                )
            )
        ) {
            Log.d("RECURSIVE", "End at (${x}, ${z})")
            return true
        }

        if (recursiveFill(x + 1, z, maxX, startState)) {
            Log.d("RECURSIVE", "Set filled (${x}, ${z})")
            grid.getOrPut(x) {mutableMapOf()}
                .put(z, CellState.FILLED)
            return true
        }
        return false
    }

    /** Creates an unmutable deep copy of the grid */
    fun getGridCopy(): Map<Int, Map<Int, CellState>> {
        val output: MutableMap<Int, Map<Int, CellState>> = mutableMapOf()
        for (x in grid) {
            val row: MutableMap<Int, CellState> = mutableMapOf()
            for (y in x.value) {
                row.put(y.key, y.value)
            }
            output.put(x.key, row.toMap())
        }
        return output.toMap()
    }
}