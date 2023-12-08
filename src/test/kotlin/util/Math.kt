package util

import java.math.BigInteger

class Math {
    companion object {
        fun greatestCommonDenominator(a: BigInteger, b: BigInteger): BigInteger {
            // Euclidean algorithm to find the greatest common divisor (GCD)
            return if (b == BigInteger.ZERO) a else greatestCommonDenominator(b, a % b)
        }

        fun greatestCommonDenominator(numbers: List<BigInteger>): BigInteger {
            var gcd = numbers[0]
            for (i in 1 until numbers.size) {
                gcd = greatestCommonDenominator(gcd, numbers[i])
            }
            return gcd
        }

        fun leastCommonMultiple(a: BigInteger, b: BigInteger): BigInteger {
            // LCM = (a * b) / GCD(a, b)
            return (a * b) / greatestCommonDenominator(a, b)
        }

        fun leastCommonMultiple(numbers: List<BigInteger>): BigInteger {
            var lcm = BigInteger.ONE
            for (number in numbers) {
                lcm = leastCommonMultiple(lcm, number)
            }
            return lcm
        }
    }
}