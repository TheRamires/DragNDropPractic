package com.example.dragndrop2.drag_n_drop_another

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.zIndex
import com.example.dragndrop2.model.Category

@Composable
fun rememberDragDropStateAnother(
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


@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItemAnother(
    dragDropState: DragDropStateAnother,
    category: Category,
    index: Int,
    modifier: Modifier,
    content: @Composable ColumnScope.(isDragging: Boolean) -> Unit
) {
    val current: Float by animateFloatAsState(dragDropState.draggingItemOffset * 0.67f)
    val previous: Float by animateFloatAsState(dragDropState.previousItemOffset.value * 0.67f)
    val dragging = index == dragDropState.currentIndexOfDraggedItem
    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = current
            }
    } else if (index == dragDropState.previousIndexOfDraggedItem) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY = previous
            }
    } else {
        Modifier/*.animateItemPlacement(
            tween(easing = FastOutLinearInEasing)
        )*/
    }
    Column(modifier = modifier.then(draggingModifier)) {
        content(dragging)
    }
}