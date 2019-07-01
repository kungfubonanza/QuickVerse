package com.kungfubonanza.quickverse

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import kotlinx.coroutines.*
import android.text.method.ScrollingMovementMethod

/**
 * Data class that represents a reference to a specific book-chapter-verse.
 */
data class BibleRef(var book: String, var chapter: Int, var verse: Int) {
    override fun toString(): String {
        return "$book+$chapter:$verse"
    }
}

/**
 * Data class that describes a book of the Bible.
 */
data class BibleBook(val name: String, val chapters: Int, val versesPerChapter: IntArray)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // grab the ESV API key--exit the app if it can't be retrieved
        cacheEsvApiKey()

        // make the text views scrollable
        findViewById<TextView>(R.id.ntVerseText).movementMethod = ScrollingMovementMethod()
        findViewById<TextView>(R.id.otVerseText).movementMethod = ScrollingMovementMethod()

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

        val ntBooks = Array<BibleBook>(ntBookNames.count()) { i ->
            BibleBook(
                ntBookNames[i],
                ntBookChapters[i],
                ntBookVersesPerChapter[i]
            )
        }

        for(book in ntBooks) {
            assert(book.chapters == book.versesPerChapter.count())
        }

        val bookSpinner = findViewById<Spinner>(R.id.ntBookSpinner)

        ArrayAdapter.createFromResource(this, R.array.ntBookNames, R.layout.spinner_item
        ).also { adapter ->

            val ntBcv = BibleRef("x", 0, 0)

            // specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // apply the adapter to the spinner
            bookSpinner.adapter = adapter
            // Create an object that listens to the change of a book
            bookSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                    //findViewById<TextView>(R.id.ntBookName).text = ntBooks[position].name
                    //findViewById<TextView>(R.id.ntBookChapter).text = ntBooks[position].chapters.toString()

                    ntBcv.book = ntBooks[position].name

                    // populate the chapter spinner with an item for each chapter in the book
                    val chapterSpinner = findViewById<Spinner>(R.id.ntChapterSpinner)
                    chapterSpinner.adapter = ArrayAdapter(this@MainActivity, R.layout.spinner_item, Array<Int>(ntBooks[position].chapters) { i -> i+1})

                    // create an object that listens to the change of a chapter
                    chapterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                            ntBcv.chapter = parent.getItemAtPosition(position).toString().toInt()

                            // populate the verse spinner with an item for each verse in the book
                            val verseSpinner = findViewById<Spinner>(R.id.ntVerseSpinner)
                            verseSpinner.adapter = ArrayAdapter(this@MainActivity, R.layout.spinner_item, Array<Int>(ntBooks[position].versesPerChapter[position]) { i -> i+1 })

                            verseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                                    ntBcv.verse = parent.getItemAtPosition(position).toString().toInt()
                                    executeVerseLookup(ntBcv)
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
                }

                override fun onNothingSelected(parent: AdapterView<*>) {
                    // Code to perform some action when nothing is selected
                }
            }
        }
    }

    /**
     * The API key used to retrieve data from the ESV API.
     */
    private var _esvApiKey: String = ""

    /**
     * Exits the application if the ESV API key cannot be retrieved.
     */
    private fun cacheEsvApiKey() {
        val esvApiKeyResourceName = "@string/esvApiKey"
        resources.getIdentifier(esvApiKeyResourceName, "string", this.packageName).also {
            // TODO: Handle this more gently by giving the user the option of providing
            //       his/her own API key.
            if(0 == it) {
                val builder = AlertDialog.Builder(this@MainActivity)
                // Set the dialog title
                builder.setTitle(R.string.esv_api_key_unavailable)
                    // Set the action buttons
                    .setPositiveButton(R.string.ok,
                        DialogInterface.OnClickListener { _, _ ->
                            // User clicked OK, so exit the application
                            finishAffinity()
                        })
                    .create()
                    .show()
            } else {
                // do that
                _esvApiKey = resources.getString(resources.getIdentifier(esvApiKeyResourceName, "string", this.packageName))
            }
        }
    }

    /**
     * Retrieves the verse identified by [bcv].
     */
    private fun executeVerseLookup(bcv: BibleRef) {
        GlobalScope.launch(Dispatchers.Main) {
            val verseText = EsvApi(_esvApiKey).getVerseText(bcv.toString()) ?: "failed"
            findViewById<TextView>(R.id.ntVerseText).text = verseText
        }
    }
}

// TODO: Fix this code and use it instead of the terrible nested stuff above.
/*
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
*/