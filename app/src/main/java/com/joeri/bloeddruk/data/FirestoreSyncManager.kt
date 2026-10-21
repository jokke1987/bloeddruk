package com.joeri.bloeddruk.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * FASE 5: Real-time Firestore Synchronisatie tussen Telefoon en Tablet.
 * Bewaart en luistert naar het strikte pad: /users/{userId}/measurements
 */
object FirestoreSyncManager {

    private const val TAG = "FirestoreSyncManager"
    private var snapshotListener: ListenerRegistration? = null

    /** Start de real-time listener voor de ingelogde gebruiker */
    fun startRealtimeSync(
        database: AppDatabase,
        scope: CoroutineScope
    ) {
        val auth = FirebaseAuth.getInstance()

        // Luister naar inlog- / uitlog-status
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                attachSnapshotListener(user.uid, database, scope)
            } else {
                detachSnapshotListener()
            }
        }

        // Als er nu al een gebruiker ingelogd is:
        auth.currentUser?.let { user ->
            attachSnapshotListener(user.uid, database, scope)
        }
    }

    private fun attachSnapshotListener(
        userId: String,
        database: AppDatabase,
        scope: CoroutineScope
    ) {
        detachSnapshotListener() // Annuleer eventuele oude listener

        val collectionRef = FirebaseFirestore.getInstance()
            .collection("users")
            .document(userId)
            .collection("measurements")

        Log.d(TAG, "Start real-time Firestore listener op: users/$userId/measurements")

        snapshotListener = collectionRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Fout bij ophalen Firestore snapshot", error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                scope.launch(Dispatchers.IO) {
                    val entriesFromFirestore = snapshot.documents.mapNotNull { doc ->
                        try {
                            val id = doc.getLong("id") ?: doc.id.hashCode().toLong()
                            val timestamp = doc.getLong("timestamp") ?: return@mapNotNull null
                            val systolic = doc.getLong("systolic")?.toInt() ?: return@mapNotNull null
                            val diastolic = doc.getLong("diastolic")?.toInt() ?: return@mapNotNull null
                            val pulse = doc.getLong("pulse")?.toInt() ?: 70
                            val isMorning = doc.getBoolean("isMorning") ?: false
                            val note = doc.getString("note") ?: ""
                            val tags = doc.getString("tags") ?: ""

                            BloodPressureEntry(
                                id = id,
                                timestamp = timestamp,
                                isMorning = isMorning,
                                systolic = systolic,
                                diastolic = diastolic,
                                pulse = pulse,
                                note = note,
                                tags = tags
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Fout bij parsen document: ${doc.id}", e)
                            null
                        }
                    }

                    // Sla op in de lokale Room database (wat automatisch de UI bijwerkt via StateFlow)
                    entriesFromFirestore.forEach { entry ->
                        database.bloodPressureDao().upsert(entry)
                    }
                }
            }
        }
    }

    /** Stop de real-time listener bij uitloggen */
    fun detachSnapshotListener() {
        snapshotListener?.remove()
        snapshotListener = null
    }

    /** Sla een nieuwe of bewerkte meting op in Firestore op het pad: users/{userId}/measurements/{id} */
    fun saveMeasurementToFirestore(entry: BloodPressureEntry) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val docId = if (entry.id != 0L) entry.id.toString() else entry.timestamp.toString()

        val map = hashMapOf(
            "id" to entry.id,
            "timestamp" to entry.timestamp,
            "isMorning" to entry.isMorning,
            "systolic" to entry.systolic,
            "diastolic" to entry.diastolic,
            "pulse" to entry.pulse,
            "note" to entry.note,
            "tags" to entry.tags
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("measurements")
            .document(docId)
            .set(map)
            .addOnSuccessListener {
                Log.d(TAG, "Meting $docId succesvol opgeslagen in Firestore op pad /users/${user.uid}/measurements")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Fout bij opslaan in Firestore", e)
            }
    }

    /** Verwijder een meting uit Firestore */
    fun deleteMeasurementFromFirestore(entry: BloodPressureEntry) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val docId = if (entry.id != 0L) entry.id.toString() else entry.timestamp.toString()

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("measurements")
            .document(docId)
            .delete()
            .addOnSuccessListener {
                Log.d(TAG, "Meting $docId verwijderd uit Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Fout bij verwijderen uit Firestore", e)
            }
    }
}
