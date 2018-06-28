package im.mash.moebooru.common.base

import android.content.Context
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet

class SafeGridLayoutManager : GridLayoutManager {

    constructor(context: Context?, spanCount: Int): super(context, spanCount)

    constructor(context: Context?, spanCount: Int, orientation: Int, reverseLayout: Boolean): super(context, spanCount, orientation, reverseLayout)

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int): super(context, attrs, defStyleAttr, defStyleRes)

    override fun onLayoutChildren(recycler: RecyclerView.Recycler?, state: RecyclerView.State?) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
        }
    }
}