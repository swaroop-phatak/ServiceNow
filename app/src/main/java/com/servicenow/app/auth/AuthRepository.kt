package com.servicenow.app.auth

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

class AuthRepository(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val usersCollection = firestore.collection("users")

    fun startPhoneNumberVerification(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: (verificationId: String) -> Unit,
        onVerificationCompleted: () -> Unit,
        onError: (String) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                signInWithPhoneAuthCredential(
                    credential = credential,
                    onSuccess = {
                        onVerificationCompleted()
                    },
                    onError = onError
                )
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e("AuthRepository", "onVerificationFailed", e)
                onError(e.localizedMessage ?: "Verification failed")
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                super.onCodeSent(verificationId, token)
                onCodeSent(verificationId)
            }
        }

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(
        verificationId: String,
        otp: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, otp)
        signInWithPhoneAuthCredential(
            credential = credential,
            onSuccess = onSuccess,
            onError = onError
        )
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user
                    if (user != null) {
                        createUserIfNotExists(user, onSuccess, onError)
                    } else {
                        onError("Authentication failed")
                    }
                } else {
                    Log.e("AuthRepository", "signInWithCredential failed", task.exception)
                    onError(task.exception?.localizedMessage ?: "Authentication failed")
                }
            }
    }

    private fun createUserIfNotExists(
        user: FirebaseUser,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val docRef = usersCollection.document(user.uid)
        docRef.get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.exists()) {
                    val data = mapOf(
                        "uid" to user.uid,
                        "phoneNumber" to user.phoneNumber,
                        "roles" to listOf("user"),
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                    docRef.set(data)
                        .addOnSuccessListener {
                            onSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.e("AuthRepository", "Failed to create user document", e)
                            onError(e.localizedMessage ?: "Failed to save user")
                        }
                } else {
                    onSuccess()
                }
            }
            .addOnFailureListener { e ->
                Log.e("AuthRepository", "Failed to fetch user document", e)
                onError(e.localizedMessage ?: "Failed to fetch user")
            }
    }
}

