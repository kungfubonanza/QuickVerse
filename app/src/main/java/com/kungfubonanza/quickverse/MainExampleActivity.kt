package com.kungfubonanza.quickverse

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

class MainExampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_example)
    }

    fun toastMe(view: View) {
        val myToast = Toast.makeText(this, "Hello Toast!", Toast.LENGTH_SHORT)
        myToast.show()
    }

    fun countMe(view: View) {
        val showCountTextView = findViewById<TextView>(R.id.textView)

        val countString = showCountTextView.text.toString()

        var count: Int = Integer.parseInt(countString)

        count++

        showCountTextView.setText(count.toString())
    }

    fun randomMe(view: View) {
        val randomIntent = Intent(this, MainActivity::class.java)

        /*val countString = textView.text.toString()

        var count = Integer.parseInt(countString)

        randomIntent.putExtra(SecondActivity.TOTAL_COUNT, count)*/

        startActivity(randomIntent)
    }

}
