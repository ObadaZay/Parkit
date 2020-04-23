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

        // define var for signup sucess
        //0 means fail or signed out, 1 means success
        //2 means he entered phone and waiting for verification message, 3 means he is logged in
        var signupsuccess: String? = null

        // define var for signup result serverside
        //0 means fail, 1 means success
        //2 means user phone exists
        var signupresult: String? = null


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

        //user db firstName
        var dbfirstName = "null"
        //user db fatherName
        var dbfatherName = "null"
        //user db familyName
        var dbfamilyName = "null"
        //user db password
        var dbpassword = "null"
        //user db phonenumber
        var dbphone = "null"
    }



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        //the font
        //val typeface = Typeface.createFromAsset(assets, "Arslan-Wessam-B.ttf")



        //sharedpreferences get
        prefs = this.getSharedPreferences(prefsFileName, MODE_PRIVATE)
        signupresult = prefs!!.getString("signupresult", "")
        signupsuccess = prefs!!.getString("signupsuccess", "")

    }

    override fun onResume() {
        super.onResume()

        //check if user signup is successful if its not take him through the signup form else take him to the main activity

        if (signupresult.isNullOrEmpty() || signupresult == "0" || signupresult == "2" || signupsuccess.isNullOrEmpty()) {

            //the delay on splash and goto first activity
            val background = object : Thread() {
                override fun run() {
                    try {
                        Thread.sleep(4000)

                        //todo it was first activity
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            background.start()

        }

        else if (signupsuccess == "2") {

            //the delay on splash and goto auth activity
            val background = object : Thread() {
                override fun run() {
                    try {
                        Thread.sleep(2000)

                        //todo turkish scholarship
                        //todo this was phoneauth activity
                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            background.start()

        }

        else {

            //the delay on splash and goto main activity
            val background = object : Thread() {
                override fun run() {
                    try {
                        Thread.sleep(2000)

                        val intent = Intent(baseContext, MainActivity::class.java)
                        startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            background.start()
        }
    }
}