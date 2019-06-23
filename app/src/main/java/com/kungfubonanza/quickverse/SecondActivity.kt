package com.kungfubonanza.quickverse

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlin.random.Random

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)
        showRandomNumber()
    }

    companion object {
        const val TOTAL_COUNT = "total_count"
    }

    fun showRandomNumber() {
        val count = intent.getIntExtra(TOTAL_COUNT, 0)

        var randomInt = 0

        if (count > 0) {
            randomInt = Random.nextInt(count + 1)
        }

        findViewById<TextView>(R.id.textView_random).text = Integer.toString(randomInt)

        findViewById<TextView>(R.id.textView_label).text = getString(R.string.random_heading, count)
    }
}
