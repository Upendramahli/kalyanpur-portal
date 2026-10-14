path = "app/src/main/java/com/example/ui/screens/AdminScreen.kt"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

old_exp = """                                         IconButton(onClick = { /* fixed */ }) {
                                             Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                         }"""

new_exp = """                                         IconButton(onClick = {
                                             viewModel.deleteExpense(e.id, e.projectId, e.amount)
                                             successDialogMsg = "🗑️ खर्च सफलतापूर्वक हटा दिया गया!"
                                             Toast.makeText(context, "🗑️ खर्च सफलतापूर्वक हटा दिया गया!", Toast.LENGTH_SHORT).show()
                                         }) {
                                             Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                         }"""

old_proj = """                                                 IconButton(onClick = { /* fixed */ }) {
                                                     Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                 }"""

new_proj = """                                                 IconButton(onClick = {
                                                     viewModel.deleteProject(p.id)
                                                     successDialogMsg = "🗑️ योजना सफलतापूर्वक हटा दी गई!"
                                                     Toast.makeText(context, "🗑️ योजना सफलतापूर्वक हटा दी गई!", Toast.LENGTH_SHORT).show()
                                                 }) {
                                                     Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                 }"""

old_reg = """                                                 IconButton(onClick = { /* fixed */ }) {
                                                     Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                 }"""

new_reg = """                                                 IconButton(onClick = {
                                                     viewModel.deleteRegisteredDonor(rd.id)
                                                     successDialogMsg = "🗑️ पंजीकृत दाता हटा दिया गया!"
                                                     Toast.makeText(context, "🗑️ पंजीकृत दाता हटा दिया गया!", Toast.LENGTH_SHORT).show()
                                                 }) {
                                                     Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                                 }"""

old_not = """                                             IconButton(onClick = { /* fixed */ }) {
                                                 Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                             }"""

new_not = """                                             IconButton(onClick = {
                                                 viewModel.deleteNotice(n.id)
                                                 successDialogMsg = "🗑️ सूचना सफलतापूर्वक हटा दी गई!"
                                                 Toast.makeText(context, "🗑️ सूचना सफलतापूर्वक हटा दी गई!", Toast.LENGTH_SHORT).show()
                                             }) {
                                                 Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                             }"""

content = content.replace(old_exp, new_exp)
content = content.replace(old_proj, new_proj)
content = content.replace(old_reg, new_reg)
content = content.replace(old_not, new_not)

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("Successfully updated delete buttons!")
