package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Donor
import com.example.data.Expense
import com.example.data.Project

@Composable
fun YojanaDetailScreen(
    project: Project,
    donors: List<Donor>,
    expenses: List<Expense>,
    isHindi: Boolean,
    onBack: () -> Unit,
    onOpenDonors: () -> Unit,
    onOpenExpenses: () -> Unit
) {
    val totalCollected = donors.sumOf { it.amount }
    val totalSpent = expenses.sumOf { it.amount }
    val balance = totalCollected - totalSpent
    val isCompleted = project.status.contains("पूर्ण") || project.status.contains("Completed")
    val statusColor = if (isCompleted) Color(0xFF2E7D32) else Color(0xFFEF6C00)

    val backText = if (isHindi) "← वापस" else "← Back"
    val chandaCardTitle = if (isHindi) "💰 कुल जमा चंदा (Total Chanda)" else "💰 Total Chanda Collected"
    val expenseCardTitle = if (isHindi) "💸 कुल खर्च विवरण (Total Expenses)" else "💸 Total Expenses"
    val balanceCardTitle = if (isHindi) "📊 शेष राशि (Remaining Balance)" else "📊 Remaining Balance"
    val clickHint = if (isHindi) "विस्तृत सूची देखने के लिए क्लिक करें ➔" else "Click to view full list ➔"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Category Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(backText)
                }
                Badge(containerColor = statusColor.copy(alpha = 0.2f)) {
                    Text(project.status, modifier = Modifier.padding(6.dp), fontSize = 11.sp, color = statusColor)
                }
            }
        }

        // 4:3 Image Banner
        if (project.imageUrl.isNotBlank()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(model = project.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                }
            }
        }

        // Title and Description Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                        Text(project.category, modifier = Modifier.padding(6.dp), fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(project.title, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(project.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("${if (isHindi) "ारंभ तिथि" else "Start Date"}: ${project.startDate}", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }

        // Total Chanda Card (Clickable to open Donors Screen)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenDonors,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(chandaCardTitle, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32), fontSize = 15.sp)
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFF2E7D32))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("₹$totalCollected", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF2E7D32))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${donors.size} ${if (isHindi) "दाताओं का योगदान" else "donors contributed"} • $clickHint", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Total Expenses Card (Clickable to open Expenses Screen)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                onClick = onOpenExpenses,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828).copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(expenseCardTitle, fontWeight = FontWeight.Bold, color = Color(0xFFC62828), fontSize = 15.sp)
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = Color(0xFFC62828))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("₹$totalSpent", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFFC62828))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${expenses.size} ${if (isHindi) "खर्च प्रविष्टियां एवं बिल" else "expense entries & bills"} • $clickHint", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }

        // Remaining Balance Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0277BD).copy(alpha = 0.12f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(balanceCardTitle, fontWeight = FontWeight.Bold, color = Color(0xFF0277BD), fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("₹$balance", fontWeight = FontWeight.Bold, fontSize = 26.sp, color = Color(0xFF0277BD))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(if (isHindi) "लक्ष्य बजट: ₹${project.targetBudget}" else "Target Budget: ₹${project.targetBudget}", fontSize = 11.sp, color = Color.Gray)
                }
            }
        }
    }
}
