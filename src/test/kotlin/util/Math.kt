package util

@Suppress("unused", "MemberVisibilityCanBePrivate")
class Math {
    companion object {
        fun greatestCommonDenominator(a: Long, b: Long): Long {
            // Euclidean algorithm to find the greatest common divisor (GCD)
            return if (b == 0L) a else greatestCommonDenominator(b, a % b)
        }

        fun greatestCommonDenominator(numbers: List<Long>): Long {
            var gcd = numbers[0]
            for (i in 1 until numbers.size) {
                gcd = greatestCommonDenominator(gcd, numbers[i])
            }
            return gcd
        }

        fun leastCommonMultiple(a: Long, b: Long): Long {
            // LCM = (a * b) / GCD(a, b)
            return (a * b) / greatestCommonDenominator(a, b)
        }

        fun leastCommonMultiple(numbers: List<Long>): Long {
            var lcm = 1L
            for (number in numbers) {
                lcm = leastCommonMultiple(lcm, number)
            }
            return lcm
        }
    }
}
