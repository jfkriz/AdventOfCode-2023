package day19

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.extensions.chunked

@DisplayName("Day 19 - Aplenty")
@TestMethodOrder(OrderAnnotation::class)
class AplentyTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 19114`() {
        assertEquals(19114, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 167409079868000`() {
        assertEquals(167409079868000L, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 367602`() {
        assertEquals(367602, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 125317461667458`() {
        assertEquals(125317461667458L, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val workflows = data.chunked()[0].map { Workflow.fromInputLine(it) }.associateBy { it.name }
    private val parts = data.chunked()[1].map { Part.fromString(it) }

    fun solvePartOne(): Long {
        return parts.filter { acceptPart(it) }.sumOf { it.rating }
    }

    fun solvePartTwo(): Long {
        val start = WorkflowRange("in")
        val queue = ArrayDeque<WorkflowRange>().apply { add(start) }
        val accepted = mutableListOf<WorkflowRange>()

        while (queue.isNotEmpty()) {
            val range = queue.removeLast()
            val splits = range.split()
            accepted.addAll(splits.filter { it.name.isAccepted() })
            queue.addAll(splits)
        }

        return accepted.sumOf { it.combinations }
    }

    private fun String.isComplete() = this.isAccepted() || this.isRejected()

    private fun String.isAccepted() = this == "A"

    private fun String.isRejected() = this == "R"

    private fun acceptPart(part: Part): Boolean {
        var nextRule = "in"
        while (!nextRule.isComplete()) {
            nextRule = workflows.getValue(nextRule).executeRules(part)
        }
        return nextRule.isAccepted()
    }

    private fun WorkflowRange.split(): List<WorkflowRange> {
        val workflow = workflows[this.name] ?: return emptyList()
        val remainingRanges = this.ranges
        val result = mutableListOf<WorkflowRange>()
        for (rule in workflow.rules) {
            val ruleRange = remainingRanges.getValue(rule.category)
            val newRanges = remainingRanges.map { it.key to it.value }.toMap().toMutableMap()
            if (rule.operator == '<') {
                newRanges[rule.category] = ruleRange.first until rule.operand.toInt()
                remainingRanges[rule.category] = rule.operand.toInt()..ruleRange.last
            } else {
                newRanges[rule.category] = rule.operand.toInt() + 1..ruleRange.last
                remainingRanges[rule.category] = ruleRange.first..rule.operand.toInt()
            }
            result.add(WorkflowRange(rule.target, newRanges))
        }
        result.add(WorkflowRange(workflow.terminator, remainingRanges))

        return result
    }
}

data class Workflow(val name: String, val rules: List<Rule>, val terminator: String) {
    companion object {
        fun fromInputLine(line: String): Workflow {
            val parts = line.replace("[}{]".toRegex(), " ").trim().split(" ")
            val name = parts[0]
            val rules = parts[1].split(",").dropLast(1)
            val terminator = parts[1].split(",").last()

            return Workflow(name, rules.map { Rule.fromString(it) }, terminator.trim())
        }
    }

    fun executeRules(part: Part): String = rules.firstOrNull { it.allows(part) }?.target ?: terminator
}

data class Rule(val category: Char, val operator: Char, val operand: Long, val target: String) {
    companion object {
        fun fromString(input: String): Rule = input.split(':').let { parts ->
            Rule(parts[0][0], parts[0][1], parts[0].drop(2).toLong(), parts[1])
        }
    }

    fun allows(part: Part): Boolean = when (operator) {
        '>' -> part[category] > operand
        '<' -> part[category] < operand
        else -> false
    }
}

data class Part(val x: Long, val m: Long, val a: Long, val s: Long) {
    companion object {
        fun fromString(input: String): Part = input.replace("[}{]".toRegex(), "").split(",").let { parts ->
            Part(
                parts[0].split("=")[1].toLong(),
                parts[1].split("=")[1].toLong(),
                parts[2].split("=")[1].toLong(),
                parts[3].split("=")[1].toLong()
            )
        }
    }

    val rating: Long = x + m + a + s

    operator fun get(category: Char): Long =
        when (category) {
            'x' -> x
            'm' -> m
            'a' -> a
            's' -> s
            else -> throw IllegalArgumentException("Invalid category $category")
        }
}

data class WorkflowRange(
    val name: String,
    val x: IntRange = 1..4000,
    val m: IntRange = 1..4000,
    val a: IntRange = 1..4000,
    val s: IntRange = 1..4000
) {
    constructor(name: String, ranges: Map<Char, IntRange>) : this(
        name,
        ranges.getValue('x'),
        ranges.getValue('m'),
        ranges.getValue('a'),
        ranges.getValue('s')
    )

    val ranges: MutableMap<Char, IntRange> = mutableMapOf(
        'x' to x,
        'm' to m,
        'a' to a,
        's' to s
    )

    val combinations: Long
        get() = ranges.values.map { it.last - it.first + 1L }.reduce { acc, l -> l * acc }
}
