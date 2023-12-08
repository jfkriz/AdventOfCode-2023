package day08

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.Math
import java.math.BigInteger

@DisplayName("Day 08 - Haunted Wasteland")
@TestMethodOrder(OrderAnnotation::class)
class HauntedWastelandTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val sampleSolverPartOneExample2 by lazy {
        Solver(loadOtherInput("test-input-part-1-example-2.txt"))
    }
    private val sampleSolverPartTwo by lazy {
        Solver(loadOtherInput("test-input-part-2.txt"))
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 2`() {
        assertEquals(2, sampleSolver.solvePartOne())
    }

    @Test
    @Order(2)
    fun `Part 1 Sample Input 2 should return 6`() {
        assertEquals(6, sampleSolverPartOneExample2.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Sample Input should return 6`() {
        assertEquals("6".toBigInteger(), sampleSolverPartTwo.solvePartTwo())
    }

    @Test
    @Order(3)
    fun `Part 1 Real Input should return 17141`() {
        assertEquals(17141, solver.solvePartOne())
    }

    @Test
    @Order(5)
    fun `Part 2 Real Input should return 10818234074807`() {
        assertEquals("10818234074807".toBigInteger(), solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val instructions = data[0].toCharArray().toList()
    private val nodes = data.drop(2).map { Node.fromInputLine(it) }.associateBy { it.position }

    fun solvePartOne(): Int {
        return calculateSteps(nodes["AAA"]!!, "ZZZ")
    }

    fun solvePartTwo(): BigInteger {
        val startingNodes = nodes.filter { it.key.endsWith('A') }.toMap()

        val allSteps = startingNodes.values.associateWith { start ->
            calculateSteps(start, "Z")
        }

        return Math.leastCommonMultiple(allSteps.values.map { it.toBigInteger() })
    }

    private fun calculateSteps(start: Node, endMatch: String): Int {
        var currentNode = start
        var steps = 0
        while (true) {
            instructions.forEach { direction ->
                if (currentNode.position.endsWith(endMatch)) {
                    return steps
                }
                steps++
                currentNode = nodes[currentNode.move(direction)]!!
            }
        }
    }
}

data class Node(val position: String, val left: String, val right: String) {
    companion object {
        fun fromInputLine(line: String): Node {
            val parts = line.replace("[^A-Z0-9 ]".toRegex(), " ").split("\\s+".toRegex())
            return Node(parts[0], parts[1], parts[2])
        }
    }

    fun move(direction: Char): String =
        if (direction == 'L') {
            left
        } else {
            right
        }
}
