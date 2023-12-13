package day13

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.chunked

@DisplayName("Day 13 - Point of Incidence")
@TestMethodOrder(OrderAnnotation::class)
class PointOfIncidenceTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    fun `String reflectsAt works`() {
        loadSampleInput().chunked()[0].forEach {
            assertTrue(it.reflectsAt(5), "Improper reflection point for $it")
        }
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input Reflections Should be Vertical 5, Horizontal 4`() {
        val reflections = sampleSolver.findReflections()
        println("Reflections:\n${reflections.joinToString("\n")}")
        assertTrue(reflections.contains(Reflection(5, Direction.VERTICAL)))
        assertTrue(reflections.contains(Reflection(4, Direction.HORIZONTAL)))
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 405`() {
        assertEquals(405, sampleSolver.solvePartOne())
    }

    @Test
    @Order(1)
    fun `Part 2 Sample Input Reflections Should be Horizontal 1, Horizontal 4`() {
        val reflections = sampleSolver.findReflectionsWithSmudge()
        println("Reflections:\n${reflections.joinToString("\n")}")
        assertTrue(reflections.contains(Reflection(3, Direction.HORIZONTAL)))
        assertTrue(reflections.contains(Reflection(1, Direction.HORIZONTAL)))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 400`() {
        assertEquals(400, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 40006`() {
        assertEquals(40006, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 28627`() {
        assertEquals(28627, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val allNotes = data.chunked().map { ValleyNotes.fromInputLines(it) }

    fun solvePartOne(): Int {
        return findReflections().sumOf { reflection ->
            when (reflection.direction) {
                Direction.VERTICAL -> reflection.reflectionPoint
                Direction.HORIZONTAL -> reflection.reflectionPoint * 100
                else -> 0
            }
        }
    }

    fun findReflections() = allNotes.map { it.findReflection() }

    fun findReflectionsWithSmudge() = allNotes.map { it.findReflectionWithSmudge() }

    fun solvePartTwo(): Int {
        return findReflectionsWithSmudge().sumOf { reflection ->
            when (reflection.direction) {
                Direction.VERTICAL -> reflection.reflectionPoint
                Direction.HORIZONTAL -> reflection.reflectionPoint * 100
                else -> 0
            }
        }
    }
}

data class Reflection(val reflectionPoint: Int, val direction: Direction)

enum class Direction {
    HORIZONTAL,
    VERTICAL,
    NONE
}

data class ValleyNotes(val notes: List<String>) {
    private val transposed: List<String> = notes.transpose()

    companion object {
        fun fromInputLines(lines: List<String>) =
            ValleyNotes(lines.map { it })
    }

    fun findReflection(): Reflection {
        val horizontal = findHorizontalLineOfReflection()
        if (horizontal >= 0) {
            return Reflection(horizontal, Direction.HORIZONTAL)
        }
        val vertical = findVerticalLineOfReflection()
        if (vertical >= 0) {
            return Reflection(vertical, Direction.VERTICAL)
        }

        return Reflection(-1, Direction.NONE)
    }

    fun findReflectionWithSmudge(): Reflection {
        val originalReflection = findReflection()
        val horizontal = findReflectionsWithSmudge(notes.transpose()).map {
            Reflection(it, Direction.HORIZONTAL)
        }
        val vertical = findReflectionsWithSmudge(notes).map {
            Reflection(it, Direction.VERTICAL)
        }

        return (horizontal + vertical).filterNot { it == originalReflection }.first()
    }

    private fun findHorizontalLineOfReflection(): Int {
        return findReflection(transposed)
    }

    private fun findVerticalLineOfReflection(): Int {
        return findReflection(notes)
    }

    private fun findReflection(rows: List<String>): Int {
        (1 until rows[0].length).forEach { col ->
            if (rows.all { it.reflectsAt(col) }) {
                return col
            }
        }
        return -1
    }

    private fun findReflectionsWithSmudge(rows: List<String>): List<Int> {
        return (1 until rows[0].length).mapNotNull { col ->
            if (rows.count { !it.reflectsAt(col) } == 1) {
                col
            } else {
                null
            }
        }
    }

    private fun List<String>.transpose(): List<String> {
        val numRows = this.size
        val numCols = this[0].length

        return List(numCols) { col ->
            List(numRows) { row ->
                this[row][col]
            }.joinToString("")
        }
    }
}

fun String.reflectsAt(index: Int): Boolean {
    val left = this.substring(0, index)
    val right = this.drop(index)
    val maxLength = minOf(left.length, right.length)
    val leftCompare = left.reversed().take(maxLength)
    val rightCompare = right.take(maxLength)
    return leftCompare == rightCompare
}
