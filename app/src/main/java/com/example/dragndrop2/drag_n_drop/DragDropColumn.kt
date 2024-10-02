package com.example.dragndrop2.drag_n_drop

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LocalPinnableContainer
import androidx.compose.ui.unit.dp
import com.example.dragndrop2.model.Category
import com.example.dragndrop2.model.PersonUiModel
import kotlinx.coroutines.Job

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T : Any> DragDropColumn(
    modifier: Modifier = Modifier,
    category: Category,
    listState: LazyListState = rememberLazyListState(),
    dragDropState2: DragDropState2,
    list: List<PersonUiModel>,
    onSwap: (Int, Int) -> Unit,
    itemContent: @Composable LazyItemScope.(item: PersonUiModel, index: Int) -> Unit
) {
    Log.d("TAGS42", "------")
    Log.d("TAGS42", "$list")

    var overscrollJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val dragDropState = rememberDragDropState(listState) { fromIndex, toIndex ->
        onSwap(fromIndex, toIndex)
    }

    LazyColumn(
        modifier = modifier
        /*.pointerInput(dragDropState) {
            detectDragGesturesAfterLongPress(
                onDrag = { change, offset ->
                    change.consume()
                    dragDropState.onDrag(offset = offset)

                    if (overscrollJob?.isActive == true)
                        return@detectDragGesturesAfterLongPress

                    dragDropState
                        .checkForOverScroll()
                        .takeIf { it != 0f }
                        ?.let {
                            overscrollJob =
                                scope.launch {
                                    dragDropState.state.animateScrollBy(
                                        it * 1.3f, tween(easing = FastOutLinearInEasing)
                                    )
                                }
                        }
                        ?: run { overscrollJob?.cancel() }
                },
                onDragStart = { offset -> dragDropState.onDragStart(offset) },
                onDragEnd = {
                    dragDropState.onDragInterrupted()
                    overscrollJob?.cancel()
                },
                onDragCancel = {
                    dragDropState.onDragInterrupted()
                    overscrollJob?.cancel()
                }
            )
        }*/,
        state = listState,
        //contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items = list) { index, item ->
            DraggableItem(
                dragDropState2 = dragDropState2,
                index = index,
                category = category,
                modifier = Modifier
            ) { isDragging ->
                val elevation by animateDpAsState(if (isDragging) 4.dp else 0.dp)
                Card(
                    modifier = Modifier,
                    elevation = CardDefaults.cardElevation(defaultElevation = elevation)
                ) {
                    itemContent(item, index)
                }
            }
        }
    }
}