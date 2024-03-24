package com.example.nasaphotoofthedayapi
import android.annotation.SuppressLint
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.TextHttpResponseHandler
import cz.msebera.android.httpclient.Header
import org.json.JSONException
import org.json.JSONObject

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var titleTextView: TextView
    private lateinit var dateTextView: TextView
    private lateinit var explanationTextView: TextView
    private lateinit var searchButton: Button
    private lateinit var dateEditText:EditText
    private lateinit var previousButton: Button

    private val baseUrl = "https://api.nasa.gov/planetary/apod"
    private val apiKey = "YAXP48XBgAUysWms7P7t7Nl436Fup9wDmgDlVSUC"

   // @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageView)
        titleTextView = findViewById(R.id.textViewTitle)
        dateTextView = findViewById(R.id.textViewDate)
        explanationTextView = findViewById(R.id.textViewExplanation)
        searchButton = findViewById(R.id.buttonFetch)
        dateEditText = findViewById(R.id.editTextDate)
        previousButton = findViewById(R.id.button)

        //loads a default picture for the current day
        loadPhotoOfTheDay(getTodayDate())


        searchButton.setOnClickListener {
            Log.d("MainActivity", "Search Button Clicked")
            val selectedDate = dateEditText.text.toString()
            if (isValidDate(selectedDate)) {
                loadPhotoForDate(selectedDate)
            } else {
                dateTextView.error = "Invalid date format! Use yyyy-MM-dd"
            }
        }

        previousButton.setOnClickListener {
            loadPreviousPhoto()
        }


    }

    //gets today's date
    private fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }


    //checks if the date entered by the user is in correct format
    private fun isValidDate(date: String): Boolean {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        dateFormat.isLenient = false
        return try {
            dateFormat.parse(date)
            true
        } catch (e: Exception) {
            false
        }
    }


    //this function loads the photo for the current date
    private fun loadPhotoOfTheDay(selectedDate: String) {
        val client = AsyncHttpClient()
        val url = "$baseUrl?api_key=$apiKey&date=$selectedDate"

        client.get(url, object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseString: String?) {
                responseString?.let {
                    try {
                        Log.d("MainActivity", "Response for date $selectedDate: $responseString")
                        val response = JSONObject(it)
                        displayPhoto(response)
                    } catch (e: JSONException) {
                        Log.e("MainActivity", "JSON Exception: ${e.localizedMessage}")
                    }
                }
            }

            override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Log.e("MainActivity", "Request failed with status code: $statusCode")
            }
        })
    }

    //loads the photo for a given date
    private fun loadPhotoForDate(selectedDate: String) {
        val client = AsyncHttpClient()
        val url = "$baseUrl?api_key=$apiKey&date=$selectedDate"

        client.get(url, object : TextHttpResponseHandler() {
            override fun onSuccess(statusCode: Int, headers: Array<out Header>?, responseString: String?) {
                responseString?.let {
                    try {
                        val response = JSONObject(it)
                        displayPhoto(response)
                    } catch (e: JSONException) {
                        Log.e("MainActivity", "JSON Exception: ${e.localizedMessage}")
                    }
                }
            }
                        override fun onFailure(statusCode: Int, headers: Array<out Header>?, responseString: String?, throwable: Throwable?) {
                Log.e("MainActivity", "Request failed with status code: $statusCode")
            }
        })
    }

    //this function is responsible for displaying the photo,the date it belongs to,the name of the pic and an explanation
    private fun displayPhoto(response: JSONObject) {
       try {
            val title = response.getString("title")
            val date = response.getString("date")
            val explanation = response.getString("explanation")
            val imageUrl = response.getString("url")

            titleTextView.text = title
            dateTextView.text = date
            explanationTextView.text = explanation

           //using Glide library to diplay the photo
            Glide.with(this@MainActivity)
                .load(imageUrl)
                .fitCenter()
                .into(imageView)
            Log.d("MainActivity", "Image URL: $imageUrl")
        } catch (e: JSONException) {
           Log.e("MainActivity", "JSON Exception: ${e.localizedMessage}")
        }
    }

    //the function loads the photo for the previous day
    private fun loadPreviousPhoto() {
        val currentDateString = dateTextView.text.toString()
        if (isValidDate(currentDateString)) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = sdf.parse(currentDateString)
            val previousDate = getPreviousDate(currentDate)

            val previousDateString = sdf.format(previousDate)
            dateEditText.setText(previousDateString)
            loadPhotoForDate(previousDateString)
        } else {
            dateEditText.error = "Invalid date format! Use yyyy-MM-dd"
        }
    }

    //the function calculates the date before current date
    private fun getPreviousDate(currentDate: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = currentDate
        calendar.add(Calendar.DATE, -1) // Subtract 1 day
        return calendar.time
    }
}


