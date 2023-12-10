package day10

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import util.DataPoint
import util.Direction

@DisplayName("Day 10 - Pipe Maze")
@TestMethodOrder(OrderAnnotation::class)
class PipeMazeTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }

    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 4`() {
        assertEquals(4, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input 1 should return 4`() {
        val input = """
            ...........
            .S-------7.
            .|F-----7|.
            .||.....||.
            .||.....||.
            .|L-7.F-J|.
            .|..|.|..|.
            .L--J.L--J.
            ...........
        """.trimIndent().split("\n").map { it.trim() }
        assertEquals(4, Solver(input).solvePartTwo())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input 2 should return 8`() {
        val input = """
            .F----7F7F7F7F-7....
            .|F--7||||||||FJ....
            .||.FJ||||||||L7....
            FJL7L7LJLJ||LJ.L-7..
            L--J.L7...LJS7F-7L7.
            ....F-J..F7FJ|L7L7L7
            ....L7.F7||L7|.L7L7|
            .....|FJLJ|FJ|F7|.LJ
            ....FJL-7.||.||||...
            ....L---J.LJ.LJLJ...
        """.trimIndent().split("\n").map { it.trim() }
        assertEquals(8, Solver(input).solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 6864`() {
        assertEquals(6864, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 349`() {
        assertEquals(349, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val maze = Maze.fromInputLines(data)

    fun solvePartOne(): Int {
        return maze.mainLoop.size / 2
    }

    fun solvePartTwo(): Int {
        val fl = listOf(maze.mainLoop.first(), maze.mainLoop.last())

        val startIsLowerCornerOrVertical =
            fl.containsAll(
                listOf(
                    maze.mainLoop[0].copy().move(Direction.Up),
                    listOf(maze.mainLoop[0].copy().move(Direction.Down))
                )
            ) ||
                fl.containsAll(
                    listOf(
                        maze.mainLoop[0].copy().move(Direction.Up),
                        listOf(maze.mainLoop[0].copy().move(Direction.Right))
                    )
                ) ||
                fl.containsAll(
                    listOf(
                        maze.mainLoop[0].copy().move(Direction.Up),
                        listOf(maze.mainLoop[0].copy().move(Direction.Left))
                    )
                )
        val loopPointsToConsider = if (startIsLowerCornerOrVertical) {
            listOf(Tile.VERTICAL, Tile.BOTTOM_LEFT, Tile.BOTTOM_RIGHT, Tile.START)
        } else {
            listOf(Tile.VERTICAL, Tile.BOTTOM_LEFT, Tile.BOTTOM_RIGHT)
        }

        val ground = maze.tiles.filter { it.value == Tile.GROUND }
        var total = 0
        ground.forEach { g ->
            val loopPointsToLeft =
                maze.mainLoop.filter { it.y == g.y && it.x < g.x && loopPointsToConsider.contains(it.value) }
            if (loopPointsToLeft.size % 2 != 0) {
                total++
            }
        }
        return total
    }
}

data class Maze(val tiles: Set<DataPoint<Tile>>, val mainLoop: List<DataPoint<Tile>>) {
    companion object {
        fun fromInputLines(lines: List<String>): Maze {
            val mazePoints = lines.mapIndexed { y, row ->
                row.mapIndexed { x, col ->
                    DataPoint(x, y, Tile.fromSymbol(col))
                }
            }.flatten().toSet()

            val start = mazePoints.find { it.value == Tile.START }!!
            val path = LinkedHashSet<DataPoint<Tile>>()
            var complete = false

            for (initialHeading in start.getConnectionHeadings(mazePoints)) {
                path.clear()
                var currentHeading = initialHeading
                var current = start
                while (true) {
                    path.add(current.copy())
                    val next = current.moveTo(currentHeading, mazePoints) ?: break

                    if (next == start) {
                        complete = true
                        break
                    }

                    val nextHeadings = next.getConnectionHeadings(mazePoints)
                    if (nextHeadings.contains(Direction.Up) && !path.contains(next.moveTo(Direction.Up, mazePoints))) {
                        currentHeading = Direction.Up
                        current = next
                        continue
                    }

                    if (nextHeadings.contains(Direction.Down) && !path.contains(
                            next.moveTo(
                                    Direction.Down,
                                    mazePoints
                                )
                        )
                    ) {
                        currentHeading = Direction.Down
                        current = next
                        continue
                    }

                    if (nextHeadings.contains(Direction.Left) && !path.contains(
                            next.moveTo(
                                    Direction.Left,
                                    mazePoints
                                )
                        )
                    ) {
                        currentHeading = Direction.Left
                        current = next
                        continue
                    }

                    if (nextHeadings.contains(Direction.Right) && !path.contains(
                            next.moveTo(
                                    Direction.Right,
                                    mazePoints
                                )
                        )
                    ) {
                        currentHeading = Direction.Right
                        current = next
                        continue
                    }

                    // No valid unvisited direction from this spot
                    path.add(next)
                    break
                }

                if (complete) {
                    break
                }
            }

            return Maze(
                mazePoints.map {
                    if (path.contains(it)) {
                        it
                    } else {
                        it.copy().apply { value = Tile.GROUND }
                    }
                }.toSet(),
                path.toList()
            )
        }
    }
}

enum class Tile(val symbol: Char, val directions: Set<Direction>) {
    VERTICAL('|', setOf(Direction.Up, Direction.Down)),
    HORIZONTAL('-', setOf(Direction.Left, Direction.Right)),
    BOTTOM_LEFT('L', setOf(Direction.Up, Direction.Right)),
    BOTTOM_RIGHT('J', setOf(Direction.Up, Direction.Left)),
    TOP_RIGHT('7', setOf(Direction.Down, Direction.Left)),
    TOP_LEFT('F', setOf(Direction.Down, Direction.Right)),
    GROUND('.', emptySet()),
    START('S', setOf(Direction.Up, Direction.Down, Direction.Left, Direction.Right));

    companion object {
        fun fromSymbol(symbol: Char): Tile =
            entries.firstOrNull { it.symbol == symbol } ?: throw IllegalArgumentException("Invalid symbol '$symbol'")
    }
}

fun DataPoint<Tile>.moveTo(heading: Direction, tiles: Set<DataPoint<Tile>>): DataPoint<Tile>? =
    tiles.find { this.copy().move(heading).isSameLocation(it) }

fun DataPoint<Tile>.getConnectionHeadings(tiles: Set<DataPoint<Tile>>) =
    this.value.directions.filter {
        tiles.any { tile ->
            tile.isSameLocation(this.copy().move(it))
        }
    }
