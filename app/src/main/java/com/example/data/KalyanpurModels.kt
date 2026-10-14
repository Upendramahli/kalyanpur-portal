package com.example.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.Timestamp

data class Project(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val category: String = "विकास योजना (Development Yojana)",
    val status: String = "प्रगति पर (Ongoing)",
    val targetBudget: Long = 0L,
    val totalCollected: Long = 0L,
    val totalSpent: Long = 0L,
    val startDate: String = "",
    val createdBy: String = "",
    val imageUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Donor(
    @DocumentId val id: String = "",
    val projectId: String = "",
    val donorName: String = "",
    val fatherName: String = "",
    val mobile: String = "",
    val address: String = "",
    val amount: Long = 0L,
    val date: String = "",
    val paymentMode: String = "नकद (Cash)",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class RegisteredDonor(
    @DocumentId val id: String = "",
    val name: String = "",
    val fatherName: String = "",
    val mobile: String = "",
    val address: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Expense(
    @DocumentId val id: String = "",
    val projectId: String = "",
    val title: String = "",
    val amount: Long = 0L,
    val date: String = "",
    val billRef: String = "रसीद उपलब्ध (Available)",
    val imageUrl: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class Notice(
    @DocumentId val id: String = "",
    val title: String = "",
    val content: String = "",
    val date: String = "",
    val priority: String = "सामान्य (Normal)",
    @ServerTimestamp val createdAt: Timestamp? = null
)
