package com.example.dragndrop2.drag_n_drop

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.example.dragndrop2.drag_n_drop_another.DragDropStateAnother
import com.example.dragndrop2.model.Category

@Composable
fun rememberDragDropState(
    lazyListState: LazyListState,
    onSwap: (Int, Int) -> Unit
): DragDropStateAnother {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropStateAnother(
            state = lazyListState,
            onSwap = onSwap,
            scope = scope
        )
    }
    return state
}

fun LazyListState.getVisibleItemInfoFor(absoluteIndex: Int): LazyListItemInfo? {
    return this
        .layoutInfo
        .visibleItemsInfo
        .getOrNull(absoluteIndex - this.layoutInfo.visibleItemsInfo.first().index)
}

val LazyListItemInfo.offsetEnd: Int
    get() = this.offset + this.size

@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem(
    dragDropState2: DragDropState2,
    category: Category,
    index: Int,
    modifier: Modifier,
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit
) {
    val current = animateFloatAsState(dragDropState2.currentItemOffset //* 0.67f
    )
    val changed = animateFloatAsState(dragDropState2.changedItem.value)
    val dragging = index == dragDropState2.currentItem.index && category == dragDropState2.currentItem.category
    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                //Log.d("TAGS42", "dragging graphicsLayer $current")
                translationY += current.value
            }
    } else if (index == dragDropState2.changedIndex.index && category == dragDropState2.changedIndex.category) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = changed.value
                //Log.d("TAGS42", "dragging graphicsLayer $translationY")
            }
    } else {
        //Log.d("TAGS42", "dragging index $index; changedIndex ${dragDropState2.changedIndex}")
        Modifier.animateItemPlacement(
            tween(easing = FastOutLinearInEasing)
        )
    }
    Column(modifier = modifier.then(draggingModifier)) {
        content(dragging)
    }
}