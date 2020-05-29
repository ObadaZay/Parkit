package com.just.parkit.ui.home

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.just.parkit.R
import com.just.parkit.SplashActivity.Companion.firstName
import com.just.parkit.SplashActivity.Companion.phone
import com.just.parkit.SplashActivity.Companion.prefs
import com.just.parkit.models.Park
import com.just.parkit.models.Spots
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.concurrent.TimeUnit
import com.google.firebase.database.ServerValue

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var rootRef: DatabaseReference

    var spots: String? = null
    var status: String? = null
    var startTime: String? = null
    var endTime: String? = null
    var cost: Long? = null



    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        firstName = prefs!!.getString("firstName", "").toString()
        tv_main_welcome.text = "Welcome Back $firstName ^^"
        tv_main_reserved.text = "Welcome Back $firstName ^^"

        //replace +962 with 0 to make it easier to enter in the Arudino panel
        phone = prefs!!.getString("phone", "").toString().replaceFirst("^[+]962".toRegex(), "0")
        prefs?.edit()?.putString("phone", phone)?.apply()
        phone = prefs!!.getString("phone", "").toString()

        //initialize the root reference
        rootRef = Firebase.database.reference

        //get spots count and show it
        checkSpots()

        getStatus()

        getStartTime()

        bu_find_parking.setOnClickListener {
            reserve(phone)
        }

        bu_cancel_reservation.setOnClickListener {
            cancelReservation()
        }

        bu_checkout.setOnClickListener {
            pushEndTime()

            //checkout()
        }
    }


    //get spots count
    private fun checkSpots(){
        rootRef.child("parking/spots/count").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Data object and use the values to update the UI
                    // ...
                    spots = dataSnapshot.value.toString()
                    //print spots count
                    tv_spots_count.text = resources.getString(R.string.spots_count) + spots
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Data failed, log a message
                    Log.w(TAG, "LoginData:onCancelled", databaseError.toException())
                    // ...
                    Toast.makeText(this@HomeFragment.context, "Error receiving the parking spots count, please check your internet and try again later", Toast.LENGTH_LONG).show()
                    tv_spots_count.text = resources.getString(R.string.spots_count) + "Unknown"
                }
            })
    }


    //add the users to firebase function
    private fun reserve(phoneNumber: String) {

        if (spots!! > "0"){

            //this is a var to know if the user paid yet or not
            var checkout = 0

            //this is a var stores the start time
            var startTime = 0

            //a variable to save the user state if he is in the parking or not
            var status = 0


            // Create new reservation at /parking/users/$phone

            //initialize the values and convert them to hash map type
            val park = Park(phoneNumber,checkout,startTime,status)
            val parkValues = park.toMap()

            //define the updates variable
            val childUpdates = HashMap<String, Any>()
            childUpdates["/parking/users/$phone"] = parkValues

            rootRef.updateChildren(childUpdates)
                .addOnSuccessListener {
                    rootRef.child("/parking/users/$phone/regTime").setValue(ServerValue.TIMESTAMP)
                    //decrease spots count
                    updateSpots((spots!!.toInt()-1))
                    //fun to hide reserve layout and show checkout
                    hideReserve()
                    // Write was successful!
                    Toast.makeText(this.context, "Reservation made successfully ^_^", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    // Write failed
                    Toast.makeText(this.context, "Error reserving your spot please try again later", Toast.LENGTH_LONG).show()
                    Log.w(TAG, "Error reserving your spot, please check your internet and try again later")
                }
        }

        else {
            Toast.makeText(this.context, "Sorry, there are no free parking spots right now, please check again later", Toast.LENGTH_LONG).show()
        }

    }

    //add the users to firebase function
    private fun updateSpots(count: Int) {
        // update spots at /parking/spots/count
        //initialize the values and convert them to hash map type

        val count = Spots(count)
        val spotsValues = count.toMap()

        //define the updates variable
        val childUpdates = HashMap<String, Any>()
        childUpdates["/parking/spots"] = spotsValues

        rootRef.updateChildren(childUpdates)

    }

    //hide reservation layout
    private fun hideReserve(){
        //hide reserve layout
        reserveLayout.visibility = View.GONE

        //show checkout layout
        checkoutLayout.visibility = View.VISIBLE
    }

    //hide reservation layout
    private fun showReserve(){
        //show reserve layout
        reserveLayout.visibility = View.VISIBLE

        //hide checkout layout
        checkoutLayout.visibility = View.GONE
    }

    //get user status
    private fun getStatus(){
        //get user status to check if he is in the parking spot or not
        rootRef.child("parking/users/$phone/status").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Data object and use the values to update the UI
                    // ...
                    status = dataSnapshot.value.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Data failed, log a message
                    Log.w(TAG, "LoginData:onCancelled", databaseError.toException())
                    // ...
                    Toast.makeText(this@HomeFragment.context, "Error receiving your status, please check your internet and try again later", Toast.LENGTH_LONG).show()
                }
            })
    }

    //cancel reservation
    private fun cancelReservation() {

        when (status) {
            "0" -> {
                rootRef.child("parking/users/$phone").removeValue()
                Toast.makeText(this@HomeFragment.context, "Your reservation is successfully canceled! ", Toast.LENGTH_LONG).show()
                showReserve()
                //increase spot count
                updateSpots((spots!!.toInt()+1))
            }
            "1" -> {
                Toast.makeText(this@HomeFragment.context, "Sorry You can't cancel now, you are already in the parking", Toast.LENGTH_LONG).show()
            }
            else -> {
                Toast.makeText(this@HomeFragment.context, "Error canceling!", Toast.LENGTH_LONG).show()
            }
        }

    }

    private fun getStartTime(){
        //get start time
        //get current time
        rootRef.child("parking/users/$phone/startTime").addValueEventListener(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Data object and use the values to update the UI
                    // ...
                    startTime = dataSnapshot.value.toString()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Data failed, log a message
                    Log.w(TAG, "LoginData:onCancelled", databaseError.toException())
                    // ...
                    Toast.makeText(this@HomeFragment.context, "Error receiving your the time you arrived, please check your internet and try again later", Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun pushEndTime(){
        rootRef.child("parking/users/$phone/endTime").setValue(ServerValue.TIMESTAMP).addOnSuccessListener {
            getEndTime()
        }
    }

    private fun getEndTime(){
        //get end time
        rootRef.child("parking/users/$phone/endTime").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Get Data object and use the values to update the UI
                    // ...
                    endTime = dataSnapshot.value.toString()
                    val asd = ""
                    checkout()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // Getting Data failed, log a message
                    Log.w(TAG, "LoginData:onCancelled", databaseError.toException())
                    // ...
                    Toast.makeText(this@HomeFragment.context, "Error! your the current time cannot be retrieved, please check your internet and try again later", Toast.LENGTH_LONG).show()
                }
            })
    }


    private fun checkout() {
        //get user status to check if he is in the parking spot or not
        when (status) {
            "0" -> {
                Toast.makeText(
                    this@HomeFragment.context,
                    "Sorry You can't checkout now, you are not in the parking yet",
                    Toast.LENGTH_LONG
                ).show()
            }
            "1" -> {
                if (!(startTime.isNullOrEmpty()) && startTime != "0"){
                    if (!(endTime.isNullOrEmpty()) && endTime != "0"){
                        //procceed to checkout
                        cost = TimeUnit.MILLISECONDS.toSeconds(endTime!!.toLong() - startTime!!.toLong())
                        Toast.makeText(this@HomeFragment.context, "cost: $cost JOD", Toast.LENGTH_LONG).show()

                        //todo change checkout to 1
                    }

                    else{
                        Toast.makeText(this@HomeFragment.context, "Error retrieving your current time please check your internet and try again later", Toast.LENGTH_LONG).show()
                    }
                }

                else {
                    Toast.makeText(this@HomeFragment.context, "Error retrieving your arrive time please check your internet and try again later", Toast.LENGTH_LONG).show()
                }
            }
            else -> {
                Toast.makeText(this@HomeFragment.context, "Error proceeding to checkout!", Toast.LENGTH_LONG).show()
            }
        }
    }


}
