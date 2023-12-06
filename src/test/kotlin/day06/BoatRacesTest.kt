package day06

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

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
    val chargeTimesWithWinningResultCount: Int =
        (0..chargeTimeAllowed).filter { chargeTime ->
            (chargeTime * (chargeTimeAllowed - chargeTime)) > recordDistanceMm
        }.size
}
