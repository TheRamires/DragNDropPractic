package com.example.dragndrop2.drag_n_drop_3.drag_drop_5

import androidx.compose.ui.geometry.Offset

data class DragDrop5(
    val originalIndex: Int,
    val originalPosition: ItemPosition,
    val newIndex: Int = originalIndex,
    val newPosition: ItemPosition = originalPosition
) {
    fun getOffset() = newPosition.start - originalPosition.start

    fun isEmpty() = originalIndex == -1

    fun withChangedPosition(offset: Offset): DragDrop5 {
        val oldPosition = this.newPosition
        val newPosition = ItemPosition(start = oldPosition.start + offset.y, end = oldPosition.end + offset.y)
        return copy(newPosition = newPosition)
    }

    fun withChangedIndex(index: Int): DragDrop5 = copy(newIndex = index)

    companion object {
        val empty get() = DragDrop5(-1, ItemPosition.empty, -1, ItemPosition.empty)
    }
}

class ItemPosition(
    val start: Float,
    val end: Float
) {
    companion object {
        val empty get() = ItemPosition(-1f, -1f)
    }
}

enum class Direction {
    MOVE_UP, MOVE_DOWN, NONE
}

data class OnDragEvent(val offset: Offset, val direction: Direction)