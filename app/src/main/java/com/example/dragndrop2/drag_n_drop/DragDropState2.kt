package com.example.dragndrop2.drag_n_drop

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dragndrop2.model.Category
import com.example.dragndrop2.screens.ListScreenViewModel

data class Item(val category: Category, val index: Int, val enterEvent: EnterEvent) {
    companion object {
        fun empty() = Item(Category.FIRST, -1, EnterEvent.empty())
    }
}

data class EnterEvent(val offsetFromTop: Float, val yTopPosition: Float, val yBottomPosition: Float) {
    companion object {
        fun empty() = EnterEvent(0f, 0f, 0f)
    }
}

class DragDropState2(
    private val state1: LazyListState,
    private val state2: LazyListState,
    private val move: (Item, Item) -> Unit) {

    private val visibleItemsInfo1 get() = state1.layoutInfo.visibleItemsInfo
    private val visibleItemsInfo2 get() = state2.layoutInfo.visibleItemsInfo
    private fun getLayoutInfo(category: Category) = when (category) {
        Category.FIRST -> state1.layoutInfo
        Category.SECOND -> state2.layoutInfo
        Category.THIRD -> throw IllegalStateException("No no no no")
    }
    private fun getVisibleItemsInfo(category: Category) = when (category) {
        Category.FIRST -> visibleItemsInfo1
        Category.SECOND -> visibleItemsInfo2
        Category.THIRD -> throw IllegalStateException("No no no no")
    }

    private fun getCurrentItemInfo(category: Category) = when (category) {
        Category.FIRST -> visibleItemsInfo1.firstOrNull { it.index == currentItem.index }
        Category.SECOND -> visibleItemsInfo2.firstOrNull { it.index == currentItem.index }
        Category.THIRD -> throw IllegalStateException("No no no no")
    }

    var prevItem = Item.empty()
        private set

    var currentItem = Item.empty()
        private set

    var lastOffset = 0f
        private set

    var currentItemOffset by mutableFloatStateOf(0f)

    var changedIndex = Item.empty()
    val changedItem = Animatable(0f)

    fun onEntered(category: Category, index: Int, y: Float) {
        val visibleItemsInfo = when (category) {
            Category.FIRST -> visibleItemsInfo1
            Category.SECOND -> visibleItemsInfo2
            Category.THIRD -> throw IllegalStateException("No no no")
        }

        visibleItemsInfo.forEach {
            Log.d("TAGS42", "visibleItemsInfo ${it.key}; ${it.offset} to ${it.offsetEnd}")
        }
        val item = visibleItemsInfo.firstOrNull {
            y.toInt() in it.offset..it.offsetEnd
        } ?: return
        val index = item.index
        Log.d("TAGS42", "index $index; onEntered $y")
        val itemTop = item.offset
        val itemBottom = item.offsetEnd

        currentItem = Item(category, index, EnterEvent(y - itemTop, itemTop * 1f, itemBottom * 1f))

        //move(lastItem, currentItem)
    }

    suspend fun onMoved(x: Float, y: Float) {
        with(currentItem.enterEvent) {
            offsetFromTop
            yTopPosition

            currentItemOffset = y - offsetFromTop - yTopPosition
            //Log.d("TAGS42", "onMoved: $y; currentOffsetState: ${currentOffsetState}")

            val realYOfCurrent = yTopPosition + currentItemOffset
            val category = currentItem.category
            val currentIndex = currentItem.index
            /*val currentItem = getVisibleItemsInfo(category)[currentItem.index]
            val prevItem = if (currentIndex > 0) {
                getVisibleItemsInfo(category)[currentItem.index - 1]
            } else null
            val nextItem = if (currentIndex < getLayoutInfo(category).totalItemsCount -1) {
                getVisibleItemsInfo(category)[currentItem.index + 1]
            } else null*/

            /*val currentY = realYOfCurrent.toInt()
            when {
                currentY < currentItem.enterEvent.yTopPosition -> {
                    changedIndex = Item(category, currentItem.index -1, EnterEvent.empty())
                    changedItem.animateTo(
                        currentItem.enterEvent.yTopPosition,
                        tween(easing = FastOutLinearInEasing)
                    )
                    onExited()
                }
                currentY > currentItem.enterEvent.yBottomPosition -> {
                    changedIndex = Item(category, currentItem.index +1, EnterEvent.empty())
                    changedItem.animateTo(
                        currentItem.enterEvent.yTopPosition,
                        tween(easing = FastOutLinearInEasing)
                    )
                    onExited()
                }
            }*/
        }
    }

    fun onDrop() {
        Log.d("TAGS42", "onDrop")
        prevItem = Item.empty()
        currentItem = Item.empty()
        currentItemOffset = 0f
    }

    fun onExited() {
        Log.d("TAGS42", "onExited")
        currentItemOffset = 0f
        prevItem = currentItem
        //currentOffsetState = 0f
    }
}

@Composable
fun rememberDragDropState2(state1: LazyListState, state2: LazyListState, viewModel: ListScreenViewModel): DragDropState2 {
    val state = remember {
        DragDropState2(state1, state2, viewModel::swap)
    }
    return state
}