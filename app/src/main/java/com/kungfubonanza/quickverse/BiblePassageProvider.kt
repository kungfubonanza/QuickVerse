package com.kungfubonanza.quickverse

/**
 * Interface to a service that provides Bible passages.
 */
interface BiblePassageProvider {
    /** Returns the passage for [ref]. */
    suspend fun passage(ref: BibleRef) : BiblePassage {
        return BiblePassage(ref)
    }
}
