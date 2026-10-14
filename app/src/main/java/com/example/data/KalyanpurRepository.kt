package com.example.data

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class KalyanpurRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(FirebaseFirestore.getInstance())

    private val projectsRef = db.collection("projects")
    private val donorsRef = db.collection("donors")
    private val expensesRef = db.collection("expenses")
    private val noticesRef = db.collection("notices")
    private val registeredDonorsRef = db.collection("registered_donors")

    fun observeProjects(): Flow<List<Project>> = projectsRef.snapshots()
        .map { snapshot -> snapshot.toObjects(Project::class.java) }

    fun observeDonors(): Flow<List<Donor>> = donorsRef.snapshots()
        .map { snapshot -> snapshot.toObjects(Donor::class.java) }

    fun observeExpenses(): Flow<List<Expense>> = expensesRef.snapshots()
        .map { snapshot -> snapshot.toObjects(Expense::class.java) }

    fun observeNotices(): Flow<List<Notice>> = noticesRef.snapshots()
        .map { snapshot -> snapshot.toObjects(Notice::class.java) }

    fun observeRegisteredDonors(): Flow<List<RegisteredDonor>> = registeredDonorsRef.snapshots()
        .map { snapshot -> snapshot.toObjects(RegisteredDonor::class.java) }

    suspend fun addRegisteredDonor(donor: RegisteredDonor, onComplete: () -> Unit = {}) {
        try {
            registeredDonorsRef.add(donor.copy(createdAt = null)).await()
        } catch (e: Exception) {
            Log.e("Repository", "Failed to add registered donor", e)
        } finally {
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    suspend fun deleteRegisteredDonor(donorId: String, onComplete: () -> Unit = {}) {
        try {
            registeredDonorsRef.document(donorId).delete().await()
        } catch (e: Exception) {
            Log.e("Repository", "Failed to delete registered donor", e)
        } finally {
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    suspend fun updateRegisteredDonor(donor: RegisteredDonor, onComplete: () -> Unit = {}) {
        try {
            if (donor.id.isNotBlank()) {
                registeredDonorsRef.document(donor.id).set(donor).await()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to update registered donor", e)
        } finally {
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    suspend fun addProject(project: Project, onComplete: () -> Unit = {}) {
        try {
            projectsRef.add(project.copy(createdAt = null)).await()
        } catch (e: Exception) {
            Log.e("Repository", "Failed to add yojana", e)
        } finally {
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    suspend fun updateProject(project: Project, onComplete: () -> Unit = {}) {
        try {
            if (project.id.isNotBlank()) {
                projectsRef.document(project.id).set(project).await()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to update yojana", e)
        } finally {
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    suspend fun deleteProject(projectId: String) {
        try {
            projectsRef.document(projectId).delete().await()
        } catch (e: Exception) {
            Log.e("Repository", "Failed to delete yojana", e)
        }
    }

    suspend fun addDonor(donor: Donor, onComplete: () -> Unit = {}) {
        try {
            donorsRef.add(donor.copy(createdAt = null)).await()
            if (donor.projectId.isNotBlank()) {
                val projDoc = projectsRef.document(donor.projectId)
                val snapshot = projDoc.get().await()
                val currentCollected = snapshot.getLong("totalCollected") ?: 0L
                projDoc.update("totalCollected", currentCollected + donor.amount).await()
            }
            withContext(Dispatchers.Main) { onComplete() }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to add donor", e)
        }
    }

    suspend fun deleteDonor(donorId: String, projectId: String, amount: Long) {
        try {
            donorsRef.document(donorId).delete().await()
            if (projectId.isNotBlank()) {
                val projDoc = projectsRef.document(projectId)
                val snapshot = projDoc.get().await()
                val currentCollected = snapshot.getLong("totalCollected") ?: 0L
                val newCollected = (currentCollected - amount).coerceAtLeast(0L)
                projDoc.update("totalCollected", newCollected).await()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to delete donor", e)
        }
    }

    suspend fun addExpense(expense: Expense, onComplete: () -> Unit = {}) {
        try {
            expensesRef.add(expense.copy(createdAt = null)).await()
            if (expense.projectId.isNotBlank()) {
                val projDoc = projectsRef.document(expense.projectId)
                val snapshot = projDoc.get().await()
                val currentSpent = snapshot.getLong("totalSpent") ?: 0L
                projDoc.update("totalSpent", currentSpent + expense.amount).await()
            }
            withContext(Dispatchers.Main) { onComplete() }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to add expense", e)
        }
    }

    suspend fun deleteExpense(expenseId: String, projectId: String, amount: Long) {
        try {
            expensesRef.document(expenseId).delete().await()
            if (projectId.isNotBlank()) {
                val projDoc = projectsRef.document(projectId)
                val snapshot = projDoc.get().await()
                val currentSpent = snapshot.getLong("totalSpent") ?: 0L
                val newSpent = (currentSpent - amount).coerceAtLeast(0L)
                projDoc.update("totalSpent", newSpent).await()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to delete expense", e)
        }
    }

    suspend fun addNotice(notice: Notice, onComplete: () -> Unit = {}) {
        try {
            noticesRef.add(notice.copy(createdAt = null)).await()
            withContext(Dispatchers.Main) { onComplete() }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to add notice", e)
        }
    }

    suspend fun deleteNotice(noticeId: String) {
        try {
            noticesRef.document(noticeId).delete().await()
        } catch (e: Exception) {
            Log.e("Repository", "Failed to delete notice", e)
        }
    }

    suspend fun seedInitialDataIfNeeded() {
        try {
            val projSnap = projectsRef.get().await()
            if (projSnap.isEmpty) {
                val p1 = Project(
                    title = "गाँव मुख्य सड़क पक्कीकरण योजना",
                    description = "कल्याणपुर मुख्य चौक से शिव मंदिर तक पीसीसी रोड निर्माण कार्य।",
                    category = "बुनियादी ढांचा (Infrastructure)",
                    status = "प्रगति पर (Ongoing)",
                    targetBudget = 250000L,
                    totalCollected = 180000L,
                    totalSpent = 120000L,
                    startDate = "10 Oct 2026",
                    createdBy = "पंचायत समिति"
                )
                val p2 = Project(
                    title = "सोलर स्ट्रीट लाइट स्थापना योजना",
                    description = "पूरेगाँव में सौर ऊर्जा आधारित स्ट्रीट लाइट लगाना ताकि रात में रोशनी रहे।",
                    category = "बिजली एवं ऊर्जा (Energy)",
                    status = "पूर्ण (Completed)",
                    targetBudget = 80000L,
                    totalCollected = 85000L,
                    totalSpent = 78000L,
                    startDate = "01 Sep 2026",
                    createdBy = "ग्राम विकास समिति"
                )
                val p3 = Project(
                    title = "स्वच्छता एवं वृक्षारोपण अभियान",
                    description = "गाँव के तालाब और स्कूल परिसर की सफाई तथा पौधे लगाना।",
                    category = "स्वच्छता अभियान (Cleanliness)",
                    status = "आगामी (Upcoming)",
                    targetBudget = 30000L,
                    totalCollected = 15000L,
                    totalSpent = 5000L,
                    startDate = "20 Oct 2026",
                    createdBy = "ग्राम युवा संघ"
                )
                val docRef1 = projectsRef.add(p1).await()
                val docRef2 = projectsRef.add(p2).await()
                val docRef3 = projectsRef.add(p3).await()

                donorsRef.add(Donor(projectId = docRef1.id, donorName = "श्री रामेश्वर महतो", amount = 25000L, date = "11 Oct 2026", paymentMode = "नकद (Cash)")).await()
                donorsRef.add(Donor(projectId = docRef1.id, donorName = "डॉ. वी. के. सिंह", amount = 50000L, date = "12 Oct 2026", paymentMode = "ऑनलाइन (Online)")).await()
                donorsRef.add(Donor(projectId = docRef2.id, donorName = "कल्याणपुर युवा क्लब", amount = 30000L, date = "05 Sep 2026", paymentMode = "नकद (Cash)")).await()
                donorsRef.add(Donor(projectId = docRef3.id, donorName = "मुखिया जी", amount = 15000L, date = "14 Oct 2026", paymentMode = "नकद (Cash)")).await()

                expensesRef.add(Expense(projectId = docRef1.id, title = "सीमेंट और बालू खरीद", amount = 80000L, date = "13 Oct 2026", billRef = "Bill #102")).await()
                expensesRef.add(Expense(projectId = docRef2.id, title = "10 सोलर लाइट सेट खरीद", amount = 78000L, date = "08 Sep 2026", billRef = "Bill #45")).await()
                expensesRef.add(Expense(projectId = docRef3.id, title = "पौधे एवं खाद खरीद", amount = 5000L, date = "14 Oct 2026", billRef = "Bill #12")).await()

                noticesRef.add(Notice(
                    title = "विशेष ग्राम सभा बैठक की सूचना",
                    content = "आगामी रविवार को पंचायत भवन कल्याणपुर में शीतकालीन विकास कार्यों व योजनाओं पर विशेष ग्राम सभा बैठक आयोजित की जाएगी। सभी ग्रामीण सादर आमंत्रित हैं।",
                    date = "13 Oct 2026",
                    priority = "महत्वपूर्ण (Important)"
                )).await()
            }
        } catch (e: Exception) {
            Log.e("Repository", "Failed to seed data", e)
        }
    }
}
