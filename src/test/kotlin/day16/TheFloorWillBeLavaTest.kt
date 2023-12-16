package day16

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Direction
import util.Point
import java.util.Stack

@DisplayName("Day 16 - The Floor Will Be Lava")
@TestMethodOrder(OrderAnnotation::class)
class TheFloorWillBeLavaTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 46`() {
        assertEquals(46, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 51`() {
        assertEquals(51, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 7728`() {
        assertEquals(7728, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 8061`() {
        assertEquals(8061, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    val map = MirrorMap.fromInputLines(data)
    fun solvePartOne(): Int {
        val processed = map.processLightBeam(Point(0, 0), Direction.Right)
        val energized = processed.energizedMirrors
        return energized.size
    }

    fun solvePartTwo(): Int {
        val rowMax = map.mirrors.indices.maxOf { row ->
            maxOf(
                map.processLightBeam(Point(0, row), Direction.Right).energizedMirrors.count(),
                map.processLightBeam(Point(map.mirrors[0].size - 1, row), Direction.Left).energizedMirrors.count()
            )
        }
        val colMax = map.mirrors[0].indices.maxOf { col ->
            maxOf(
                map.processLightBeam(Point(col, 0), Direction.Down).energizedMirrors.count(),
                map.processLightBeam(Point(col, map.mirrors.size - 1), Direction.Up).energizedMirrors.count()
            )
        }

        return maxOf(rowMax, colMax)
    }
}

data class MirrorMap(val mirrors: List<List<Mirror>>) {
    companion object {
        fun fromInputLines(lines: List<String>) =
            MirrorMap(lines.map { row ->
                row.map { Mirror(MirrorDirection.fromSymbol(it)) }
            })
    }

    val energizedMirrors = mirrors.flatten().filter { it.isEnergized }

    fun processLightBeam(start: Point, heading: Direction): MirrorMap {
        val remainingDirections = Stack<Pair<Point, Direction>>()
        val directionsVisited = hashSetOf<Pair<Point, Direction>>()
        continueLightBeam(start, heading, mirrors, remainingDirections)
        while (remainingDirections.isNotEmpty()) {
            val (point, direction) = remainingDirections.pop()
            if (point to direction in directionsVisited) {
                continue
            }
            directionsVisited.add(point to direction)
            val next = point.copy().move(direction)
            if (!mirrors.containsPoint(next)) {
                continue
            }
            continueLightBeam(next, direction, mirrors, remainingDirections)
        }

        val newMirrors = mirrors.map { row -> row.map { it.copy() } }

        directionsVisited.forEach { (point, _) ->
            newMirrors[point.y][point.x].energy++
        }

        return MirrorMap(newMirrors)
    }

    private fun continueLightBeam(point: Point, heading: Direction, mirrors: List<List<Mirror>>, remaining: Stack<Pair<Point, Direction>>) {
        remaining.addAll(handleLightBeamAt(point, heading, mirrors))
    }

    private fun handleLightBeamAt(point: Point, direction: Direction, mirrors: List<List<Mirror>>): List<Pair<Point, Direction>> {
        val mirror = mirrors.at(point.x, point.y)

        if (mirror.lightBeamCanPassThrough(direction)) {
            return listOf(point.copy() to direction)
        }

        if (mirror.direction == MirrorDirection.HORIZONTAL && direction.isVertical()) {
            return listOf(Direction.Left, Direction.Right).map { point.copy() to it }
        }

        if (mirror.direction == MirrorDirection.VERTICAL && direction.isHorizontal()) {
            return listOf(Direction.Up, Direction.Down).map { point.copy() to it }
        }

        if (mirror.direction == MirrorDirection.TILTED_RIGHT) {
            return listOf(when (direction) {
                Direction.Up -> Direction.Right
                Direction.Down -> Direction.Left
                Direction.Right -> Direction.Up
                // Direction.Left
                else -> Direction.Down
            }).map { point.copy() to it }
        }

        if (mirror.direction == MirrorDirection.TILTED_LEFT) {
            return listOf(when (direction) {
                Direction.Up -> Direction.Left
                Direction.Down -> Direction.Right
                Direction.Right -> Direction.Down
                // Direction.Left
                else -> Direction.Up
            }).map { point.copy() to it }
        }

        // Shouldn't get here...
        throw IllegalArgumentException("At row $point.y, col $point.x, mirror ${mirror.direction.symbol}, not sure where to go next")
    }

    private fun List<List<Mirror>>.at(x: Int, y: Int): Mirror = this[y][x]

    private fun List<List<Mirror>>.containsPoint(point: Point) =
        point.y >= 0 && point.y < this.size && point.x >= 0 && point.x < this[0].size

}

data class Mirror(val direction: MirrorDirection, var energy: Int = 0) {
    val isEnergized: Boolean
        get() = energy > 0

    fun lightBeamCanPassThrough(direction: Direction) =
        this.direction == MirrorDirection.EMPTY ||
            (direction.isHorizontal() && this.direction == MirrorDirection.HORIZONTAL) ||
            (direction.isVertical() && this.direction == MirrorDirection.VERTICAL)
}

enum class MirrorDirection(val symbol: Char) {
    HORIZONTAL('-'),
    VERTICAL('|'),
    TILTED_RIGHT('/'),
    TILTED_LEFT('\\'),
    EMPTY('.');

    companion object {
        fun fromSymbol(symbol: Char) = entries.firstOrNull { it.symbol == symbol }
            ?: throw IllegalArgumentException("Cannot find mirror direction for symbol '$symbol'")
    }
}

fun Direction.isHorizontal() =
    this == Direction.Right || this == Direction.Left

fun Direction.isVertical() =
    this == Direction.Up || this == Direction.Down

