package com.just.parkit

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.just.parkit.SplashActivity.Companion.familyName
import com.just.parkit.SplashActivity.Companion.fatherName
import com.just.parkit.SplashActivity.Companion.firstName
import com.just.parkit.SplashActivity.Companion.password
import com.just.parkit.SplashActivity.Companion.phone
import com.just.parkit.SplashActivity.Companion.prefs
import com.just.parkit.SplashActivity.Companion.userState
import com.just.parkit.models.User
import kotlinx.android.synthetic.main.activity_signup_auth.buttonResend
import kotlinx.android.synthetic.main.activity_signup_auth.buttonStartVerification
import kotlinx.android.synthetic.main.activity_signup_auth.buttonVerifyPhone
import kotlinx.android.synthetic.main.activity_signup_auth.detail
import kotlinx.android.synthetic.main.activity_signup_auth.fieldPhoneNumber
import kotlinx.android.synthetic.main.activity_signup_auth.fieldVerificationCode
import java.util.concurrent.TimeUnit

class SignupAuthActivity : AppCompatActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    private lateinit var rootRef: DatabaseReference

    private var verificationInProgress = false
    private var storedVerificationId: String? = ""
    private lateinit var resendToken: PhoneAuthProvider.ForceResendingToken
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup_auth)

        // Restore instance state
        if (savedInstanceState != null) {
            onRestoreInstanceState(savedInstanceState)
        }

        // Assign click listeners
        buttonStartVerification.setOnClickListener(this)
        buttonVerifyPhone.setOnClickListener(this)
        buttonResend.setOnClickListener(this)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]

        //initialize the root reference
        rootRef = Firebase.database.reference



        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                // [START_EXCLUDE silent]
                // Update the UI and attempt sign in with the phone credential
                updateUI(STATE_VERIFY_SUCCESS, credential)
                // [END_EXCLUDE]
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)
                // [START_EXCLUDE silent]
                verificationInProgress = false
                // [END_EXCLUDE]

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    // [START_EXCLUDE]
                    fieldPhoneNumber.error = "Invalid phone number."
                    // [END_EXCLUDE]
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    // [START_EXCLUDE]
                    Snackbar.make(findViewById(android.R.id.content), "Quota exceeded.",
                        Snackbar.LENGTH_SHORT).show()
                    // [END_EXCLUDE]
                }

                // Show a message and update the UI
                // [START_EXCLUDE]
                updateUI(STATE_VERIFY_FAILED)
                // [END_EXCLUDE]
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token

                // [START_EXCLUDE]
                // Update UI
                updateUI(STATE_CODE_SENT)
                // [END_EXCLUDE]
            }
        }
        // [END phone_auth_callbacks]
    }

    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)

        // [START_EXCLUDE]
        if (verificationInProgress && validatePhoneNumber()) {
            startPhoneNumberVerification(fieldPhoneNumber.text.toString())
        }
        // [END_EXCLUDE]
    }
    // [END on_start_check_user]

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_VERIFY_IN_PROGRESS, verificationInProgress)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        verificationInProgress = savedInstanceState.getBoolean(KEY_VERIFY_IN_PROGRESS)
    }

    private fun startPhoneNumberVerification(phoneNumber: String) {
        // [START start_phone_auth]
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks) // OnVerificationStateChangedCallbacks
        // [END start_phone_auth]

        verificationInProgress = true
    }

    private fun verifyPhoneNumberWithCode(verificationId: String?, code: String) {
        // [START verify_with_code]
        val credential = PhoneAuthProvider.getCredential(verificationId!!, code)
        // [END verify_with_code]
        signInWithPhoneAuthCredential(credential)
    }

    // [START resend_verification]
    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            phoneNumber, // Phone number to verify
            60, // Timeout duration
            TimeUnit.SECONDS, // Unit of timeout
            this, // Activity (for callback binding)
            callbacks, // OnVerificationStateChangedCallbacks
            token) // ForceResendingToken from callbacks
    }
    // [END resend_verification]

    // [START sign_in_with_phone]
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    // [START_EXCLUDE]
                    updateUI(STATE_SIGNIN_SUCCESS, user)
                    // [END_EXCLUDE]
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        // [START_EXCLUDE silent]
                        fieldVerificationCode.error = "Invalid code."
                        fieldVerificationCode.requestFocus()
                        // [END_EXCLUDE]
                    }
                    // [START_EXCLUDE silent]
                    // Update UI
                    updateUI(STATE_SIGNIN_FAILED)
                    // [END_EXCLUDE]
                }
            }
    }
    // [END sign_in_with_phone]

    private fun signOut() {
        auth.signOut()
        updateUI(STATE_INITIALIZED)
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            updateUI(STATE_SIGNIN_SUCCESS, user)
        } else {
            updateUI(STATE_INITIALIZED)
        }
    }

    private fun updateUI(uiState: Int, cred: PhoneAuthCredential) {
        updateUI(uiState, null, cred)
    }

    private fun updateUI(
        uiState: Int,
        user: FirebaseUser? = auth.currentUser,
        cred: PhoneAuthCredential? = null
    ) {
        when (uiState) {
            STATE_INITIALIZED -> {
                // Initialized state, show only the phone number field and start button
                enableViews(buttonStartVerification, fieldPhoneNumber)
                disableViews(buttonVerifyPhone, buttonResend, fieldVerificationCode)
                detail.text = null
            }
            STATE_CODE_SENT -> {
                // Code sent state, show the verification field, the
                enableViews(buttonVerifyPhone, buttonResend, fieldPhoneNumber, fieldVerificationCode)
                disableViews(buttonStartVerification)
                detail.setText(R.string.status_code_sent)
            }
            STATE_VERIFY_FAILED -> {
                // Verification has failed, show all options
                enableViews(buttonStartVerification, buttonVerifyPhone, buttonResend, fieldPhoneNumber,
                    fieldVerificationCode)
                detail.setText(R.string.status_verification_failed)
            }
            STATE_VERIFY_SUCCESS -> {
                // Verification has succeeded, proceed to firebase sign in
                disableViews(buttonStartVerification, buttonVerifyPhone, buttonResend, fieldPhoneNumber,
                    fieldVerificationCode)
                detail.setText(R.string.status_verification_succeeded)

                // Set the verification text based on the credential
                if (cred != null) {
                    if (cred.smsCode != null) {
                        fieldVerificationCode.setText(cred.smsCode)
                    } else {
                        fieldVerificationCode.setText(R.string.instant_validation)
                    }
                }
            }
            STATE_SIGNIN_FAILED ->
                // No-op, handled by sign-in check
                detail.setText(R.string.status_sign_in_failed)
            STATE_SIGNIN_SUCCESS -> {
            }
        } // Np-op, handled by sign-in check

        if (user == null) {
            // Signed out

            //todo
        } else {
            // Signed in

            //shared preferences get and initializing
            prefs = this.getSharedPreferences(SplashActivity.prefsFileName, MODE_PRIVATE)
            firstName = prefs!!.getString("firstName", "").toString()
            fatherName = prefs!!.getString("fatherName", "").toString()
            familyName = prefs!!.getString("familyName", "").toString()
            password = prefs!!.getString("password", "").toString()
            phone = fieldPhoneNumber.text.toString().replaceFirst("^0|962".toRegex(), "+962")
            prefs?.edit()?.putString("phone", phone)?.apply()
            phone = prefs!!.getString("phone", "").toString()

            var passPhone = "$password,$phone"

            //add the user to firebase
            addUser(firstName,fatherName, familyName, phone, password, passPhone)



        }
    }

    private fun validatePhoneNumber(): Boolean {
        val phoneNumber = fieldPhoneNumber.text.toString()
        if (TextUtils.isEmpty(phoneNumber)) {
            fieldPhoneNumber.error = "Invalid phone number."
            fieldPhoneNumber.requestFocus()
            return false
        }

        return true
    }

    private fun enableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = true
        }
    }

    private fun disableViews(vararg views: View) {
        for (v in views) {
            v.isEnabled = false
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.buttonStartVerification -> {
                if (!validatePhoneNumber()) {
                    return
                }

                //using regex to replace first if equals to 0 or 962 with +962
                startPhoneNumberVerification(fieldPhoneNumber.text.toString().replaceFirst("^0|962".toRegex(), "+962"))

                buttonStartVerification?.setBackgroundColor(Color.parseColor("#66FFFFFF"))
                buttonStartVerification?.isEnabled = false
                buttonStartVerification?.isClickable = false

                //start timer for resend
                val timer = object: CountDownTimer(80000, 10) {
                    override fun onTick(millisUntilFinished: Long) {
                        buttonResend.text = "RESEND AFTER:" + millisUntilFinished / 1000
                        buttonResend?.setBackgroundColor(Color.parseColor("#66FFFFFF"))
                        buttonResend?.setTextColor(Color.parseColor("#FF006064"))
                        buttonResend?.isEnabled = false
                        buttonResend?.isClickable = false
                    }

                    override fun onFinish() {
                        buttonResend?.text = "RESEND"
                        buttonResend?.setBackgroundColor(Color.parseColor("#E6FFFFFF"))
                        buttonResend?.setTextColor(Color.parseColor("#26C6DA"))
                        buttonResend?.isEnabled = true
                        buttonResend?.isClickable = true
                    }
                }
                timer.start()
            }
            R.id.buttonVerifyPhone -> {
                val code = fieldVerificationCode.text.toString()
                if (TextUtils.isEmpty(code)) {
                    fieldVerificationCode.error = "Cannot be empty."
                    fieldVerificationCode.requestFocus()
                    return
                }

                verifyPhoneNumberWithCode(storedVerificationId, code)
            }
            R.id.buttonResend -> {
                resendVerificationCode(fieldPhoneNumber.text.toString().replaceFirst("^0|962".toRegex(), "+962"), resendToken)


                //start timer for resend
                val timer = object: CountDownTimer(80000, 10) {
                    override fun onTick(millisUntilFinished: Long) {
                        buttonResend?.isEnabled = false
                        buttonResend?.isClickable = false
                        buttonResend.text = "RESEND AFTER:" + millisUntilFinished / 1000
                        buttonResend?.setBackgroundColor(Color.parseColor("#66FFFFFF"))
                        buttonResend?.setTextColor(Color.parseColor("#FF006064"))

                    }

                    override fun onFinish() {
                        buttonResend?.text = "RESEND"
                        buttonResend?.isEnabled = true
                        buttonResend?.isClickable = true
                        buttonResend?.setBackgroundColor(Color.parseColor("#E6FFFFFF"))
                        buttonResend?.setTextColor(Color.parseColor("#26C6DA"))

                    }
                }
                timer.start()
            }
        }
    }

    //add the users to firebase function
    private fun addUser(firstName: String, fatherName: String, familyName: String, phoneNumber: String, password: String, passPhone: String) {

        // Create new user at /users/$userid
        //create the push key
        val key = rootRef.child("user_id").push().key
        //if error generating the key
        if (key == null) {
            Toast.makeText(applicationContext, "Error registering please try again later", Toast.LENGTH_LONG).show()
            Log.w(TAG, "Error registering please try again later")
            return
        }

        //initialize the values and convert them to hash map type
        val user = User(firstName, fatherName, familyName, phoneNumber, password,passPhone)
        val userValues = user.toMap()


        //define the updates variable
        val childUpdates = HashMap<String, Any>()
        childUpdates["/users/$key"] = userValues

        rootRef.updateChildren(childUpdates)
            .addOnSuccessListener {
                // Write was successful!
                userState = "1"
                Toast.makeText(applicationContext, "Account created successfully ^_^", Toast.LENGTH_LONG).show()

                auth.signOut()
                userState = "0"

                updateUI(STATE_INITIALIZED)
                //change state to signed out and take user to login activity
                val intent = Intent(baseContext, LoginActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                // Write failed
                userState = "0"

                Toast.makeText(applicationContext, "Error registering please try again later", Toast.LENGTH_LONG).show()
                Log.w(TAG, "Error registering please try again later")
            }
    }

    companion object {
        private const val TAG = "SignupAuthActivity"
        private const val KEY_VERIFY_IN_PROGRESS = "key_verify_in_progress"
        private const val STATE_INITIALIZED = 1
        private const val STATE_VERIFY_FAILED = 3
        private const val STATE_VERIFY_SUCCESS = 4
        private const val STATE_CODE_SENT = 2
        private const val STATE_SIGNIN_FAILED = 5
        private const val STATE_SIGNIN_SUCCESS = 6
    }
}
