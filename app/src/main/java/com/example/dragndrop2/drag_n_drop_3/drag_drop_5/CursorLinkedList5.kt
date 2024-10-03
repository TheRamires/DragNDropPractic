package com.example.dragndrop2.drag_n_drop_3.drag_drop_5

import java.util.LinkedList

class CursorLinkedList5 {
    private var cursor: CursorNode? = null
    private var count = 0

    fun getCursor() = cursor

    fun addCursor(cursorItem: DragDrop5) {
        if (cursor != null) throw IllegalStateException()
        cursor = CursorNode(cursorItem)
        count++
    }

    fun clear() {
        count = 0
        cursor = null
    }

    fun count() = count

    fun getByOriginalIndex(originalIndex: Int): DragDrop5? {
        val cursor = cursor ?: return null
        val first = findFirst(cursor)

        var cur: CursorNode? = first
        while (cur != null) {
            val draggableItem = cur.draggableItem
            if (draggableItem.originalIndex == originalIndex) {
                return draggableItem
            }
            cur = cur.below
        }
        return null
    }

    @Synchronized
    tailrec fun moveDown(new: DragDrop5): Boolean {
        val cursor = cursor ?: return false//throw IllegalStateException() //!!

        val above = cursor.above
        val below = cursor.below
        val newNode = CursorNode(new)

        return when (below) {
            null -> {
                newNode.above = cursor.above
                cursor.above = newNode
                count++
                false
            }
            else -> {
                cursor.below = below.below
                cursor.above = null
                count--

                if (below.index != newNode.index) {
                    moveDown(newNode.draggableItem)
                } else {
                    true
                }
            }
        }
    }

    @Synchronized
    tailrec fun moveUp(new: DragDrop5): Boolean {
        val cursor = cursor ?: return false//throw IllegalStateException("No added cursor")

        val above = cursor.above
        val below = cursor.below
        val newNode = CursorNode(new)

        return when (above) {
            null -> {
                newNode.below = cursor.below
                cursor.below = newNode
                count++
                false
            }
            else -> {
                cursor.above = above.above
                cursor.below = null
                count--

                if (above.index != newNode.index) {
                    moveUp(newNode.draggableItem)
                } else {
                    true
                }
            }
        }
    }

    fun getIndexResultList() = getResultList().map { it.originalIndex }

    fun getResultList(): List<DragDrop5> {
        val cursor = cursor ?: return emptyList()
        val result = LinkedList<DragDrop5>()

        val first = findFirst(cursor)
        result.addLast(first.draggableItem)

        var below = first.below
        while (below != null) {
            result.addLast(below.draggableItem)
            below = below.below
        }
        return result
    }

    private tailrec fun findFirst(cursorNode: CursorNode): CursorNode {
        val above = cursorNode.above
        return if (above == null) {
            cursorNode
        } else {
            findFirst(above)
        }
    }
}

class CursorNode(
    val draggableItem: DragDrop5,
) {
    var above: CursorNode? = null
        set(value) {
            field = value
            if (field?.below != this) {
                field?.below = this
            }
        }
    var below: CursorNode? = null
        set(value) {
            field = value
            if (field?.above != this) {
                field?.above = this
            }
        }

    val index get() = draggableItem.originalIndex
}