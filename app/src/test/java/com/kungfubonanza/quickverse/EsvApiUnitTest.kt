package com.kungfubonanza.quickverse

import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests the EsvApi code.
 */
class EsvApiUnitTest {

    /**
     * Verifies parsing of Genesis 1:1, which has no previous chapter or verse,
     * is correct.
     */
    @Test
    fun firstVerseParsing_isCorrect() {
        // this example was returned by https://api.esv.org/v3/passage/text/?q=Gen+1:1
        // on 04 Jul 2019
        val lastVerseJson: String = """
            {
                "query": "Gen 1:1", 
                "canonical": "Genesis 1:1", 
                "parsed": [[1001001, 1001001]], 
                "passage_meta": 
                [
                    {"canonical": "Genesis 1:1", 
                    "chapter_start": [1001001, 1001031], 
                    "chapter_end": [1001001, 1001031], 
                    "prev_verse": null, "next_verse": 1001002, 
                    "prev_chapter": null, 
                    "next_chapter": [1002001, 1002025]}
                ], 
                "passages": 
                [
                    "Genesis 1:1\n\nThe Creation of the World\n\n  [1] In the beginning, God created the heavens and the earth. (ESV)"
                ]
            }
        """

        EsvApi().parseJsonResponse(lastVerseJson).also {
            assertEquals("Gen 1:1", it?.query)
            assertEquals("Genesis 1:1", it?.canonical)
            assertNull(it!!.passage_meta[0].prev_verse)
            assertNull(it.passage_meta[0].prev_chapter)
        }
    }

    /**
     * Verifies parsing of Revelation 22:21, which has no next chapter or verse,
     * is correct.
     */
    @Test
    fun lastVerseParsing_isCorrect() {
        // this example was returned by https://api.esv.org/v3/passage/text/?q=Rev+22:21
        // on 04 Jul 2019
        val lastVerseJson: String = """
            {
                "query": "Rev 22:21",
                "canonical": "Revelation 22:21", 
                "parsed": [[66022021, 66022021]], 
                "passage_meta": 
                [
                    {"canonical": "Revelation 22:21", 
                    "chapter_start": [66022001, 66022021], 
                    "chapter_end": [66022001, 66022021], 
                    "prev_verse": 66022020, 
                    "next_verse": null, 
                    "prev_chapter": [66021001, 66021027], 
                    "next_chapter": null}
                ], 
                "passages": [
                    "Revelation 22:21\n\n  [21] The grace of the Lord Jesus be with all.(1) Amen.\n\nFootnotes\n\n(1) 22:21 Some manuscripts *all the saints*\n (ESV)"
                ]
            }
        """

        EsvApi().parseJsonResponse(lastVerseJson).also {
            assertEquals("Rev 22:21", it?.query)
            assertEquals("Revelation 22:21", it?.canonical)
            assertNull(it!!.passage_meta[0].next_verse)
            assertNull(it.passage_meta[0].next_chapter)
        }
    }

    /**
     * Verifies that the structure built from the JSON returned by the ESV API
     * is correct by ensuring we correctly parse [the example JSON](https://api.esv.org/docs/passage-text/)
     * provided in the ESV API documentation.
     */
    @Test
    fun jsonStructure_isCorrect() {
        // this example JSON is from https://api.esv.org/docs/passage-text/
        val exampleJson: String = """
                    {
                      "query": "John 11:35",
                      "canonical": "John 11:35",
                      "parsed": [
                        [
                          43011035,
                          43011035
                        ]
                      ],
                      "passage_meta": [
                        {
                          "canonical": "John 11:35",
                          "chapter_start": [
                            43011001,
                            43011057
                          ],
                          "chapter_end": [
                            43011001,
                            43011057
                          ],
                          "prev_verse": 43011034,
                          "next_verse": 43011036,
                          "prev_chapter": [
                            43010001,
                            43010042
                          ],
                          "next_chapter": [
                            43012001,
                            43012050
                          ]
                        }
                      ],
                      "passages": [
                        "John 11:35\n\n  [35] Jesus wept. (ESV)"
                      ]
                    }
            """

        EsvApi().parseJsonResponse(exampleJson).also {
            // for now we'll be fine spot-checking a few fields
            assertEquals("John 11:35", it?.query)
            assertEquals("John 11:35", it?.canonical)
            assertEquals(listOf(43011035, 43011035), it?.parsed?.get(0))
            assertEquals(listOf(43010001, 43010042), it?.passage_meta?.get(0)?.prev_chapter)
            assertEquals("John 11:35\n\n  [35] Jesus wept. (ESV)", it?.passages?.get(0))
        }
    }
}
