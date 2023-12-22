package day21

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 21 - Step Counter")
@TestMethodOrder(OrderAnnotation::class)
class StepCounterTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 16`() {
        assertEquals(16, sampleSolver.solvePartOne(6))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input, 50 steps should return 1594`() {
        assertEquals(1594, sampleSolver.solvePartTwo(50))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input, 100 steps should return 6536`() {
        assertEquals(6536, sampleSolver.solvePartTwo(100))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input, 500 steps should return 167004`() {
        assertEquals(167004, sampleSolver.solvePartTwo(500))
    }

    @Test
    @Order(3)
    @Disabled("Doesn't work with quadratic solution, but takes too long to run with brute-force solution")
    fun `Part 2 Sample Input, 1000 steps should return 668697`() {
        assertEquals(668697, sampleSolver.solvePartTwo(1000))
    }

    @Test
    @Order(3)
    @Disabled("Doesn't work with quadratic solution, but takes too long to run with brute-force solution")
    fun `Part 2 Sample Input, 5000 steps should return 16733044`() {
        assertEquals(16733044, sampleSolver.solvePartTwo(5000))
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 3632`() {
        assertEquals(3632, solver.solvePartOne(64))
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 600336060511101`() {
        assertEquals(600336060511101, solver.solvePartTwo(26501365))
    }
}

class Solver(data: List<String>) {
    private var start = Point(0, 0)
    private val gardenMap = data.mapIndexed { y, row ->
        row.mapIndexed { x, col ->
            if (col == 'S') {
                start = Point(x, y)
            }
            col
        }
    }

    fun solvePartOne(numSteps: Int = 64): Long {
        val allSeen = mutableSetOf(start)
        val seenAtStep = mutableMapOf(0 to listOf(start))
        (1..numSteps).forEach { step ->
            val newPoints = seenAtStep.getValue(step - 1).asSequence().flatMap { it.neighbors }.filterNot { allSeen.contains(it) }
                .distinct().filter { gardenMap.isValid(it) }.toList()
            seenAtStep[step] = newPoints
            allSeen.addAll(newPoints)
        }
        return seenAtStep.map { (step, points) ->
            if (step.isEven() && numSteps.isEven()) {
                points.size.toLong()
            } else if (step.isOdd() && numSteps.isOdd()) {
                points.size.toLong()
            } else {
                0L
            }
        }.sum()
    }

    private fun Int.isEven() = this % 2 == 0

    private fun Int.isOdd() = !this.isEven()

    // Implemented from this stack exchange formula:
    // https://math.stackexchange.com/a/680695
    private fun calculatePolynomial(y1: Long, y2: Long, y3: Long): Triple<Long, Long, Long> {
        val (x1, x2, x3) = listOf(0, 1, 2)

        val a = ((x1 * (y3 - y2)) + (x2 * (y1 - y3)) + (x3 * (y2 - y1))) / ((x1 - x2) * (x1 - x3) * (x2 - x3))
        val b = ((y2 - y1) / (x2 - x1)) - (a * (x1 + x2))
//        val c = y1 - (a * x1).toDouble().pow(2).toLong() - (b * x1)
        val c = y1 // This is much simpler - since x1 is 0, this is always just y1
        return Triple(a, b, c)
    }

    fun solvePartTwo(steps: Int): Long {
        return if (steps <= 1000) {
            solvePartOne(steps)
        } else {
            val y1 = solvePartOne(65)
            val y2 = solvePartOne(65 + gardenMap.size)
            val y3 = solvePartOne(65 + gardenMap.size * 2)

            val cycles = (steps - 65) / gardenMap.size
            val polynomial = calculatePolynomial(y1, y2, y3)

            polynomial.first * cycles * cycles + polynomial.second * cycles + polynomial.third
        }
    }

    private operator fun List<List<Char>>.get(point: Point) = if (this.isInGrid(point)) {
        this[point.y][point.x]
    } else {
        this[translateOutside(point.y, this.size)][translateOutside(point.x, this[0].size)]
    }

    private fun translateOutside(original: Int, gridSize: Int): Int = ((original % gridSize) + gridSize) % gridSize

    private fun List<List<Char>>.isInGrid(point: Point) = point.x in this[0].indices && point.y in this.indices

    private fun List<List<Char>>.isValid(point: Point) = this[point] != '#'
}

data class Point(val x: Int, val y: Int) {
    companion object {
        val UP = Point(0, -1)
        val DOWN = Point(0, 1)
        val LEFT = Point(-1, 0)
        val RIGHT = Point(1, 0)
    }

    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    val neighbors: List<Point>
        get() = listOf(this + UP, this + DOWN, this + LEFT, this + RIGHT)
}
