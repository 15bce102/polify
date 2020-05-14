package com.example.polify.util

import android.util.Log
import android.view.View
import android.widget.ListView

private const val TAG = "listViewExtLog"

fun ListView.getViewByPosition(pos: Int): View? {

    Log.d(TAG, "listView get view for position $pos")

    val firstListItemPosition = firstVisiblePosition
    val lastListItemPosition = firstListItemPosition + childCount - 1
    return if (pos < firstListItemPosition || pos > lastListItemPosition) {
        adapter.getView(pos, null, this)
    } else {
        val childIndex = pos - firstListItemPosition
        getChildAt(childIndex)
    }
}