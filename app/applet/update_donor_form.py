path = "app/src/main/java/com/example/ui/screens/AdminScreen.kt"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

target = 'OutlinedTextField(value = donorName, onValueChange = { donorName = it }, label = { Text("दाता का नाम (Donor Name)") }, modifier = Modifier.fillMaxWidth())'

replacement = '''OutlinedTextField(
                                        value = donorName,
                                        onValueChange = { donorName = it },
                                        label = { Text("दाता का नाम (Donor Name)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            if (donorName.isNotBlank()) {
                                                IconButton(onClick = { donorName = ""; donorFatherName = ""; donorMobile = ""; donorAddress = "" }) {
                                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                                }
                                            }
                                        }
                                    )

                                    if (donorName.isNotBlank()) {
                                        val matchingRegistered = registeredDonors.filter {
                                            it.name.contains(donorName, ignoreCase = true) || it.mobile.contains(donorName)
                                        }
                                        if (matchingRegistered.isNotEmpty()) {
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(if (isHindi) "सुझाए गए पंजीकृत दाता (Click to select):" else "Suggested Registered Donors:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                    matchingRegistered.take(4).forEach { rd ->
                                                        OutlinedCard(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            onClick = {
                                                                donorName = rd.name
                                                                donorFatherName = rd.fatherName
                                                                donorMobile = rd.mobile
                                                                donorAddress = rd.address
                                                            }
                                                        ) {
                                                            Column(modifier = Modifier.padding(8.dp)) {
                                                                Text(rd.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                                Text("पिता: ${rd.fatherName} | मो: ${rd.mobile}", fontSize = 11.sp, color = Color.Gray)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }'''

if target in content:
    content = content.replace(target, replacement)
    with open(path, "w", encoding="utf-8") as f:
        f.write(content)
    print("Successfully updated donor form!")
else:
    print("Target not found!")
