package day23

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 23 - A Long Walk")
@TestMethodOrder(OrderAnnotation::class)
class LongWalkTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 94`() {
        assertEquals(94, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 154`() {
        assertEquals(154, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 2170`() {
        assertEquals(2170, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 6502`() {
        assertEquals(6502, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val grid = data.map { row ->
        row.map { col ->
            MapSymbol.fromChar(col)
        }
    }
    val start = Point(1, 0)
    val end = Point(grid[0].size - 2, grid.size - 1)

    fun solvePartOne(): Int {
        val seen = Array(grid.size) { BooleanArray(grid[0].size) }

        return findMax(
            current = start,
            seen = seen,
            getNeighbors = { current ->
                when (grid[current]) {
                    MapSymbol.SLOPE_LEFT, MapSymbol.SLOPE_RIGHT,
                    MapSymbol.SLOPE_UP, MapSymbol.SLOPE_DOWN -> listOf(current + grid[current].direction to 1)

                    else -> {
                        current.neighbors.filter { it.isValid() && grid[it].canHike }.map { it to 1 }
                    }
                }.filter { it.first.isValid() }
            }
        )
    }

    fun solvePartTwo(): Int {
        val forks = mutableMapOf(
            start to mutableListOf<Pair<Point, Int>>(),
            end to mutableListOf()
        )

        for (y in grid.indices) {
            for (x in grid[y].indices) {
                if (grid[y][x] == MapSymbol.PATH) {
                    val point = Point(x, y)
                    if (point.neighbors.filter { it.isValid() && grid[it].canHike }.size > 2) {
                        forks[point] = mutableListOf()
                    }
                }
            }
        }

        for (fork in forks.keys) {
            var current = setOf(fork)
            val seen = mutableSetOf(fork)
            var distanceTraveled = 0

            while (current.isNotEmpty()) {
                distanceTraveled++
                current = buildSet {
                    for (c in current) {
                        c.neighbors.filter { it.isValid() && grid[it].canHike && it !in seen }.forEach { n ->
                            if (n in forks) {
                                forks.getValue(fork).add(n to distanceTraveled)
                            } else {
                                add(n)
                                seen.add(n)
                            }
                        }
                    }
                }
            }
        }

        val seen = Array(grid.size) { BooleanArray(grid[0].size) }

        return findMax(
            current = start,
            seen = seen,
            getNeighbors = { current -> forks.getValue(current) }
        )
    }

    private fun findMax(
        current: Point,
        seen: Array<BooleanArray>,
        distanceTraveled: Int = 0,
        getNeighbors: (Point) -> List<Pair<Point, Int>>
    ): Int {
        if (current == end) {
            return distanceTraveled
        }

        seen[current] = true
        val max = getNeighbors(current)
            .filter { (neighbor, _) -> !seen[neighbor] }
            .maxOfOrNull { (neighbor, weight) ->
                findMax(neighbor, seen, distanceTraveled + weight, getNeighbors)
            }
        seen[current] = false

        return max ?: 0
    }

    private operator fun Array<BooleanArray>.set(point: Point, value: Boolean) {
        this[point.y][point.x] = value
    }

    private operator fun Array<BooleanArray>.get(point: Point) =
        this[point.y][point.x]

    private operator fun <T> List<List<T>>.get(point: Point) =
        this[point.y][point.x]

    private fun Point.isValid() =
        this.x in grid[0].indices && this.y in grid.indices
}

enum class MapSymbol(val direction: Point = Point.NOOP, val canHike: Boolean = true) {
    PATH,
    FOREST(Point.NOOP, false),
    SLOPE_UP(Point.UP),
    SLOPE_RIGHT(Point.RIGHT),
    SLOPE_DOWN(Point.DOWN),
    SLOPE_LEFT(Point.LEFT);

    companion object {
        fun fromChar(char: Char) = when (char) {
            '.' -> PATH
            '#' -> FOREST
            '^' -> SLOPE_UP
            '>' -> SLOPE_RIGHT
            'v' -> SLOPE_DOWN
            '<' -> SLOPE_LEFT
            else -> throw IllegalArgumentException("Invalid symbol $char")
        }
    }
}

data class Point(val x: Int, val y: Int) {
    companion object {
        val NOOP = Point(0, 0)
        val UP = Point(0, -1)
        val DOWN = Point(0, 1)
        val LEFT = Point(-1, 0)
        val RIGHT = Point(1, 0)
    }

    operator fun plus(other: Point) = Point(x + other.x, y + other.y)

    val neighbors: List<Point>
        get() = listOf(this + UP, this + DOWN, this + LEFT, this + RIGHT)
}
