package com.kungfubonanza.quickverse

import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult

class EsvApi(esvApiKey: String) {

    /**
     * Unique key that allows access to the ESV API. See https://api.esv.org/docs/ for how
     * on get a key.
     *
     */
    private val _esvApiKey: String = esvApiKey

    /**
     * Data class that models the fields in the "passage_meta" field contained in a JSON response
     * from the ESV API.
     *
     * Note that the variable names are identical to the field names in the JSON.
     */
    data class EsvApiResponsePassageMeta(
        val canonical: String,
        val chapter_start: List<Int>,
        val chapter_end: List<Int>,
        val prev_verse: Int,
        val next_verse: Int,
        val prev_chapter: List<Int>,
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
        val passage_meta: List<EsvApiResponsePassageMeta>,
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
    internal fun parseJsonResponse(response: String): Response? {
        return Klaxon().parse<Response>(response)
    }

    /**
     * Provides the full text of [verse] as returned by the ESV API.
     */
    suspend fun getVerseText(verse: String = "John 11:35"): String {
        // set up the FuelManager
        FuelManager.instance.baseHeaders = mapOf(
            Headers.ACCEPT to "application/json",
            Headers.AUTHORIZATION to _esvApiKey
        )

        // prepare the variable that we'll return
        var verseText = String()

        // make the request to the server
        val (_, _, result) = Fuel.get(
            _esvApiBasePath,
            listOf("q" to verse)
        ).awaitStringResponseResult()

        // parse the result
        result.fold(
            { data ->
                val apiResponse = parseJsonResponse(data)
                verseText = apiResponse?.passages?.get(0) ?: "Invalid JSON returned by ESV API."
                println("THE PASSAGE: $verseText")
            },
            { error ->
                verseText = "Unable to retrieve text of $verse.\n\nError: $error."
            }
        )

        // return the text or an error string
        return verseText
    }
}