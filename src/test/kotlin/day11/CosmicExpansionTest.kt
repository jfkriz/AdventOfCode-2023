package day11

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.DataPoint
import util.collections.Matrix
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
    private val universe = Universe(data)
    fun solvePartOne(): Int {
//        val expanded = universe.expand()
//        val galaxies = expanded.galaxies

        val galaxies = universe.expandedUniverseGalaxies(2)
        val paths = galaxies.asSequence().combinations(2).toList().map {
            "${it[0].y},${it[0].x} -> ${it[1].y},${it[1].x}" to it[0].distanceFrom(it[1])
        }
        return paths.sumOf { it.second }
    }

    fun solvePartTwo(expansionFactor: Int): Long {
        val galaxies = universe.expandedUniverseGalaxies(expansionFactor)
        val paths = galaxies.asSequence().combinations(2).toList().map {
            "${it[0].y},${it[0].x} -> ${it[1].y},${it[1].x}" to it[0].distanceFrom(it[1])
        }
        return paths.sumOf { it.second.toLong() }
    }
}

data class Universe(val matrix: Matrix<UniverseComponent>) {
    constructor(input: List<String>) : this(
        Matrix(
            input.map { row ->
                row.map { col ->
                    UniverseComponent.fromSymbol(col)
                }
            })
    )

    fun expand(): Universe {
        val columnsWithGalaxies = galaxies.map { it.x }.toSet()
        val rowsWithGalaxies = galaxies.map { it.y }.toSet()
        val columnsWithNoGalaxies = (0 until matrix.width).filterNot { columnsWithGalaxies.contains(it) }
        val rowsWithNoGalaxies = (0 until matrix.height).filterNot { rowsWithGalaxies.contains(it) }

        val newColumnLocations = (0 until matrix.width).map {
            it + columnsWithNoGalaxies.filter { col -> col < it }.size
        }
        val newRowLocations = (0 until matrix.height).map {
            it + rowsWithNoGalaxies.filter { row -> row < it }.size
        }

        val newUniverseData = MutableList(matrix.height + rowsWithNoGalaxies.size) {
            MutableList(matrix.width + columnsWithNoGalaxies.size) {
                UniverseComponent.SPACE
            }
        }
        galaxies.forEach {
            newUniverseData[newRowLocations[it.y]][newColumnLocations[it.x]] = it.value
        }

        return Universe(Matrix(newUniverseData))
    }

    val galaxies: List<DataPoint<UniverseComponent>>
        get() = matrix.find { it == UniverseComponent.GALAXY }

    fun expandedUniverseGalaxies(expansionFactor: Int): List<DataPoint<UniverseComponent>> {
        val columnsWithGalaxies = galaxies.map { it.x }.toSet()
        val rowsWithGalaxies = galaxies.map { it.y }.toSet()
        val columnsWithNoGalaxies = (0 until matrix.width).filterNot { columnsWithGalaxies.contains(it) }
        val rowsWithNoGalaxies = (0 until matrix.height).filterNot { rowsWithGalaxies.contains(it) }

        val newColumnLocations = (0 until matrix.width).map {
            it + (columnsWithNoGalaxies.filter { col -> col <= it }.size * (expansionFactor - 1))
        }
        val newRowLocations = (0 until matrix.height).map {
            it + (rowsWithNoGalaxies.filter { row -> row <= it }.size * (expansionFactor - 1))
        }

        return galaxies.map {
            DataPoint(newColumnLocations[it.x], newRowLocations[it.y], UniverseComponent.GALAXY)
        }
    }
}

enum class UniverseComponent(val symbol: Char) {
    GALAXY('#'),
    SPACE('.');

    companion object {
        fun fromSymbol(symbol: Char): UniverseComponent =
            entries.firstOrNull { it.symbol == symbol }
                ?: throw IllegalArgumentException("Cannot find component with symbol '$symbol'")
    }

    override fun toString(): String {
        return symbol.toString()
    }
}
