package com.example.dragndrop2.drag_n_drop_6

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dragndrop2.data.SwapModel
import com.example.dragndrop2.drag_n_drop_3.CHANGED_DURATION_MS
import com.example.dragndrop2.drag_n_drop_3.PADDING_CONTENT_LAZY_COLUMN_DP
import com.example.dragndrop2.drag_n_drop_3.drag_drop_5.DragDropState5
import com.example.dragndrop2.model.Category
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun rememberDragDropState6(
    lazyListState: LazyListState,
    swapList: (List<SwapModel>) -> Unit
): DragDropState6 {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState6(
            coroutineScope = coroutineScope,
            state = lazyListState,
            paddingPx = PADDING_CONTENT_LAZY_COLUMN_DP * density,
            swapList = swapList
        )
    }
    return state
}

@Composable
private fun rememberAnimateWrapper() = remember { DragDropAnimationState() }

@Stable
class DragDropAnimationState {
    val dragOffset: MutableState<Float> = mutableFloatStateOf(0f)
    val exchangeAnimatable: Animatable<Float, AnimationVector1D> = Animatable(0f)
}

private const val SHAPE_CORNER_DP = 14

@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem6(
    dragDropState: DragDropState6,
    category: Category,
    index: Int,
    modifier: Modifier,
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()
    val paddingContent = PADDING_CONTENT_LAZY_COLUMN_DP * density

    val itemDragState = rememberDraggingState()

    var draggableOffset by remember { mutableFloatStateOf(0f) }

    val draggableAnim: Float by animateFloatAsState(
        targetValue = draggableOffset,
        label = "draggableAnimation"
    )

    DisposableEffect(Unit) {
        onDispose {
            Log.d("TAGS42", "on dispose $index")
        }
    }

    //dragging launch effect
    LaunchedEffect(Unit) {
        dragDropState.draggableApi.collect { draggableItem ->
            when {
                index == draggableItem.originalIndex -> {
                    //draggableOffset = draggableItem.offset.value
                    itemDragState.onDragging()
                }

                draggableItem.isEmpty() -> {
                    /*if (draggableOffset != 0f) {
                        draggableOffset = 0f
                    }*/
                    itemDragState.onStopDragging()
                }
            }
        }
    }

    //exchange launch effect
    LaunchedEffect(Unit) {
        dragDropState.exchangeApi.collect { exchangeItem ->
            val exchangedItemIndex = exchangeItem.originalIndex
            //Log.d("TAGS42", "$index onExchanging $changedItemIndex")
            itemDragState.onExchanging(index == exchangedItemIndex)
        }
    }

    val draggableModifier = remember(itemDragState.state) {
        val isDragging = itemDragState.state == ItemDragState.Type.DRAGGING
        val none = itemDragState.state == ItemDragState.Type.NONE
        val exchange = itemDragState.state == ItemDragState.Type.EXCHANGE
        val draggableModifier = when {
            isDragging -> {
                //Log.d("TAGS42", "change draggableModifier to ${itemDragState.state}; ${index}")
                Modifier
                    .zIndex(2f)
                    .graphicsLayer {
                        translationY += dragDropState.draggableApi.replayCache.last().offset.value
                    }
            }

            exchange -> {
                //Log.d("TAGS42", "change draggableModifier to ${itemDragState.state}; ${index}")
                Modifier
                    .zIndex(1f)
                    .graphicsLayer {
                        translationY += dragDropState.exchangeApi.replayCache.last().animatable.value
                    }
            }

            else -> {
                //Log.d("TAGS42", "change draggableModifier to ${itemDragState.state}; ${index}")
                Modifier.graphicsLayer {

                    //translationY += animateWrapper.exchangeAnimatable.value
                    //Log.d("TAGS42", "${index}; draggableOffset $draggableOffset; exchangeAnimatable ${animateWrapper.exchangeAnimatable.value}; translationY $translationY")
                }
            }
        }

        val shadowModifier = if (isDragging) Modifier.shadow(
            elevation = 6.dp,
            ambientColor = Color.Green,
            shape = RoundedCornerShape(SHAPE_CORNER_DP.dp)
        ) else Modifier
        draggableModifier.then(shadowModifier)
    }

    Box(
        modifier = draggableModifier
    ) {
        Card(shape = RoundedCornerShape(SHAPE_CORNER_DP.dp)) {
            content()
        }
    }
}

@Composable
private fun rememberDraggingState(): ItemDragState {
    val draggingState = remember { ItemDragState() }
    return draggingState
}

@Stable
private class ItemDragState {
    var state by mutableStateOf(Type.NONE)

    /*
    none
    dragging
    none
     */

    /*
    none
    exchanging
    none
     */

    fun onDragging() {
        state = Type.DRAGGING
    }

    fun onExchanging(isExchange: Boolean) {
        if (isExchange) state = Type.EXCHANGE
    }

    fun onStopDragging() {
        state = Type.NONE
    }

    enum class Type {
        DRAGGING, EXCHANGE, NONE

    }
}

@Composable
private fun Content(
    itemDragState: ItemDragState.Type,
    draggableAnimation: Float,
    exchangeAnimatable: Float,
    content: @Composable () -> Unit
) {
    val isDragging = itemDragState == ItemDragState.Type.DRAGGING
    val none = itemDragState == ItemDragState.Type.NONE
    val exchange = itemDragState == ItemDragState.Type.EXCHANGE

    val draggingModifier = Modifier
        .zIndex(if (isDragging) 2f else 1f)
        .graphicsLayer {
            translationY += when {
                isDragging -> draggableAnimation
                none -> 0f
                exchange -> exchangeAnimatable
                else -> 0f
            }
        }

    /*val draggingModifier = if (isDragging) {
        Modifier
            .zIndex(2f)
            .graphicsLayer {
                translationY += draggableAnimation
                //Log.d("TAGS42", "-- translationY $currentYOffset; -- $currentYOffset")
            }
    } else {
        Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY += exchangeAnimatable.value
            }
    }*/
    /*else {
        Modifier.animateItemPlacement(
            tween(easing = FastOutLinearInEasing)
        )
    }*/

    val shadowModifier = if (isDragging) {
        Modifier.shadow(
            elevation = 6.dp,
            ambientColor = Color.Green,
            shape = RoundedCornerShape(SHAPE_CORNER_DP.dp)
        )
    } else {
        Modifier
    }

    Box(
        modifier = draggingModifier.then(shadowModifier)
    ) {
        Card(shape = RoundedCornerShape(SHAPE_CORNER_DP.dp)) {
            content()
        }
    }
}
