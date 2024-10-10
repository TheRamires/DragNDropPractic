package com.example.dragndrop2.screens

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dragndrop2.drag_n_drop.Item
import com.example.dragndrop2.data.SwapModel
import com.example.dragndrop2.model.Category
import com.example.dragndrop2.model.PersonUiModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

data class PersonsUiState(
    val list1: List<PersonUiModel>,
    val list2: List<PersonUiModel>,
    val list3: List<PersonUiModel>,
) {
    companion object {
        fun empty() = PersonsUiState(emptyList(), emptyList(), emptyList())
    }
}

class ListScreenViewModel(
    private val getList1: () -> List<PersonUiModel>,
    private val getList2: () -> List<PersonUiModel>,
    private val getList3: () -> List<PersonUiModel>,
) : ViewModel(), CoroutineScope {

    override val coroutineContext: CoroutineContext = viewModelScope.coroutineContext

    private val uiState_ = MutableStateFlow(PersonsUiState.empty())
    val uiState get() = uiState_.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        launch {
            delay(1000)
            uiState_.emit(
                PersonsUiState(
                    list1 = getList1(),
                    list2 = getList2(),
                    list3 = getList3()
                )
            )
        }
    }

    fun swap(from: Item, to: Item) {
        if (from == to) return
        if (from.index < 0 || to.index < 0) return
        when {
            from.category == to.category -> swap(to.category, from.index, to.index)
            else -> {
                addToCategory(to.category, to.index, findItem(from.category, from.index))
                removeFromCategory(from.category, from.index)
            }
        }
    }

    fun swap(fromIdx: Int, toIdx: Int) = swap(Category.FIRST, fromIdx, toIdx)

    fun swapList(category: Category, swapList: List<SwapModel>) {
        uiState_.update {
            when (category) {
                Category.FIRST -> {
                    val oldlist = it.list1
                    val newList = oldlist.toMutableList()

                    Log.d("TAGS42", "swapList ${swapList}")

                    val map = mutableMapOf<Int, PersonUiModel>()
                    swapList.forEach {
                        map[it.to] = oldlist[it.from]
                    }

                    for (i in 0..newList.lastIndex) {
                        newList[i] = map[i] ?: newList[i]
                    }

                    it.copy(list1 = newList)
                }
                else -> it
            }
        }
    }

    fun swap(category: Category, fromIdx: Int, toIdx: Int) {
        if (fromIdx == toIdx) return
        if (fromIdx < 0 || toIdx < 0) return
        uiState_.update { old ->
            when (category) {
                Category.FIRST -> {
                    val oldList = old.list1
                    val newList = oldList.move(fromIdx, toIdx)
                    old.copy(list1 = newList)
                }
                Category.SECOND -> {
                    val oldList = old.list2
                    val newList = oldList.move(fromIdx, toIdx)
                    old.copy(list2 = newList)
                }
                Category.THIRD -> {
                    val oldList = old.list3
                    val newList = oldList.move(fromIdx, toIdx)
                    old.copy(list3 = newList)
                }
                else -> return
            }
        }
    }

    class Factory(
        private val getList1: () -> List<PersonUiModel>,
        private val getList2: () -> List<PersonUiModel>,
        private val getList3: () -> List<PersonUiModel>
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ListScreenViewModel(getList1, getList2, getList3) as T
        }
    }

    private fun removeFromCategory(category: Category, index: Int) {
        when (category) {
            Category.FIRST -> {
                uiState_.update { old ->
                    val newList = old.list1.filterIndexed { inx, _ ->
                        inx != index
                    }
                    old.copy(list1 = newList)
                }
            }
            Category.SECOND -> {
                uiState_.update { old ->
                    val newList = old.list2.filterIndexed { inx, _ ->
                        inx != index
                    }
                    old.copy(list2 = newList)
                }
            }
            Category.THIRD -> {
                uiState_.update { old ->
                    val newList = old.list3.filterIndexed { inx, _ ->
                        inx != index
                    }
                    old.copy(list3 = newList)
                }
            }
        }
    }

    private fun addToCategory(category: Category, index: Int, item: PersonUiModel) {
        when (category) {
            Category.FIRST -> {
                uiState_.update { old ->
                    val newList = old.list1.toMutableList().apply {
                       add(index, item)
                    }
                    old.copy(list1 = newList)
                }
            }
            Category.SECOND -> {
                uiState_.update { old ->
                    val newList = old.list2.toMutableList().apply {
                        add(index, item)
                    }
                    old.copy(list2 = newList)
                }
            }
            Category.THIRD -> {
                uiState_.update { old ->
                    val newList = old.list3.toMutableList().apply {
                        add(index, item)
                    }
                    old.copy(list3 = newList)
                }
            }
        }
    }

    private fun findItem(category: Category, inx: Int): PersonUiModel {
        return when (category) {
            Category.FIRST -> uiState_.value.list1[inx]
            Category.SECOND -> uiState_.value.list2[inx]
            Category.THIRD -> uiState_.value.list3[inx]
        }
    }
}

private fun <T> List<T>.move(fromIdx: Int, toIdx: Int): List<T> {
    val list = this.toMutableList()

    if (toIdx > fromIdx) {
        for (i in fromIdx until toIdx) {
            list[i] = list[i + 1].also { list[i + 1] = list[i] }
        }
    } else {
        for (i in fromIdx downTo toIdx + 1) {
            list[i] = list[i - 1].also { list[i - 1] = list[i] }
        }
    }
    return list
}
