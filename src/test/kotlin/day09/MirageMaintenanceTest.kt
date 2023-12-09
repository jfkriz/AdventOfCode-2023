package day09

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 09 - MirageMaintenance")
@TestMethodOrder(OrderAnnotation::class)
class MirageMaintenanceTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 114`() {
        assertEquals(114, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 2`() {
        assertEquals(2, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 1921197370`() {
        assertEquals(1921197370, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 1124`() {
        assertEquals(1124, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val oasisReport = data.map { NumberSequence(it) }
    fun solvePartOne(): Int {
        return oasisReport.map { it.predict() }.sumOf { it.numbers.last() }
    }

    fun solvePartTwo(): Int {
        return oasisReport.map { it.predictBackwards() }.sumOf { it.numbers.first() }
    }
}

class NumberSequence(val numbers: List<Int>) {
    constructor(line: String) : this(line.split("\\s+".toRegex()).map(String::toInt))

    fun predict(): NumberSequence {
        val predictionSequences = mutableListOf(numbers.toMutableList())
        while (!predictionSequences.last().all { it == 0 }) {
            predictionSequences.add(
                predictionSequences.last().windowed(2, 1, false).map { it[1] - it[0] }.toMutableList()
            )
        }
        predictionSequences.last().add(0)
        predictionSequences.reversed().windowed(2, 1, false).forEach {
            it[1].add(it[0].last() + it[1].last())
        }

        return NumberSequence(predictionSequences.first())
    }

    fun predictBackwards(): NumberSequence {
        val predictionSequences = mutableListOf(numbers.reversed().toMutableList())
        while (!predictionSequences.last().all { it == 0 }) {
            predictionSequences.add(
                predictionSequences.last().windowed(2, 1, false).map { it[0] - it[1] }.toMutableList()
            )
        }
        predictionSequences.last().add(0)
        predictionSequences.reversed().windowed(2, 1, false).forEach {
            it[1].add(it[1].last() - it[0].last())
        }

        return NumberSequence(predictionSequences.first().reversed())
    }
}
