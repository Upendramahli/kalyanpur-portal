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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.Expense
import com.example.data.Project

@Composable
fun ExpensesListScreen(
    project: Project,
    expenses: List<Expense>,
    isHindi: Boolean,
    onBack: () -> Unit
) {
    var selectedBillUrl by remember { mutableStateOf<String?>(null) }

    val titleText = if (isHindi) "💸 खर्च एवं बिल रसीद सूची (Expenses & Bills)" else "💸 Expenses & Bill Receipts"
    val backText = if (isHindi) "← वापस" else "← Back"
    val totalSpentText = if (isHindi) "कुल खर्च:" else "Total Spent:"
    val viewBillText = if (isHindi) "📄 बिल / रसीद देखें" else "📄 View Bill Receipt"

    // Dialog to view full bill receipt image
    if (selectedBillUrl != null) {
        AlertDialog(
            onDismissRequest = { selectedBillUrl = null },
            title = { Text(if (isHindi) "खरीद बिल / रसीद" else "Purchase Bill Receipt", fontWeight = FontWeight.Bold) },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(4f / 3f)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    AsyncImage(model = selectedBillUrl, contentDescription = null, modifier = Modifier.fillMaxSize())
                }
            },
            confirmButton = {
                Button(onClick = { selectedBillUrl = null }) {
                    Text(if (isHindi) "बंद करें" else "Close")
                }
            }
        )
    }

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

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFC62828).copy(alpha = 0.15f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(titleText, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFFC62828))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("$totalSpentText ₹${project.totalSpent}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                }
            }
        }

        if (expenses.isEmpty()) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                    Text(if (isHindi) "इस योजना पर अभी कोई खर्च नहीं है।" else "No expenses recorded yet.", color = Color.Gray)
                }
            }
        } else {
            items(expenses) { expense ->
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

                        if (expense.imageUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = { selectedBillUrl = expense.imageUrl },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(viewBillText, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
