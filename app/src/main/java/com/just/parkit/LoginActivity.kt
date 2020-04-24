package com.just.parkit


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
import com.google.firebase.ktx.Firebase
import com.just.parkit.SplashActivity.Companion.password
import com.just.parkit.SplashActivity.Companion.phone
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

        bu_signup.setOnClickListener {
            val intent = Intent(baseContext, SignupActivity::class.java)
            startActivity(intent)
        }
        bu_login.setOnClickListener{

            //add the users to firebase function
            login()
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
                    for (userSnapshot in dataSnapshot.children) {
                        val user: User? = userSnapshot.getValue(User::class.java)
                        userList.add(user)
                        // Get Data object and use the values to update the UI
                        // ...
                        Toast.makeText(applicationContext, "done!!!}", Toast.LENGTH_LONG).show()
                    }

                }
                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Data failed, log a message
                    Log.w(TAG, "LoginData:onCancelled", databaseError.toException())
                    // ...
                    Toast.makeText(applicationContext, "error", Toast.LENGTH_LONG).show()
                }
            })

    }

    companion object {
        private const val TAG = "LoginActivity"
    }

}
