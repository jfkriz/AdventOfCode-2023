package day03

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.DataPoint
import util.Point

@DisplayName("Day 03 - Gear Ratios")
@TestMethodOrder(OrderAnnotation::class)
class GearRatiosTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 4361`() {
        assertEquals(4361, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 467835`() {
        assertEquals(467835, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 560670`() {
        assertEquals(560670, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 91622824`() {
        assertEquals(91622824, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val schematic = EngineSchematic(data)

    fun solvePartOne(): Int {
        val partNumbers =
            schematic.parts.filter { part -> schematic.symbols.any { symbol -> part.isAdjacentTo(symbol) } }
        return partNumbers.sumOf { it.number.toInt() }
    }

    fun solvePartTwo(): Int {
        return schematic.gearRatios.sum()
    }
}

class EngineSchematic(data: List<String>) {
    private val partNumberRegex = Regex("\\d+") // Any sequence of consecutive numbers on a line will match
    private val symbolRegex = Regex("[^0-9.]") // Any individual character except 0-9 or . on a line will match

    val parts = data.mapIndexed { y, line ->
        partNumberRegex.findAll(line).map {
            Part(it.value, it.range.first, y)
        }.toList()
    }.flatten()

    val symbols = data.mapIndexed { y, line ->
        symbolRegex.findAll(line).map {
            Symbol(it.value.first(), it.range.first, y)
        }.toList()
    }.flatten()

    val gearRatios: List<Int>
        get() = symbols.filter { it.isPossiblyAGear }
            .map { it.getAdjacentParts(parts) }
            .filter { it.size == 2 }
            .map { gearParts ->
                gearParts.map { part -> part.number.toInt() }.reduce { acc, i -> acc * i }
            }
}

data class Part(val number: String, val startX: Int, val startY: Int) {
    private val points = number.mapIndexed { index, _ -> Point(startX + index, startY) }
    fun isAdjacentTo(symbol: Symbol): Boolean =
        points.any { it.isNeighboringLocation(symbol) }
}

class Symbol(value: Char, x: Int, y: Int) : DataPoint<Char>(x, y, value) {
    val isPossiblyAGear: Boolean
        get() = value == '*'

    fun getAdjacentParts(allParts: List<Part>): List<Part> =
        allParts
            .filter { part -> part.startY in y - 1..y + 1 } // Just a small optimization, so we only consider parts in our row, or immediately above or below
            .filter { part -> part.isAdjacentTo(this) }
}
