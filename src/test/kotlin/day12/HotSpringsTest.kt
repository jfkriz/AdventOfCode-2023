package day12

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.collections.ImmutableStack
import util.extensions.repeatWithSeparator

@DisplayName("Day 12 - Hot Springs")
@TestMethodOrder(OrderAnnotation::class)
class HotSpringsTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 21`() {
        assertEquals(21L, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 525152`() {
        assertEquals(525152L, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 7221`() {
        assertEquals(7221L, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 7139671893722`() {
        assertEquals(7139671893722L, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val rows = data.map { SpringMapRow.fromInputLine(it) }
    fun solvePartOne(): Long {
        return rows.sumOf { it.findPossibleCombinations() }
    }

    fun solvePartTwo(): Long {
        return rows.sumOf { it.unfold(5).findPossibleCombinations() }
    }
}

data class SpringMapRow(val springs: String, val damagedSprings: List<Int>) {
    companion object {
        fun fromInputLine(line: String): SpringMapRow {
            val (s, g) = line.split(" ")
            val groups = g.split(",").map(String::toInt)
            return SpringMapRow(s, groups)
        }
    }

    fun findPossibleCombinations(): Long {
        val patternCache = mutableMapOf<String, Long>()
        val damaged = ImmutableStack(damagedSprings.reversed())
        return calculateCombinationsForSprings(springs, damaged, patternCache)
    }

    fun unfold(times: Int): SpringMapRow {
        val repeatedSprings = springs.repeatWithSeparator(times, "?")
        val repeatedDamagedSprings = damagedSprings.repeatWithSeparator(times).split(",").map(String::toInt)
        return SpringMapRow(repeatedSprings, repeatedDamagedSprings)
    }

    private fun calculateCombinationsForSprings(
        springs: String,
        damaged: ImmutableStack<Int>,
        patternCache: MutableMap<String, Long>
    ): Long {
        val key = "$springs,$damaged"

        if (!patternCache.containsKey(key)) {
            patternCache[key] = handleNextCharacter(springs, damaged, patternCache)
        }

        return patternCache[key]!!
    }

    private fun handleNextCharacter(
        springs: String,
        damaged: ImmutableStack<Int>,
        patternCache: MutableMap<String, Long>
    ): Long {
        return when (springs.firstOrNull()) {
            '.' -> calculateCombinationsForSprings(springs.substring(1), damaged, patternCache)
            '?' -> calculateCombinationsForSprings(
                "." + springs.substring(1),
                damaged,
                patternCache
            ) + calculateCombinationsForSprings(
                "#" + springs.substring(1),
                damaged,
                patternCache
            )

            '#' -> handleDamagedSpring(springs, damaged, patternCache)
            else -> if (damaged.isNotEmpty()) {
                0L
            } else {
                1L
            }
        }
    }

    private fun handleDamagedSpring(
        springs: String,
        damaged: ImmutableStack<Int>,
        patternCache: MutableMap<String, Long>
    ): Long {
        if (damaged.isEmpty()) {
            return 0
        }
        val nextDamagedCount = damaged.peek()

        val possiblyDamaged = springs.takeWhile { it == '#' || it == '?' }.count()
        return when {
            possiblyDamaged < nextDamagedCount -> 0
            springs.length == nextDamagedCount -> calculateCombinationsForSprings("", damaged.pop(), patternCache)
            springs[nextDamagedCount] == '#' -> 0
            else -> calculateCombinationsForSprings(
                springs.substring(nextDamagedCount + 1),
                damaged.pop(),
                patternCache
            )
        }
    }
}
