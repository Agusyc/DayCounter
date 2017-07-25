package com.agusyc.daycounter

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.GridLayout

class AutoGridLayout : GridLayout {

    private var defaultColumnCount: Int = 0
    private var columnWidth: Int = 0

    constructor(context: Context) : super(context) {
        init(null, 0)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(attrs, defStyleAttr)
    }

    private fun init(attrs: AttributeSet?, defStyleAttr: Int) {
        var a = context.obtainStyledAttributes(attrs, R.styleable.AutoGridLayout, 0, defStyleAttr)
        try {
            columnWidth = a.getDimensionPixelSize(R.styleable.AutoGridLayout_columnWidth, 0)

            val set = intArrayOf(android.R.attr.columnCount /* id 0 */)
            a = context.obtainStyledAttributes(attrs, set, 0, defStyleAttr)
            defaultColumnCount = a.getInt(0, 10)
        } finally {
            a.recycle()
        }

        /* Initially set columnCount to 1, will be changed automatically later. */
        columnCount = 1
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        super.onMeasure(widthSpec, heightSpec)

        val width = View.MeasureSpec.getSize(widthSpec)
        if (columnWidth > 0 && width > 0) {
            val totalSpace = width - paddingRight - paddingLeft
            val columnCount = Math.max(1, totalSpace / columnWidth)
            setColumnCount(columnCount)
        } else {
            columnCount = defaultColumnCount
        }
    }
}