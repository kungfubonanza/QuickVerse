package com.kungfubonanza.quickverse

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests the EsvApi code.
 */
class EsvApiUnitTest {
    /**
     * Verifies parsing of the JSON returned by the ESV API by checking if we
     * correctly parse [the example JSON](https://api.esv.org/docs/passage-text/)
     * provided in the ESV API documentation.
     */
    @Test
    fun jsonParsing_isCorrect() {
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
        val result = EsvApi().parseJsonResponse(exampleJson)

        // for now we'll be fine spot checking a few fields
        assertEquals("John 11:35", result?.query)
        assertEquals("John 11:35", result?.canonical)
        assertEquals(listOf(43011035, 43011035), result?.parsed?.get(0))
        assertEquals(listOf(43010001, 43010042), result?.passage_meta?.get(0)?.prev_chapter)
        assertEquals("John 11:35\n\n  [35] Jesus wept. (ESV)", result?.passages?.get(0))
    }
}
