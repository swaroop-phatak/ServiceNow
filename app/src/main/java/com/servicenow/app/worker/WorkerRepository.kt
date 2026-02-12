package com.servicenow.app.worker

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.random.Random

data class WorkerJob(
    val id: String,
    val description: String
)

class WorkerRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val jobsCollection = firestore.collection("jobs")

    fun observeRequestedJobs(
        onJobsChanged: (List<WorkerJob>) -> Unit,
        onError: (String) -> Unit
    ): ListenerRegistration {
        return jobsCollection
            .whereEqualTo("status", "requested")
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    onError(e.localizedMessage ?: "Failed to listen for jobs")
                    return@addSnapshotListener
                }
                if (snapshots == null) {
                    onJobsChanged(emptyList())
                    return@addSnapshotListener
                }
                val jobs = snapshots.documents.mapNotNull { doc ->
                    val description = doc.getString("description") ?: return@mapNotNull null
                    WorkerJob(
                        id = doc.id,
                        description = description
                    )
                }
                onJobsChanged(jobs)
            }
    }

    fun acceptJob(
        jobId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            onError("User not logged in")
            return
        }

        jobsCollection.document(jobId)
            .update(
                mapOf(
                    "status" to "accepted",
                    "workerId" to currentUser.uid
                )
            )
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to accept job")
            }
    }

    fun markInProgress(
        jobId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        jobsCollection.document(jobId)
            .update("status", "in_progress")
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to update job")
            }
    }

    fun markCompletedWithOtp(
        jobId: String,
        onOtpGenerated: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        val otp = Random.nextInt(1000, 9999).toString()
        jobsCollection.document(jobId)
            .update(
                mapOf(
                    "status" to "awaiting_confirmation",
                    "otp" to otp
                )
            )
            .addOnSuccessListener {
                onOtpGenerated(otp)
            }
            .addOnFailureListener { e ->
                onError(e.localizedMessage ?: "Failed to update job")
            }
    }
}

