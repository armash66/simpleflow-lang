# SimpleFlow

**SimpleFlow** is a minimal interpreted programming language built from scratch in **Java**.  
It implements a complete interpreter pipeline — **lexer, parser, AST, and interpreter** —  
designed to help learners understand how programming languages work internally.

The language uses **simple English-based keywords** to keep syntax readable while staying  
distinct from mainstream languages.

---

## ✨ Features

- Variable declaration and assignment  
- Arithmetic expressions  
- Conditional execution  
- Loops  
- Block scoping  
- Boolean values (`true`, `false`)  
- User-defined functions with parameters and return values  
- File-based execution  
- Runtime interpretation  

---

## 🔑 Language Syntax

### Variable Declaration
```bash
set x = 10
```

### Output
```bash
say "hello"
show x

### Conditional
```bash
when (x > 5) {
    say "greater"
} otherwise {
    say "smaller"
}
```

### Loop
```bash
while (x > 0) {
    show x
    x = x - 1
}
```

### Functions
```bash
define add(a, b) {
    return a + b
}

set result = add(2, 3)
show result
```

### End Program
```bash
exit
```

### 🧠 Example Program
```bash
define countdown(n) {
    while (n > 0) {
        show n
        n = n - 1
    }
    return n
}

set final = countdown(3)

when (final == 0) {
    say "finished"
} otherwise {
    say "error"
}

exit
```

### Output
3
2
1
finished

--- 

## 🏗️ Project Architecture

SimpleFlow follows a classic interpreter pipeline:

Source Code
     ↓
Lexer        → Tokens
     ↓
Parser       → AST
     ↓
Interpreter → Execution

--- 

## 📁 Directory Structure
src/

 ├── lexer/         // Tokenization logic

 ├── parser/        // Grammar & parsing

 ├── ast/           // Expression & statement nodes

 ├── interpreter/   // Runtime execution engine

 └── Main.java      // Program entry point

--- 

## 🚀 Running the Project

### Compile
```bash
javac -d out src/Main.java src/lexer/*.java src/parser/*.java src/ast/*.java src/interpreter/*.java
```
### Run
```bash
java -cp out Main program.sf
```

--- 

## 🧪 After Running the Project

Once the program runs successfully, the SimpleFlow interpreter executes the source file
line by line and prints output directly to the terminal.

### What Happens Internally

When you run:

```bash
java -cp out Main program.sf
```

### Execution Flow

SimpleFlow performs the following steps when a program is run:

1. Reads the source file (`program.sf`) as plain text  
2. Tokenizes the code using the lexer  
3. Parses tokens into an Abstract Syntax Tree (AST)  
4. Interprets the AST, executing statements in order  
5. Prints output or errors to the terminal  

This process closely mimics how real programming languages execute code internally.

--- 

## 📝 Writing Your Own Programs

You can create and run your own .sf files.

Example:
```bash
set a = 5
set b = 10

define multiply(x, y) {
    return x * y
}

show multiply(a, b)
exit
```

### Save the file (for example, test.sf) and run:
```bash
java -cp out Main test.sf
```

--- 

## ⚠️ Error Handling

SimpleFlow reports runtime and syntax errors with line numbers to help with debugging.

### Examples of Errors
- Using an undefined variable  
- Calling a function with the wrong number of arguments  
- Invalid syntax or unexpected tokens  

Error messages are intentionally simple and designed for learning purposes.

---

## 🧠 Learning Tips

To better understand how the language works, try the following:

- Modify the lexer to add new keywords  
- Extend the parser with new grammar rules  
- Add new AST nodes for new features  
- Enhance the interpreter to support new behaviors  

SimpleFlow is designed to be **read, modified, and experimented with**.  
Breaking things is part of the learning process.

---

## 📂 Recommended Experiments

- Add a new keyword (e.g., `repeat_until`)  
- Implement logical operators (`and`, `or`)  
- Add string concatenation  
- Create a standard library file  
- Build a REPL (interactive mode)  

Each experiment helps deepen understanding of language design.

--- 

## 🎯 Purpose

This project was built as a **learning-focused interpreter implementation** to understand:

- Language grammar design  
- Recursive-descent parsing  
- Abstract Syntax Tree (AST) construction  
- Runtime interpretation  
- Variable scoping and environments  
- Function calls and return mechanics  

It is intentionally minimal and **not intended for production use**.

---

## 📌 Future Improvements

- Better error messages with source highlighting  
- More data types (floats, string operations)  
- Standard library functions  
- REPL (interactive mode)  
- Packaging as a runnable JAR  

---

## 📜 License

This project is intended for **educational and experimental purposes**.
