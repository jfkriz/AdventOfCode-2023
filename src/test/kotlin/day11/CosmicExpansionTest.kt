package day11

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point
import util.extensions.combinations

@DisplayName("Day 11 - Cosmic Expansion")
@TestMethodOrder(OrderAnnotation::class)
class CosmicExpansionTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 374`() {
        assertEquals(374, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 1030 for 10x `() {
        assertEquals(1030L, sampleSolver.solvePartTwo(10))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 8410 for 100x`() {
        assertEquals(8410L, sampleSolver.solvePartTwo(100))
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 10276166`() {
        assertEquals(10276166, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 598693078798`() {
        assertEquals(598693078798L, solver.solvePartTwo(1_000_000))
    }
}

class Solver(data: List<String>) {
    private val universe = Universe.fromInput(data)
    fun solvePartOne(): Int {
        val expanded = universe.expand(2)
        val paths = expanded.galaxyDistances
        return paths.values.sum()
    }

    fun solvePartTwo(expansionFactor: Int): Long {
        val expanded = universe.expand(expansionFactor)
        val paths = expanded.galaxyDistances
        return paths.values.sumOf { it.toLong() }
    }
}

data class Universe(private val galaxies: List<Point>) {
    companion object {
        fun fromInput(input: List<String>): Universe = Universe(
            input.mapIndexed { y, row ->
                row.mapIndexedNotNull { x, c ->
                    if (c == '#') {
                        Point(x, y)
                    } else {
                        null
                    }
                }
            }.flatten()
        )
    }

    fun expand(expansionFactor: Int): Universe {
        val width = galaxies.maxOf { it.x + 1 }
        val height = galaxies.maxOf { it.y + 1 }
        val columnsWithGalaxies = galaxies.map { it.x }.toSet()
        val rowsWithGalaxies = galaxies.map { it.y }.toSet()
        val columnsWithNoGalaxies = (0 until width).filterNot { columnsWithGalaxies.contains(it) }
        val rowsWithNoGalaxies = (0 until height).filterNot { rowsWithGalaxies.contains(it) }

        val newColumnLocations = (0 until width).map {
            it + (columnsWithNoGalaxies.filter { col -> col <= it }.size * (expansionFactor - 1))
        }
        val newRowLocations = (0 until height).map {
            it + (rowsWithNoGalaxies.filter { row -> row <= it }.size * (expansionFactor - 1))
        }

        return Universe(
            galaxies.map {
                Point(newColumnLocations[it.x], newRowLocations[it.y])
            }
        )
    }

    val galaxyDistances: Map<String, Int>
        get() = galaxies.asSequence().combinations(2).toList().associate {
            "${it[0].y},${it[0].x} -> ${it[1].y},${it[1].x}" to it[0].distanceFrom(it[1])
        }
}
