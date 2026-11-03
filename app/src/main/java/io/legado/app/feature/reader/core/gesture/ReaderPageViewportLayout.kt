package io.legado.app.feature.reader.core.gesture

import io.legado.app.feature.reader.core.model.ReaderPage
import io.legado.app.feature.reader.core.model.ReaderPageWindow
import io.legado.app.feature.reader.core.model.ReaderRect
import io.legado.app.feature.reader.core.selection.ReaderSelection
import io.legado.app.feature.reader.core.selection.mergeSelectionBounds

data class ReaderPagePlacement(
    val page: ReaderPage,
    val offsetY: Float,
) {
    fun containsViewportPoint(x: Float, y: Float): Boolean =
        x in 0f..page.widthPx.toFloat() && y in offsetY..(offsetY + page.scrollExtentPx)

    fun localY(viewportY: Float): Float = viewportY - offsetY
}

data class ReaderSelectionVisualBounds(
    val page: ReaderPage,
    val bounds: ReaderRect,
)

/** Single coordinate source for hit testing and selection across the three-page scroll stack. */
class ReaderPageViewportLayout private constructor(
    val placements: List<ReaderPagePlacement>,
) {
    fun pageAt(x: Float, y: Float): ReaderPagePlacement? =
        placements.firstOrNull { it.containsViewportPoint(x, y) }

    fun selectionBounds(selection: ReaderSelection): List<ReaderSelectionVisualBounds> =
        placements.flatMap { placement ->
            selection.bounds(placement.page).mergeSelectionBounds().map { bounds ->
                ReaderSelectionVisualBounds(
                    page = placement.page,
                    bounds = bounds.offsetY(placement.offsetY),
                )
            }
        }

    companion object {
        fun paged(window: ReaderPageWindow): ReaderPageViewportLayout =
            ReaderPageViewportLayout(
                listOfNotNull(window.current?.let { ReaderPagePlacement(it, 0f) }),
            )

        fun scroll(window: ReaderPageWindow, scrollOffsetPx: Float): ReaderPageViewportLayout {
            val current = window.current ?: return ReaderPageViewportLayout(emptyList())
            val placements = buildList {
                window.previous?.let {
                    add(ReaderPagePlacement(it, scrollOffsetPx - it.scrollExtentPx))
                }
                add(ReaderPagePlacement(current, scrollOffsetPx))
                var offsetY = scrollOffsetPx + current.scrollExtentPx
                window.next?.let { next ->
                    add(ReaderPagePlacement(next, offsetY))
                    offsetY += next.scrollExtentPx
                    window.nextPlus?.let { following ->
                        add(ReaderPagePlacement(following, offsetY))
                        offsetY += following.scrollExtentPx
                        // 短页窗口补出来的页也在视口里，命中测试/选区必须能落到它们身上。
                        window.followings.forEach { tailPage ->
                            add(ReaderPagePlacement(tailPage, offsetY))
                            offsetY += tailPage.scrollExtentPx
                        }
                    }
                }
            }
            return ReaderPageViewportLayout(placements)
        }
    }
}
