# SimpleFlow

SimpleFlow is a minimal interpreted programming language built from scratch in Java.  
It features a custom lexer, parser, abstract syntax tree (AST), and interpreter,  
designed to demonstrate how programming languages work internally.

The language uses simple English-based synonym keywords to keep syntax readable  
while remaining distinct from mainstream languages.

---

## ✨ Language Features

- Variable declarations
- Arithmetic expressions
- Conditional execution
- Loops
- Block scoping
- Runtime interpretation

---

## 🔑 Language Syntax

### Variable Declaration
put x = 10

### Output
say "hello"
show x

### Conditional
when (x > 5) {
    say "greater"
} or {
    say "smaller"
}

### Loop
repeat (x > 0) {
    show x
    x = x - 1
}

### End Program
stop

### 🧠 Example Program
put count = 3

repeat (count > 0) {
    show count
    count = count - 1
}

when (count == 0) {
    say "finished"
} or {
    say "error"
}

stop

### Output
3
2
1
finished

---

## 🏗️ Project Architecture
The language is implemented using a classic interpreter pipeline:

Source Code
   ↓
Lexer        → Tokens
   ↓
Parser       → AST
   ↓
Interpreter → Execution

## Directory Structure
src/

 ├── lexer/         // Tokenization

 ├── parser/        // Grammar & parsing logic

 ├── ast/           // Expression & statement nodes

 ├── interpreter/   // Execution engine

 └── Main.java      // Entry point

---

## 🚀 Running the Project
Compile
javac src/Main.java src/lexer/*.java src/parser/*.java src/ast/*.java src/interpreter/*.java

Run
java -cp src Main

---

## 🎯 Purpose

This project was built as a learning-focused compiler/interpreter implementation  
to understand:

- Language grammar design
- Recursive descent parsing
- AST construction
- Runtime interpretation
- Error handling in custom languages

---

## 📌 Future Improvements

- File-based source execution
- User-defined functions
- Better error reporting
- Additional data types

---

## 📜 License

This project is intended for educational and experimental purposes.
