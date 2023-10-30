package com.pfortbe22bgrupo2.architectapp.types

import dev.romainguy.kotlin.math.Float3

class Int3(
    val x: Int,
    val y: Int,
    val z: Int,
) {
    operator fun plus(other: Int3): Int3 { return Int3(x + other.x, y + other.y, z + other.z) }
    operator fun minus(other: Int3): Int3 { return Int3(x - other.x, y - other.y, z - other.z) }
    override operator fun equals(other: Any?): Boolean { return (other is Int3) && x == other.x && y == other.y && z == other.z }
    fun toFloat3(): Float3 { return Float3(x.toFloat(), y.toFloat(), z.toFloat()) }
    operator fun unaryMinus(): Int3 { return Int3(-x, -y, -z) }
}