package com.example.dragndrop2.drag_n_drop_3

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dragndrop2.model.Category
import com.example.dragndrop2.model.PersonUiModel
import com.example.dragndrop2.screens.ListScreenViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SectionListUI3(
    viewModel: ListScreenViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val listState1: LazyListState = rememberLazyListState()
    val listState2: LazyListState = rememberLazyListState()

    val configuration = LocalConfiguration.current
    val portrait by remember(configuration.orientation) {
        mutableStateOf(configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
    }

    var enteredIndex by remember {
        mutableIntStateOf(-1)
    }

    val move: (Int, Int) -> Unit = remember { { from, to -> viewModel.swap(from, to) } }

    if (portrait) {
        Column(modifier = modifier) {
            DragDropColumn3<PersonUiModel>(
                modifier = Modifier,
                category = Category.FIRST,
                list = uiState.list1,
                onSwap = { from, to ->
                    viewModel.swap(Category.FIRST, from, to)
                }
            ) { item ->
                ListItem3(
                    model = item,
                    category = Category.FIRST
                )
            }
        }
    } else {
        Row(modifier.fillMaxSize()) {
            DragDropColumn3<PersonUiModel>(
                modifier = Modifier.weight(1f),
                category = Category.FIRST,
                list = uiState.list1,
                onSwap = { from, to ->
                    viewModel.swap(Category.FIRST, from, to)
                }
            ) { item ->
                ListItem3(
                    model = item,
                    category = Category.FIRST
                )
            }

            DragDropColumn3<PersonUiModel>(
                modifier = Modifier.weight(1f),
                category = Category.SECOND,
                list = uiState.list2,
                onSwap = { from, to ->
                    viewModel.swap(Category.SECOND, from, to)
                }
            ) { item ->
                ListItem3(
                    model = item,
                    category = Category.SECOND
                )
            }
        }
    }
}