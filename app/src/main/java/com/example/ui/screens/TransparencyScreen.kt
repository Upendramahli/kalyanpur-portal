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
import com.example.data.Expense
import com.example.data.Project

@Composable
fun TransparencyScreen(
    isHindi: Boolean,
    projects: List<Project>,
    donors: List<Donor>,
    expenses: List<Expense>
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) } // 0 = Donors, 1 = Expenses

    val titleText = if (isHindi) "💰 चंदा लेज़र एवं वित्तीय पारदर्शिता" else "💰 Chanda Ledger & Transparency"
    val subtitleText = if (isHindi) "किस व्यक्ति ने कितना चंदा दिया और कहाँ पैसा खर्च हुआ - पूरी सूची।" else "Every rupee donated and spent - complete public ledger."
    val collectedTitle = if (isHindi) "कुल जमा चंदा" else "Total Collected"
    val spentTitle = if (isHindi) "कुल खर्च" else "Total Spent"
    val searchPlaceholder = if (isHindi) "नाम या रसीद से खोजें..." else "Search by name or reference..."
    val donorsTab = if (isHindi) "चंदा दाता (Donors)" else "Donors"
    val expensesTab = if (isHindi) "खर्च विवरण (Expenses)" else "Expenses"
    val noDonors = if (isHindi) "कोई दाता नहीं मिला।" else "No donors found."
    val noExpenses = if (isHindi) "कोई खर्च नहीं मिला।" else "No expenses found."

    val filteredDonors = donors.filter {
        it.donorName.contains(searchQuery, ignoreCase = true) || it.date.contains(searchQuery, ignoreCase = true)
    }

    val filteredExpenses = expenses.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.billRef.contains(searchQuery, ignoreCase = true)
    }

    val totalCollected = donors.sumOf { it.amount }
    val totalSpent = expenses.sumOf { it.amount }

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

        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32).copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(collectedTitle, fontSize = 12.sp, color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹$totalCollected", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828).copy(alpha = 0.12f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(spentTitle, fontSize = 12.sp, color = Color(0xFFC62828))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("₹$totalSpent", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFFC62828))
                    }
                }
            }
        }

        // Tab Selector & Search Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(donorsTab) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(expensesTab) }
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(searchPlaceholder) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        if (selectedTab == 0) {
            if (filteredDonors.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                        Text(noDonors)
                    }
                }
            } else {
                items(filteredDonors) { donor ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(donor.donorName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${donor.date} • ${donor.paymentMode}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(
                                text = "+₹${donor.amount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }
                }
            }
        } else {
            if (filteredExpenses.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(30.dp), contentAlignment = Alignment.Center) {
                        Text(noExpenses)
                    }
                }
            } else {
                items(filteredExpenses) { expense ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(expense.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${expense.date} • ${expense.billRef}", fontSize = 12.sp, color = Color.Gray)
                            }
                            Text(
                                text = "-₹${expense.amount}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }
    }
}
