package day06

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.sqrt

@DisplayName("Day 06 - Wait For It")
@TestMethodOrder(OrderAnnotation::class)
class BoatRacesTest : DataFiles() {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 288`() {
        assertEquals(288, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 71503`() {
        assertEquals(71503, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 2269432`() {
        assertEquals(2269432, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 35865985`() {
        assertEquals(35865985, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val races = data.map { it.split(":")[1].trim() }.let {
        val chargingTimes = it[0].split("\\s+".toRegex()).map(String::toLong)
        val recordDistances = it[1].split("\\s+".toRegex()).map(String::toLong)
        chargingTimes.mapIndexed { i, chargingTime ->
            BoatRace(chargingTime, recordDistances[i])
        }
    }
    private val racePartTwo = data.map { it.split(":")[1].trim().replace(" ", "") }.let {
        BoatRace(it[0].toLong(), it[1].toLong())
    }

    fun solvePartOne(): Int {
        return races.map { it.chargeTimesWithWinningResultCount }.reduce { acc, i -> i * acc }
    }

    fun solvePartTwo(): Int {
        return racePartTwo.chargeTimesWithWinningResultCount
    }
}

data class BoatRace(val chargeTimeAllowed: Long, val recordDistanceMm: Long) {
    val chargeTimesWithWinningResultCount: Int = chargeTimesWithWinningResultCountQuadratic

    // This works but part 2 takes about 500ms, others take 30-40ms
    private val chargeTimesWithWinningResultCountBruteForce: Int
        get() = (0..chargeTimeAllowed).filter { chargeTime ->
            (chargeTime * (chargeTimeAllowed - chargeTime)) > recordDistanceMm
        }.size

    // This works and each part takes ~1ms
    private val chargeTimesWithWinningResultCountQuadratic: Int
        get() {
            val discriminator = (chargeTimeAllowed * chargeTimeAllowed - 4 * recordDistanceMm).toDouble()

            if (discriminator < 0) {
                return 0
            }

            val lowerBound = floor(((chargeTimeAllowed - sqrt(discriminator)) / 2) + 1)

            val upperBound = ceil(((chargeTimeAllowed + sqrt(discriminator)) / 2) - 1)

            return (upperBound - lowerBound + 1).toInt()
        }
}
