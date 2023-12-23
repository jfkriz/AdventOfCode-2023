package day22

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 22 - Sand Slabs")
@TestMethodOrder(OrderAnnotation::class)
class SandSlabsTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 5`() {
        assertEquals(5, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 7`() {
        assertEquals(7, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 503`() {
        assertEquals(503, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 98431`() {
        assertEquals(98431, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val bricks = data.mapIndexed { index, line -> Brick.fromInputLine(index, line) }.sortedBy { it.zRange.first }

    private fun dropBricks(): Pair<Map<Int, Set<Int>>, Map<Int, Set<Int>>> {
        val supports = mutableMapOf<Int, MutableSet<Int>>()
        val supported = mutableMapOf<Int, MutableSet<Int>>()

        val highestZIndices = mutableMapOf<Point, Pair<Int, Int>>()

        bricks.map { it.copy() }.forEach { brick ->
            // find the highest brick at any of this brick's x,y points -
            val highestBrick = brick.xyPoints.map { highestZIndices.getOrDefault(it, -1 to 0) }.maxOf { it.second }
            // Drop this brick - shift it's Z down to 1 above the highest brick
            brick.zRange = brick.zRange.shiftTo(highestBrick + 1)

            // determine which bricks this one supports, and which ones support this one
            brick.xyPoints.forEach { point ->
                val (id, z) = highestZIndices.getOrDefault(point, -1 to 0)
                if (id != -1 && z == highestBrick) {
                    supports.getOrPut(id) { mutableSetOf() }.add(brick.id)
                    supported.getOrPut(brick.id) { mutableSetOf() }.add(id)
                }
                highestZIndices[point] = brick.id to brick.zRange.last
            }
        }

        return supports to supported
    }

    fun solvePartOne(): Int {
        val (supports, supported) = dropBricks()

        val notDisintegrated = supports.count { (_, others) -> others.any { other -> supported.getValue(other).size == 1 } }
        return bricks.size - notDisintegrated
    }

    fun solvePartTwo(): Int {
        val (supports, supported) = dropBricks()
        return bricks.sumOf { brick ->
            val falling = mutableSetOf(brick.id)

            var nextBricks: Set<Int> = supports.getOrDefault(brick.id, emptySet())
            while (nextBricks.isNotEmpty()) {
                nextBricks = buildSet {
                    for (next in nextBricks) {
                        if ((supported.getValue(next) - falling).isEmpty()) {
                            falling += next
                            addAll(supports.getOrDefault(next, emptySet()))
                        }
                    }
                }
            }

            falling.size - 1
        }
    }

    private fun IntRange.shiftTo(start: Int) = start until start + (this.last - this.first + 1)
}

data class Brick(val id: Int, val xRange: IntRange, val yRange: IntRange, var zRange: IntRange) {
    companion object {
        fun fromInputLine(id: Int, line: String): Brick {
            val (start, end) = line.split("~")
            val (x1, y1, z1) = start.split(",").map { it.trim().toInt() }
            val (x2, y2, z2) = end.split(",").map { it.trim().toInt() }

            return Brick(id, x1..x2, y1..y2, z1..z2)
        }
    }

    val xyPoints: List<Point>
        get() = xRange.map { x ->
            yRange.map { y ->
                Point(x, y)
            }
        }.flatten()
}

data class Point(val x: Int, val y: Int)
