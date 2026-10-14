path = "app/src/main/java/com/example/ui/screens/AdminScreen.kt"
with open(path, "r", encoding="utf-8") as f:
    content = f.read()

content = content.replace(r'\"', '"')

with open(path, "w", encoding="utf-8") as f:
    f.write(content)

print("Fixed escaped quotes")
