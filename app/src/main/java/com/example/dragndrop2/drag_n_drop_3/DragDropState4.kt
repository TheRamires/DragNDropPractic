package com.example.dragndrop2.drag_n_drop_3

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import com.example.dragndrop2.drag_n_drop.offsetEnd
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.sync.Mutex

@Stable
class DragDropState4(
    val state: LazyListState,
    val paddingPx: Float
) {
    private val layoutInfo get() = state.layoutInfo
    private val visibleItemsInfo get() = layoutInfo.visibleItemsInfo

    var selectedDraggable = DraggableItem4.empty()
        private  set

    var yOffsetOfDraggable by mutableFloatStateOf(0f)
        private set

    val changedItemFlow = MutableSharedFlow<DraggableItem4>()
    val draggableItemFlow = MutableSharedFlow<DraggableItem4>()

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

    val mutext = Mutex()
    private suspend fun handleDragEvent(onDragEvent: OnDragEvent) = with(onDragEvent) {
        if (selectedDraggable.itemIndex < 0) return

        yOffsetOfDraggable += offset.y
        val yTopOfDraggable = selectedDraggable.startPosition.yTop + yOffsetOfDraggable


        val moveUp = moveDirection == -1
        val moveDown = moveDirection == 1

        val visibleItemsInfoList = if (moveUp) visibleItemsInfo.reversed() else visibleItemsInfo

        visibleItemsInfoList.forEach { itemInfoTop ->
            val index = itemInfoTop.index

            val top: Float
            val bottom: Float

            val swappedStackLast = swappedStack.lastOrNull()
            if (swappedStackLast?.itemIndex == index) {
                //return@forEach
                top = swappedStackLast.startPosition.yTop
                bottom = swappedStackLast.startPosition.tBottom
            } else {
                top = itemInfoTop.offset * 1f
                bottom = itemInfoTop.offsetEnd * 1f
            }
            val height = bottom - top

            when {
                moveUp -> {
                    if (selectedDraggable.itemIndex < index) {
                        return@forEach
                    }
                    val range = top + (0.5 * height).toInt()
                    val inRangeOfPrevious = yTopOfDraggable in top..range
                    val previousHasPassed = yTopOfDraggable.toInt() < range

                    if (previousHasPassed) {
                        val swapDistance = height //+ padding
                        val item = DraggableItem4(
                            itemIndex = index,
                            startPosition = DraggableItem4.ItemOffset(top - swapDistance - paddingPx, bottom - swapDistance - paddingPx)
                        )

                        if (swappedStack.firstOrNull { it.itemIndex == index } == null) {
                            swappedStack.addLast(item)
                            Log.d("TAGS42", "-- changedItemFlow ${item.itemIndex}")
                            changedItemFlow.emit(item)
                        } else {
                            swappedStack.removeLast()
                            changedItemFlow.emit(item)
                        }
                    }
                }
                moveDown -> {
                    if (selectedDraggable.itemIndex >= index) {
                        return@forEach
                    }
                    val range = top - (0.5 * height).toInt()
                    val inRangeOfNext = yTopOfDraggable in range..top
                    val nextHasPassed = yTopOfDraggable.toInt() > range
                    if (nextHasPassed) {
                        val swapDistance = height //+ padding
                        val item = DraggableItem4(
                            itemIndex = index,
                            startPosition = DraggableItem4.ItemOffset(top + swapDistance + paddingPx, bottom + swapDistance + paddingPx)
                        )

                        if (swappedStack.firstOrNull { it.itemIndex == index } == null) {
                            swappedStack.addLast(item)
                            Log.d("TAGS42", "-- changedItemFlow ${item.itemIndex}")
                            changedItemFlow.emit(item)
                        } else {
                            swappedStack.removeLast()
                            changedItemFlow.emit(item)
                        }
                    }
                }
            }
        }
    }

    fun onDragInterrupted() {
        yOffsetOfDraggable = 0f
        selectedDraggable = DraggableItem4.empty()
        swappedStack.clear()
    }

    fun checkForOverScroll(): Float = 0f
}
