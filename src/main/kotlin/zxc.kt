package com.nezuko

import java.util.Objects

enum class Header(val level: Int) {
    FIRST(1),
    SECOND(2),
    THIRD(3),
    FOURTH(4),
    FIFTH(5),
    SIXTH(6);

    companion object {
        fun from(level: Int) : Header {
            return when(level) {
                1 -> FIRST
                2 -> SECOND
                3 -> THIRD
                4 -> FOURTH
                5 -> FIFTH
                6 -> SIXTH
                else -> throw IllegalArgumentException()
            }
        }
    }
}

sealed class MdBlock {
    data class MdText(
        val text: String,
        var italicIndexes: List<IntArray> = arrayListOf(),
        var boldIndexes: List<IntArray> = arrayListOf(),
        var crossedOutIndexes: List<IntArray> = arrayListOf(),
        var header: Header? = null
    ) : MdBlock() {
        override fun hashCode(): Int {
            return Objects.hash(text, italicIndexes.size, boldIndexes.size, crossedOutIndexes.size, header)
        }
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as MdText
            if (text != other.text) return false
            if (!listsOfArraysEqual(italicIndexes, other.italicIndexes)) return false
            if (!listsOfArraysEqual(boldIndexes, other.boldIndexes)) return false
            if (!listsOfArraysEqual(crossedOutIndexes, other.crossedOutIndexes)) return false

            return true
        }

        fun listsOfArraysEqual(a: List<IntArray>, b: List<IntArray>): Boolean {
            if (a.size != b.size) return false
            return a.zip(b).all { (x, y) -> x contentEquals y }
        }
    }
    class MdEmptyLine : MdBlock()

    data class MdImage(val ref: String) : MdBlock()
    data class MdTable(val content: Map<MdText, List<MdText>>): MdBlock()
}