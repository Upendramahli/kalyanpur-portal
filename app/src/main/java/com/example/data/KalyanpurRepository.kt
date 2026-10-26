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
        // No-op: Demo data removed. App relies strictly on real Firebase Firestore data.
    }
}
