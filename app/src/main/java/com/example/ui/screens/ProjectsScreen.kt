package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun ProjectsScreen(
    isHindi: Boolean,
    projects: List<Project>,
    donors: List<Donor>,
    expenses: List<Expense>,
    onYojanaClick: (Project) -> Unit
) {
    val titleText = if (isHindi) "📋 गाँव की सभी योजनाएं, प्रोजेक्ट एवं अभियान" else "📋 Village Yojnas, Projects & Abhiyan"
    val subtitleText = if (isHindi) "किसी भी योजना पर क्लिक करके उसका पूरा डैशबोर्ड और हिसाब देखें।" else "Click any Yojana to view its full dashboard & ledger."
    val emptyText = if (isHindi) "कोई योजना उपलब्ध नहीं है।" else "No Yojnas available."

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (projects.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(emptyText)
                }
            }
        } else {
            items(projects) { project ->
                ProjectCard(
                    project = project,
                    donors = donors,
                    expenses = expenses,
                    isHindi = isHindi,
                    onClick = { onYojanaClick(project) }
                )
            }
        }
    }
}

@Composable
fun ProjectCard(project: Project, donors: List<Donor>, expenses: List<Expense>, isHindi: Boolean, onClick: () -> Unit) {
    val collectedText = if (isHindi) "चंदा जमा (Collected)" else "Collected"
    val spentText = if (isHindi) "कुल खर्च (Spent)" else "Spent"
    val balanceText = if (isHindi) "शेष राशि (Balance)" else "Balance"
    val detailsBtnText = if (isHindi) "योजना का डैशबोर्ड देखें ➔" else "View Yojana Dashboard ➔"

    val totalCollected = donors.filter { it.projectId == project.id }.sumOf { it.amount }
    val totalSpent = expenses.filter { it.projectId == project.id }.sumOf { it.amount }
    val balance = totalCollected - totalSpent
    val isCompleted = project.status.contains("पूर्ण") || project.status.contains("Completed")
    val statusColor = if (isCompleted) Color(0xFF2E7D32) else Color(0xFFEF6C00)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (project.imageUrl.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(
                        model = project.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                    Text(text = project.category, modifier = Modifier.padding(6.dp), fontSize = 11.sp)
                }
                Badge(containerColor = statusColor.copy(alpha = 0.2f)) {
                    Text(
                        text = project.status,
                        modifier = Modifier.padding(6.dp),
                        fontSize = 11.sp,
                        color = statusColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = project.title, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = project.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = MaterialTheme.colorScheme.surfaceVariant)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(collectedText, fontSize = 10.sp, color = Color.Gray)
                    Text("₹$totalCollected", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF2E7D32))
                }
                Column {
                    Text(spentText, fontSize = 10.sp, color = Color.Gray)
                    Text("₹$totalSpent", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFC62828))
                }
                Column {
                    Text(balanceText, fontSize = 10.sp, color = Color.Gray)
                    Text("₹$balance", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0277BD))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onClick) {
                    Text(detailsBtnText, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
