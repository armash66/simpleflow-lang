# SimpleFlow

**SimpleFlow** is a minimal interpreted programming language built from scratch in **Java**.  
It includes a full interpreter pipeline — **lexer, parser, AST, and interpreter** — and uses
English-like keywords to keep syntax readable.

---

## Features

- Variables via `store`
- Arithmetic and comparisons
- Logical operators: `and`, `or`, `not`
- Conditionals: `when` / `otherwise` (chained `else if` style)
- Loops: `while` and `loop` (C-style `for` desugared to `while`)
- Loop control: `next` (continue), `leave` (break)
- Functions with parameters and `return`
- `print` (no newline) and `show` (newline) output
- `null` literal + null-safe `==`
- Single data structure **cell**:
  - literal `@(1, 2, 3)`
  - 1-based indexing `c[1]`
  - index assignment `c[2] = 99`
  - mixed keys `c["name"] = "mylang"`
  - `length(cell)`
- `++` / `--` postfix increment/decrement
- File-based execution

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

### While Loop
```sf
store x = 3
while (x > 0) {
  show x
  x--
}
```

### Loop (for-style)
```sf
loop (store i = 1; i <= 5; i++) {
  show i
}
```

### Loop Control
```sf
while (true) {
  next
  leave
}
```

### Functions
```sf
define add(a, b) {
  return a + b
}

show add(2, 3)
```

### Null
```sf
store x = null
show x == null
```

### Cell (single structure)
```sf
store c = @(1, 2, 3)
show c[1]
show length(c)

c[2] = 99
c["name"] = "mylang"
show c["name"]
```

---

## Example Program

```sf
store a = 10
store b = 3

print "a+b="
show a + b

when (a > 10) {
  show "big"
} otherwise when (a > 5 and b < 5) {
  show "medium"
} otherwise {
  show "small"
}

loop (store i = 1; i <= 3; i++) {
  show i
}

store c = @(1, 2, null, 4)
show c[2] == null

exit
```

---

## Running

From `simpleflow-lang`:

```bash
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object FullName)
java -cp out com.simpleflow.lang.Main test.sf
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

SimpleFlow reports syntax and runtime errors with line/column info where possible:

- Unexpected tokens / invalid syntax
- Undefined variables
- Wrong function arity
- Invalid operations (e.g., indexing non-cell)

---

## Notes

- `show` is for developer inspection (newline).
- `print` / `say` are for user output (no newline).
- Indexing is **1-based**: `c[1]` is the first element.

---

## License

Educational and experimental use only.
