package day17

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point
import util.extensions.containsPoint
import util.extensions.get
import java.util.PriorityQueue

@DisplayName("Day 17 - Clumsy Crucible")
@TestMethodOrder(OrderAnnotation::class)
class ClumsyCrucibleTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 102`() {
        assertEquals(102, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 94`() {
        assertEquals(94, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input 2 should return 71`() {
        val input = """
            111111111111
            999999999991
            999999999991
            999999999991
            999999999991
        """.trimIndent().split("\n")
        val solver = Solver(input)
        assertEquals(71, solver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 767`() {
        assertEquals(767, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 904`() {
        assertEquals(904, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val cityMap = CityMap.fromInputLines(data)
    fun solvePartOne(): Int {
        val path = cityMap.findPath()

        return path.cost
    }

    fun solvePartTwo(): Int {
        val path = cityMap.findPath(4, 10)

        return path.cost
    }
}

data class CityMap(val grid: List<List<Int>>) {
    companion object {
        fun fromInputLines(lines: List<String>) =
            CityMap(
                lines.map { row ->
                    row.map { it.toString().toInt() }
                }
            )
    }

    private val validDirections = mapOf(
        Point.RIGHT to setOf(Point.RIGHT, Point.UP, Point.DOWN),
        Point.LEFT to setOf(Point.LEFT, Point.UP, Point.DOWN),
        Point.UP to setOf(Point.UP, Point.LEFT, Point.RIGHT),
        Point.DOWN to setOf(Point.DOWN, Point.LEFT, Point.RIGHT)
    )

    fun findPath(minimumSteps: Int = 1, maximumSteps: Int = 3): State =
        listOf(Point.RIGHT, Point.DOWN).mapNotNull { findPath(minimumSteps, maximumSteps, it) }.minBy { it.cost }

    private fun findPath(minimumSteps: Int, maximumSteps: Int, startingDirection: Point): State? {
        val start = State(Point(0, 0), startingDirection, 0)
        val end = Point(grid.first().size - 1, grid.size - 1)

        val visited = mutableSetOf<State>()
        val queue = PriorityQueue<State>(compareBy { it.cost })

        queue.offer(start)
        visited.add(start)

        while (queue.isNotEmpty()) {
            val current = queue.poll()
            if (current.location == end && current.steps >= minimumSteps) {
                return current
            }

            val currentDirection = validDirections.getValue(current.direction)

            val nextDirectionsOnGrid = currentDirection.filter { grid.containsPoint(current.location + it) }

            val validNextDirections = nextDirectionsOnGrid.filter { nextDirection ->
                current.steps in minimumSteps until maximumSteps ||
                    (current.steps >= maximumSteps && current.direction != nextDirection) ||
                    (current.steps < minimumSteps && current.direction == nextDirection)
            }

            val nextStates = validNextDirections.map { current.next(it, grid[current.location + it]) }.filterNot { it in visited }

            nextStates.forEach {
                queue.offer(it)
                visited.add(it)
            }
        }
        return null
    }
}

data class State(val location: Point, val direction: Point, val steps: Int, val path: List<Pair<Point, Int>> = listOf(location to 0)) {
    fun next(nextDirection: Point, cost: Int): State =
        State(location + nextDirection, nextDirection, if (direction == nextDirection) steps + 1 else 1, path + (nextDirection to cost))

    val cost: Int
        get() = path.sumOf { it.second }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as State

        if (location != other.location) return false
        if (direction != other.direction) return false
        if (steps != other.steps) return false

        return true
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + direction.hashCode()
        result = 31 * result + steps
        return result
    }
}
