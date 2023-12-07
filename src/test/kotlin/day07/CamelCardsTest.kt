package day07

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import util.DataFiles

@DisplayName("Day 07 - Camel Cards")
@TestMethodOrder(OrderAnnotation::class)
class CamelCardsTest : DataFiles {
    private val sampleSolver by lazy {
        Solver(loadSampleInput())
    }
    private val solver by lazy {
        Solver(loadInput())
    }

    @Test
    @Order(1)
    fun `Part 1 Sample Input should return 6440`() {
        assertEquals(6440, sampleSolver.solvePartOne())
    }

    @Test
    @Order(3)
    fun `Part 2 Sample Input should return 5905`() {
        assertEquals(5905, sampleSolver.solvePartTwo())
    }

    @Test
    @Order(2)
    fun `Part 1 Real Input should return 253638586`() {
        assertEquals(253638586, solver.solvePartOne())
    }

    @Test
    @Order(4)
    fun `Part 2 Real Input should return 253253225`() {
        assertEquals(253253225, solver.solvePartTwo())
    }
}

class Solver(data: List<String>) {
    private val hands = data.map { Hand(it) }

    fun solvePartOne(): Int {
        return hands.sorted().mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()
    }

    fun solvePartTwo(): Int {
        return hands.map(Hand::getWild).sorted().mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()
    }
}

data class Hand(val cards: List<CamelCard>, val bid: Int, val wild: Boolean = false) : Comparable<Hand> {
    constructor(line: String) : this(
        line.split("\\s+".toRegex())[0].map { CamelCard.fromFaceValue(it) },
        line.split("\\s+".toRegex())[1].toInt()
    )

    fun getWild() = Hand(cards, bid, true)

    val rank: HandRank
        get() = if (wild) {
            rankWild
        } else {
            rankNoWild
        }

    private val rankWild: HandRank
        get() {
            val initialRank = rankNoWild

            // If there are no jokers, or it was already five of a kind, just return the rank
            if (!cards.contains(CamelCard.JACK) || initialRank == HandRank.FiveOfAKind) {
                return initialRank
            }

            // Most of the initial ranks will just bump up to the next highest one,
            // but a few exceptions handled below
            return when (initialRank) {
                // 4444J -> 44444, 22JJJ -> 22222, 333JJ -> 33333
                HandRank.FourOfAKind, HandRank.FullHouse -> HandRank.FiveOfAKind
                HandRank.ThreeOfAKind -> HandRank.FourOfAKind
                // 1122J -> 11222, 22JJ3 -> 22223
                HandRank.TwoPair -> {
                    val groups = cards.groupingBy { it }.eachCount()

                    // If the jack was one of the pairs, it becomes 4 of a kind, otherwise it'll be a full house
                    if (groups[CamelCard.JACK] == 2) {
                        HandRank.FourOfAKind
                    } else {
                        HandRank.FullHouse
                    }

                }

                HandRank.OnePair -> HandRank.ThreeOfAKind
                HandRank.HighCard -> HandRank.OnePair
                else -> HandRank.HighCard
            }
        }

    private val rankNoWild: HandRank
        get() {
            val groups = cards.groupingBy { it }.eachCount()

            return when (groups.size) {
                1 -> HandRank.FiveOfAKind
                2 -> {
                    // If the groups are [4, 1], that means it is 4 of a kind
                    // The other possibility is [3, 2], which is a full house
                    if (groups.values.contains(4)) {
                        HandRank.FourOfAKind
                    } else {
                        HandRank.FullHouse
                    }
                }

                3 -> {
                    // One group of 3, and two groups of 1 [3, 1, 1] means 3 of a kind
                    // The other possibility is [2, 2, 1], which is two pair
                    if (groups.values.containsAll(listOf(3, 1, 1))) {
                        HandRank.ThreeOfAKind
                    } else {
                        HandRank.TwoPair
                    }
                }

                4 -> HandRank.OnePair
                else -> HandRank.HighCard
            }
        }

    override fun compareTo(other: Hand): Int {
        if (this.rank == other.rank) {
            this.cards.forEachIndexed { index, card ->
                if (card != other.cards[index]) {
                    return if (wild) {
                        card.compareToWild(other.cards[index])
                    } else {
                        card.compareTo(other.cards[index])
                    }
                }
            }
            return 0
        } else {
            return this.rank.compareTo(other.rank)
        }
    }
}

enum class CamelCard(val faceValue: Char) {
    TWO('2'),
    THREE('3'),
    FOUR('4'),
    FIVE('5'),
    SIX('6'),
    SEVEN('7'),
    EIGHT('8'),
    NINE('9'),
    TEN('T'),
    JACK('J'),
    QUEEN('Q'),
    KING('K'),
    ACE('A');

    companion object {
        fun fromFaceValue(faceValue: Char): CamelCard =
            CamelCard.entries.find { it.faceValue == faceValue }
                ?: throw IllegalArgumentException("$faceValue is not a valid card")
    }

    fun compareToWild(other: CamelCard): Int =
        if (this == other) {
            0
        } else if (this == JACK) {
            -1
        } else if (other == JACK) {
            1
        } else {
            this.compareTo(other)
        }
}

enum class HandRank {
    HighCard,
    OnePair,
    TwoPair,
    ThreeOfAKind,
    FullHouse,
    FourOfAKind,
    FiveOfAKind
}
