package com.example.dragndrop2.drag_n_drop_another

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dragndrop2.model.Category
import com.example.dragndrop2.model.PersonUiModel
import com.example.dragndrop2.screens.ListScreenViewModel

@Composable
fun SectionListUIAnother(
    viewModel: ListScreenViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val portrait by remember(configuration.orientation) {
        mutableStateOf(configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
    }

    if (portrait) {
        Column(modifier = modifier) {
            DragDropColumnAnother<PersonUiModel>(
                modifier = Modifier,
                category = Category.FIRST,
                list = uiState.list1,
                onSwap = { from, to ->
                    viewModel.swap(Category.FIRST, from, to)
                }
            ) { item ->
                ListItemAnother(
                    model = item,
                    category = Category.FIRST
                )
            }
        }
    } else {
        Row(modifier.fillMaxSize()) {
            DragDropColumnAnother<PersonUiModel>(
                modifier = Modifier.weight(1f),
                category = Category.FIRST,
                list = uiState.list1,
                onSwap = { from, to ->
                    viewModel.swap(Category.FIRST, from, to)
                }
            ) { item ->
                ListItemAnother(
                    model = item,
                    category = Category.FIRST
                )
            }
            Spacer(modifier = Modifier.width(20.dp))

            DragDropColumnAnother<PersonUiModel>(
                modifier = Modifier.weight(1f),
                category = Category.SECOND,
                list = uiState.list2,
                onSwap = { from, to ->
                    viewModel.swap(Category.SECOND, from, to)
                }
            ) { item ->
                ListItemAnother(
                    model = item,
                    category = Category.SECOND
                )
            }
        }
    }
}
