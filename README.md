# SimpleFlow

A minimal interpreted language in **Java** with a built-in web studio for editing and running code. SimpleFlow focuses on readable, English-like keywords and a single core data structure: **cell**.

---

## Table of Contents

- Overview
- Features
- Quick Start
- Web Studio
- CLI & REPL
- Language Guide
- Standard Library
- File Includes
- Examples
- Project Structure
- Error Handling
- License

---

## Overview

SimpleFlow is a small, readable language designed for learning, rapid experiments, and building tiny interpreters. It ships with:
- A full interpreter pipeline (lexer ? parser ? AST ? interpreter)
- A web studio for editing and running SimpleFlow code in the browser
- A compact syntax centered around a single structure: **cell**

---

## Features

**Core language**
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

**Single data structure: cell**
- Literal: `@(1, 2, 3)`
- 1-based indexing: `c[1]`
- Nested assignment: `c[a][b] = x`
- Mixed keys: `c["name"] = "simpleflow"`
- `length(cell)`

**Cell helpers**
- `push`, `pop`, `shift`, `unshift`
- `keys`, `values`, `has`
- `slice`, `merge`

**Standard library**
- `input`, `random`, `clock`
- `type`, `toNumber`, `toString`
- `assert`

**Other**
- `include "file.sf"` / `import "file.sf"`
- REPL mode

---

## Quick Start & Deployment

### 1) Free Cloud Hosting (Vercel)
SimpleFlow now fully supports being hosted for free entirely on Vercel utilizing a custom serverless Java bridge!
1. Push this repository to GitHub.
2. Go to [Vercel](https://vercel.com) and import the repository.
3. Don't touch any settings. Just hit **Deploy**.

Vercel will natively download the JDK under the hood, route the UI effectively, and execute Java serverlessly.

### 2) Run Web Studio Locally (Node.js)
If you just want to test the Web Studio environment locally using Vercel's simulation:
```bash
npm install -g vercel
npx vercel dev
```
Open `http://localhost:3000/`.

### 3) Run Web Studio Locally (Spring Boot)
For significantly faster local performance, use the native Java Spring Boot server:
From `web/backend/runner`:
```bash
./mvnw clean spring-boot:run
```
Open `http://localhost:8080/`. The frontend is smart enough to detect the port and route the code appropriately.

### 4) Run from CLI
From `simpleflow-lang`:
```bash
javac --release 17 -d out (Get-ChildItem -Recurse -Filter *.java | Select-Object -ExpandProperty FullName)
java -cp out com.simpleflow.lang.Main test.sf
```

### 5) Start REPL
```bash
java -cp out com.simpleflow.lang.Main
```

---

## Web Studio

The Studio is a full featured playground environment:
- Code editor with line numbers
- Output panel (console + errors)
- A guide page describing syntax and built-ins
- A program library for saving multiple scripts in the browser

---

## Language Guide

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

### Ternary
```sf
store best = a > b ? a : b
```

### Multi-line strings
```sf
store s = """line 1
line 2
line 3"""
show s
```

---

## Cells (Single Data Structure)

### Literal + indexing
```sf
store c = @(1, 2, 3)
show c[1]
```

### Mixed keys
```sf
c["name"] = "simpleflow"
show c["name"]
```

### Nested assignment
```sf
c["meta"]["version"] = 1
```

### Helpers
```sf
push(c, 4)
show pop(c)
show shift(c)
unshift(c, 0)
show keys(c)
show values(c)
show has(c, "name")
show slice(c, 1, 2)
```

---

## Standard Library

```sf
show input()
show random(1, 6)
show clock()
show type(123)
show toNumber("42")
show toString(789)
assert(1 == 1, "should pass")
```

---

## File Includes

```sf
include "utils.sf"
import "more.sf"
```

---

## Examples

### Minimal
```sf
store x = 5
show x + 10
```

### Cell demo
```sf
store c = @(1, 2, 3)
push(c, 4)
show c
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
web/
  backend/
    runner/
      src/main/resources/static
```

---

## Error Handling

SimpleFlow reports syntax and runtime errors with line/column info and a caret indicator to highlight the error location.

---

## License

Educational and experimental use only.
