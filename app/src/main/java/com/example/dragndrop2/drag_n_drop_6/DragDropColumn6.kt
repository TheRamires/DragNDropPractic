package com.example.dragndrop2.drag_n_drop_6

import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.dragndrop2.data.SwapModel
import com.example.dragndrop2.model.Category
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

const val PADDING_CONTENT_LAZY_COLUMN_DP = 8

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> DragDropColumn6(
    list: List<T>,
    category: Category,
    swapList: (List<SwapModel>) -> Unit,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val dragDropState = rememberDragDropState6(listState, swapList)

    LazyColumn(
        modifier = modifier
            .pointerInput(dragDropState) {
                detectDragGesturesAfterLongPress(
                    onDrag = { change, offset ->
                        change.consume()

                        val moveDirection = when {
                            offset.y < 0 -> Direction.MOVE_UP // move up
                            offset.y > 0 -> Direction.MOVE_DOWN // move down
                            else -> Direction.NONE
                        }

                        coroutineScope.launch {
                            dragDropState.onDrag(
                                offset = offset,
                                direction = moveDirection
                            )
                        }
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
            DraggableItem6(
                dragDropState = dragDropState,
                category = category,
                index = index,
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