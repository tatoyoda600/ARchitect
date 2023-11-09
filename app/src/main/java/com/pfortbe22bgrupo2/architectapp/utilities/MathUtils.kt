package com.pfortbe22bgrupo2.architectapp.utilities

import dev.romainguy.kotlin.math.PI
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.sign

class MathUtils {
    companion object {
        /** Converts radians to the equivalent angle. */
        fun radiansToDegrees(rad: Float): Float {
            return rad * 180.0f / PI
        }

        /** Calculates the angle of a normalized 2D vector relative to +X. */
        fun angleOf2DVector(x: Float, y: Float): Float {
            return if (radiansToDegrees(asin(-y)).sign > 0) 360 - radiansToDegrees(acos(x)) else radiansToDegrees(acos(x))
        }
    }
}