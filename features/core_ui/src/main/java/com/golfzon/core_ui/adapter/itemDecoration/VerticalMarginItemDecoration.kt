package com.golfzon.core_ui.adapter.itemDecoration

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.golfzon.core_ui.dp

class VerticalMarginItemDecoration(
    private val spacing: Int,
    private val isAddLine: Boolean = false,
    private val lineColor: Int = 0,
    private val lineMargin: Int = 0
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)

        outRect.top = 0
        outRect.bottom = if (isAddLine) 0 else spacing.dp
        outRect.left = 0
        outRect.right = 0
    }


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        if (!isAddLine) return

        val paint = Paint().apply { color = lineColor }
        val left = parent.paddingLeft + lineMargin.dp
        val right = parent.width - parent.paddingRight - lineMargin.dp

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams

            val top = child.bottom + params.bottomMargin + spacing.dp
            val bottom = top + (1.dp).toFloat()

            c.drawLine(left.toFloat(), top.toFloat(), right.toFloat(), bottom, paint)
        }
    }
}