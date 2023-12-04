package day04

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import kotlin.math.pow

@DisplayName("Day 04 - Scratchcards")
@TestMethodOrder(OrderAnnotation::class)
class ScratchcardsTest : DataFiles() {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 13`() {
        assertEquals(13, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 30`() {
        assertEquals(30, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 23678`() {
        assertEquals(23678, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 15455663`() {
        assertEquals(15455663, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val cards = data.map { ScratchCard(it) }

    fun solvePartOne(): Int {
        return cards.sumOf { it.score }
    }

    fun solvePartTwo(): Int {
        val newCards = cards.map { it.copy() }
        newCards.forEachIndexed { i, card ->
            if (card.isWinningCard) {
                val endIndex = (i + 1 + card.matchingNumbers).coerceAtMost(newCards.size)
                newCards.subList(i + 1, endIndex).forEach { it.instances += card.instances }
            }
        }
        return newCards.sumOf { it.instances }
    }
}

data class ScratchCard(
    val number: Int,
    val winningNumbers: Set<Int>,
    val ownNumbers: Set<Int>,
    var instances: Int = 1
) {
    constructor(line: String) : this(
        line.split(":")[0].split(Regex("\\s+"))[1].toInt(),
        line.split(":")[1].split("|")[0].trim().split(Regex("\\s+")).map { it.trim().toInt() }.toSet(),
        line.split(":")[1].split("|")[1].trim().split(Regex("\\s+")).map { it.trim().toInt() }.toSet()
    )

    val score: Int
        get() = (2.0).pow(matchingNumbers - 1).toInt()

    val matchingNumbers: Int
        get() = winningNumbers.intersect(ownNumbers).size

    val isWinningCard: Boolean
        get() = matchingNumbers > 0
}
