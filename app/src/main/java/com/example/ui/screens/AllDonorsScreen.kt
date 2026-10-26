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
fun AllDonorsScreen(
    isHindi: Boolean,
    projects: List<Project>,
    donors: List<Donor>
) {
    var searchQuery by remember { mutableStateOf("") }

    val pageTitle = if (isHindi) "👥 चंदा दाताओं का संपूर्ण विवरण (All Donors Directory)" else "👥 All Donors Directory"
    val totalDonationText = if (isHindi) "कुल एकत्रित दान:" else "Total Donations:"
    val totalDonorsText = if (isHindi) "कुल दाता:" else "Total Donors:"
    val searchHint = if (isHindi) "दाता के नाम, पिता या मोबाइल से खोजें..." else "Search by donor name, father or mobile..."

    // Group all donors by name + mobile across all yojanas
    val groupedDonors = donors.groupBy { 
        Pair(it.donorName.trim(), it.mobile.trim()) 
    }.entries.toList()

    val filteredDonors = if (searchQuery.isBlank()) {
        groupedDonors
    } else {
        groupedDonors.filter { (key, entries) ->
            key.first.contains(searchQuery, ignoreCase = true) || 
            key.second.contains(searchQuery, ignoreCase = true) ||
            entries.any { it.fatherName.contains(searchQuery, ignoreCase = true) || it.address.contains(searchQuery, ignoreCase = true) }
        }
    }

    val grandTotalDonations = donors.sumOf { it.amount }
    val uniqueDonorsCount = groupedDonors.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Summary Card for All Donors
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF138808).copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(pageTitle, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF138808))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("$totalDonorsText $uniqueDonorsCount", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("$totalDonationText ₹$grandTotalDonations", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF138808))
                    }
                }
            }
        }

        // Search Bar
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
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.SearchOff, contentDescription = null, modifier = Modifier.size(36.dp), tint = Color.Gray)
                        Text(
                            text = if (searchQuery.isNotBlank()) 
                                (if (isHindi) "कोई दाता नहीं मिला।" else "No matching donors found.") 
                            else 
                                (if (isHindi) "पंचायत में अभी तक कोई चंदा दर्ज नहीं हुआ है।" else "No donations recorded yet."),
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        } else {
            items(filteredDonors) { (key, entries) ->
                val donorName = key.first
                val donorMobile = key.second
                val firstEntry = entries.firstOrNull()
                val fatherName = firstEntry?.fatherName ?: ""
                val sampleAddress = firstEntry?.address ?: ""
                val totalDonorAmount = entries.sumOf { it.amount }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(donorName, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = MaterialTheme.colorScheme.primary)
                                if (fatherName.isNotBlank() || donorMobile.isNotBlank() || sampleAddress.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = buildString {
                                            if (fatherName.isNotBlank()) append("पिता: $fatherName")
                                            if (fatherName.isNotBlank() && donorMobile.isNotBlank()) append(" • ")
                                            if (donorMobile.isNotBlank()) append("📱 $donorMobile")
                                            if ((fatherName.isNotBlank() || donorMobile.isNotBlank()) && sampleAddress.isNotBlank()) append(" • ")
                                            if (sampleAddress.isNotBlank()) append("📍 $sampleAddress")
                                        },
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Grand Total Amount Badge
                            Surface(
                                color = Color(0xFF2E7D32).copy(alpha = 0.12f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "कुल योग: ₹$totalDonorAmount",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (isHindi) "योजना-वार चंदा योगदान विवरण:" else "Yojana-wise Contribution Details:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        // List all Yojnas and Dates this donor contributed to
                        entries.sortedByDescending { it.date }.forEach { entry ->
                            val projTitle = projects.firstOrNull { it.id == entry.projectId || it.title == entry.projectId }?.title
                                ?: if (entry.projectId.isNotBlank()) entry.projectId else (if (isHindi) "ग्राम विकास योजना" else "Gram Vikas Yojana")

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("🏗️ $projTitle", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("📅 दिनांक: ${entry.date} (${entry.paymentMode})", fontSize = 11.sp, color = Color.Gray)
                                    }
                                    Text(
                                        text = "+₹${entry.amount}",
                                        fontWeight = FontWeight.Bold,
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
}
