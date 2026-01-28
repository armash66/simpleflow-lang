# How SimpleFlow Works

SimpleFlow is designed as a small but complete interpreted language.  
Its implementation follows the same execution model used by many real-world languages.

At a high level, SimpleFlow converts source code into executable behavior through four main stages.

---

## 1. Lexical Analysis (Lexer)

The lexer reads the source file as raw text and breaks it into **tokens**.  
Tokens represent meaningful units such as keywords, identifiers, numbers, operators, and symbols.

This step removes the complexity of raw text and prepares structured input for parsing.

---

## 2. Parsing (Parser)

The parser consumes the stream of tokens and checks whether they follow the grammar rules of the language.

Using a **recursive-descent** approach, the parser converts tokens into an  
**Abstract Syntax Tree (AST)**.

The AST represents the logical structure of the program rather than its textual form.

---

## 3. Abstract Syntax Tree (AST)

The AST is a tree-based representation of the program where:

- Each node represents a statement or expression  
- Execution order and relationships are explicitly defined  

This separation allows the interpreter to focus on **meaning** instead of syntax.

---

## 4. Interpretation (Runtime Execution)

The interpreter walks the AST and executes it step by step.

During execution:

- Variables are stored in an environment  
- New environments are created for blocks and function calls  
- Expressions are evaluated dynamically  
- Control flow (loops, conditionals, returns) is handled at runtime  

Function calls create their own local scope, and `return` statements exit execution
early using controlled flow signals.

---

## Design Philosophy

SimpleFlow is intentionally minimal.  
It avoids optimizations, static typing, or advanced language features in favor of clarity.

The goal is not performance, but **understanding**.

Every component is written to be readable, modifiable, and easy to experiment with,
making SimpleFlow a practical learning tool for exploring how programming languages are built.
