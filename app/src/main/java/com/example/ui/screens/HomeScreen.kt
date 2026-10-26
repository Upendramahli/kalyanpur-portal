package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import coil.compose.AsyncImage
import com.example.MainActivity
import com.example.R
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
    onNavigateDonors: () -> Unit,
    onNavigateNotices: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("pwa_install_prefs", Context.MODE_PRIVATE) }
    var isInstalledOrDismissed by remember {
        mutableStateOf(prefs.getBoolean("is_added_to_home_screen", false))
    }

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

        // Add to Home Screen Banner (PWA / App Shortcut Option)
        if (!isInstalledOrDismissed) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Smartphone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isHindi) "📲 ऐप को होम स्क्रीन पर जोड़ें" else "📲 Add App to Home Screen",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (isHindi) "1-क्लिक में मोबाइल होम स्क्रीन पर ऐप आइकॉन बनाएं।" else "Create 1-click app icon on mobile home screen.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    try {
                                        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
                                            val shortcutIntent = Intent(context, MainActivity::class.java).apply {
                                                action = Intent.ACTION_MAIN
                                            }
                                            val pinShortcutInfo = ShortcutInfoCompat.Builder(context, "kalyanpur_app_shortcut")
                                                .setShortLabel(if (isHindi) "कल्याणपुर पंचायत" else "Kalyanpur App")
                                                .setLongLabel(if (isHindi) "ग्राम पंचायत कल्याणपुर" else "Gram Panchayat Kalyanpur")
                                                .setIcon(IconCompat.createWithResource(context, R.mipmap.ic_launcher))
                                                .setIntent(shortcutIntent)
                                                .build()
                                            ShortcutManagerCompat.requestPinShortcut(context, pinShortcutInfo, null)
                                            Toast.makeText(
                                                context,
                                                if (isHindi) "होम स्क्रीन पर जोड़ने का अनुरोध भेजा गया!" else "Requested pinning to home screen!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            Toast.makeText(
                                                context,
                                                if (isHindi) "ब्राउज़र मेनू (⋮) खोलें और 'Add to Home screen' चुनें।" else "Open browser menu (⋮) and tap 'Add to Home screen'.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(
                                            context,
                                            if (isHindi) "ब्राउज़र मेनू (⋮) खोलें और 'Add to Home screen' चुनें।" else "Open browser menu (⋮) and tap 'Add to Home screen'.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                    prefs.edit().putBoolean("is_added_to_home_screen", true).apply()
                                    isInstalledOrDismissed = true
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = if (isHindi) "होम स्क्रीन पर जोड़ें" else "Add to Home Screen",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Services Navigation Grid
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
                    title = if (isHindi) "दाता सूची" else "Donors Directory",
                    icon = Icons.Default.People,
                    color = Color(0xFF2E7D32),
                    onClick = onNavigateDonors
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
                TextButton(onClick = { onNavigateNotices?.invoke() }) {
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
