@file:Suppress("unused")

package util

import kotlin.math.abs
import kotlin.math.absoluteValue
import kotlin.math.sign

open class Point(var x: Int, var y: Int) {
    fun move(direction: Direction): Point =
        this.apply {
            x += direction.xOffset
            y += direction.yOffset
        }

    fun move(xCount: Int, yCount: Int): Point =
        this.apply {
            x += xCount
            y += yCount
        }

    fun isSameLocation(other: DataPoint<*>) = this.x == other.x && this.y == other.y

    fun isNeighboringLocation(other: DataPoint<*>, includeDiagonal: Boolean = true) =
        Direction.entries.filter {
            if (includeDiagonal) {
                true
            } else {
                !it.diagonal
            }
        }.map {
            (x + it.xOffset) to (y + it.yOffset)
        }.any {
            it.first == other.x && it.second == other.y
        }

    fun differenceWith(other: DataPoint<*>) = (this.x - other.x) to (this.y - other.y)

    fun distanceFrom(other: DataPoint<*>) = abs(this.x - other.x) + abs(this.y - other.y)

    fun lineTo(other: Point): List<Point> {
        val xDelta = (other.x - x).sign
        val yDelta = (other.y - y).sign
        val steps = maxOf((x - other.x).absoluteValue, (y - other.y).absoluteValue)
        return (1..steps).scan(this) { last, _ -> Point(last.x + xDelta, last.y + yDelta) }
    }

    val neighbors: Map<Direction, Point>
        get() = Direction.entries.associateWith { Point(x + it.xOffset, y + it.yOffset) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataPoint<*>

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

}

open class DataPoint<T>(x: Int, y: Int, var value: T) : Point(x, y) {
    fun lineTo(other: DataPoint<*>, fill: T) = lineTo(other).map { DataPoint(it.x, it.y, fill) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DataPoint<*>

        if (x != other.x) return false
        if (y != other.y) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    fun copy(): DataPoint<T> = DataPoint(x, y, value)
}

enum class Direction(val xOffset: Int, val yOffset: Int, val diagonal: Boolean = false) {
    Up(0, -1),
    Down(0, 1),
    Left(-1, 0),
    Right(1, 0),
    UpLeft(-1, -1, true),
    DownLeft(-1, 1, true),
    UpRight(1, -1, true),
    DownRight(1, 1, true)
}
