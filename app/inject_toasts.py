import re

path = "app/src/main/java/com/example/ui/screens/AdminScreen.kt"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

# 1. Inject Toast after successMsg assignments if not already present
def add_toast_after_success(match):
    full_line = match.group(0)
    msg_str = match.group(2)
    indent = re.match(r"^\s*", full_line).group(0)
    if "Toast.makeText" in content[match.start()-50:match.end()+50]:
        return full_line
    return f'{full_line}\n{indent}Toast.makeText(context, "{msg_str}", Toast.LENGTH_LONG).show()'

content = re.sub(r'(\s*successMsg\s*=\s*"([^"]+)")', add_toast_after_success, content)

# 2. Inject Toast before delete calls
delete_replacements = [
    (r'(viewModel\.deleteDonor\([^)]+\))', r'Toast.makeText(context, "🗑️ चंदा हटा दिया गया", Toast.LENGTH_SHORT).show(); \1'),
    (r'(viewModel\.deleteExpense\([^)]+\))', r'Toast.makeText(context, "🗑️ खर्च हटा दिया गया", Toast.LENGTH_SHORT).show(); \1'),
    (r'(viewModel\.deleteProject\([^)]+\))', r'Toast.makeText(context, "🗑️ योजना हटा दी गई", Toast.LENGTH_SHORT).show(); \1'),
    (r'(viewModel\.deleteRegisteredDonor\([^)]+\))', r'Toast.makeText(context, "🗑️ पंजीकृत दाता हटा दिया गया", Toast.LENGTH_SHORT).show(); \1'),
    (r'(viewModel\.deleteNotice\([^)]+\))', r'Toast.makeText(context, "🗑️ सूचना हटा दी गई", Toast.LENGTH_SHORT).show(); \1'),
]

for pattern, repl in delete_replacements:
    content = re.sub(pattern, repl, content)

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("Successfully injected Toasts into AdminScreen.kt")
