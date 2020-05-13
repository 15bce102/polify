package com.example.polify.util

import android.view.View
import android.widget.ListView

fun ListView.getViewByPosition(pos: Int): View? {
    val firstListItemPosition = firstVisiblePosition
    val lastListItemPosition = firstListItemPosition + childCount - 1
    return if (pos < firstListItemPosition || pos > lastListItemPosition) {
        adapter.getView(pos, null, this)
    } else {
        val childIndex = pos - firstListItemPosition
        getChildAt(childIndex)
    }
}