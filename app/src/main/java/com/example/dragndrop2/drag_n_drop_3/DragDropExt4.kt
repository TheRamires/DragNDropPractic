package com.example.dragndrop2.drag_n_drop_3

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
import com.example.dragndrop2.drag_n_drop_3.drag_drop_5.DragDropState5
import com.example.dragndrop2.model.Category
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun rememberDragDropState4(
    lazyListState: LazyListState,
    onSwap: (Int, Int) -> Unit
): DragDropState5 {
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current.density
    val scope = rememberCoroutineScope()
    val state = remember(lazyListState) {
        DragDropState5(
            coroutineScope = coroutineScope,
            state = lazyListState,
            paddingPx = PADDING_CONTENT_LAZY_COLUMN_DP * density,
            swapList = {

            }
        )
    }
    return state
}

@Composable
private fun rememberAnimateWrapper() = remember { DragDropAnimationState() }

@Stable
class DragDropAnimationState(
    val dragOffset: MutableState<Float> = mutableFloatStateOf(0f),
    val exchangeAnimatable: Animatable<Float, AnimationVector1D> = Animatable(0f)
)

private const val SHAPE_CORNER_DP = 14

@ExperimentalFoundationApi
@Composable
fun LazyItemScope.DraggableItem4(
    dragDropState: DragDropState5,
    category: Category,
    index: Int,
    modifier: Modifier,
    animateWrapper: DragDropAnimationState = rememberAnimateWrapper(),
    content: @Composable () -> Unit
) {
    val density = LocalDensity.current.density
    val coroutineScope = rememberCoroutineScope()
    val paddingContent = PADDING_CONTENT_LAZY_COLUMN_DP * density

    val itemDragState = rememberDraggingState()

    val draggableOffset: Float by animateFloatAsState(
        targetValue = animateWrapper.dragOffset.value,
        label = "draggableAnimation"
    )

    //dragging launch effect
    LaunchedEffect(Unit) {
        dragDropState.draggableApi.collect { draggableItem ->
            if (index == draggableItem.originalIndex) {
                animateWrapper.dragOffset.value = draggableItem.getOffset()
                //Log.d("TAGS42", "$index onDragging")
                itemDragState.onDragging()
            } else if (draggableItem.isEmpty()) {
                //Log.d("TAGS42", "$index onStopDragging")
                animateWrapper.dragOffset.value = 0f
                itemDragState.onStopDragging()
            }
        }
    }

    //exchange launch effect
    LaunchedEffect(Unit) {
        dragDropState.exchangeApi.collect { exchangeItem ->
            val changedItemIndex = exchangeItem.originalIndex
            //Log.d("TAGS42", "$index onExchanging $changedItemIndex")
            itemDragState.onExchanging(index == changedItemIndex)

            if (index == changedItemIndex) {

                coroutineScope.launch {
                    try {
                        val itemInfo =
                            dragDropState.state.layoutInfo.visibleItemsInfo.first { it.index == index }
                        val itemInfoTop = itemInfo.offset
                        animateWrapper.exchangeAnimatable.animateTo(
                            targetValue = exchangeItem.newPosition.start - itemInfoTop,
                            animationSpec = tween(
                                easing = LinearEasing,
                                durationMillis = CHANGED_DURATION_MS
                            )
                        )
                    } catch (ex: CancellationException) {

                        throw ex
                    }
                }.invokeOnCompletion {

                }
            }
        }
    }

    //Log.d("TAGS42", "-- recomposition $index")

    val draggableModifier =
        Modifier.createDraggingModifier(itemDragState, draggableOffset, animateWrapper.exchangeAnimatable.value)

    Box(
        modifier = draggableModifier
    ) {
        Card(shape = RoundedCornerShape(SHAPE_CORNER_DP.dp)) {
            content()
        }
    }
}

private fun Modifier.createDraggingModifier(
    itemDragState: ItemDragState,
    draggableAnimation: Float,
    exchangeAnimatable: Float
): Modifier {
    val isDragging = itemDragState.state == ItemDragState.Type.DRAGGING
    val none = itemDragState.state == ItemDragState.Type.NONE
    val exchange = itemDragState.state == ItemDragState.Type.EXCHANGE

    /*val draggableModifier = Modifier
        .zIndex(if (isDragging) 2f else 1f)
        .graphicsLayer {
            when {
                isDragging -> {
                    translationY += draggableAnimation
                }

                none -> {

                }

                exchange -> {
                    translationY += exchangeAnimatable
                }
            }
        }*/

    val draggableModifier = when {
        isDragging -> Modifier
            .zIndex(2f)
            .graphicsLayer {
                translationY += draggableAnimation
                //Log.d("TAGS42", "-- translationY $currentYOffset; -- $currentYOffset")
            }
        exchange -> Modifier
            .zIndex(1f)
            .graphicsLayer {
                translationY += exchangeAnimatable
            }
        else -> Modifier
    }

    val shadowModifier = if (isDragging) Modifier.shadow(
        elevation = 6.dp,
        ambientColor = Color.Green,
        shape = RoundedCornerShape(SHAPE_CORNER_DP.dp)
    ) else Modifier

    return this
        .then(draggableModifier)
        .then(shadowModifier)
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
