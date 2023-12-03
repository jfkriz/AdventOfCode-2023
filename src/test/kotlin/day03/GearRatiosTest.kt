package day03

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Point

@DisplayName("Day 03 - Gear Ratios")
@TestMethodOrder(OrderAnnotation::class)
class GearRatiosTest : DataFiles() {
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
    val parts = data.mapIndexed { y, line ->
        var partNum = ""
        var startX = 0
        val partsToAdd = mutableListOf<Part>()
        line.forEachIndexed { x, ch ->
            if (ch.isDigit()) {
                if (partNum.isBlank()) {
                    startX = x
                }
                partNum += ch
            } else {
                if (partNum.isNotBlank()) {
                    partsToAdd.add(Part(partNum, startX, y))
                    partNum = ""
                }
            }
        }
        if (partNum.isNotBlank()) {
            partsToAdd.add(Part(partNum, startX, y))
        }
        partsToAdd
    }.flatten()

    val symbols = data.mapIndexed { y, line ->
        line.mapIndexedNotNull { x, ch ->
            if (ch.isDigit() || ch == '.') {
                null
            } else {
                Symbol(ch, x, y)
            }
        }
    }.flatten()

    val gearRatios: List<Int>
        get() = symbols.filter { it.isPossibleGearSymbol }
            .map { it.getAdjacentParts(parts) }
            .filter { it.size == 2 }
            .map { gearParts ->
                gearParts.map { part -> part.number.toInt() }.reduce { acc, i -> acc * i }
            }
}

data class Part(val number: String, val startX: Int, val startY: Int) {
    private val points = number.mapIndexed { index, ch -> Point(startX + index, startY, ch) }
    fun isAdjacentTo(symbol: Symbol): Boolean =
        points.any { it.isNeighboringLocation(symbol.point) }
}

data class Symbol(val value: Char, val x: Int, val y: Int) {
    val point = Point(x, y, value)

    val isPossibleGearSymbol: Boolean
        get() = value == '*'

    fun getAdjacentParts(allParts: List<Part>): List<Part> =
        allParts.filter { part -> part.isAdjacentTo(this) }
}
