package io.legado.app.feature.reader.core.transition

/**
 * 滚动模式窗口的前向覆盖。
 *
 * 画布一次记录只画窗口里的页，滚动只平移图层。固定四页（previous/current/next/nextPlus）
 * 在短页（横版插图、章末残页）时盖不满一屏，视口底部会整页空白：那一页不在窗口里，
 * 既不会画图，也不会画灰占位或"加载中"。
 *
 * 当前页最多滚出它自己的 [io.legado.app.feature.reader.core.model.ReaderPage.scrollExtentPx]，
 * 所以"当前页之后"的页只要覆盖一个内容区高度，就能保证本窗口寿命内任意偏移下底部都有页。
 * 调用方从 `nextPlus` 之后按顺序补页，直到满足该条件。
 */
object ReaderScrollWindowPolicy {

    /** 单窗口最多额外补几页：极短页（页高接近 0）不该把滚动窗口撑得过大。 */
    const val MAX_TRAILING_PAGES = 6

    /**
     * 需要从 `nextPlus` 之后额外取几页。
     *
     * @param contentExtentPx 内容区高度（`contentBottomPx - contentTopPx`）。
     * @param coveredAfterCurrentPx 已经排在当前页之后的页高之和（next + nextPlus）。
     * @param followingExtentsPx `nextPlus` 之后候选页的页高，按阅读顺序。
     * @param maxPages 本次最多补几页。
     */
    fun trailingPageCount(
        contentExtentPx: Float,
        coveredAfterCurrentPx: Float,
        followingExtentsPx: List<Float>,
        maxPages: Int = MAX_TRAILING_PAGES,
    ): Int {
        if (contentExtentPx <= 0f || maxPages <= 0) return 0
        var covered = coveredAfterCurrentPx.coerceAtLeast(0f)
        var count = 0
        for (extent in followingExtentsPx) {
            if (covered >= contentExtentPx || count >= maxPages) break
            covered += extent.coerceAtLeast(0f)
            count++
        }
        return count
    }
}
