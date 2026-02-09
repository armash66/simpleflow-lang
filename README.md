# SimpleFlow

**SimpleFlow** is a minimal interpreted language in **Java** with a built-in web studio for editing and running code. It uses readable, English-like keywords and a single core data structure: **cell**.

---

## Features

- Variables via `store` (alias: `set`)
- Arithmetic, comparisons, and logical operators (`and`, `or`, `not`, `!=`)
- Conditionals: `when` / `otherwise`
- Loops: `while` and `loop` (`for`-style)
- Loop control: `next` (continue), `leave` (break)
- Functions with parameters and `return`
- Expression statements (call a function as a standalone line)
- Ternary: `a ? b : c`
- Multi-line strings: `"""line1\nline2"""`
- `print` (no newline) and `show` (newline)
- `null`, `true`, `false` literals
- Single data structure **cell**:
  - literal `@(1, 2, 3)`
  - 1-based indexing `c[1]`
  - nested assignment `c[a][b] = x`
  - mixed keys `c["name"] = "simpleflow"`
  - `length(cell)`
- Cell helpers: `push`, `pop`, `shift`, `unshift`, `keys`, `values`, `has`, `slice`, `merge`
- Standard library: `input`, `random`, `clock`, `type`, `toNumber`, `toString`, `assert`
- `include "file.sf"` / `import "file.sf"`
- REPL mode

---

## Web Studio

From `web/backend/runner`:

```bash
./mvnw spring-boot:run
```

Open:

```
http://localhost:8080/
```

---

## CLI / REPL

Compile and run a file (from `simpleflow-lang`):

```bash
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp out com.simpleflow.lang.Main test.sf
```

Start REPL:

```bash
java -cp out com.simpleflow.lang.Main
```

---

## Language Syntax

### Variables
```sf
store x = 10
x = x + 1
```

### Output
```sf
print "Hello, "
show "world"
```

### Conditionals
```sf
when (x > 10) {
  show "big"
} otherwise when (x > 5) {
  show "medium"
} otherwise {
  show "small"
}
```

### Loops
```sf
store x = 3
while (x > 0) {
  show x
  x--
}

loop (store i = 1; i <= 5; i++) {
  show i
}
```

### Functions
```sf
define add(a, b) {
  return a + b
}

show add(2, 3)
```

### Cells
```sf
store c = @(1, 2, 3)
show c[1]

c[2] = 99
c["name"] = "simpleflow"
show c["name"]
```

### Cell helpers
```sf
push(c, 4)
show pop(c)
show keys(c)
show values(c)
show has(c, "name")
show slice(c, 1, 2)
```

---

## Project Structure

```
simpleflow-lang/
  Main.java
  src/com/simpleflow/lang/
    lexer/
    parser/
    ast/
    interpreter/
```

---

## Error Handling

SimpleFlow reports syntax and runtime errors with line/column info and a caret.

---

## License

Educational and experimental use only.
