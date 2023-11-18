package com.pfortbe22bgrupo2.architectapp.utilities

import android.util.Log
import com.google.ar.sceneform.math.Vector3
import com.pfortbe22bgrupo2.architectapp.types.Int3
import dev.romainguy.kotlin.math.Float2
import dev.romainguy.kotlin.math.Float3
import dev.romainguy.kotlin.math.PI
import io.github.sceneview.math.toVector3
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.sign

class MathUtils {
    companion object {
        val EPSILON = 0.000001

        /** Converts radians to the equivalent angle. */
        fun radiansToDegrees(rad: Float): Float {
            // Log.d("FunctionNames", "radiansToDegrees")
            return rad * 180.0f / PI
        }

        /** Calculates the angle of a normalized 2D vector relative to +X. */
        fun angleOf2DVector(x: Float, y: Float): Float {
            // Log.d("FunctionNames", "angleOf2DVector")
            return if (radiansToDegrees(asin(-y)).sign > 0) 360 - radiansToDegrees(acos(x)) else radiansToDegrees(acos(x))
        }

        /** Gets the point of intersection between a ray and a floor at some height. */
        fun rayFloorIntersection(rayOrigin: Float3, rayDir: Float3, floorHeight: Float): Float3? {
            // Log.d("FunctionNames", "rayFloorIntersection")
            val output = rayPlaneIntersection(rayOrigin, rayDir, Float3(0f, floorHeight, 0f), Float3(0f, 1f, 0f))
            return if (output != null) Float3(output.x, floorHeight, output.z) else output
        }

        /** Gets the point of intersection between a ray and a plane. */
        fun rayPlaneIntersection(rayOrigin: Float3, rayDir: Float3, planePoint: Float3, planeNormal: Float3): Float3? {
            // Log.d("FunctionNames", "rayPlaneIntersection")
            val denom = Vector3.dot(planeNormal.toVector3().normalized(), rayDir.toVector3().normalized())

            if (abs(denom) > EPSILON) {
                val offset = planePoint - rayOrigin
                val dist = Vector3.dot(offset.toVector3(), planeNormal.toVector3()) / denom
                return rayOrigin + rayDir * dist
            }
            return null
        }

        /** Discards the whole part of a number, returning only the decimal portion. */
        fun getDecimal(value: Float): Float {
            // Log.d("FunctionNames", "getDecimal")
            return value - value.toInt()
        }

        /** Uses Bresenham's line algorithm to iterate through every grid cell between points (x1; y1) and (x2; y2).
         *
         * With the grid being axis-aligned and each cell corresponding to each pair of whole numbers.
         *
         * cellFunction(x, y) is run at every grid cell, if it resolves to 'true' then execution ends and (x; y) is returned. */
        fun bresenhamLine(x1: Float, y1: Float, x2: Float, y2: Float, cellFunction: (x: Float, y: Float) -> Boolean): Float2? {
            // Log.d("FunctionNames", "bresenhamLine")
            val dirX: Int = if (x1 < x2) 1 else -1 // X direction
            val dirZ: Int = if (y1 < y2) 1 else -1 // Z direction
            val distX: Float = x2 - x1 // X distance
            val distZ: Float = y2 - y1 // Z distance
            val stepX: Float = if (distZ != 0f) dirX * abs(distX / distZ) else 0f // stepX for every Z+1
            val stepZ: Float = if (distX != 0f) dirZ * abs(distZ / distX) else 0f // stepZ for every X+1
            val offsetX: Float = getDecimal(dirX - getDecimal(x1)) // Distance to first X cell border
            val offsetZ: Float = getDecimal(dirZ - getDecimal(y1)) // Distance to first Z cell border

            var xTracker: Float = offsetX // Distance advanced on the X axis
            var zTracker: Float = offsetZ // Distance advanced on the Z axis

            while (abs(xTracker) < distX || abs(zTracker) < distZ) {
                var x: Float
                var y: Float

                // If stepping 1 in the X direction increases Z by less than what stepping 1 in the Z direction increases X
                if (zTracker + abs(stepZ) < xTracker + abs(stepX)) {
                    //advance X
                    xTracker += dirX // Step 1 in the X direction

                    x = x1 + xTracker
                    y = y1 + stepZ * abs(xTracker) // The corresponding Z
                }
                else {
                    //advance Z
                    zTracker += dirZ // Step 1 in the Z direction

                    x = x1 + stepX * abs(zTracker) // The corresponding X
                    y = y1 + zTracker
                }

                if (cellFunction(x, y)) {
                    // If the point resolves to 'true'
                    // Return the point
                    return Float2(x, y)
                }
            }

            return null
        }
    }
}