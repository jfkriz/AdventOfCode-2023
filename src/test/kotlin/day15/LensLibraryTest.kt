package day15

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 15 - Lens Library")
@TestMethodOrder(OrderAnnotation::class)
class LensLibraryTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    fun `HASH result should be 52`() {
        assertEquals(52, "HASH".computeHash())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 1320`() {
        assertEquals(1320, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 145`() {
        assertEquals(145, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 504449`() {
        assertEquals(504449, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 262044`() {
        assertEquals(262044, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val lenses = data[0].split(",").map { Lens.fromInputString(it) }

    fun solvePartOne(): Long =
        lenses.sumOf {
            it.inputValue.computeHash()
        }

    fun solvePartTwo(): Long =
        Boxes(lenses).focalLengths.sum()
}

data class Lens(val label: String, val operation: Char, val focalLength: Int) {
    companion object {
        fun fromInputString(inputString: String) =
            inputString.split('-', '=').filterNot { it.isBlank() }.let { parts ->
                if (parts.size == 2) {
                    Lens(parts[0], '=', parts[1].toInt())
                } else {
                    Lens(parts[0], '-', 0)
                }
            }
    }

    val boxNumber: Int =
        label.computeHash().toInt()

    val isAddition: Boolean
        get() = operation == '='

    val inputValue: String =
        if (isAddition) {
            "$label=$focalLength"
        } else {
            "$label-"
        }
}

class Boxes(lenses: List<Lens>) {
    private val boxes: List<List<Lens>>

    init {
        boxes = List(256) { mutableListOf() }
        lenses.forEach { lens ->
            if (lens.isAddition) {
                boxes[lens.boxNumber] += lens
            } else {
                boxes[lens.boxNumber] -= lens
            }
        }
    }

    val focalLengths: List<Long>
        get() = boxes.mapIndexed { boxNum, lenses ->
            lenses.mapIndexed { slotNum, lens ->
                ((1 + boxNum) * (1 + slotNum) * lens.focalLength).toLong()
            }
        }.flatten()
}

fun String.computeHash(): Long {
    var result = 0L
    this.forEach { char ->
        result = result.addCharacterHash(char)
    }
    return result
}

fun Long.addCharacterHash(char: Char): Long =
    if (char == '\n') {
        this
    } else {
        ((this + char.code) * 17) % 256
    }

operator fun MutableList<Lens>.minusAssign(lens: Lens): Unit =
    this.indexOfFirst { it.label == lens.label }.let {
        if (it >= 0) {
            this.removeAt(it)
        }
    }

operator fun MutableList<Lens>.plusAssign(lens: Lens): Unit =
    this.indexOfFirst { it.label == lens.label }.let {
        if (it >= 0) {
            this[it] = lens
        } else {
            this.add(lens)
        }
    }
