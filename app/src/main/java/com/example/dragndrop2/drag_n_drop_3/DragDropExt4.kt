package com.example.dragndrop2.drag_n_drop_3

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch

@Composable
fun rememberDragDropState4(
    lazyListState: LazyListState,
    onSwap: (Int, Int) -> Unit
): DragDropState4 {
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState4(
            state = lazyListState,
            paddingPx = PADDING_CONTENT_LAZY_COLUMN_DP * density
        )
    }
    return state
}


@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem4(
    dragDropState: DragDropState4,
    category: Category,
    index: Int,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()

    val paddingContent = PADDING_CONTENT_LAZY_COLUMN_DP * density

    val currentYOffset: Float by animateFloatAsState(
        targetValue = dragDropState.yOffsetOfDraggable,
        label = "yOffset"
    )

    val dragging by remember(dragDropState.selectedDraggable) {
        derivedStateOf { index == dragDropState.selectedDraggable.itemIndex }
    }

    val changedAnimatable = remember { Animatable(0f) }
    //val changedItem: DraggableItem4 by dragDropState.changedItemFlow.collectAsState(DraggableItem4.empty())
    /*val thisItemChanged: Boolean by remember {
        derivedStateOf{ changedItem.itemIndex == index }
    }*/

    LaunchedEffect(Unit) {
        dragDropState.changedItemFlow.collect { changedItem ->
            val changedItemIndex = changedItem.itemIndex
            //Log.d("TAGS42", "-- -- LaunchedEffect $index; collect $changedItemIndex")
            if (index == changedItemIndex) {
                coroutineScope.launch {
                    try {
                        val itemInfo = dragDropState.state.layoutInfo.visibleItemsInfo.first { it.index == index }
                        val itemInfoTop = itemInfo.offset
                        changedAnimatable.animateTo(
                            targetValue = itemInfoTop - changedItem.startPosition.yTop,
                            animationSpec = tween(easing = LinearEasing, durationMillis = CHANGED_DURATION_MS)
                        )
                    } catch (ex: CancellationException) {
                        Log.d("TAGS42", "-- -- animation $index has ex ${ex.message}")
                        throw ex
                    }
                }.invokeOnCompletion {
                    Log.d("TAGS42", "-- -- animation $index was completed")
                }
            }
        }
    }

    val draggingModifier = if (dragging) {
        Modifier
            .zIndex(2f)
            .graphicsLayer {
                //translationX += currentXOffset
                translationY += currentYOffset
                //Log.d("TAGS42", "-- translationY $translationY; -- $a")
            }
    } else {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY += changedAnimatable.value
            }
    }
    /*else {
        Modifier.animateItemPlacement(
            tween(easing = FastOutLinearInEasing)
        )
    }*/

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
