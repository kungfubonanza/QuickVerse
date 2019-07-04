package com.kungfubonanza.quickverse

/**
 * Uniquely identifies a location in the Bible.
 */
data class BibleRef(
    /** Book that contains the passage. */
    var book: String = "Genesis",
    /** Chapter that contains the passage. */
    var chapter: Int = 1,
    /** Verse that contains the passage. */
    var verse: Int = 1) {
    override fun toString(): String {
        return "$book+$chapter:$verse"
    }
}