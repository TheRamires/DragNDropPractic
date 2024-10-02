package com.example.dragndrop2.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dragndrop2.data.loadPersonList1
import com.example.dragndrop2.data.loadPersonList2
import com.example.dragndrop2.data.loadPersonList3
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.absoluteValue

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListScreen(
    modifier: Modifier = Modifier,
    viewModel: ListScreenViewModel = viewModel(
        factory = ListScreenViewModel.Factory(
            ::loadPersonList1,
            ::loadPersonList2,
            ::loadPersonList3
        )
    )
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        viewModel.loadData()
        onDispose { }
    }

    val listState: LazyListState = rememberLazyListState()

    var position by remember {
        mutableStateOf<Float?>(null)
    }
    var draggedItem by remember {
        mutableStateOf<Int?>(null)
    }
    LaunchedEffect(Unit) {
        val listStateFlow = snapshotFlow { listState.layoutInfo }
        val positionFlow = snapshotFlow { position }.distinctUntilChanged()

        listStateFlow.combine(positionFlow) { state, pos ->
                pos?.let { draggedCenter: Float ->
                    state.visibleItemsInfo
                        .minByOrNull { (draggedCenter - (it.offset + it.size / 2f)).absoluteValue }
                }?.index
            }
            .distinctUntilChanged()
            .collect { near ->
                draggedItem = when {
                    near == null -> null
                    draggedItem == null -> near
                    else -> near.also {
                        //viewModel.swap(draggedItem!!, it)
                    }
                }
            }
    }
    LazyColumn(
        state = listState,
        modifier = modifier.pointerInput(Unit) {
            detectDragGesturesAfterLongPress(
                onDragStart = { offset: Offset ->
                    listState.layoutInfo.visibleItemsInfo
                        .firstOrNull { firstVisible ->
                            offset.y.toInt() in firstVisible.offset..firstVisible.offset + firstVisible.size
                        }?.also {
                            position = it.offset + it.size / 2f
                        }
                },
                onDrag = { change: PointerInputChange, dragAmount: Offset ->
                    change.consume()
                    position = position?.plus(dragAmount.y)
                    // Start autoscrolling if position is out of bounds
                }
            )
        }
    ) {
        /*item {
            Spacer(modifier = Modifier.height(10.dp))
        }

        itemsIndexed(items = uiState.value, key = { index: Int, item -> item }) { idx, uiModel ->
            ListItem(uiModel)
        }

        item {
            Spacer(modifier = Modifier.height(10.dp))
        }*/
    }
}
