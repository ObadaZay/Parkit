package com.just.parkit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.just.parkit.SplashActivity.Companion.familyName
import com.just.parkit.SplashActivity.Companion.fatherName
import com.just.parkit.SplashActivity.Companion.firstName
import com.just.parkit.SplashActivity.Companion.password
import com.just.parkit.SplashActivity.Companion.prefs
import com.just.parkit.SplashActivity.Companion.prefsFileName
import kotlinx.android.synthetic.main.activity_signup.*

class SignupActivity : AppCompatActivity() {

    var check1: Int? = 2 //0 means missing data, 1 means successful, 2 means not changed value
    var check2: Int? = 2 //0 means missing data, 1 means successful, 2 means not changed value
    var check3: Int? = 2 //0 means missing data, 1 means successful, 2 means not changed value
    var check4: Int? = 2 //0 means missing data, 1 means successful, 2 means not changed value

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)


        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        //get sharedpref instance
        prefs = this.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)

        //when next button is clicked
        bu_signup_next.setOnClickListener {



            //call this function which checks for errors of user input
            checkUser()

            //handle the errors
            if (check1 == 0 || check2 == 0 || check3 == 0 || check4 == 0) {
                Toast.makeText(applicationContext, "Please double check that you filled all the fields correctly ^ ^", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
            else if (check1 == 1 && check2 == 1 && check3 == 1 && check4 == 1) {
                //call saveUser() Function to save the user unitl registration is complete
                saveUser()

                //take the user to phone signup auth activity
                val intent = Intent(baseContext, SignupAuthActivity::class.java)
                startActivity(intent)
            }
            else {
                Toast.makeText(applicationContext, "Register Error, please try again later :(", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }
        }


    }


    fun checkUser() {

        if (et_signup_password.text.isNullOrEmpty()) {
            et_signup_password.error = "You can't leave this field empty"
            et_signup_password.requestFocus()
            check1 = 0
        }
        else {
            if (et_signup_password.text.toString().length < 6){
                et_signup_password.error = "Your password has to be at least 6 characters"
                et_signup_password.requestFocus()
                check1 = 0
            }
            else{
                if (et_signup_password.text!!.contains(" ")){
                    et_signup_password.error = "Your password cannot contain any spaces"
                    et_signup_password.requestFocus()
                    check1 = 0
                }
                else {
                    //success
                    check1 = 1
                }
            }
        }

        if (et_family_name.text.isNullOrEmpty()) {
            et_family_name.error = "You can't leave this field empty"
            et_family_name.requestFocus()
            check2 = 0
        }
        else {
            if (et_family_name.text.toString().length == 1){
                et_family_name.error = "Your family name is too short, please use the real one"
                et_family_name.requestFocus()
                check2 = 0
            }
            else{
                if (et_family_name.text!!.contains(" ")){
                    et_family_name.error = "Your family name cannot contain any spaces"
                    et_family_name.requestFocus()
                    check2 = 0
                }
                else {
                    //success
                    check2 = 1
                }
            }
        }

        if (et_father_name.text.isNullOrEmpty()) {
            et_father_name.error = "You can't leave this field empty"
            et_father_name.requestFocus()
            check3 = 0
        }
        else {
            if (et_father_name.text.toString().length == 1){
                et_father_name.error = "Your father name is too short, please use the real one"
                et_father_name.requestFocus()
                check3 = 0
            }
            else{
                if (et_father_name.text!!.contains(" ")){
                    et_father_name.error = "Your father name cannot contain any spaces"
                    et_father_name.requestFocus()
                    check3 = 0
                }
                else {
                    //success
                    check3 = 1
                }
            }
        }

        if (et_first_name.text.isNullOrEmpty()) {
            et_first_name.error = "You can't leave this field empty"
            et_first_name.requestFocus()
            check4 = 0
        }
        else {
            if (et_first_name.text.toString().length == 1){
                et_first_name.error = "Your first name is too short, please use your real name"
                et_first_name.requestFocus()
                check4 = 0
            }
            else{
                if (et_first_name.text!!.contains(" ")){
                    et_first_name.error = "Your first name cannot contain any spaces"
                    et_first_name.requestFocus()
                    check4 = 0
                }
                else {
                    //success
                    check4 = 1
                }
            }
        }
    }


    //this function is used to save the user data in shared preferences after check is complete
    fun saveUser() {

        //save the data of these fields at these variables
        firstName = et_first_name.text.toString()
        fatherName = et_father_name.text.toString()
        familyName = et_family_name.text.toString()
        password = et_signup_password.text.toString()

        //put the data inside shared pref until the user successfully complete his registration
        prefs?.edit()?.putString("firstName", firstName)?.apply()
        prefs?.edit()?.putString("fatherName", fatherName)?.apply()
        prefs?.edit()?.putString("familyName", familyName)?.apply()
        prefs?.edit()?.putString("password", password)?.apply()

    }

}
