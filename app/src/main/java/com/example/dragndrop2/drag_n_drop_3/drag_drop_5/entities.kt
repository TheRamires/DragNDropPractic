package com.example.dragndrop2.drag_n_drop_3.drag_drop_5

import androidx.compose.ui.geometry.Offset

data class DragDrop5(
    val originalIndex: Int,
    val originalPosition: ItemPosition,
    val newIndex: Int = originalIndex,
    val newPosition: ItemPosition = originalPosition
) {
    fun getOffset(): Float = newPosition.start - originalPosition.start

    fun isEmpty(): Boolean = originalIndex == -1

    fun plusYOffset(y: Float): DragDrop5 = plusOffset(Offset(0f, y))

    fun change(
        changeOriginalPosition: (ItemPosition) -> ItemPosition,
        changeNewPosition: (ItemPosition) -> ItemPosition
    ): DragDrop5 = copy(
        originalPosition = changeOriginalPosition(originalPosition),
        newPosition = changeNewPosition(newPosition)
    )
    fun plusOffset(yOffset: Float): DragDrop5 {
        val oldPosition = this.newPosition
        val newPosition = ItemPosition(start = oldPosition.start + yOffset, end = oldPosition.end + yOffset)
        return copy(newPosition = newPosition)
    }

    fun plusOffset(offset: Offset): DragDrop5 {
        val oldPosition = this.newPosition
        val newPosition = ItemPosition(start = oldPosition.start + offset.y, end = oldPosition.end + offset.y)
        return copy(newPosition = newPosition)
    }

    fun withChangedIndex(index: Int): DragDrop5 = copy(newIndex = index)

    companion object {
        val empty get() = DragDrop5(-1, ItemPosition.empty, -1, ItemPosition.empty)
    }
}

data class ItemPosition(
    val start: Float,
    val end: Float
) {
    fun plus(startPlus: Float, endPlus: Float) = this.copy(start = start + startPlus, end = end + endPlus)

    companion object {
        val empty get() = ItemPosition(-1f, -1f)
    }
}

enum class Direction {
    MOVE_UP, MOVE_DOWN, NONE
}

data class OnDragEvent(val offset: Offset, val direction: Direction)

sealed class DragDropInternalEvent {

    data class ScrollEvent(val dragDrop5: DragDrop5, val direction: Direction): DragDropInternalEvent()

    data class ExchangeEvent(val dragDrop5: DragDrop5, val direction: Direction): DragDropInternalEvent()
}
