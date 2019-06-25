package com.kungfubonanza.quickverse

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.beust.klaxon.Klaxon
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.Headers
import com.github.kittinunf.fuel.core.extensions.cUrlString

data class EsvApiResponsePassageMeta(
    val canonical: String,
    val chapter_start: List<Int>,
    val chapter_end: List<Int>,
    val prev_verse: Int,
    val next_verse: Int,
    val prev_chapter: List<Int>,
    val next_chapter: List<Int>
)

data class EsvApiResponse(
    val query: String,
    val canonical: String,
    val parsed: List<List<Int>>,
    val passage_meta: List<EsvApiResponsePassageMeta>,
    val passages: List<String>
)

class MainActivity : AppCompatActivity() {
/*
    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
       // TODO("nnt implemented") //To change body of created functions use File | Settings | File Templates.

        var helloString : String = ""
        when(parent) {
            findViewById<Spinner>(R.id.ntSpinner) -> helloString = "Hello NT $id"
            findViewById<Spinner>(R.id.ntSpinner) -> helloString = "Hello nt $id"
        }

      //  if(view === findViewById<Spinner>(R.id.ntSpinner)) {
            val myToast = Toast.makeText(this, helloString, Toast.LENGTH_SHORT)
            myToast.show()
        //}


    }
*/

    data class BibleBook(val name: String, val chapters: Int, val versesPerChapter: IntArray)

    private fun getVerseText(verse: String) : String? {
        val testResult = Klaxon()
            .parse<EsvApiResponse>(
                """
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
            """)


        //Test Test Test

        assert("John 11:35" == testResult?.query)
        assert("John 11:35" == testResult?.canonical)
        assert(listOf(43011035,43011035) == testResult?.parsed?.get(0))
        assert(listOf(43010001,43010042) == testResult?.passage_meta?.get(0)?.prev_chapter)

       // println("DOODODODODODODODO ${result?.query} -- ${result?.passages?.get(0)}--------------")

        //return result?.passages?.get(0)

        val esvApiKey = "Token f5e237de333408ce3cf7481d75c1d9f4c80e6718"

        var passage: String? = null

        Fuel.get("https://api.esv.org/v3/passage/text/?", listOf("q" to "John+11:35"))
            .header(Headers.ACCEPT, "application/json")
            .header(Headers.AUTHORIZATION, esvApiKey)
            .response { request, response, result ->
                println("CURL S: ${request.cUrlString()}")
                //println(response)
                val (bytes, error) = result
                if (bytes != null) {
                    //println("[response bytes] ${String(bytes)}")
                    val responseBody = String(response.body().toByteArray())
                    println("RESPONSE BODY: $responseBody")
                    val apiResponse = Klaxon().parse<EsvApiResponse>(responseBody)
                    //println("TEH QUERY IS: ${apiResponse?.query}")

                    println("THE PASSAGE: ${apiResponse?.passages?.get(0)}")

                    passage = apiResponse?.passages?.get(0)

                }

            }.run { passage = "hello"}

            println(passage)

        return passage
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var verseText = getVerseText("John 11:35")

        // build an array of book names
        val ntBookNames: Array<String> = resources.getStringArray(R.array.ntBookNames)
        // build an array of book chapter counts
        val ntBookChapters: IntArray = resources.getIntArray(R.array.ntBookChapters)
        // build an array of strings in which each string contains a comma-separated list of verses in each chapter
        val ntBookVersesStrings = resources.getStringArray(R.array.ntVersesPerChapter)

        // build an array of integer arrays
        // * each integer array corresponds to a book
        // * each integer in each array represents the number of chapters in a book
        //val x = ntBookVersesStrings[0].split(",").map { it.toInt() }.toIntArray()
        val ntBookVersesPerChapter = Array<IntArray>(ntBookNames.count()) {
            // 1. split each string
            // 2. create a list of integers by converting each string to an integer
            // 3. convert each list to an IntArray, and then put the IntArray in the big array
                i -> ntBookVersesStrings[i].split(",").map { it.toInt() }.toIntArray()
        }

        val ntBooks = Array<MainActivity.BibleBook>(ntBookNames.count()) { i ->
            MainActivity.BibleBook(
                ntBookNames[i],
                ntBookChapters[i],
                ntBookVersesPerChapter[i]
            )
        }

        for(book in ntBooks) {
            assert(book.chapters == book.versesPerChapter.count())
        }


        // Get our IP
        //val s = (khttp.get("http://httpbin.org/ip").jsonObject.getString("origin"))
        // Get our IP in a simpler way
        //println(khttp.get("http://icanhazip.com").text)
      //  val myToast = Toast.makeText(this@MainActivity, "$s", Toast.LENGTH_SHORT)
      //  myToast.show()


        //val ntBookNames: Array<String> = resources.getStringArray(R.array.ntBookNames)
        //val ntBookChapters: IntArray = resources.getIntArray(R.array.ntBookChapters)
        //val ntBooks = Array<BibleBook>(ntBookNames.count()) {i -> BibleBook(ntBookNames[i], ntBookChapters[i])}

        var bookSpinner = findViewById<Spinner>(R.id.ntBookSpinner)

        ArrayAdapter.createFromResource(this, R.array.ntBookNames, R.layout.spinner_item
        ).also { adapter ->

            var ntBookName : String
            var ntBookChapter : Int = 0
            var ntBookVerse : Int = 0

            // specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // apply the adapter to the spinner
            bookSpinner.adapter = adapter
            // Create an object that listens to the change of a book
            bookSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                    //findViewById<TextView>(R.id.ntBookName).text = ntBooks[position].name
                    //findViewById<TextView>(R.id.ntBookChapter).text = ntBooks[position].chapters.toString()

                    ntBookName = ntBooks[position].name

                    // populate the chapter spinner with an item for each chapter in the book
                    var chapterSpinner = findViewById<Spinner>(R.id.ntChapterSpinner)
                    chapterSpinner.adapter = ArrayAdapter(this@MainActivity, R.layout.spinner_item, Array<Int>(ntBooks[position].chapters) { i -> i+1})

                    // create an object that listens to the change of a chapter
                    chapterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                            ntBookChapter = parent.getItemAtPosition(position).toString().toInt()

                            // populate the verse spinner with an item for each verse in the book
                            var verseSpinner = findViewById<Spinner>(R.id.ntVerseSpinner)
                            verseSpinner.adapter = ArrayAdapter(this@MainActivity, R.layout.spinner_item, Array<Int>(ntBooks[position].versesPerChapter[position]) { i -> i + 1 })

                            verseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                                    ntBookVerse = parent.getItemAtPosition(position).toString().toInt()

                                    val myToast = Toast.makeText(this@MainActivity, "$ntBookName $ntBookChapter:$ntBookVerse", Toast.LENGTH_SHORT)
                                    myToast.show()
                                }

                                override fun onNothingSelected(parent: AdapterView<*>) {
                                    // Code to perform some action when nothing is selected
                                }
                            }
                        }

                        override fun onNothingSelected(parent: AdapterView<*>) {
                            // Code to perform some action when nothing is selected
                        }
                    }

                        //val myToast = Toast.makeText(this@MainActivity, "${parent.getItemAtPosition(position)}", Toast.LENGTH_SHORT)
                        //myToast.show()
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Code to perform some action when nothing is selected
                }
            }
        }
    }
}

class ntSpinnerActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {


    override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
        // An item was selected. You can retrieve the selected item using
        parent.getItemAtPosition(pos).toString()
        val myToast = Toast.makeText(this, "ROCK THE nt", Toast.LENGTH_SHORT)
        myToast.show()
    }

    override fun onNothingSelected(parent: AdapterView<*>) {
        // Another interface callback
    }
}