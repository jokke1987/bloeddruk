package com.joeri.bloeddruk.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.joeri.bloeddruk.ui.toLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * FASE 5: Real-time Idempotente Firestore Synchronisatie tussen Smartphone en Tablet.
 *
 * Oplossingen voor dubbele metingen & herrijzende verwijderingen:
 * 1. Unieke, deterministische ID op basis van de timestamp (geen dubbele auto-ids).
 * 2. Tombstone-registratie (isDeleted = true) zodat verwijderde metingen op alle toestellen gewist blijven.
 * 3. Automatische opschoning van dubbele ochtend- en avondmetingen per dag.
 */
object FirestoreSyncManager {

    private const val TAG = "FirestoreSyncManager"
    private var snapshotListener: ListenerRegistration? = null
    private var activeUserId: String? = null

    /** Start de real-time listener voor de ingelogde gebruiker */
    fun startRealtimeSync(
        database: AppDatabase,
        scope: CoroutineScope
    ) {
        val auth = FirebaseAuth.getInstance()

        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                attachSnapshotListener(user.uid, database, scope)
            } else {
                detachSnapshotListener()
            }
        }

        auth.currentUser?.let { user ->
            attachSnapshotListener(user.uid, database, scope)
        }
    }

    private fun attachSnapshotListener(
        userId: String,
        database: AppDatabase,
        scope: CoroutineScope
    ) {
        if (activeUserId == userId && snapshotListener != null) {
            Log.d(TAG, "Listener voor gebruiker $userId is al actief.")
            return
        }

        detachSnapshotListener()
        activeUserId = userId

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
                    val validEntries = mutableListOf<BloodPressureEntry>()
                    val deletedTimestamps = mutableSetOf<Long>()

                    snapshot.documents.forEach { doc ->
                        try {
                            val isDeleted = doc.getBoolean("isDeleted") ?: false
                            val timestamp = doc.getLong("timestamp") ?: doc.getLong("id") ?: return@forEach

                            if (isDeleted) {
                                deletedTimestamps.add(timestamp)
                            } else {
                                val systolic = doc.getLong("systolic")?.toInt() ?: return@forEach
                                val diastolic = doc.getLong("diastolic")?.toInt() ?: return@forEach
                                val pulse = doc.getLong("pulse")?.toInt() ?: 70
                                val isMorning = doc.getBoolean("isMorning") ?: false
                                val note = doc.getString("note") ?: ""
                                val tags = doc.getString("tags") ?: ""

                                validEntries.add(
                                    BloodPressureEntry(
                                        id = timestamp, // Deterministische ID op basis van de timestamp
                                        timestamp = timestamp,
                                        isMorning = isMorning,
                                        systolic = systolic,
                                        diastolic = diastolic,
                                        pulse = pulse,
                                        note = note,
                                        tags = tags
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "Fout bij parsen document: ${doc.id}", e)
                        }
                    }

                    // 1. Verwijder alle tombstones (verwijderde metingen) uit de lokale Room DB
                    deletedTimestamps.forEach { ts ->
                        database.bloodPressureDao().deleteByTimestamp(ts)
                    }

                    // 2. Ontdubbel geldige metingen (max 1 ochtend- en 1 avondmeting per kalenderdag)
                    val deduplicatedEntries = deduplicateEntries(validEntries)

                    // 3. Vervang de lokale Room database met de schone, ontdubbelde lijst
                    database.bloodPressureDao().deleteAll()
                    deduplicatedEntries.forEach { entry ->
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
        activeUserId = null
    }

    /** Sla een nieuwe of bewerkte meting op in Firestore met deterministische ID */
    fun saveMeasurementToFirestore(entry: BloodPressureEntry) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val docId = entry.timestamp.toString()

        val map = hashMapOf(
            "id" to entry.timestamp,
            "timestamp" to entry.timestamp,
            "isMorning" to entry.isMorning,
            "systolic" to entry.systolic,
            "diastolic" to entry.diastolic,
            "pulse" to entry.pulse,
            "note" to entry.note,
            "tags" to entry.tags,
            "isDeleted" to false
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("measurements")
            .document(docId)
            .set(map)
            .addOnSuccessListener {
                Log.d(TAG, "Meting $docId succesvol geüpload naar Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Fout bij opslaan in Firestore", e)
            }
    }

    /** Markeert een meting als verwijderd (Tombstone) in Firestore */
    fun deleteMeasurementFromFirestore(entry: BloodPressureEntry) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val docId = entry.timestamp.toString()

        val tombstoneMap = hashMapOf(
            "id" to entry.timestamp,
            "timestamp" to entry.timestamp,
            "isDeleted" to true,
            "deletedAt" to System.currentTimeMillis()
        )

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("measurements")
            .document(docId)
            .set(tombstoneMap)
            .addOnSuccessListener {
                Log.d(TAG, "Tombstone voor meting $docId geplaatst in Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Fout bij plaatsen tombstone in Firestore", e)
            }
    }

    /**
     * Hulpmethode om dubbele metingen per dagdeel te filteren.
     * Behoudt maximaal 1 ochtend- en 1 avondmeting per kalenderdatum (de meest recente).
     */
    fun deduplicateEntries(entries: List<BloodPressureEntry>): List<BloodPressureEntry> {
        return entries
            .sortedByDescending { it.timestamp }
            .groupBy { e ->
                val dt = e.timestamp.toLocalDateTime().toLocalDate()
                dt to e.isMorning
            }
            .map { (_, groupEntries) ->
                // Kies de meest recente meting van dat specifieke dagdeel
                groupEntries.maxByOrNull { it.timestamp }!!
            }
            .sortedByDescending { it.timestamp }
    }

    /** Handmatige opschoonfunctie om bestaande dubbele metingen in Room op te schonen */
    suspend fun cleanupLocalDuplicates(database: AppDatabase) {
        val allEntries = database.bloodPressureDao().getAllEntries()
        val cleaned = deduplicateEntries(allEntries)
        database.bloodPressureDao().deleteAll()
        cleaned.forEach { database.bloodPressureDao().upsert(it) }
    }
}
