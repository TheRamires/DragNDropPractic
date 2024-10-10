package com.example.dragndrop2.drag_n_drop_6

import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import com.example.dragndrop2.data.SwapModel
import com.example.dragndrop2.drag_n_drop.offsetEnd
import com.example.dragndrop2.drag_n_drop_3.CHANGED_DURATION_MS
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@Immutable
class DragDropState6(
    coroutineScope: CoroutineScope,
    val state: LazyListState,
    private val paddingPx: Float,
    private val swapList: (List<SwapModel>) -> Unit
) {
    val draggableApi get() = draggable.asSharedFlow()
    val exchangeApi get() = exchangeFlow.asSharedFlow()

    private val dragFlow = MutableSharedFlow<OnDragEvent>()

    private val draggable = MutableSharedFlow<DraggableItem>(replay = 1)

    private var exchangeFlow = MutableSharedFlow<ExchangeItem>(replay = 1)

    private var lastExchangeEvent: DragDropInternalEvent? = null

    private var dragJob: Job? = null

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

        val item = DraggableItem(index, start)
        draggable.emit(item)
        dragJob = coroutineScope {
            dragFlow.collect { onDragEvent ->
                handleDragEvent(onDragEvent, draggable.replayCache.last())
            }
        }
    }

    suspend fun onDrag(
        offset: Offset,
        direction: Direction
    ) = dragFlow.emit(OnDragEvent(offset, direction))

    suspend fun onDragInterrupted() {
        dragJob?.cancel()

        lastExchangeEvent = null
        Log.d("TAGS42", "onDragInterrupted")
        this.draggable.emit(DraggableItem.empty)
        exchangeFlow.emit(ExchangeItem.empty)
    }

    private fun isTheSameExchangeEvent(onDragEvent: OnDragEvent, originalIndex: Int): Boolean {
        val lastExchange = lastExchangeEvent
        if (lastExchange !is DragDropInternalEvent.ExchangeEvent) return false
        return lastExchange.dragDrop5.originalIndex == originalIndex
                && lastExchange.direction == onDragEvent.direction
    }

    private suspend fun handleDragEvent(onDragEvent: OnDragEvent, draggableItem: DraggableItem) {
        if (draggableItem.isEmpty()) return

        val movingUp = onDragEvent.direction == Direction.MOVE_UP
        val movingDown = onDragEvent.direction == Direction.MOVE_DOWN

        val offset = draggableItem.offset.value + onDragEvent.offset.y
        draggableItem.offset.value = offset

        val visibleItemsInfoList = if (movingUp) visibleItemsInfo.reversed() else visibleItemsInfo

        visibleItemsInfoList.forEach { listInfo ->
            val originalIndex = listInfo.index
            if (isTheSameExchangeEvent(onDragEvent, originalIndex)) return@forEach

            val index = listInfo.index
            val top = listInfo.offset * 1f
            val bottom = listInfo.offsetEnd * 1f
            val height = bottom - top

            val draggableTop = draggableItem.getNewPosition()

            when {
                movingUp -> {
                    if (draggableItem.originalIndex <= index) return@forEach
                    //Log.d("TAGS42", "draggable ${draggable.newIndex}; index $index")

                    val range = top + (0.5 * height).toInt()
                    val inRangeOfPrevious = draggableTop in top..range
                    val previousHasPassed = draggableTop.toInt() < range

                    if (previousHasPassed) {
                        val swapDistance = height + paddingPx //+ padding
                        val exchange = DragDrop6(
                            originalIndex = originalIndex,
                            originalPosition = ItemPosition(top, bottom),
                            newIndex = index + 1,
                            newPosition = ItemPosition(
                                top + swapDistance,
                                bottom + swapDistance
                            )
                        )
                        lastExchangeEvent =
                            DragDropInternalEvent.ExchangeEvent(exchange, onDragEvent.direction)
                        val exchangeItem = ExchangeItem(originalIndex)
                        exchangeFlow.emit(exchangeItem)

                        coroutineScope {
                            launch {
                                exchangeItem.animatable.animateTo(
                                    targetValue = height,
                                    animationSpec = tween(
                                        easing = LinearEasing,
                                        durationMillis = CHANGED_DURATION_MS
                                    )
                                )
                                swapList(
                                    listOf(
                                        SwapModel(from = originalIndex, to = draggableItem.originalIndex),
                                        SwapModel(from = draggableItem.originalIndex, to = originalIndex)
                                    )
                                )
                            }
                        }

                    }
                }

                movingDown -> {
                    if (draggableItem.originalIndex >= index) return@forEach

                    val range = top - (0.5 * height).toInt()
                    val inRangeOfNext = draggableTop in range..top
                    val nextHasPassed = draggableTop.toInt() > range

                    if (nextHasPassed) {
                        val swapDistance = height + paddingPx //+ padding
                        val exchange = DragDrop6(
                            originalIndex = originalIndex,
                            originalPosition = ItemPosition(top, bottom),
                            newIndex = index - 1,
                            newPosition = ItemPosition(
                                top - swapDistance,
                                bottom - swapDistance
                            )
                        )

                        lastExchangeEvent =
                            DragDropInternalEvent.ExchangeEvent(exchange, onDragEvent.direction)
                        val exchangeItem = ExchangeItem(originalIndex)
                        exchangeFlow.emit(exchangeItem)
                        coroutineScope {
                            launch {
                                exchangeItem.animatable.animateTo(
                                    targetValue = -height,
                                    animationSpec = tween(
                                        easing = LinearEasing,
                                        durationMillis = CHANGED_DURATION_MS
                                    )
                                )

                                swapList(
                                    listOf(
                                        SwapModel(from = originalIndex, to = draggableItem.originalIndex),
                                        SwapModel(from = draggableItem.originalIndex, to = originalIndex)
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}