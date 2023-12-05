package day01

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.replaceLast

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
        assertEquals(281, Trebuchet(loadOtherInput("test-input-part-2.txt")).findCalibrationValueWithDigitWords())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 55208`() {
        assertEquals(55208, Trebuchet(loadInput()).findCalibrationValue())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 54578`() {
        assertEquals(54578, Trebuchet(loadInput()).findCalibrationValueWithDigitWords())
    }
}

class Trebuchet(data: List<String>) {
    private var calibrationData = data

    /**
     * This feels like a hack, I'm sure there is a better way to do this... In order to reliably convert
     * digit words to digits in the input strings, we need to make sure we're only converting the first and last
     * word that would be a digit. Otherwise, if there are overlaps of words in a line, we may incorrectly choose the
     * wrong word to make into a number. For instance, given this input line:
     * gtzqvvsntvqnhrkxqb6twonebt
     * The left-most digit should be 6, and the right-most should be 1 (because of "one" in the line, with no other
     * digit words or digits after it), and the resulting number should be 61.
     *
     * If we don't consider actual digits AND words that represent digits while processing the line, we might end up
     * converting the first word we encounter ("two", in this case) to a digit, and we'd end up with 62 as the result.
     */
    private var digitMap = mapOf(
        "one" to "1",
        "two" to "2",
        "three" to "3",
        "four" to "4",
        "five" to "5",
        "six" to "6",
        "seven" to "7",
        "eight" to "8",
        "nine" to "9",
        "1" to "1",
        "2" to "2",
        "3" to "3",
        "4" to "4",
        "5" to "5",
        "6" to "6",
        "7" to "7",
        "8" to "8",
        "9" to "9"
    )

    fun findCalibrationValue(): Int {
        return calculateCalibrationValues(calibrationData)
    }

    private fun calculateCalibrationValues(data: List<String>): Int = data
        .map { line -> line.filter(Char::isDigit) }
        .map { line -> line.map(Char::digitToInt) }
        .sumOf { digits -> (digits.first() * 10) + digits.last() }

    fun findCalibrationValueWithDigitWords(): Int {
        return calculateCalibrationValues(calibrationData.map { line -> replaceDigitWords(line) })
    }

    /**
     * Replace the first and last digit "word" ("one" - "nine") on the line with the associated digit (1-9).
     * If a digit occurs before the first word, the digit takes precedence and no word replacement will happen
     * for that word, and if a digit occurs after the last word, the digit takes precedence and no replacement will
     * happen for the last word. The only time we'll replace a word with a digit is if it would become the first or last
     * digit on the line. This also makes the assumption that each input line is valid, meaning it has at least one
     * occurrence of a digit or a digit word. This code will blow up if a line is invalid (like "abcdefg").
     *
     * Examples:
     * 1twothree4 - resulting line will remain 1twothree4
     * one2three4 - resulting line will be 12three4
     * one23four - resulting line will be 1234
     */
    private fun replaceDigitWords(text: String): String {
        // Find the first digit "word" on the input line - and with the digitMap above, this may actually
        // be a digit (1-9), not a word ("one"-"nine")
        val firstDigitWord = digitMap.keys.map {
            it to text.indexOf(it)
        }.filter { it.second >= 0 }.minBy { it.second }.first

        // Find the last digit "word" on the input line - and as with the first word, this may actually be a digit
        val lastDigitWord = digitMap.keys.map {
            it to text.lastIndexOf(it)
        }.filter { it.second >= 0 }.maxBy { it.second }.first

        return text.replaceFirst(firstDigitWord, digitMap[firstDigitWord]!!)
            .replaceLast(lastDigitWord, digitMap[lastDigitWord]!!)
    }
}
