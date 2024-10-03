package com.example.dragndrop2.drag_n_drop_3.drag_drop_5

import android.util.Log
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import com.example.dragndrop2.drag_n_drop.offsetEnd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.Stack

@Immutable
class DragDropState5(
    val state: LazyListState,
    private val coroutineScope: CoroutineScope,
    private val paddingPx: Float,
    private val swapList: (list: IntArray) -> Unit
) {
    val draggableApi get() = draggable.asSharedFlow()
    val exchangeApi get() = exchange.asSharedFlow()


    private val dragFlow = MutableSharedFlow<OnDragEvent>()

    private val draggable = MutableSharedFlow<DragDrop5>(replay = 1)

    private val exchange = MutableSharedFlow<DragDrop5>()

    private val cursorLinkedList = CursorLinkedList5()

    private val exchangeEventStack = Stack<ExchangeEvent>()

    private val scopeExecutor = ScopeExecutor(coroutineScope)

    //region Вспомогательные поля LazyListState
    private val layoutInfo get() = state.layoutInfo
    private val visibleItemsInfo get() = layoutInfo.visibleItemsInfo
    //endregion

    suspend fun onDragStart(clickOffset: Offset) {
        val considered = visibleItemsInfo.find { item ->
            clickOffset.y.toInt() in item.offset..(item.offset + item.size)
        } ?: return

        val index = considered.index
        val start = considered.offset * 1f
        val end = considered.offsetEnd * 1f

        visibleItemsInfo.forEach {
            Log.d("TAGS42", "onDragStart ${it.index}; ${it.offset}")
        }

        val item = DragDrop5(index, ItemPosition(start, end))
        cursorLinkedList.addCursor(item)
        draggable.emit(item)
        dragFlow.onEach { onDragEvent ->
            handleDragEvent(onDragEvent, draggable.replayCache.last())
        }.launchIn(scopeExecutor.newScope())
    }

    suspend fun onDrag(
        offset: Offset,
        direction: Direction
    ) = dragFlow.emit(OnDragEvent(offset, direction))

    suspend fun onDragInterrupted() {
        swapList(cursorLinkedList.getIndexResultList().toIntArray())

        cursorLinkedList.clear()
        exchangeEventStack.clear()
        Log.d("TAGS42", "onDragInterrupted")
        this.draggable.emit(DragDrop5.empty)
        exchange.emit(DragDrop5.empty)
        scopeExecutor.stopScope()
    }

    fun checkForOverScroll(): Float = 0f

    private fun isTheSameEvent(onDragEvent: OnDragEvent, originalIndex: Int): Boolean {
        val lastExchange = exchangeEventStack.lastOrNull()
        return lastExchange != null
                && lastExchange.dragDrop5.originalIndex == originalIndex
                && lastExchange.direction == onDragEvent.direction
    }

    private fun fillParams(cursorLinkedList: CursorLinkedList5, originalIndex: Int, listInfo: LazyListItemInfo): Triple<Int, Float, Float> {
        val stackItem = cursorLinkedList.getByOriginalIndex(originalIndex)

        /*val top = listInfo.offset * 1f
        val bottom = listInfo.offsetEnd * 1f
        return top to bottom*/

        return if (stackItem != null) {
            val index = stackItem.newIndex
            val top = stackItem.newPosition.start
            val bottom = stackItem.newPosition.end
            Triple(index, top, bottom)
        } else {
            val index = listInfo.index
            val top = listInfo.offset * 1f
            val bottom = listInfo.offsetEnd * 1f
            Triple(index, top, bottom)
        }
    }

    private suspend fun handleDragEvent(onDragEvent: OnDragEvent, draggableItem: DragDrop5) {
        if (draggableItem.isEmpty()) return

        val movingUp = onDragEvent.direction == Direction.MOVE_UP
        val movingDown = onDragEvent.direction == Direction.MOVE_DOWN

        var draggable = draggableItem.withChangedPosition(onDragEvent.offset)

        val visibleItemsInfoList = if (movingUp) visibleItemsInfo.reversed() else visibleItemsInfo

        visibleItemsInfoList.forEach { listInfo ->
            val originalIndex = listInfo.index
            if (isTheSameEvent(onDragEvent, originalIndex)) return@forEach

            val (index, top, bottom) = fillParams(cursorLinkedList, originalIndex, listInfo)
            val height = bottom - top

            val draggableTop = draggable.newPosition.start

            when {
                movingUp -> {
                    if (draggable.newIndex <= index) return@forEach
                    //Log.d("TAGS42", "draggable ${draggable.newIndex}; index $index")

                    val range = top + (0.5 * height).toInt()
                    val inRangeOfPrevious = draggableTop in top..range
                    val previousHasPassed = draggableTop.toInt() < range

                    if (previousHasPassed) {
                        val swapDistance = height + paddingPx //+ padding
                        val exchange = DragDrop5(
                            originalIndex = originalIndex,
                            originalPosition = ItemPosition(top, bottom),
                            newIndex = index + 1,
                            newPosition = ItemPosition(
                                top + swapDistance ,
                                bottom + swapDistance
                            )
                        )
                        exchangeEventStack.add(ExchangeEvent(exchange, onDragEvent.direction))
                        cursorLinkedList.moveUp(exchange)

                        Log.d("TAGS42", "movingUp - old draggable ${draggable.newIndex}; exchange ${index}")
                        draggable = draggable.withChangedIndex(index)
                        Log.d("TAGS42", "movingUp - new draggable ${draggable.newIndex}; exchange ${exchange.newIndex}")
                        this.exchange.emit(exchange)
                    }
                }

                movingDown -> {
                    if (draggable.newIndex >= index) return@forEach

                    val range = top - (0.5 * height).toInt()
                    val inRangeOfNext = draggableTop in range..top
                    val nextHasPassed = draggableTop.toInt() > range

                    if (nextHasPassed) {
                        val swapDistance = height + paddingPx //+ padding
                        val exchange = DragDrop5(
                            originalIndex = originalIndex,
                            originalPosition = ItemPosition(top, bottom),
                            newIndex = index - 1,
                            newPosition = ItemPosition(
                                top - swapDistance,
                                bottom - swapDistance
                            )
                        )

                        exchangeEventStack.add(ExchangeEvent(exchange, onDragEvent.direction))
                        cursorLinkedList.moveDown(exchange)

                        Log.d("TAGS42", "movingDown - old draggable ${draggable.newIndex}; exchange ${index}")
                        draggable = draggable.withChangedIndex(index)
                        Log.d("TAGS42", "movingDown - new draggable ${draggable.newIndex}; exchange ${exchange.newIndex}")
                        this.exchange.emit(exchange)
                    }
                }
            }
        }
        this.draggable.emit(draggable)
    }
}