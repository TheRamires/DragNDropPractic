package com.example.dragndrop2.drag_n_drop_3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.example.dragndrop2.drag_n_drop.offsetEnd
import kotlinx.coroutines.flow.MutableSharedFlow

data class DraggableItem4(
    val itemIndex: Int,
    val startPosition: ItemOffset
) {
    class ItemOffset(val yTop: Float, val tBottom: Float)
    companion object {
        fun empty() = DraggableItem4(-1, ItemOffset(0f, 0f))
    }
}

@Stable
class DragDropState4(
    val state: LazyListState,
) {
    private val layoutInfo get() = state.layoutInfo
    private val visibleItemsInfo get() = layoutInfo.visibleItemsInfo

    var selectedDraggable = DraggableItem4.empty()
        private  set

    var yOffsetOfDraggable by mutableFloatStateOf(0f)
        private set

    val changed = MutableSharedFlow<DraggableItem4>()

    private data class OnDragEvent(val offset: Offset, val moveDirection: Int)
    private val dragFlow = MutableSharedFlow<OnDragEvent>()

    private val swappedStack = ArrayDeque<DraggableItem4>()

    suspend fun onDragStart(clickOffset: Offset) {
        val item = visibleItemsInfo.firstOrNull { item ->
            clickOffset.y.toInt() in item.offset..(item.offset + item.size)
        } ?: return
        val itemTopOffset = item.offset * 1f
        val itemBottomOffset = item.offsetEnd * 1f

        selectedDraggable = DraggableItem4(
            itemIndex = item.index,
            startPosition = DraggableItem4.ItemOffset(itemTopOffset, itemBottomOffset)
        )
        dragFlow.collect { onDragEvent ->
            handleDragEvent(onDragEvent)
        }
    }

    suspend fun onDrag(
        offset: Offset,
        moveDirection: Int
    ) {
        dragFlow.emit(OnDragEvent(offset, moveDirection))
    }

    private suspend fun handleDragEvent(onDragEvent: OnDragEvent) = with(onDragEvent) {
        yOffsetOfDraggable += offset.y
        val yTopOfDraggable = selectedDraggable.startPosition.yTop + yOffsetOfDraggable

        visibleItemsInfo.forEach { itemInfoTop ->
            val index = itemInfoTop.index

            val top: Float
            val bottom: Float

            val swappedStackLast = swappedStack.lastOrNull()
            if (swappedStackLast?.itemIndex == index) {
                return@forEach
                top = swappedStackLast.startPosition.yTop
                bottom = swappedStackLast.startPosition.tBottom
            } else {
                top = itemInfoTop.offset * 1f
                bottom = itemInfoTop.offsetEnd * 1f
            }
            val height = bottom - top

            val moveUp = moveDirection == -1
            val moveDown = moveDirection == 1

            when {
                moveUp -> {
                    val range = top + (0.5 * height).toInt()
                    val inRangeOfPrevious = yTopOfDraggable in top..range
                    val previousHasPassed = yTopOfDraggable.toInt() < range

                    if (inRangeOfPrevious) {
                        val swapDistance = height //+ padding
                        val item = DraggableItem4(
                            itemIndex = index,
                            startPosition = DraggableItem4.ItemOffset(top - swapDistance, bottom + swapDistance)
                        )

                        if (swappedStackLast?.itemIndex == index) {
                            swappedStack.removeLast()
                        } else {
                            swappedStack.addLast(item)
                        }

                        changed.emit(item)
                    }
                }
                moveDown -> {
                    val range = top - (0.5 * height).toInt()
                    val inRangeOfNext = yTopOfDraggable in range..top
                    if (inRangeOfNext) {

                    }

                }
            }
        }
    }

    fun onDragInterrupted() {
        selectedDraggable = DraggableItem4.empty()
        yOffsetOfDraggable = 0f
    }

    fun checkForOverScroll(): Float = 0f
}
