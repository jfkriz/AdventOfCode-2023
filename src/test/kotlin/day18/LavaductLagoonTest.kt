package day18

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point
import kotlin.math.abs

@DisplayName("Day 18 - Lavaduct Lagoon")
@TestMethodOrder(OrderAnnotation::class)
class LavaductLagoonTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 62`() {
        assertEquals(62, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 952408144115`() {
        assertEquals(952408144115L, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 36725`() {
        assertEquals(36725, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 97874103749720`() {
        assertEquals(97874103749720L, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val instructions = data.map { Instruction.fromInputLine(it) }
    fun solvePartOne(): Long {
        return findArea(instructions)
    }

    fun solvePartTwo(): Long {
        return findArea(instructions.map { it.swapped })
    }

    private fun findArea(instructions: List<Instruction>): Long {
        val vertices = instructions.map { it.direction * it.steps }
        val positions = vertices.scan(LongPoint(0, 0)) { acc, direction ->
            acc + direction
        }

        val perimeter = vertices.sumOf { abs(it.x) + abs(it.y) }
        val shoelaceArea = positions.zipWithNext { a, b ->
            a.x * b.y - a.y * b.x
        }.sum() / 2

        val area = shoelaceArea + perimeter / 2L + 1L

        return area
    }
}

data class Instruction(val direction: LongPoint, val steps: Long, val color: String) {
    companion object {
        fun fromInputLine(line: String): Instruction {
            val (dir, num, rgb) = line.split(" ")
            return Instruction(dir[0].toDirection(), num.toLong(), rgb.lowercase().replace("[^0-9a-f]".toRegex(), ""))
        }
    }

    val swapped: Instruction
        get() {
            val dir = color.last().toDirection()
            val steps = color.take(5).toLong(16)
            return Instruction(dir, steps, color)
        }
}

private fun Char.toDirection(): LongPoint =
    when (this) {
        '0', 'R' -> LongPoint.RIGHT
        '1', 'D' -> LongPoint.DOWN
        '2', 'L' -> LongPoint.LEFT
        '3', 'U' -> LongPoint.UP
        else -> throw IllegalArgumentException("Invalid direction $this")
    }

data class LongPoint(val x: Long, val y: Long) {
    companion object {
        val UP = fromPoint(Point.UP)
        val DOWN = fromPoint(Point.DOWN)
        val LEFT = fromPoint(Point.LEFT)
        val RIGHT = fromPoint(Point.RIGHT)

        private fun fromPoint(point: Point): LongPoint = LongPoint(point.x.toLong(), point.y.toLong())
    }

    operator fun times(steps: Long) = LongPoint(x * steps, y * steps)

    operator fun plus(other: LongPoint) = LongPoint(x + other.x, y + other.y)
}
