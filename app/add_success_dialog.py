path = "app/src/main/java/com/example/ui/screens/AdminScreen.kt"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Add successDialogMsg state declaration
if "successDialogMsg" not in content:
    content = content.replace(
        'var successMsg by remember { mutableStateOf("") }',
        'var successMsg by remember { mutableStateOf("") }\n    var successDialogMsg by remember { mutableStateOf<String?>(null) }'
    )

# 2. Set successDialogMsg whenever successMsg is assigned
content = content.replace(
    'successMsg = ',
    'successDialogMsg = '
)

# 3. Add AlertDialog at the end of AdminScreen (before the last closing brace)
dialog_code = '''
    if (successDialogMsg != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { successDialogMsg = null },
            title = { androidx.compose.material3.Text(if (isHindi) "🎉 सफलता (Success)" else "🎉 Success") },
            text = { androidx.compose.material3.Text(successDialogMsg!!, fontSize = 16.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Medium) },
            confirmButton = {
                androidx.compose.material3.Button(onClick = { successDialogMsg = null }) {
                    androidx.compose.material3.Text(if (isHindi) "ठीक है (OK)" else "OK")
                }
            }
        )
    }
'''

# Insert before the last closing brace
last_brace_idx = content.rfind('}')
if last_brace_idx != -1 and "successDialogMsg != null" not in content[last_brace_idx-500:]:
    content = content[:last_brace_idx] + dialog_code + "\n}\n"

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("Successfully added success dialog and state")
