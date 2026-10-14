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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Donor
import com.example.data.Project

@Composable
fun DonorsListScreen(
    project: Project,
    donors: List<Donor>,
    isHindi: Boolean,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val titleText = if (isHindi) "💰 चंदा दाताओं की सूची (Donors Ledger)" else "💰 Donors Ledger"
    val backText = if (isHindi) "← वापस" else "← Back"
    val totalCollectedText = if (isHindi) "कुल जमा चंदा:" else "Total Collected:"
    val totalDonorsText = if (isHindi) "कुल दाता (Total Donors):" else "Total Donors:"
    val searchHint = if (isHindi) "दाता के नाम या मोबाइल से खोजें..." else "Search by name or mobile..."

    // Group donors by Name + Mobile to distinguish same-name individuals correctly
    val groupedDonors = donors.groupBy { 
        Pair(it.donorName.trim(), it.mobile.trim()) 
    }.entries.toList()

    val filteredDonors = if (searchQuery.isBlank()) {
        groupedDonors
    } else {
        groupedDonors.filter { (key, _) ->
            key.first.contains(searchQuery, ignoreCase = true) || 
            key.second.contains(searchQuery, ignoreCase = true)
        }
    }

    val totalUniqueDonors = groupedDonors.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                Text(project.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f), maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.End)
            }
        }

        // Summary Card with Collection & Donors Count
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(titleText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF2E7D32))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$totalDonorsText $totalUniqueDonors", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                        Text("$totalCollectedText ₹${project.totalCollected}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }
        }

        // Search Bar for filtering donors easily
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text(searchHint) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (filteredDonors.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(if (isHindi) "कोई दाता नहीं मिला।" else "No matching donors found.", color = Color.Gray)
                }
            }
        } else {
            items(filteredDonors) { (key, entries) ->
                val donorName = key.first
                val donorMobile = key.second
                val sampleAddress = entries.firstOrNull()?.address ?: ""
                val totalPersonAmount = entries.sumOf { it.amount }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(donorName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                                if (donorMobile.isNotBlank() || sampleAddress.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = buildString {
                                            if (donorMobile.isNotBlank()) append("📱 $donorMobile")
                                            if (donorMobile.isNotBlank() && sampleAddress.isNotBlank()) append(" • ")
                                            if (sampleAddress.isNotBlank()) append("📍 $sampleAddress")
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "कुल: ₹$totalPersonAmount",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Excel style date-wise ledger rows for this person
                        entries.sortedBy { it.date }.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                                    Text("${entry.date} (${entry.paymentMode})", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(
                                    text = "+₹${entry.amount}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
