package com.just.parkit


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.just.parkit.SplashActivity.Companion.familyName
import com.just.parkit.SplashActivity.Companion.fatherName
import com.just.parkit.SplashActivity.Companion.firstName
import com.just.parkit.SplashActivity.Companion.password
import com.just.parkit.SplashActivity.Companion.phone
import com.just.parkit.SplashActivity.Companion.prefs
import com.just.parkit.SplashActivity.Companion.prefsFileName
import com.just.parkit.models.User
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    private lateinit var rootRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // [START FirebaseDatabase initialize]
        //initialize the root reference
        rootRef = Firebase.database.reference
        // [END FirebaseDatabase initialize]


        //get sharedpref instance
        prefs = this.getSharedPreferences(prefsFileName, Context.MODE_PRIVATE)

        bu_signup.setOnClickListener {
            val intent = Intent(baseContext, SignupActivity::class.java)
            startActivity(intent)
        }

        bu_login.setOnClickListener{

            //check for empty fields
            if (et_login_phone.text.isNullOrEmpty() && et_login_password.text.isNullOrEmpty())
            {
                et_login_phone.error = "you can't leave this field empty"
                et_login_password.error = "you can't leave this field empty"
                et_login_phone.requestFocus()
            }
            else if (et_login_password.text.isNullOrEmpty())
            {
                et_login_password.error = "you can't leave this field empty"
                et_login_password.requestFocus()
            }
            else if (et_login_phone.text.isNullOrEmpty())
            {
                et_login_phone.error = "you can't leave this field empty"
                et_login_phone.requestFocus()
            }
            else {
                //add the users to firebase function
                login()
            }

        }
    }

    private fun login(){

        phone = et_login_phone.text.toString().replaceFirst("^0|962".toRegex(), "+962")
        password = et_login_password.text.toString()
        var passPhone = "$password,$phone"

        val userList: MutableList<User?> = ArrayList()
        rootRef.child("users").orderByChild("passPhone").equalTo(passPhone).addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    userList.clear()
                    // Get Data object and use the values to update the UI
                    // ...
                    val check = dataSnapshot.getValue<User>().toString()

                    if (check == "null"){
                        et_login_phone.error = "wrong phone or password!"
                        et_login_password.error = "wrong phone or password!"
                        Toast.makeText(applicationContext, "wrong phone or password!", Toast.LENGTH_LONG).show()
                    }
                    else {
                        for (userSnapshot in dataSnapshot.children) {
                            val user: User? = userSnapshot.getValue(User::class.java)
                            userList.add(user)

                            if (user?.phoneNumber.isNullOrEmpty())
                            {
                                //error
                                Toast.makeText(applicationContext, "please make sure you both phone and password are correct!", Toast.LENGTH_LONG).show()
                            }
                            else {
                                //do other login stuff here
                                //save the data of these fields at these variables
                                firstName = user?.firstName.toString()
                                fatherName = user?.fatherName.toString()
                                familyName = user?.familyName.toString()
                                phone = user?.phoneNumber.toString()
                                password = user?.password.toString()
                                passPhone = user?.passPhone.toString()

                                //put the data inside shared pref to use it later
                                prefs?.edit()?.putString("firstName", firstName)?.apply()
                                prefs?.edit()?.putString("fatherName", fatherName)?.apply()
                                prefs?.edit()?.putString("familyName", familyName)?.apply()
                                prefs?.edit()?.putString("password", password)?.apply()
                                prefs?.edit()?.putString("phone", phone)?.apply()
                                prefs?.edit()?.putString("passPhone", passPhone)?.apply()

                                Toast.makeText(applicationContext, "Hey ${user?.firstName}, Welcome Back!", Toast.LENGTH_LONG).show()

                                //take user to main activity
                                val intent = Intent(baseContext, MainActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }


                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Data failed, log a message
                    Log.w(TAG, "LoginData:onCancelled", databaseError.toException())
                    // ...
                    Toast.makeText(applicationContext, "error logging in, please try again later", Toast.LENGTH_LONG).show()
                }
            })

    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}
