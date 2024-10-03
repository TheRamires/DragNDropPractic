package com.example.dragndrop2.drag_n_drop_3

class CursorLinkedList {
    private var cursor: CursorNode? = null
    private var count = 0

    fun getCursor() = cursor

    fun addCursor(cursorItem: DraggableItem) {
        if (cursor != null) throw IllegalStateException()
        cursor = CursorNode(cursorItem)
        count++
    }

    fun reset() {
        count = 0
        cursor = null
    }

    fun count() = count

    @Synchronized
    fun moveDown(changed: DraggableItem): Boolean {
        val cursor = cursor ?: throw IllegalStateException()

        val above = cursor.above
        val below = cursor.below
        val newNode = CursorNode(changed)

        when {
            below != null -> {
                val alreadyContain = below.index == newNode.index
                return if (alreadyContain) {
                    cursor.below = below.below
                    cursor.above = null
                    count--
                    true
                } else {
                    throw IllegalStateException()
                }
            }

            above == null -> {
                cursor.above = newNode
                count++
                return false
            }

            else -> {
                newNode.above = above
                cursor.above = newNode
                count++
                return false
            }
        }
    }

    @Synchronized
    fun moveUp(changed: DraggableItem): Boolean {
        val cursor = cursor ?: throw IllegalStateException()

        val above = cursor.above
        val below = cursor.below
        val newNode = CursorNode(changed)

        when {
            above != null -> {
                val alreadyContain = above.index == newNode.index
                return if (alreadyContain) {
                    cursor.above = above.above
                    cursor.below = null
                    count--
                    true
                } else {
                    throw IllegalStateException()
                }
            }

            below == null -> {
                cursor.below = newNode
                count++
                return false
            }

            else -> {
                newNode.below = below
                cursor.below = newNode
                count++
                return false
            }
        }
    }
}

class CursorNode(
    val draggableItem: DraggableItem,
) {
    var above: CursorNode? = null
    var below: CursorNode? = null

    val index get() = draggableItem.itemIndex
}