package com.example.dragndrop2.drag_n_drop_3

import android.util.Log
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.dragndrop2.model.Category
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val PADDING_CONTENT_LAZY_COLUMN_DP = 8

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> DragDropColumn3(
    list: List<T>,
    category: Category,
    onSwap: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dragDropState = rememberDragDropState4(listState) { fromIndex, toIndex ->
        onSwap(fromIndex, toIndex)
    }

    LazyColumn(
        modifier = modifier
            .pointerInput(dragDropState) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, offset ->
                        change.consume()


                        val moveDirection = when {
                            offset.y < 0 -> -1 // move up
                            offset.y > 0 -> 1 // move down
                            else -> 0
                        }

                        coroutineScope.launch {
                            dragDropState.onDrag(
                                offset = offset,
                                moveDirection = moveDirection
                            )
                        }

                        if (overscrollJob?.isActive == true)
                            return@detectDragGesturesAfterLongPress

                        //Log.d("TAGS42", "-- onDrag --")

                        dragDropState
                            .checkForOverScroll()
                            .takeIf { it != 0f }
                            ?.let {
                                overscrollJob =
                                    scope.launch {
                                        dragDropState.state.animateScrollBy(
                                            it*1.3f, tween(easing = FastOutLinearInEasing)
                                        )
                                    }
                            }
                            ?: run { overscrollJob?.cancel() }
                    },
                    onDragStart = { offset ->
                        coroutineScope.launch {
                            dragDropState.onDragStart(offset)
                        }
                        //Log.d("TAGS42", "-- onDragStart --")
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            dragDropState.onDragInterrupted()
                        }
                        overscrollJob?.cancel()
                        //Log.d("TAGS42", "-- onDragEnd --")
                    },
                    onDragCancel = {
                        coroutineScope.launch {
                            dragDropState.onDragInterrupted()
                        }
                        overscrollJob?.cancel()
                        //Log.d("TAGS42", "-- onDragCancel --")
                    }
                )
            },
        state = listState,
        contentPadding = PaddingValues(PADDING_CONTENT_LAZY_COLUMN_DP.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items = list) { index, item ->
            DraggableItem4(
                dragDropState = dragDropState,
                index = index,
                category = category,
                modifier = Modifier
            ) {
                /*Card(elevation = CardDefaults.cardElevation(defaultElevation = elevation)) {
                    itemContent(item)
                }*/
                itemContent(item)
            }
        }
    }
}