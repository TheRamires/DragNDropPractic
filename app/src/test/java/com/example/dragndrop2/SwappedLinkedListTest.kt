package com.example.dragndrop2

import com.example.dragndrop2.drag_n_drop_3.DraggableItem
import com.example.dragndrop2.drag_n_drop_3.CursorLinkedList
import com.example.dragndrop2.drag_n_drop_3.CursorNode
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SwappedLinkedListTest {
    private lateinit var cursorList: CursorLinkedList
    private val map = HashMap<Int, DraggableItem>()

    private val item2 get() = map[2]!!
    private val cursorItem get() = map[3]!!
    private val item4 get() = map[4]!!
    private val item5 get() = map[5]!!

    @Before
    fun before() {
        cursorList = CursorLinkedList()

        val empty = DraggableItem.empty()
        for (i in 0..6) {
            map[i] = empty.copy(itemIndex = i)
        }
    }

    @Test
    fun `test add cursor`() {
        val cursorItem = map[3]!!
        Assert.assertEquals(0, cursorList.count())

        cursorList.addCursor(cursorItem)
        Assert.assertEquals(1, cursorList.count())

        Assert.assertThrows(IllegalStateException::class.java) {
            cursorList.addCursor(cursorItem)
        }
    }

    @Test
    fun `test reset`() {
        cursorList.addCursor(cursorItem)
        cursorList.moveDown(item4)
        cursorList.reset()
        Assert.assertEquals(0, cursorList.count())
    }

    @Test
    fun `test moving`() {
        Assert.assertEquals(0, cursorList.count())

        // null - 3 - null
        cursorList.addCursor(cursorItem)

        // null - 4 - 3 - null
        cursorList.moveDown(item4)
        Assert.assertEquals(2, cursorList.count())

        // null - 4 - 5 - 3 - null
        Assert.assertEquals(false, cursorList.moveDown(item5))
        Assert.assertEquals(3, cursorList.count())

        checkIndex(cursorList.getCursor(), 3)
        checkIndex(cursorList.getCursor()?.above, 5)

        // null - 4 - 3 - null
        Assert.assertEquals(true, cursorList.moveUp(item5))
        Assert.assertEquals(2, cursorList.count())

        // null - 3 - null
        Assert.assertEquals(true, cursorList.moveUp(item4))
        Assert.assertEquals(1, cursorList.count())

        // null - 3 - 2- null
        Assert.assertEquals(false, cursorList.moveUp(item2))
        Assert.assertEquals(2, cursorList.count())

        checkIndex(cursorList.getCursor(), 3)
        checkIndex(cursorList.getCursor()?.below, 2)
    }

    private fun checkIndex(cursor: CursorNode?, currentIndex: Int) {
        Assert.assertEquals(currentIndex, cursor?.index)
    }
}