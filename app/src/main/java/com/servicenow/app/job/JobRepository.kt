package com.servicenow.app.job

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class JobRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val jobsCollection = firestore.collection("jobs")

    fun createElectricianJob(
        description: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User not logged in")
            return
        }

        val data = mapOf(
            "customerId" to currentUser.uid,
            "serviceType" to "electrician",
            "description" to description,
            "status" to "requested",
            "createdAt" to System.currentTimeMillis()
        )

        jobsCollection.add(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to create job")
            }
    }
}

