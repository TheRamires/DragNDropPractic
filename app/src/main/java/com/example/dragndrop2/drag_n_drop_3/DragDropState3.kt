package com.example.dragndrop2.drag_n_drop_3

import android.util.Log
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import com.example.dragndrop2.drag_n_drop.offsetEnd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.abs

private const val MOVE_UP = 1
private const val MOVE_DOWN = -1
private const val MOVE_NONE = 0

const val CHANGED_DURATION_MS = 100

data class DraggableItem(
    val itemIndex: Int,
    val clickDistance: Float = -1f, // расстояние от точки клика до верха элемента
    val startOffset: ItemOffset = ItemOffset.empty()
) {
    init {
        Log.d("TAGS42", "init DraggableItem $itemIndex")
    }

    var realTopPosition = startOffset.top
        set(value) {
            offsetY = value - startOffset.top
            field = value
        }
    var offsetY by mutableFloatStateOf(0f)
        private set

    val height get() = startOffset.top - startOffset.bottom

    data class ItemOffset(
        val top: Float,
        val bottom: Float
    ) {
        companion object {
            fun empty() = ItemOffset(0f, 0f)
        }
    }

    companion object {
        fun empty() = DraggableItem(-1,0f, ItemOffset.empty())
    }
}

@Stable
open class DraggableOffset {
    var draggableXOffset by mutableFloatStateOf(0f)
        protected set

    var draggableYOffset by mutableFloatStateOf(0f)
        protected set

    fun reset() {
        draggableXOffset = 0f
        draggableYOffset = 0f
    }
}

@Stable
class ChangedPosition {
    val animatable = Animatable(0f)

    var changedIndex = -1

    suspend fun reset() {
        animatable.snapTo(0f)
        changedIndex = -1
    }
}

class DragDropState3(
    val state: LazyListState,
    private val scope: CoroutineScope,
    private val onSwap: (from: Int, to: Int) -> Unit
): DraggableOffset() {
    val changedList = CursorLinkedList()

    val changedPosition = ChangedPosition()

    private val layoutInfo get() = state.layoutInfo
    private val visibleItemsInfo get() = layoutInfo.visibleItemsInfo

    @Volatile
    var currentDraggable = DraggableItem.empty()
        private set

    fun onDragStart(offset: Offset) {
        val clickOffset = offset

        visibleItemsInfo.forEach {
            it.offset
            it.index
            Log.d("TAGS42", "visibleItemsInfo offset: ${it.offset}; index: ${it.index}")
        }

        /*val count = layoutInfo.totalItemsCount
        visibleItemsInfo.forEach {
            val index = it.index
            val offset = it.offset
            Log.d("TAGS42", "count $count; index $index; $ $offset")
        }*/
        val item = visibleItemsInfo.firstOrNull { item ->
            clickOffset.y.toInt() in item.offset..(item.offset + item.size)
        } ?: return

        val itemTopOffset = item.offset * 1f
        val itemBottomOffset = item.offsetEnd * 1f
        Log.d("TAGS42", "onDragStart -- $clickOffset; ${clickOffset.y - itemTopOffset * 1f}")

        currentDraggable = DraggableItem(
            clickDistance = clickOffset.y - itemTopOffset * 1f,
            itemIndex = item.index,
            startOffset = DraggableItem.ItemOffset(itemTopOffset, itemBottomOffset)
        )
        changedList.addCursor(currentDraggable)
    }
    private var last = Pair<Int, Int>(-1, MOVE_NONE)

    //создать очередь событий
    private var action = 0

    private val mutex = Mutex()

    private var job = Job()

    suspend fun onDrag(
        offset: Offset, fingerOffset: Offset, moveDirection: Int, composableScope: CoroutineScope) = coroutineScope {
        if (moveDirection == 0) return@coroutineScope

        with(currentDraggable) {
            val startTopPosition = startOffset.top
            val realTop = fingerOffset.y - clickDistance

            draggableXOffset += offset.x
            draggableYOffset += offset.y  // - clickDistance - startTopPosition

            realTopPosition += offset.y
            //Log.d("TAGS42", "-- onDrag realTopPosition $realTopPosition; offset $offset")

            val realTopOf = visibleItemsInfo.firstOrNull { it.index == itemIndex }?.offset ?: -1
            //Log.d("TAGS42", "-- onDrag -- realTop ${realTop}; realTopPosition ${realTopPosition}; realTopOf $realTopOf")

            visibleItemsInfo.forEach { iteminfo ->
                val itemInfoIndex = iteminfo.index

                val top = iteminfo.offset
                val bottom = iteminfo.offsetEnd
                val height = bottom - top

                val moveUp = moveDirection == -1
                val moveDown = moveDirection == 1

                Log.d("TAGS42", "-- for each -- index $itemIndex; itemInfoIndex $itemInfoIndex; realTopPosition $realTopPosition")
                when {
                    moveUp -> {
                        /*if (abs(itemInfoIndex - itemIndex) != 1) {
                            //Log.d("TAGS42", "interrupt index $index; itemInfoIndex $itemInfoIndex ")
                            return@forEach
                        }*/

                        val range = top + (0.5 * height).toInt()
                        val inRangeOfPrevious = realTopPosition.toInt() in top..range
                        val previousHasPassed = realTopPosition.toInt() < range
                        Log.d("TAGS42", "-- > inRangeOfPrevious $inRangeOfPrevious; realTopPosition $realTopPosition; range $range")
                        if (inRangeOfPrevious) {
                            //Log.d("TAGS42", "realTopPosition $realTopPosition; top $top; bottom $bottom; range $range")

                            changedList.moveUp(DraggableItem(itemInfoIndex))

                            with(changedPosition) {
                                changedIndex = itemInfoIndex
                                animatable.animateTo(targetValue = height * 1f, animationSpec = tween(easing = LinearEasing, durationMillis = CHANGED_DURATION_MS))
                                changedPosition.reset()
                            }
                            Log.d("TAGS42", "index $itemIndex; itemInfoIndex $itemInfoIndex")
                            onSwap(itemInfoIndex, itemIndex)

                            currentDraggable = DraggableItem(
                                itemIndex = itemInfoIndex,
                                startOffset = DraggableItem.ItemOffset(top * 1f, bottom * 1f)
                            ).also {
                                it.realTopPosition = 1f * realTopPosition //- (realTopPosition - top)//realTopPosition - PADDING_CONTENT_LAZY_COLUMN_DP * density
                            }

                            action = 0
                            return@coroutineScope
                        }
                    }
                    moveDown -> {
                        if (abs(itemInfoIndex - itemIndex) != 1) {
                            Log.d("TAGS42", "interrupt index $itemIndex; itemInfoIndex $itemInfoIndex ")
                            return@forEach
                        }
                        /*if (last.first == itemInfoIndex && last.second == MOVE_DOWN) {
                            return
                        } else {
                            last = itemInfoIndex to MOVE_DOWN
                        }*/
                        //val realBottomPosition = (realTopPosition + height).toInt()
                        //val range = bottom - (0.3 * height).toInt()

                        val range = top - (0.5 * height).toInt()
                        val inRangeOfNext = realTopPosition.toInt() in range..top
                        if (inRangeOfNext) {
                            if (action == -1) {
                                return@coroutineScope
                            } else {
                                action = -1
                            }
                            val wasAlreadyExist = changedList.moveDown(DraggableItem(itemInfoIndex))
                            launch {
                                Log.d("LOL", "1")
                                with(changedPosition) {
                                    changedIndex = itemInfoIndex
                                    animatable.animateTo(targetValue = - height * 1f, animationSpec = tween(easing = LinearEasing, durationMillis = CHANGED_DURATION_MS))
                                    changedPosition.reset()
                                }
                                Log.d("LOL", "2")
                            }

                            currentDraggable = DraggableItem(
                                itemIndex = itemInfoIndex,
                                startOffset = DraggableItem.ItemOffset(top * 1f, bottom * 1f)
                            ).also {
                                it.realTopPosition = 1f * realTopPosition //- (realTopPosition - top)//realTopPosition - PADDING_CONTENT_LAZY_COLUMN_DP * density
                            }

                            onSwap(itemInfoIndex, itemIndex)
                            action = 0
                            return@coroutineScope
                        }
                    }
                    else -> Unit
                }
            }

            /*changedItem?.let {
                val top = it.offset
                val bottom = it.offsetEnd
                val height = bottom - top
                with(changedPosition) {
                    if (replaced) return@let
                    index = it.index
                    animatable.animateTo(height * 1f)
                    replaced = true
                }
                Log.d("TAGS42", "changedItem ${it.index}")
            }*/
        }
    }
    suspend fun onDragInterrupted() {
        Log.d("TAGS42", "onDragInterrupted --")
        currentDraggable = DraggableItem.empty()
        reset()
        changedList.reset()
        changedPosition.reset()
    }
    fun checkForOverScroll(): Float {
        return 0f
    }
}