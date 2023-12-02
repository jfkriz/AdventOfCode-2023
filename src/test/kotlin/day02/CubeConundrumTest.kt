package day02

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 2 - Cube Conundrum")
@TestMethodOrder(OrderAnnotation::class)
class CubeConundrumTest : DataFiles() {
    private val sampleCubeGames by lazy {
        CubeGames(loadSampleInput())
    }
    private val cubeGames by lazy {
        CubeGames(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 8`() {
        assertEquals(8, sampleCubeGames.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 2286`() {
        assertEquals(2286, sampleCubeGames.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 2006`() {
        assertEquals(2006, cubeGames.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 84911`() {
        assertEquals(84911, cubeGames.solvePartTwo())
    }
}

class CubeGames(data: List<String>) {
    private val games = data.map { CubeGame(it) }
    fun solvePartOne(): Int {
        return games.filter { it.canPossiblyMatch(12, 13, 14) }.sumOf { it.gameNumber }
    }

    fun solvePartTwo(): Int {
        return games.sumOf { it.calculatePower() }
    }
}

class CubeGame(line: String) {
    val gameNumber = line.split(":")[0].split(" ")[1].toInt()
    private val samples = line.split(":")[1].split(";").map { handfuls ->
        handfuls.split(",").associate { colors ->
            val c = colors.trim().split(" ")
            c[1].trim() to c[0].trim().toInt()
        }
    }

    fun canPossiblyMatch(red: Int, green: Int, blue: Int): Boolean =
        samples.all {
            (it["red"] ?: 0) <= red && (it["green"] ?: 0) <= green && (it["blue"] ?: 0) <= blue
        }

    fun calculatePower(): Int =
        samples.flatMap { it.entries }.groupBy({ it.key }, { it.value })
            .mapValues { (_, values) -> values.maxOrNull() ?: 0 }.values.reduce { acc, i -> acc * i }
}
