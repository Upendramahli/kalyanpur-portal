package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Donor
import com.example.data.Expense
import com.example.data.Notice
import com.example.data.Project

@Composable
fun HomeScreen(
    isHindi: Boolean,
    projects: List<Project>,
    donors: List<Donor>,
    expenses: List<Expense>,
    notices: List<Notice>,
    onYojanaClick: (Project) -> Unit,
    onNavigateYojnas: () -> Unit,
    onNavigateNotices: () -> Unit
) {
    val panchayatTitle = if (isHindi) "ग्राम पंचायत कल्याणपुर" else "Gram Panchayat Kalyanpur"
    val subtitleText = if (isHindi) "जिला - बोकारो, झारखण्ड | आधिकारिक पारदर्शिता पोर्टल" else "District - Bokaro, Jharkhand | Official Transparency Portal"
    val activeYojnasHeading = if (isHindi) "🏗️ सभी योजनाएं एवं अभियान (डैशबोर्ड)" else "🏗️ All Yojnas & Abhiyan Dashboards"
    val subtitleYojnas = if (isHindi) "किसी भी योजना पर क्लिक करके उसका पूरा हिसाब देखें:" else "Click any Yojana to view its complete ledger & bills:"
    val noticesHeading = if (isHindi) "📢 नवीनतम सूचनाएं" else "📢 Latest Notices"
    val viewAllText = if (isHindi) "सभी देखें" else "View All"
    val noNoticesText = if (isHindi) "कोई सूचना उपलब्ध नहीं है।" else "No notices available."

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card (Government Portal Style)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF138808), Color(0xFF0055A5))
                        )
                    )
                    .padding(20.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountBalance,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = panchayatTitle,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitleText,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Quick Services Navigation Grid (Removed Chanda Ledger as requested)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickServiceCard(
                    modifier = Modifier.weight(1f),
                    title = if (isHindi) "सभी योजनाएं" else "All Yojnas",
                    icon = Icons.Default.Engineering,
                    color = Color(0xFF1565C0),
                    onClick = onNavigateYojnas
                )
                QuickServiceCard(
                    modifier = Modifier.weight(1f),
                    title = if (isHindi) "सूचना पट" else "Notices",
                    icon = Icons.Default.Notifications,
                    color = Color(0xFF6A1B9A),
                    onClick = onNavigateNotices
                )
            }
        }

        // Yojana-wise Accounting Section
        item {
            Column {
                Text(
                    text = activeYojnasHeading,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitleYojnas,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (projects.isEmpty()) {
            item {
                Text(
                    text = if (isHindi) "कोई योजना उपलब्ध नहीं है।" else "No Yojnas available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(projects) { project ->
                val totalCollected = donors.filter { it.projectId == project.id }.sumOf { it.amount }
                val totalSpent = expenses.filter { it.projectId == project.id }.sumOf { it.amount }
                val balance = totalCollected - totalSpent
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    onClick = { onYojanaClick(project) },
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
                            Badge(containerColor = if (project.status.contains("पूर्ण") || project.status.contains("Completed")) Color(0xFF2E7D32).copy(alpha = 0.2f) else Color(0xFFEF6C00).copy(alpha = 0.2f)) {
                                Text(
                                    text = project.status,
                                    modifier = Modifier.padding(6.dp),
                                    fontSize = 11.sp,
                                    color = if (project.status.contains("पूर्ण") || project.status.contains("Completed")) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = project.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = project.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(12.dp))

                        Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(if (isHindi) "चंदा जमा (Collected)" else "Collected", fontSize = 10.sp, color = Color.Gray)
                                Text("₹$totalCollected", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                            }
                            Column {
                                Text(if (isHindi) "कुल खर्च (Spent)" else "Spent", fontSize = 10.sp, color = Color.Gray)
                                Text("₹$totalSpent", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFC62828))
                            }
                            Column {
                                Text(if (isHindi) "शेष राशि (Balance)" else "Balance", fontSize = 10.sp, color = Color.Gray)
                                Text("₹$balance", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0277BD))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Text(
                                text = if (isHindi) "योजना का पूरा डैशबोर्ड देखें ➔" else "View Yojana Dashboard ➔",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Recent Notices Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = noticesHeading,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateNotices) {
                    Text(viewAllText)
                }
            }
        }

        if (notices.isEmpty()) {
            item {
                Text(
                    text = noNoticesText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(notices.take(2)) { notice ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = notice.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                                Text(notice.priority, modifier = Modifier.padding(4.dp), fontSize = 10.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = notice.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = notice.date, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun QuickServiceCard(
    modifier: Modifier = Modifier,
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(85.dp),
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = color)
        }
    }
}
