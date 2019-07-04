package com.kungfubonanza.quickverse

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult

/**
 * Class that provides Bible passages retrieved from the ESV API at
 * https://api.esv.org.
 */
class EsvApi(esvApiKey: String = "TOKEN <insert key here>"): BiblePassageProvider {
    private val _esvApiKey: String = esvApiKey

    /**
     * Parameters that customize the content returned by the ESV API.
     */
    private val _parameters: MutableMap<String, Boolean> = mutableMapOf(
        "include-passage-references" to false,
        "include-verse-numbers" to false,
        "include-first-verse-numbers" to false,
        "include-footnotes" to false,
        "include-footnote-body" to false,
        "include-headings" to false
        )

    /**
     * Include passage references in the passage text.
     */
    var includePassageReferences: Boolean
        get() = _parameters.getOrElse("include-passage-references", {false})
        set(value) = _parameters.set("include-passage-references", value)

    /**
     * Include verse numbers in the passage text.
     */
    var includeVerseNumbers: Boolean
        get() = _parameters.getOrElse("include-verse-numbers", {false})
        set(value) = _parameters.set("include-verse-numbers", value)

    /**
     * Include first verse numbers in the passage text.
     */
    var includeFirstVerseNumbers: Boolean
        get() = _parameters.getOrElse("include-first-verse-numbers", {false})
        set(value) = _parameters.set("include-first-verse-numbers", value)

    /**
     * Include footnotes in the passage text.
     */
    var includeFootnotes: Boolean
        get() = _parameters.getOrElse("include-footnotes", {false})
        set(value) = _parameters.set("include-footnotes", value)

    /**
     * Include footnote body in the passage text.
     */
    var includeFootnoteBody: Boolean
        get() = _parameters.getOrElse("include-footnote-body", {false})
        set(value) = _parameters.set("include-footnote-body", value)

    /**
     * Include section headings in the passage text.
     */
    var includeHeadings: Boolean
        get() = _parameters.getOrElse("include-headings", {false})
        set(value) = _parameters.set("include-headings", value)

    init {
        // set up the FuelManager
        FuelManager.instance.baseHeaders = mapOf(
            Headers.ACCEPT to "application/json",
            Headers.AUTHORIZATION to _esvApiKey
        )
    }

    /**
     * Data class that models the fields in the "passage_meta" field contained in a JSON response
     * from the ESV API.
     *
     * Note that the variable names are identical to the field names in the JSON.
     */
    data class ResponsePassageMeta(
        val canonical: String,
        val chapter_start: List<Int>,
        val chapter_end: List<Int>,
        val prev_verse: Int?, // is allowed to be null because prev_verse for Genesis 1:1 is null
        val next_verse: Int,
        val prev_chapter: List<Int>?, // is allowed to be null because prev_chapter for Genesis 1:1 is null
        val next_chapter: List<Int>
    )

    /**
     * Data class that represents the JSON response from the ESV API.
     *
     * Note that the variable names are identical to the field names in the JSON.
     */
    data class Response(
        val query: String,
        val canonical: String,
        val parsed: List<List<Int>>,
        val passage_meta: List<ResponsePassageMeta>,
        val passages: List<String>
    )

    /**
     * Base path (or URL) of the ESV API.
     */
    private val _esvApiBasePath: String = "https://api.esv.org/v3/passage/text/?"

    /**
     * Converts the JSON [response] from the ESV API into an EsvApi.Response data object or null
     * if parsing failed.
     */
    fun parseJsonResponse(response: String): Response? {
        return Klaxon().parse<Response>(response)
    }

    /**
     *  Returns the passage for [ref]. The text for the passage
     *  is provided by the ESV API.
     */
    override suspend fun passage(ref: BibleRef): BiblePassage {
        return getVerseText(ref)
    }

    /**
     * Returns the full text (from the ESV API) of [ref].
     */
    private suspend fun getVerseText(ref: BibleRef = BibleRef()): BiblePassage {
        var verseText = String()

        // make the request to the server
        val (_, _, result) = Fuel.get(
            _esvApiBasePath,
            listOf("q" to ref.toString().replace("\\s+", "")) + _parameters.toList()
        ).awaitStringResponseResult()

        // parse the result
        result.fold(
            { data ->
                val apiResponse = parseJsonResponse(data)
                verseText = apiResponse?.passages?.get(0)?.trim() ?: "Invalid JSON returned by ESV API."
                //println("THE PASSAGE: $verseText")
            },
            { error ->
                verseText = "Unable to retrieve text of $ref.toString().\n\nError: $error."
            }
        )

        // return the text or an error string
        return BiblePassage(ref, verseText)
    }
}