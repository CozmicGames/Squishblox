package com.cozmicgames.common.utils.collections

import com.badlogic.gdx.utils.Disposable

class PriorityList<E : Comparable<E>> : MutableList<E>, Disposable {
    private val list = arrayListOf<E>()

    override val size by list::size

    override fun contains(element: E) = list.contains(element)

    override fun containsAll(elements: Collection<E>) = list.containsAll(elements)

    override fun get(index: Int) = list[index]

    override fun indexOf(element: E) = list.indexOf(element)

    override fun isEmpty() = list.isEmpty()

    override fun iterator() = list.iterator()

    override fun lastIndexOf(element: E) = list.lastIndexOf(element)

    override fun add(element: E): Boolean {
        var added = false

        for (i in indices) {
            if (this[i] > element) {
                list.add(i, element)
                added = true
                break
            }
        }

        if (!added)
            list.add(element)

        return true
    }

    override fun add(index: Int, element: E) = list.add(index, element)

    override fun addAll(index: Int, elements: Collection<E>) = list.addAll(index, elements)

    override fun addAll(elements: Collection<E>): Boolean {
        elements.forEach {
            add(it)
        }
        return elements.isNotEmpty()
    }

    override fun clear() = list.clear()

    override fun listIterator() = list.listIterator()

    override fun listIterator(index: Int) = list.listIterator(index)

    override fun remove(element: E) = list.remove(element)

    override fun removeAll(elements: Collection<E>) = list.removeAll(elements)

    override fun removeAt(index: Int) = list.removeAt(index)

    override fun retainAll(elements: Collection<E>) = list.retainAll(elements)

    override fun set(index: Int, element: E) = list.set(index, element)

    override fun subList(fromIndex: Int, toIndex: Int) = list.subList(fromIndex, toIndex)

    override fun dispose() {
        list.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}