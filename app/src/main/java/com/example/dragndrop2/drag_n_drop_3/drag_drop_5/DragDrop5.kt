package com.example.dragndrop2.drag_n_drop_3.drag_drop_5

import android.util.Log
import androidx.collection.IntList
import androidx.collection.mutableIntListOf
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.ui.geometry.Offset
import com.example.dragndrop2.drag_n_drop.offsetEnd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Stack

class DragDropState5(
    coroutineScope: CoroutineScope,
    val state: LazyListState,
    private val paddingPx: Float,
    private val swapList: (list: IntList) -> Unit
) {
    private val layoutInfo get() = state.layoutInfo
    private val visibleItemsInfo get() = layoutInfo.visibleItemsInfo

    private val moveUpStack = Stack<DragDrop5>()
    private val moveDownStack = Stack<DragDrop5>()

    private val dragFlow = MutableSharedFlow<OnDragEvent>()

    private val draggable = MutableSharedFlow<DragDrop5>(replay = 1)
    val draggableApi get() = draggable.asSharedFlow()

    private val exchange = MutableSharedFlow<DragDrop5>()
    val exchangeApi get() = exchange.asSharedFlow()

    suspend fun onDragStart(clickOffset: Offset) {
        val considered = visibleItemsInfo.find { item ->
            clickOffset.y.toInt() in item.offset..(item.offset + item.size)
        } ?: return

        val index = considered.index
        val start = considered.offset * 1f
        val end = considered.offsetEnd * 1f

        val item = DragDrop5(index, ItemPosition(start, end))
        draggable.emit(item)
        dragFlow.collect { onDragEvent ->
            handleDragEvent(onDragEvent, draggable.replayCache.last())
        }
    }

    /*init {
        dragFlow.combine(draggable) { onDragEvent, draggable ->
            onDragEvent to draggable
        }
            *//*.distinctUntilChanged { old, new ->
            val oldOnDragEvent = old.first
            val oldDraggable = old.second
            val newOnDragEvent = new.first
            val newDraggable = new.second

            val directionTheSame = oldOnDragEvent.direction == newOnDragEvent.direction
            val indexTheSame = oldDraggable.newIndex == newDraggable.newIndex
            directionTheSame && indexTheSame
        }*//*
            .onEach { (onDragEvent, draggable) ->
                handleDragEvent(onDragEvent, draggable)
            }
            .launchIn(coroutineScope)
    }*/

    suspend fun onDrag(
        offset: Offset,
        direction: Direction
    ) = dragFlow.emit(OnDragEvent(offset, direction))

    suspend fun onDragInterrupted() {
        Log.d("TAGS42", "-- -- onDragInterrupted -- --")
        val draggable = draggable.replayCache.lastOrNull()
        Log.d(
            "TAGS42",
            "onDragInterrupted originalIndex: ${draggable?.originalIndex}; newIndex: ${draggable?.newIndex}"
        )
        val moveUpList = moveUpStack.toList()
        val moveDownList = moveDownStack.toList()
        Log.d("TAGS42", "onDragInterrupted moveUpList $moveUpList")
        Log.d("TAGS42", "onDragInterrupted moveDownList $moveDownList")

        val swapList = mutableIntListOf()
        draggable?.newIndex?.let { swapList.add(it) }
        moveUpStack.forEach { swapList.add(it.newIndex) }
        moveDownList.forEach { swapList.add(it.newIndex) }
        swapList(swapList)
        Log.d("TAGS42", "onDragInterrupted swapList $swapList")

        moveUpStack.clear()
        moveDownStack.clear()
        this.draggable.emit(DragDrop5.empty)
        exchange.emit(DragDrop5.empty)
    }

    fun checkForOverScroll(): Float = 0f

    private suspend fun handleDragEvent(onDragEvent: OnDragEvent, draggableItem: DragDrop5) {
        if (draggableItem.isEmpty()) return

        val movingUp = onDragEvent.direction == Direction.MOVE_UP
        val movingDown = onDragEvent.direction == Direction.MOVE_DOWN

        var draggable = draggableItem.withChangedPosition(onDragEvent.offset)

        val visibleItemsInfoList = if (movingUp) visibleItemsInfo.reversed() else visibleItemsInfo

        visibleItemsInfoList.forEach { listInfo ->
            val index = listInfo.index

            val top: Float
            val bottom: Float

            val stackItem = if (movingUp) {
                moveDownStack.find { it.originalIndex == index }
            } else {
                moveUpStack.find { it.originalIndex == index }
            }

            if (stackItem != null) {
                top = stackItem.newPosition.start
                bottom = stackItem.newPosition.end
            } else {
                top = listInfo.offset * 1f
                bottom = listInfo.offsetEnd * 1f
            }

            val height = bottom - top
            val draggableTop = draggable.newPosition.start

            when {
                movingUp -> {
                    //Log.d("TAGS42", "index $index; newIndex ${draggable.newIndex}; movingUp $movingUp; movingDown $movingDown")
                    if (draggable.newIndex < index) return@forEach

                    val range = top + (0.5 * height).toInt()
                    val inRangeOfPrevious = draggableTop in top..range
                    val previousHasPassed = draggableTop.toInt() < range
                    Log.d(
                        "TAGS42",
                        "newItem ${draggable.newIndex}; item $index; draggableTop $draggableTop; range $range"
                    )

                    if (previousHasPassed) {
                        val swapDistance = height //+ padding
                        val exchange = DragDrop5(
                            originalIndex = index,
                            originalPosition = ItemPosition(top, bottom),
                            newIndex = index + 1,
                            newPosition = ItemPosition(
                                top - swapDistance - paddingPx,
                                bottom - swapDistance - paddingPx
                            )
                        )
                        val fromMoveUpStack = moveUpStack.find { it.newIndex == index }
                        val fromMoveDownStack = moveDownStack.find { it.newIndex == index }

                        if (fromMoveUpStack != null) {
                            return
                        }

                        if (fromMoveDownStack != null) {
                            moveDownStack.remove(fromMoveDownStack)
                        } else {
                            moveUpStack.add(exchange)
                        }
                        Log.d("TAGS42", "exchange $exchange")
                        draggable = draggable.withChangedIndex(index - 1)
                        this.exchange.emit(exchange)
                    }
                }

                movingDown -> {
                    if (draggable.newIndex > index) return@forEach

                    val range = top - (0.5 * height).toInt()
                    val inRangeOfNext = draggableTop in range..top
                    val nextHasPassed = draggableTop.toInt() > range

                    if (nextHasPassed) {
                        val swapDistance = height //+ padding
                        val exchange = DragDrop5(
                            originalIndex = index,
                            originalPosition = ItemPosition(top, bottom),
                            newIndex = index - 1,
                            newPosition = ItemPosition(
                                top + swapDistance + paddingPx,
                                bottom + swapDistance + paddingPx
                            )
                        )
                        val fromMoveUpStack = moveUpStack.find { it.newIndex == index }
                        val fromMoveDownStack = moveDownStack.find { it.newIndex == index }

                        if (fromMoveDownStack != null) {
                            return
                        }

                        if (fromMoveUpStack != null) {
                            moveUpStack.remove(fromMoveUpStack)
                        } else {
                            moveDownStack.add(exchange)
                        }
                        draggable = draggable.withChangedIndex(index + 1)
                        this.exchange.emit(exchange)
                    }
                }
            }
        }
        Log.d("TAGS42", "draggable $draggable")
        this.draggable.emit(draggable)
    }

    private fun Stack<DragDrop5>.contains(index: Int): Boolean {
        return this.find {
            it.originalIndex == index
        } != null
    }
}