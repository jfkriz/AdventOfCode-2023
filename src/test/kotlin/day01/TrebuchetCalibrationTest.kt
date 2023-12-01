package day01

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 01 - Trebuchet?!")
@TestMethodOrder(OrderAnnotation::class)
class TrebuchetCalibrationTest : DataFiles() {
    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 142`() {
        assertEquals(142, Trebuchet(loadSampleInput()).findCalibrationValue())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 281`() {
        assertEquals(281, Trebuchet(loadOtherInput("test-input-part-2.txt")).findCalibrationValue(true))
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 55208`() {
        assertEquals(55208, Trebuchet(loadInput()).findCalibrationValue())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 54578`() {
        assertEquals(54578, Trebuchet(loadInput()).findCalibrationValue(true))
    }
}

class Trebuchet(data: List<String>) {
    private var calibrationValues = data

    private var numberMap = mapOf(
        "one" to 1,
        "two" to 2,
        "three" to 3,
        "four" to 4,
        "five" to 5,
        "six" to 6,
        "seven" to 7,
        "eight" to 8,
        "nine" to 9,
        "1" to 1,
        "2" to 2,
        "3" to 3,
        "4" to 4,
        "5" to 5,
        "6" to 6,
        "7" to 7,
        "8" to 8,
        "9" to 9,
    )

    fun findCalibrationValue(replaceNumberWords: Boolean = false): Int {
        var lines = if (replaceNumberWords) {
            calibrationValues.map { line -> replaceNumberWords(line) }
        } else {
            calibrationValues
        }

        lines = lines
            .map { line -> line.filter(Char::isDigit) }
            .map { digits -> digits.first().toString() + digits.last() }

        return lines
            .sumOf { number -> number.toInt() }
    }

    private fun replaceNumberWords(text: String): String {
        val firstNumberWord = numberMap.keys.map {
            it to text.indexOf(it)
        }.filter { it.second >= 0 }.minByOrNull { it.second }

        val firstNumberWordStart = firstNumberWord?.second ?: 0

        val lastNumberWord = numberMap.keys.map {
            it to text.substring(firstNumberWordStart + 1).lastIndexOf(it)
        }.filter { it.second >= 0 }.maxByOrNull { it.second }


        var newText = text
        if (firstNumberWord != null) {
            newText = newText.replaceFirst(firstNumberWord.first, numberMap[firstNumberWord.first].toString())
        }

        if (lastNumberWord != null) {
            newText = newText.reversed().replace(lastNumberWord.first.reversed(), numberMap[lastNumberWord.first].toString()).reversed()
        }

        return newText
    }
}
