package com.kungfubonanza.quickverse

import android.app.AlertDialog
import android.content.DialogInterface
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.IdRes
import android.view.View
import android.widget.*
import android.text.method.ScrollingMovementMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * Data class that describes a book of the Bible.
 */
data class BibleBook(val name: String, val chapters: Int, val versesPerChapter: List<Int>)

/**
 * The main activity that is executed and shown on application startup.
 */
class MainActivity : AppCompatActivity() {
    /**
     * Returns an Array of BibleBooks described by the resource identified by [res].
     */
    private fun getBooks(res : Int) = Array<BibleBook>(resources.getStringArray(res).count()) {
        // build an array of strings that each describe a book
        // * each string is of the form "bookName:A,B,C", where A is the number of
        //   verses in the first chapter, B is the number of verses in the second
        //   chapter, etc.
        // * of course, the number of verses for each chapter is given
        val bookDescriptions = resources.getStringArray(res)

        // build an array of BibleBook objects -- each object is created based on the
        // information given in its bookDescription string
        val books = Array<BibleBook>(bookDescriptions.count()) { i ->
            // split the string into a book name and a list of chapters
            val (bookName, chapters) = bookDescriptions[i].split(":")
            // build an array of ints -- each int is the number of verses in a chapter
            val chapterCounts = chapters.split(",").map { it.toInt() }
            // create a BibleBook object from the name, number of chapters, and
            // verses in each chapter
            BibleBook(
                bookName,
                chapterCounts.count(),
                chapterCounts
            )
        }

        // ideally, this would be unit tested, but unit testing resource files
        // without mocking is tough, so we'll do this for now
        books.forEach { assert(it.chapters == it.versesPerChapter.count()) }

        return books
    }

    /**
     * Function override that is executed when the activity is created.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // grab the ESV API key--exit the app if it can't be retrieved
        cacheEsvApiKey()

        // make the text views scrollable
        findViewById<TextView>(R.id.ntVerseText).movementMethod = ScrollingMovementMethod()
        findViewById<TextView>(R.id.otVerseText).movementMethod = ScrollingMovementMethod()

        // set up the OT spinners
        activateViews(getBooks(R.array.otBooks), R.id.otBookSpinner, R.id.otChapterSpinner, R.id.otVerseSpinner, R.id.otVerseText)

        // set up the OT spinners
        activateViews(getBooks(R.array.ntBooks), R.id.ntBookSpinner, R.id.ntChapterSpinner, R.id.ntVerseSpinner, R.id.ntVerseText)
    }

    /**
     * Activates the spinners and the verse view for [books].
     * @param books Array that describes the books whose associated views are being activated
     * @param bookSpinnerRes Id of the spinner that allows selection of a book
     * @param chapterSpinnerRes Id of the spinner that allows selection of a chapter
     * @param verseSpinnerRes Id of the spinner that allows selection of a verse
     * @param verseTextRes Id of the view that displays the selected verse
     */
    private fun activateViews(books: Array<BibleBook>, @IdRes bookSpinnerRes: Int, @IdRes chapterSpinnerRes: Int, @IdRes verseSpinnerRes: Int, @IdRes verseTextRes: Int) {
        val bookSpinner = findViewById<Spinner>(bookSpinnerRes)

        ArrayAdapter(this, R.layout.spinner_item, Array<String>(books.count()) { i -> books[i].name }
        ).also { adapter ->
            // specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // apply the adapter to the spinner
            bookSpinner.adapter = adapter
            // Create an object that listens to the change of a book

            bookSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                    val bibleRef = BibleRef(books[position].name)

                    // populate the chapter spinner with an item for each chapter in the book
                    val chapterSpinner = findViewById<Spinner>(chapterSpinnerRes)
                    chapterSpinner.adapter = ArrayAdapter(
                        this@MainActivity,
                        R.layout.spinner_item,
                        Array<Int>(books[position].chapters) { i -> i + 1 })

                    // create an object that listens to the change of a chapter
                    chapterSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                            bibleRef.chapter = parent.getItemAtPosition(position).toString().toInt()

                            // populate the verse spinner with an item for each verse in the book
                            val verseSpinner = findViewById<Spinner>(verseSpinnerRes)
                            verseSpinner.adapter = ArrayAdapter(
                                this@MainActivity,
                                R.layout.spinner_item,
                                Array<Int>(books[position].versesPerChapter[position]) { i -> i + 1 })

                            verseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(
                                    parent: AdapterView<*>,
                                    view: View,
                                    position: Int,
                                    id: Long
                                ) {
                                    bibleRef.verse = parent.getItemAtPosition(position).toString().toInt()
                                    executeVerseLookup(bibleRef, verseTextRes)
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
                // cache the key provided by the resource file
                _esvApiKey = resources.getString(resources.getIdentifier(esvApiKeyResourceName, "string", this.packageName))
            }
        }
    }

    /**
     * Retrieves the verse identified by [ref].
     */
    private fun executeVerseLookup(ref: BibleRef, @IdRes verseTextRes: Int) {
        GlobalScope.launch(Dispatchers.Main) {
            findViewById<TextView>(verseTextRes).text = EsvApi(_esvApiKey).passage(ref).text
        }
    }
}

// TODO: Use this code instead of the nested stuff above?
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