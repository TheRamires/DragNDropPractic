package com.example.dragndrop2.drag_n_drop

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.dragndrop2.R
import com.example.dragndrop2.drag_n_drop.DragDropState2
import com.example.dragndrop2.model.Category
import com.example.dragndrop2.model.PersonUiModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ListItem(
    model: PersonUiModel,
    category: Category,
    index: Int,
    dragDropState2: DragDropState2
) {
    val coroutineScope = rememberCoroutineScope()

    Column(
        Modifier
            .then(
                if (dragDropState2.currentItem.category == category && dragDropState2.currentItem.index == index) {
                    Modifier.background(Color.White)
                } else Modifier.background(Color.Cyan)
            )
            .fillMaxWidth()
            .dragAndDropTarget(
                shouldStartDragAndDrop = {
                    true
                },
                target = object : DragAndDropTarget {
                    override fun onMoved(event: DragAndDropEvent) {
                        super.onMoved(event)
                        val x: Float = event.toAndroidDragEvent().x
                        val y: Float = event.toAndroidDragEvent().y
                        coroutineScope.launch {
                            dragDropState2.onMoved(x, y)
                        }
                    }

                    override fun onDrop(event: DragAndDropEvent): Boolean {
                        dragDropState2.onDrop()
                        return true
                    }

                    override fun onEntered(event: DragAndDropEvent) {
                        super.onEntered(event)
                        val y: Float = event.toAndroidDragEvent().y
                        dragDropState2.onEntered(category, index, y)
                    }

                    override fun onExited(event: DragAndDropEvent) {
                        dragDropState2.onExited()
                        super.onExited(event)
                    }
                }
            )
            .dragAndDropSource(
                drawDragDecoration = {}
            ) {
                detectTapGestures(
                    onLongPress = {
                        startTransfer(
                            DragAndDropTransferData(
                                ClipData(
                                    "",
                                    arrayOf("array of mimeTypes - those tell dragAndDropTargets which type of dragAndDropSource this is, such as an image, text, phone number, or your custom type"),
                                    ClipData.Item("you can add new clip items with data here")
                                )
                            )
                        )
                    }
                )
            }
    ) {
        Row(
            Modifier
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .fillMaxWidth()
                .height(100.dp)
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(text = model.name)
            Icon(
                painter = painterResource(id = R.drawable.ic_grabber),
                contentDescription = null
            )
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = Color.Black
        )
    }
}