package day25

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 25 - Snowverload")
@TestMethodOrder(OrderAnnotation::class)
class SnowverloadTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 54`() {
        assertEquals(54, sampleSolver.solvePartOne())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 582590`() {
        assertEquals(582590, solver.solvePartOne())
    }
}

class Solver(data: List<String>) {
    private val components = parseMap(data)

    fun solvePartOne(): Int {
        val uses = mutableMapOf<Pair<String, String>, Int>()
        repeat(1000) {
            val (a, b) = components.keys.toList().shuffled().take(2)
            val path = getPath(a, b)
            path?.let {
                for (i in 0 until it.size - 1) {
                    val edge = Pair(it[i], it[i + 1]).toList().sorted().let { (first, second) -> Pair(first, second) }
                    uses[edge] = (uses[edge] ?: 0) + 1
                }
            }
        }

        val sUses = uses.toList().sortedByDescending { it.second }

        val banned = sUses.take(3).map { it.first }

        val (s1, s2) = Pair(getComponentSize(banned[0].first, banned), getComponentSize(banned[0].second, banned))
        return s1 * s2
    }

    private fun parseMap(lines: List<String>): Map<String, Set<String>> {
        val map = mutableMapOf<String, MutableSet<String>>().withDefault { mutableSetOf() }
        lines.forEach { line ->
            val (name, rest) = line.split(": ")
            val connections = rest.split(" ")
            connections.forEach { conn ->
                map.getOrPut(name) { mutableSetOf() }.add(conn)
                map.getOrPut(conn) { mutableSetOf() }.add(name)
            }
        }

        return map
    }

    private fun getComponentSize(root: String, banned: List<Pair<String, String>>): Int {
        val nodes = mutableListOf(root)
        val seen = mutableSetOf(root)
        while (nodes.isNotEmpty()) {
            val newNodes = mutableListOf<String>()
            for (node in nodes) {
                for (neighbour in components[node] ?: emptySet()) {
                    if (neighbour in seen || Pair(node, neighbour) in banned || Pair(neighbour, node) in banned) {
                        continue
                    }
                    seen.add(neighbour)
                    newNodes.add(neighbour)
                }
            }
            nodes.clear()
            nodes.addAll(newNodes)
        }
        return seen.size
    }

    private fun getPath(start: String, end: String): List<String>? {
        val prev = mutableMapOf(start to start)
        val nodes = mutableListOf(start)
        val seen = mutableSetOf(start)
        while (nodes.isNotEmpty()) {
            val newNodes = mutableListOf<String>()
            for (node in nodes) {
                for (neighbour in components[node] ?: emptySet()) {
                    if (neighbour in seen) {
                        continue
                    }
                    seen.add(neighbour)
                    prev[neighbour] = node
                    newNodes.add(neighbour)
                }
            }
            nodes.clear()
            nodes.addAll(newNodes)
        }

        if (prev[end] == null) {
            return null
        }

        val path = mutableListOf<String>()
        var node = end
        while (node != start) {
            path.add(node)
            node = prev[node]!!
        }
        path.add(start)
        return path.reversed()
    }
}
