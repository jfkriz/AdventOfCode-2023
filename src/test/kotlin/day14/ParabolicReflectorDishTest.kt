package day14

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Direction
import util.extensions.rotateLeft
import util.extensions.rotateRight

@DisplayName("Day 14 - Parabolic Reflector Dish")
@TestMethodOrder(OrderAnnotation::class)
class ParabolicReflectorDishTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 136`() {
        assertEquals(136, sampleSolver.solvePartOne())
    }

    @Test
    @Order(1)
    fun `Part 2 Sample Input should cycle the grid properly`() {
        val cycle1 =
            """.....#....
            |....#...O#
            |...OO##...
            |.OO#......
            |.....OOO#.
            |.O#...O#.#
            |....O#....
            |......OOOO
            |#...O###..
            |#..OO#....
            """.trimMargin()
        val cycle2 =
            """.....#....
            |....#...O#
            |.....##...
            |..O#......
            |.....OOO#.
            |.O#...O#.#
            |....O#...O
            |.......OOO
            |#..OO###..
            |#.OOO#...O
            """.trimMargin()
        val cycle3 =
            """.....#....
            |....#...O#
            |.....##...
            |..O#......
            |.....OOO#.
            |.O#...O#.#
            |....O#...O
            |.......OOO
            |#...O###.O
            |#.OOO#...O
            """.trimMargin()

        assertEquals(cycle1, sampleSolver.tiltDish(1).toString())
        assertEquals(cycle2, sampleSolver.tiltDish(2).toString())
        assertEquals(cycle3, sampleSolver.tiltDish(3).toString())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 64`() {
        assertEquals(64, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 108935`() {
        assertEquals(108935, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 100876`() {
        assertEquals(100876, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val dish = ReflectorDish.fromInputLines(data)
    fun solvePartOne(): Int {
        val tilted = tiltDish(0)
        val weights = tilted.topRowWeight
        return weights.sum()
    }

    fun solvePartTwo(cycles: Int = 1_000_000_000): Int {
        val tilted = tiltDish(cycles)
        val weights = tilted.topRowWeight
        return weights.sum()
    }

    fun tiltDish(cycles: Int): ReflectorDish = dish.tilt(cycles)
}

data class ReflectorDish(val rocks: List<List<Rock>>) {
    companion object {
        fun fromInputLines(lines: List<String>): ReflectorDish =
            ReflectorDish(
                lines.map { row ->
                    row.map { Rock.fromSymbol(it) }
                }
            )
    }

    val topRowWeight: List<Int>
        get() = rocks.reversed().mapIndexed { index, row ->
            row.count { it == Rock.ROUND } * (index + 1)
        }.reversed()

    /**
     * Tilt the reflector and return a new reflector with the state after tilting. If the [numCycles] is zero or negative,
     * this will just tilt the reflector "up" and return. If the number of cycles is one or more, this will run cycles,
     * tilting the reflector Up, Left, Down, and Right (in that order), and will repeat that for [numCycles]. Since
     * [numCycles] could be a very large number, we try to spot a pattern so that we can short-circuit the cycles. The
     * assumption is that after a certain number of cycles (hopefully smaller than the actual number given), we will see
     * that some state along the way has been repeated, and at that point, we just have to figure out which one to use
     * for the final calculation.
     */
    fun tilt(numCycles: Int = 0): ReflectorDish =
        if (numCycles <= 0) {
            ReflectorDish(tiltUp(rocks))
        } else {
            var result = rocks
            var repeatStart = 0
            val patterns = mutableListOf(rocks.key())
            val tiltOrder = listOf(Direction.Up, Direction.Left, Direction.Down, Direction.Right)

            for (i in 0..numCycles) {
                tiltOrder.forEach { d ->
                    result = when (d) {
                        Direction.Up -> tiltUp(result)
                        Direction.Left -> tiltLeft(result)
                        Direction.Down -> tiltDown(result)
                        else -> tiltRight(result)
                    }
                }
                val pat = patterns.indexOfFirst { it == result.key() }
                if (pat >= 0) {
                    repeatStart = pat
                    break
                }
                patterns.add(result.key())
            }

            if (numCycles < repeatStart) {
                fromInputLines(patterns[numCycles].split("\n"))
            } else {
                fromInputLines(patterns[(repeatStart + ((numCycles - repeatStart) % (patterns.size - repeatStart)))].split("\n"))
            }
        }

    private fun List<List<Rock>>.key(): String =
        this.joinToString("\n") { row -> row.map { it.symbol }.joinToString("") }

    /**
     * Tilt the reflector so the rocks roll to the top.
     *
     * This makes a mutable copy of the reflector's rocks grid, then iterates over pairs of rows, moving each row's
     * rocks up to the one above it, until no more rocks can be moved. This calls [rollRocks] to do that work.
     */
    private fun tiltUp(grid: List<List<Rock>>): List<List<Rock>> {
        val tilted = grid.mutableCopy()
        var stillMoving = true
        while (stillMoving) {
            stillMoving = (0 until tilted.size).windowed(2, 1).map {
                tilted.rollRocks(it[0], it[1])
            }.any { it }
        }

        return tilted
    }

    /**
     * Tilt the reflector so the rocks roll to the bottom.
     *
     * Since all of these operations just call [tiltUp], tilting down means we want to flip the grid upside down,
     * so the bottom side is then up. Then we'll do the [tiltUp] operation, and finally flip upside down again
     * to get it back to its original orientation.
     */
    private fun tiltDown(grid: List<List<Rock>>) = tiltUp(grid.reversed()).reversed()

    /**
     * Tilt the reflector so the rocks roll to the left.
     *
     * Since all of these operations just call [tiltUp], tilting to the left means we want to rotate the grid to the
     * right, so the left side is then up. Then we'll do the [tiltUp] operation, and finally rotate back to the
     * left to get it back to its original orientation.
     */
    private fun tiltLeft(grid: List<List<Rock>>) = tiltUp(grid.rotateRight()).rotateLeft()

    /**
     * Tilt the reflector so the rocks roll to the right.
     *
     * Since all of these operations just call [tiltUp], tilting to the right means we want to rotate the grid to the
     * left, so the left side is then up. Then we'll do the [tiltUp] operation, and finally rotate back to the
     * right to get it back to its original orientation.
     */
    private fun tiltRight(grid: List<List<Rock>>) = tiltUp(grid.rotateLeft()).rotateRight()

    /**
     * This takes two rows from a grid, the [base], which is the row we want to roll the rocks onto,
     * and the [overlay], which is the row where the rocks are coming from. We'll check each position in
     * the [overlay] row to see if the position contains a rock that can move. If so, we'll check the same position in
     * the [base] row, and if it is an "open" spot, we'll move the rock from the overlay onto the base, and open up
     * the spot in the overlay. This expects to be given pairs of rows, like (1, 2), (2, 3), (3, 4), etc, and this
     * mutates the source grid.
     */
    private fun MutableList<MutableList<Rock>>.rollRocks(base: Int, overlay: Int): Boolean {
        val newBase = mutableListOf<Rock>()
        val newOverlay = mutableListOf<Rock>()

        val baseRow = this[base]
        val overlayRow = this[overlay]
        overlayRow.forEachIndexed { index, rock ->
            if (rock.canMove && baseRow[index] == Rock.NONE) {
                newBase.add(rock)
                newOverlay.add(Rock.NONE)
            } else {
                newBase.add(baseRow[index])
                newOverlay.add(rock)
            }
        }

        if (baseRow.joinToString("") == newBase.joinToString("") && overlayRow.joinToString("") == newOverlay.joinToString("")) {
            return false
        }

        this[base] = newBase
        this[overlay] = newOverlay
        return true
    }

    private fun List<List<Rock>>.mutableCopy(): MutableList<MutableList<Rock>> =
        this.map { it.toMutableList() }.toMutableList()

    override fun toString(): String = rocks.joinToString("\n") { it.joinToString("") }
}

enum class Rock(val symbol: Char, val canMove: Boolean) {
    ROUND('O', true),
    CUBE('#', false),
    NONE('.', false);

    companion object {
        fun fromSymbol(symbol: Char): Rock =
            entries.firstOrNull { it.symbol == symbol } ?: NONE
    }

    override fun toString(): String = symbol.toString()
}
