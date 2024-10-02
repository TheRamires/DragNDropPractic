package com.example.dragndrop2.drag_n_drop_3

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.example.dragndrop2.model.Category

@Composable
fun rememberDragDropState3(
    lazyListState: LazyListState,
    onSwap: (Int, Int) -> Unit
): DragDropState3 {
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState3(
            state = lazyListState,
            onSwap = onSwap,
            scope = scope
        )
    }
    return state
}


@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem3(
    dragDropState: DragDropState3,
    category: Category,
    index: Int,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current.density
    val paddingContent = PADDING_CONTENT_LAZY_COLUMN_DP * density

    val currentXOffset: Float by animateFloatAsState(
        targetValue = dragDropState.draggableXOffset,
        label = "xOffset"
    )
    /*val currentYOffset: Float by animateFloatAsState(
        targetValue = dragDropState.draggableYOffset,
        label = "yOffset"
    )*/
    val currentYOffset = dragDropState.draggableYOffset
    val a = dragDropState.currentDraggable.offsetY

    val changedYOffset = dragDropState.changedPosition.animatable

    val dragging by remember(dragDropState.currentDraggable) {
        derivedStateOf { index == dragDropState.currentDraggable.itemIndex }
    }

    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(2f)
            .graphicsLayer {
                //translationX += currentXOffset
                translationY = a
                //Log.d("TAGS42", "-- translationY $translationY; -- $a")
            }
    }
    else if (index == dragDropState.changedPosition.changedIndex) {
        Modifier
            .zIndex(1f)
            .graphicsLayer {

                Log.d("LOL", "graphicsLayer 3")
                translationY += changedYOffset.value + paddingContent
            }
    }
    else {
        Modifier.animateItemPlacement(
            tween(easing = FastOutLinearInEasing)
        )
    }

    val roundedShape = RoundedCornerShape(14.dp)

    val shadowModifier = if (dragging) {
        Modifier.shadow(
            elevation = 6.dp,
            ambientColor = Color.Green,
            shape = roundedShape
        )
    } else {
        Modifier
    }

    Box(
        modifier = draggingModifier.then(shadowModifier)
    ) {
        Card(shape = roundedShape) {
            content()
        }
    }
}
