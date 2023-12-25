package day24

import com.microsoft.z3.Context
import com.microsoft.z3.Status
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles
import kotlin.math.abs

@DisplayName("Day 24 - Never Tell Me The Odds")
@TestMethodOrder(OrderAnnotation::class)
class NeverTellMeTheOddsTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 2`() {
        assertEquals(2, sampleSolver.solvePartOne(7, 27))
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 47`() {
        assertEquals(47, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 17867`() {
        assertEquals(17867, solver.solvePartOne(200000000000000L, 400000000000000L))
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 557743507346379`() {
        assertEquals(557743507346379L, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val hailstones = data.map(Hailstone::fromInputLine)
    fun solvePartOne(min: Long, max: Long): Int {
        val timeRange = min.toDouble()..max.toDouble()
        var intersectCount = 0
        hailstones.dropLast(1).forEachIndexed { i, h1 ->
            hailstones.drop(i + 1).forEach { h2 ->
                when (val intersection = h1.intersection(h2)) {
                    Intersection.PARALLEL -> { /* Do nothing */
                    }

                    Intersection.SAME -> intersectCount++
                    else -> if (intersection.future && intersection.x in timeRange && intersection.y in timeRange) {
                        intersectCount++
                    }
                }
            }
        }
        return intersectCount
    }

    fun solvePartTwo(): Long {
        val ctx = Context()
        val solver = ctx.mkSolver()
        val mx = ctx.mkRealConst("mx")
        val m = ctx.mkRealConst("m")
        val mz = ctx.mkRealConst("mz")
        val mxv = ctx.mkRealConst("mxv")
        val mv = ctx.mkRealConst("mv")
        val mzv = ctx.mkRealConst("mzv")
        repeat(3) {
            val (sx, sy, sz, sxv, syv, szv) = hailstones[it]
            val t = ctx.mkRealConst("t$it")
            solver.add(ctx.mkEq(ctx.mkAdd(mx, ctx.mkMul(mxv, t)), ctx.mkAdd(ctx.mkReal(sx.toString()), ctx.mkMul(ctx.mkReal(sxv.toString()), t))))
            solver.add(ctx.mkEq(ctx.mkAdd(m, ctx.mkMul(mv, t)), ctx.mkAdd(ctx.mkReal(sy.toString()), ctx.mkMul(ctx.mkReal(syv.toString()), t))))
            solver.add(ctx.mkEq(ctx.mkAdd(mz, ctx.mkMul(mzv, t)), ctx.mkAdd(ctx.mkReal(sz.toString()), ctx.mkMul(ctx.mkReal(szv.toString()), t))))
        }
        if (solver.check() == Status.SATISFIABLE) {
            val model = solver.model
            val solution = listOf(mx, m, mz).sumOf { model.eval(it, false).toString().toDouble() }
            return solution.toLong()
        }
        throw IllegalStateException("Could not solve it!")
    }
}

data class Hailstone(val x: Long, val y: Long, val z: Long, val vX: Int, val vY: Int, val vZ: Int) {
    companion object {
        fun fromInputLine(line: String) =
            line.replace("@", ",")
                .replace("\\s".toRegex(), "")
                .split(",")
                .map { it.toLong() }
                .let {
                    Hailstone(it[0], it[1], it[2], it[3].toInt(), it[4].toInt(), it[5].toInt())
                }
    }

    fun intersection(other: Hailstone): Intersection {
        if (this.a.isClose(other.a)) {
            return if (this.b.isClose(other.b)) {
                Intersection.SAME
            } else {
                Intersection.PARALLEL
            }
        }

        val cx = (other.b - this.b) / (this.a - other.a)
        val cy = cx * this.a + this.b
        val inFuture = (cx > this.x == this.vX > 0) && (cx > other.x == other.vX > 0)
        return Intersection(inFuture, cx, cy)
    }

    val a: Double = this.vY.toDouble() / this.vX.toDouble()

    val b: Double = this.y.toDouble() - (this.a * this.x.toDouble())

    private fun Double.isClose(other: Double, tolerance: Double = 1e-9) =
        abs(this - other) < tolerance
}

data class Intersection(val future: Boolean, val x: Double, val y: Double) {
    companion object {
        val SAME = Intersection(true, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY)
        val PARALLEL = Intersection(false, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY)
    }
}
