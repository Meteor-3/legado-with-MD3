package io.legado.app.feature.reader.core.transition

import org.junit.Assert.assertEquals
import org.junit.Test

class ReaderScrollWindowPolicyTest {

    @Test
    fun `adds pages until the forward extent covers the viewport`() {
        // 当前页之后已有 40，还差 60；每页 30，需要 2 页（40 + 60 = 100）。
        assertEquals(
            2,
            ReaderScrollWindowPolicy.trailingPageCount(
                contentExtentPx = 100f,
                coveredAfterCurrentPx = 40f,
                followingExtentsPx = listOf(30f, 30f, 30f),
            ),
        )
    }

    @Test
    fun `keeps the four page window when next pages already cover the viewport`() {
        assertEquals(
            0,
            ReaderScrollWindowPolicy.trailingPageCount(
                contentExtentPx = 100f,
                coveredAfterCurrentPx = 120f,
                followingExtentsPx = listOf(30f),
            ),
        )
    }

    @Test
    fun `caps the extra pages for very short pages`() {
        assertEquals(
            2,
            ReaderScrollWindowPolicy.trailingPageCount(
                contentExtentPx = 1000f,
                coveredAfterCurrentPx = 0f,
                followingExtentsPx = listOf(10f, 10f, 10f),
                maxPages = 2,
            ),
        )
    }

    @Test
    fun `does not add pages when the viewport has no extent`() {
        assertEquals(
            0,
            ReaderScrollWindowPolicy.trailingPageCount(
                contentExtentPx = 0f,
                coveredAfterCurrentPx = 0f,
                followingExtentsPx = listOf(30f),
            ),
        )
    }

    @Test
    fun `stops at the end of the candidate pages`() {
        assertEquals(
            2,
            ReaderScrollWindowPolicy.trailingPageCount(
                contentExtentPx = 100f,
                coveredAfterCurrentPx = 10f,
                followingExtentsPx = listOf(30f, 30f),
            ),
        )
    }
}
