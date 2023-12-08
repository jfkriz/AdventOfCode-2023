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
        return hands.map(Hand::makeItWild).sorted().mapIndexed { index, hand -> hand.bid * (index + 1) }.sum()
    }
}

data class Hand(val cards: List<CamelCard>, val bid: Int, val wild: Boolean = false) : Comparable<Hand> {
    constructor(line: String) : this(
        line.split("\\s+".toRegex())[0].map { CamelCard.fromFaceValue(it) },
        line.split("\\s+".toRegex())[1].toInt()
    )

    fun makeItWild() = Hand(cards, bid, true)

    private val rank: HandRank
        get() = HandRank.fromCards(cards, wild)

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
    FiveOfAKind;

    companion object {
        fun fromCards(cards: List<CamelCard>, wild: Boolean): HandRank =
            cards.groupingBy { it }.eachCount().values.sortedDescending().joinToString("").let { groups ->
                when (groups) {
                    "5" -> FiveOfAKind
                    "41" -> FourOfAKind
                    "32" -> FullHouse
                    "311" -> ThreeOfAKind
                    "221" -> TwoPair
                    "2111" -> OnePair
                    else -> HighCard
                }
            }.let { initialRank ->
                if (!wild || !cards.contains(CamelCard.JACK) || initialRank == FiveOfAKind) {
                    initialRank
                } else {
                    // Most of the initial ranks will just bump up to the next highest one,
                    // but a few exceptions handled below
                    return when (initialRank) {
                        // 4444J -> 44444, 22JJJ -> 22222, 333JJ -> 33333
                        FourOfAKind, FullHouse -> FiveOfAKind
                        ThreeOfAKind -> FourOfAKind
                        // 1122J -> 11222, 22JJ3 -> 22223
                        TwoPair -> {
                            // If the jack was one of the pairs, it becomes 4 of a kind, otherwise it'll be a full house
                            if (cards.count { it == CamelCard.JACK } == 2) {
                                FourOfAKind
                            } else {
                                FullHouse
                            }
                        }

                        OnePair -> ThreeOfAKind
                        HighCard -> OnePair
                        else -> HighCard
                    }
                }
            }
    }
}
