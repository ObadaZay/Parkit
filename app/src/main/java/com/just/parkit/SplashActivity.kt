package com.just.parkit

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import java.lang.Exception
import android.content.SharedPreferences


class SplashActivity : AppCompatActivity() {

    companion object {

        //variables for the shared preferences
        val prefsFileName = "com.just.parkit.prefs"
        var prefs: SharedPreferences? = null

        // define var for user State
        //0 means fail or signed out, 1 means success
        //2 means he entered phone and waiting for verification message, 3 means he is logged in
        var userState = "4"


        //user firstName
        var firstName = "null"
        //user fatherName
        var fatherName = "null"
        //user familyName
        var familyName = "null"
        //user password
        var password = "null"
        //user phonenumber
        var phone = "null"

    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        //the font
        //val typeface = Typeface.createFromAsset(assets, "Arslan-Wessam-B.ttf")



        //sharedpreferences get
        prefs = this.getSharedPreferences(prefsFileName, MODE_PRIVATE)
        userState = prefs!!.getString("userState", "").toString()

    }

    override fun onResume() {
        super.onResume()

        //check if user signup is successful if its not take him through the signup form else take him to the main activity

        //the delay on splash and goto first activity
        val background = object : Thread() {
            override fun run() {
                try {
                    Thread.sleep(4000)

                    val intent = Intent(baseContext, LoginActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        background.start()
    }
}