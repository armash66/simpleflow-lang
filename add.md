
3️⃣ else if equivalent (without syntax hell)

Since you already chose when / otherwise:

when (x > 10) {
  show "big"
} otherwise when (x > 5) {
  show "medium"
} otherwise {
  show "small"
}


This is zero new syntax, just parser chaining.

⚙️ Tier 2 — Strong language ergonomics
4️⃣ for loop (desugared to while)

Classic C-style:

for (set i = 0; i < 5; i = i + 1) {
  show i
}


Internally:

Parser converts it to a while

Interpreter doesn’t even know it exists

Very compiler-ish, very cool.

5️⃣ Arrays / Lists

You need collections.

set a = [1, 2, 3]
show a[0]


Minimum viable:

literal [ ]

index access

length

🧪 Tier 3 — Language personality
6️⃣ null

Right now everything exists or crashes.

Add:

set x = null


Then:

null comparisons

safer APIs later

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