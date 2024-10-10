package com.example.dragndrop2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dragndrop2.data.loadPersonList1
import com.example.dragndrop2.data.loadPersonList2
import com.example.dragndrop2.data.loadPersonList3
import com.example.dragndrop2.drag_n_drop.SectionListUI
import com.example.dragndrop2.drag_n_drop_3.SectionListUI3
import com.example.dragndrop2.drag_n_drop_6.SectionListUI6
import com.example.dragndrop2.drag_n_drop_another.SectionListUIAnother
import com.example.dragndrop2.screens.ListScreenViewModel
import com.example.dragndrop2.ui.theme.DragNDrop2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DragNDrop2Theme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    //drag_n_drop_7()
                    SectionListUI3(
                        viewModel = viewModel(
                            factory = ListScreenViewModel.Factory(
                                ::loadPersonList1,
                                ::loadPersonList2,
                                ::loadPersonList3
                            )
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    //ListScreen(Modifier.padding(horizontal = 10.dp))
                }
            }
        }
    }
}
