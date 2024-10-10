package com.example.dragndrop2.drag_n_drop_3.drag_drop_5

import android.util.Log
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import com.example.dragndrop2.data.SwapModel
import com.example.dragndrop2.drag_n_drop.offsetEnd
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Immutable
class DragDropState5(
    private val coroutineScope: CoroutineScope,
    val state: LazyListState,
    private val paddingPx: Float,
    private val swapList: (List<SwapModel>) -> Unit
) {
    val draggableApi get() = draggable.asSharedFlow()
    val exchangeApi get() = exchange.asSharedFlow()

    private val dragFlow = MutableSharedFlow<OnDragEvent>()

    private val draggable = MutableSharedFlow<DragDrop5>(replay = 1)

    private val exchange = MutableSharedFlow<DragDrop5>()

    private val cursorLinkedList = CursorLinkedList5()

    private var lastExchangeEvent: DragDropInternalEvent? = null

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
            //Log.d("TAGS42", "onDragStart ${it.index}; ${it.offset}")
        }

        val item = DragDrop5(index, ItemPosition(start, end))
        cursorLinkedList.addCursor(item)
        draggable.emit(item)
        dragFlow.onEach { onDragEvent ->
            handleDragEvent(onDragEvent, draggable.replayCache.last())
        }.launchIn(scopeExecutor.newScope()).invokeOnCompletion {

        }
    }

    suspend fun onDrag(
        offset: Offset,
        direction: Direction
    ) = dragFlow.emit(OnDragEvent(offset, direction))

    suspend fun onDragInterrupted() {
        scopeExecutor.stopScope()

        lastExchangeEvent = null
        Log.d("TAGS42", "onDragInterrupted")
        this.draggable.emit(DragDrop5.empty)
        exchange.emit(DragDrop5.empty)

        //delay(1000)
        swapList(cursorLinkedList.getResultList().map {
            SwapModel(from = it.originalIndex, to = it.newIndex)
        })
        cursorLinkedList.clear()
    }

    suspend fun justMove(yOffset: Float) {
        val item = draggable.replayCache.last()
        draggable.emit(item.plusYOffset(yOffset))
    }

    private fun isTheSameExchangeEvent(onDragEvent: OnDragEvent, originalIndex: Int): Boolean {
        val lastExchange = lastExchangeEvent
        if (lastExchange !is DragDropInternalEvent.ExchangeEvent) return false
        return lastExchange.dragDrop5.originalIndex == originalIndex
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
        //if (lastExchangeEvent is DragDropInternalEvent.ScrollEvent) return

        val movingUp = onDragEvent.direction == Direction.MOVE_UP
        val movingDown = onDragEvent.direction == Direction.MOVE_DOWN

        //Log.d("TAGS42", "on drag ${onDragEvent.offset.y}")

        val startOfLayout = layoutInfo.viewportStartOffset
        val endOfLayout = layoutInfo.viewportEndOffset

        val startOfItem = draggableItem.newPosition.start
        val endOfItem = draggableItem.newPosition.end
        val scrollOffset = endOfItem - startOfItem

        var draggable = when {
            movingUp -> {
                if (isScrolling != 1 && startOfItem <= startOfLayout) {
                    coroutineScope.launch {
                        state.animateScrollBy(
                            -10000f, tween(easing = FastOutLinearInEasing, durationMillis = 5000)
                        )
                    }
                    isScrolling = 1

                    return
                    cursorLinkedList.changeForEach {
                        it.copy(
                            originalPosition = it.originalPosition.plus(scrollOffset, scrollOffset),
                            newPosition = it.newPosition.plus(scrollOffset, scrollOffset)
                        )
                    }
                    draggableItem.plusOffset(-scrollOffset)
                } else {
                    if (isScrolling == -1) {
                        isScrolling = 0
                        state.stopScroll()
                    }
                    draggableItem.plusOffset(onDragEvent.offset)
                }
            }
            movingDown -> {
                if (isScrolling != -1 && endOfItem >= endOfLayout) {
                    coroutineScope.launch {
                        state.animateScrollBy(
                            10000f, tween(easing = FastOutLinearInEasing, durationMillis = 5000)
                        )
                    }
                    isScrolling = -1
                    return
                    cursorLinkedList.changeForEach {
                        it.copy(
                            originalPosition = it.originalPosition.plus(-scrollOffset, -scrollOffset),
                            newPosition = it.newPosition.plus(-scrollOffset, -scrollOffset)
                        )
                    }
                    /*draggableItem.change(
                        changeOriginalPosition = { it.plus(-scrollOffset, -scrollOffset) },
                        changeNewPosition = { it.plus(scrollOffset, scrollOffset) },
                    ).also {
                        Log.d("TAGS42", "scrollOffset $scrollOffset; originalPosition ${it.originalPosition}; newPosition ${it.newPosition}")
                    }*/
                    draggableItem.plusOffset(scrollOffset)
                } else {
                    if(isScrolling == 1) {
                        isScrolling = 0
                        state.stopScroll()
                    }
                    draggableItem.plusOffset(onDragEvent.offset)
                }
            }
            else -> {
                Log.d("TAGS42", "scrolling none none none")
                state.stopScroll()
                draggableItem
            }
        }
        if (isScrolling != 0) {
            return
        }


        val visibleItemsInfoList = if (movingUp) visibleItemsInfo.reversed() else visibleItemsInfo

        visibleItemsInfoList.forEach { listInfo ->
            val originalIndex = listInfo.index
            if (isTheSameExchangeEvent(onDragEvent, originalIndex)) return@forEach

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
                        lastExchangeEvent = DragDropInternalEvent.ExchangeEvent(exchange, onDragEvent.direction)
                        cursorLinkedList.moveUp(exchange)

                        //Log.d("TAGS42", "movingUp - old draggable ${draggable.newIndex}; exchange ${index}")
                        draggable = draggable.withChangedIndex(index)
                        cursorLinkedList.refreshCursor(draggable)
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

                        lastExchangeEvent = DragDropInternalEvent.ExchangeEvent(exchange, onDragEvent.direction)
                        cursorLinkedList.moveDown(exchange)

                        //Log.d("TAGS42", "movingDown - old draggable ${draggable.newIndex}; exchange ${index}")
                        draggable = draggable.withChangedIndex(index)
                        cursorLinkedList.refreshCursor(draggable)
                        Log.d("TAGS42", "movingDown - new draggable ${draggable.newIndex}; exchange ${exchange.newIndex}")
                        this.exchange.emit(exchange)
                    }
                }
            }
        }
        this.draggable.emit(draggable)
    }

    var isScrolling = 0

    /*suspend fun checkForOverScroll(moveDirection: Direction) {
        val item = draggable.replayCache.last()
        val start = layoutInfo.viewportStartOffset
        val end = layoutInfo.viewportEndOffset
        val itemStart = item.newPosition.start
        val itemEnd = item.newPosition.end

        val lastExchangeEvent_ = lastExchangeEvent
        val b = false//lastExchangeEvent_ is DragDropInternalEvent.ScrollEvent && moveDirection != lastExchangeEvent_.direction

        when {
            itemEnd > end && moveDirection == Direction.MOVE_DOWN -> {
                isScrolling = true
                val offset = 50f //itemEnd - end +
                justMove(offset)
                Log.d("TAGS42", "-- 2 checkForOverScroll -- start $start; end $end; itemStart $itemStart; itemEnd $itemEnd")

                lastExchangeEvent = DragDropInternalEvent.ScrollEvent(item, moveDirection)
                cursorLinkedList.changeForEach { item ->
                    item.copy(
                        originalPosition = item.originalPosition.plus(-offset, -offset),
                        newPosition = item.newPosition.plus(-offset, -offset)
                    )
                }
                draggable.emit(item.change(
                    changeOriginalPosition = { it.plus(-offset, -offset) },
                    changeNewPosition = { it.plus(-offset, -offset) }
                ))
                //draggable.emit(item.plusYOffset(offset))
                offset
                state.animateScrollBy(
                    offset, tween(easing = FastOutLinearInEasing)
                )
            }
            itemStart < start && moveDirection == Direction.MOVE_UP -> {
                isScrolling = true
                val offset = -50f //itemStart - start - 50
                justMove(offset)
                Log.d("TAGS42", "-- 3 checkForOverScroll -- start $start; end $end; itemStart $itemStart; itemEnd $itemEnd")

                lastExchangeEvent = DragDropInternalEvent.ScrollEvent(item, moveDirection)
                cursorLinkedList.changeForEach { item ->
                    item.copy(
                        originalPosition = item.originalPosition.plus(-offset, -offset),
                        newPosition = item.newPosition.plus(-offset, -offset)
                    )
                }
                draggable.emit(item.change(
                    changeOriginalPosition = { it.plus(-offset, -offset) },
                    changeNewPosition = { it.plus(-offset, -offset) }
                ))
                //draggable.emit(item.plusYOffset(offset))
                offset
                state.animateScrollBy(
                    offset, tween(easing = FastOutLinearInEasing)
                )
            }
            else -> {
                isScrolling = false
                if (lastExchangeEvent is DragDropInternalEvent.ScrollEvent) {
                    lastExchangeEvent = null
                    Log.d("TAGS42", "-- 4 checkForOverScroll -- start $start; end $end; itemStart $itemStart; itemEnd $itemEnd")
                }
                0f
            }
        }
    }*/
}