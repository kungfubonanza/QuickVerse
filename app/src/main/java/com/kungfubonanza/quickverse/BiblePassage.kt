package com.kungfubonanza.quickverse

/**
 * A passage in the Bible.
 */
data class BiblePassage(
    /** Reference that uniquely identifies the location. */
    var ref: BibleRef = BibleRef(),
    /** Text contained in the passage. */
    var text: String = "In the beginning, God created the heavens and the earth."
)
