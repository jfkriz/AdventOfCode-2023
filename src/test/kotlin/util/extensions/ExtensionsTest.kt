package util.extensions

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ExtensionsTest {
    @Test
    fun `lcm should work for large numbers`() {
        assertEquals(10818234074807L, listOf(17141L, 16579L, 18827L, 12083L, 13207L, 22199L).lcm())
        assertEquals(10241191004509L, listOf(13207L, 22199L, 14893L, 16579L, 20513L, 12083L).lcm())
    }
}
