package com.example.dragndrop2.drag_n_drop_6

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.geometry.Offset

data class DragDrop6(
    val originalIndex: Int,
    val originalPosition: ItemPosition,
    val newIndex: Int = originalIndex,
    val newPosition: ItemPosition = originalPosition
) {
    fun getOffset(): Float = newPosition.start - originalPosition.start

    fun isEmpty(): Boolean = originalIndex == -1

    fun plusYOffset(y: Float): DragDrop6 = plusOffset(Offset(0f, y))

    fun change(
        changeOriginalPosition: (ItemPosition) -> ItemPosition,
        changeNewPosition: (ItemPosition) -> ItemPosition
    ): DragDrop6 = copy(
        originalPosition = changeOriginalPosition(originalPosition),
        newPosition = changeNewPosition(newPosition)
    )

    fun plusOffset(yOffset: Float): DragDrop6 {
        val oldPosition = this.newPosition
        val newPosition =
            ItemPosition(start = oldPosition.start + yOffset, end = oldPosition.end + yOffset)
        return copy(newPosition = newPosition)
    }

    fun plusOffset(offset: Offset): DragDrop6 {
        val oldPosition = this.newPosition
        val newPosition =
            ItemPosition(start = oldPosition.start + offset.y, end = oldPosition.end + offset.y)
        return copy(newPosition = newPosition)
    }

    fun withChangedIndex(index: Int): DragDrop6 = copy(newIndex = index)

    companion object {
        val empty get() = DragDrop6(-1, ItemPosition.empty, -1, ItemPosition.empty)
    }
}

data class ItemPosition(
    val start: Float,
    val end: Float
) {
    fun plus(startPlus: Float, endPlus: Float) =
        this.copy(start = start + startPlus, end = end + endPlus)

    companion object {
        val empty get() = ItemPosition(-1f, -1f)
    }
}

data class DraggableItem(
    val originalIndex: Int,
    val startPosition: Float,
    val offset: MutableState<Float> = mutableFloatStateOf(0f)
) {
    fun isEmpty() = originalIndex == -1

    fun getNewPosition() = startPosition + offset.value

    companion object {
        val empty get() = DraggableItem(-1, 0f)
    }
}

data class ExchangeItem(
    val originalIndex: Int,
    val animatable: Animatable<Float, AnimationVector1D> = Animatable(0f)
) {
    fun isEmpty() = originalIndex == -1

    companion object {
        val empty get() = ExchangeItem(-1)
    }
}

enum class Direction {
    MOVE_UP, MOVE_DOWN, NONE
}

data class OnDragEvent(val offset: Offset, val direction: Direction)

sealed class DragDropInternalEvent {

    data class ScrollEvent(val dragDrop5: DragDrop6, val direction: Direction) :
        DragDropInternalEvent()

    data class ExchangeEvent(val dragDrop5: DragDrop6, val direction: Direction) :
        DragDropInternalEvent()
}
