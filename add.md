1️⃣ foreach (v in c)
2️⃣ delete c[key]
3️⃣ nested cells
4️⃣ truthy checks like if (c)
5️⃣ standard library (push, pop, keys)

7️⃣ print vs show

Right now show does everything.

Consider:

print x   // no newline
show x    // newline


Tiny change, huge UX win.

🧭 What I recommend RIGHT NOW

If we’re picking one thing for the next commit:

✅ Add break and continue

Why?

Minimal parser changes

Clean interpreter logic