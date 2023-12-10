package day05

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.collections.FastLongRange
import util.extensions.chunked

@DisplayName("Day 05 - If You Give A Seed A Fertilizer")
@TestMethodOrder(OrderAnnotation::class)
class SeedFertilizerTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 35`() {
        assertEquals(35, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 46`() {
        assertEquals(46, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 484023871`() {
        assertEquals(484023871, solver.solvePartOne())
    }

    @Test
    @Order(4)
    @Disabled("This test runs way too long, about 4 minutes on my laptop...")
    fun `Part 2 Real Input should return 46294175`() {
        assertEquals(46294175, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    // In part 1, the seeds are just individual seed numbers
    private val seeds = data[0].split(":")[1].trim().split(" ").map(String::toLong)

    // In part 2, the seeds are actually pairs of ranges of seeds
    private val seedsPartTwo =
        data[0].split(":")[1].trim().split(" ").chunked(2).map { it[0].toLong()..it[0].toLong() + it[1].toLong() }
    private val maps = data.drop(2).chunked().map { SourceDestinationMap(it) }.associateBy { it.source }

    fun solvePartOne(): Long {
        val seedLocations = seeds.map { seed -> seed to getSeedLocation(seed) }
        return seedLocations.minBy { it.second }.second
    }

    fun solvePartTwo(): Long {
        var minLocation: Long? = null
        seedsPartTwo.forEach { seedRange ->
            seedRange.forEach { seed ->
                val location = getSeedLocation(seed)
                if (minLocation == null || location < minLocation!!) {
                    minLocation = location
                }
            }
        }
        return minLocation!!
    }

    private fun getSeedLocation(seed: Long): Long = getNextDestination(seed, maps["seed"])

    private fun getNextDestination(currentNumber: Long, map: SourceDestinationMap?): Long {
        if (map == null) {
            return currentNumber
        }

        return getNextDestination(map.getDestination(currentNumber), maps[map.destination])
    }
}

data class SourceDestinationMap(
    val source: String,
    val destination: String,
    val ranges: List<RangePair>
) {
    constructor(lines: List<String>) : this(
        source = lines[0].split("-")[0],
        destination = lines[0].split("-")[2].split(" ")[0],
        ranges = lines.drop(1).map { RangePair(it) }.sortedBy { it.source.start }
    )

    fun getDestination(n: Long) =
        ranges.firstOrNull { it.isInSourceRange(n) }?.getDestination(n) ?: n
}

data class RangePair(val source: FastLongRange, val destination: FastLongRange) {
    constructor(line: String) : this(
        source = line.split(" ").let {
            FastLongRange(it[1].toLong(), it[2].toLong())
        },
        destination = line.split(" ").let {
            FastLongRange(it[0].toLong(), it[2].toLong())
        }
    )

    fun isInSourceRange(n: Long): Boolean = source.contains(n)

    fun getDestination(n: Long): Long? =
        if (isInSourceRange(n)) {
            destination.elementAt(source.indexOf(n))
        } else {
            null
        }
}
