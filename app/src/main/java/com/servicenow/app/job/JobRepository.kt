package com.servicenow.app.job

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

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

    data class UserJob(
        val id: String,
        val description: String,
        val serviceType: String,
        val status: String,
        val workerId: String?
    )

    fun observeUserJobs(
        onJobsChanged: (List<UserJob>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User not logged in")
            return ListenerRegistration { }
        }

        return jobsCollection
            .whereEqualTo("customerId", currentUser.uid)
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onError(e.localizedMessage ?: "Failed to listen for jobs")
                    return@addSnapshotListener
                }
                if (snapshots == null) {
                    onJobsChanged(emptyList())
                    return@addSnapshotListener
                }
                val jobs = snapshots.documents.map { doc ->
                    UserJob(
                        id = doc.id,
                        description = doc.getString("description") ?: "",
                        serviceType = doc.getString("serviceType") ?: "",
                        status = doc.getString("status") ?: "",
                        workerId = doc.getString("workerId")
                    )
                }
                onJobsChanged(jobs)
            }
    }
}

