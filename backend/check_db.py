import sqlite3

conn = sqlite3.connect('backend/optisight.db')
c = conn.cursor()

c.execute("SELECT name FROM sqlite_master WHERE type='table'")
tables = [r[0] for r in c.fetchall()]

print("=== OptiSight Database Audit ===")
print("Database File: backend/optisight.db")
print("Tables Found:", tables)

for t in tables:
    count = c.execute(f"SELECT count(*) FROM {t}").fetchone()[0]
    print(f"- Table '{t}': {count} records")

print("\nRecent Analysis Reports:")
for row in c.execute("SELECT id, condition, confidence, health_score, analysis_date FROM analysis_reports ORDER BY id DESC LIMIT 5"):
    print("  Report ID:", row[0], "| Condition:", row[1], "| Conf:", row[2], "| Score:", row[3], "| Date:", row[4])

print("\nRegistered Users:")
for row in c.execute("SELECT id, full_name, email, age, gender FROM users LIMIT 5"):
    print("  User ID:", row[0], "| Name:", row[1], "| Email:", row[2], "| Age:", row[3], "| Gender:", row[4])

conn.close()
