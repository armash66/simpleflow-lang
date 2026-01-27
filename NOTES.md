## Project Status
- Lexer: DONE
- Parser: DONE
- Interpreter: DONE
- Assignment support: DONE

## Last Working Output
3
2
1
finished

## Next Planned Work
- Git repo setup
- File-based execution
- Functions

## Project Structure Overview

The project is divided into four main parts.

### Lexer
The lexer reads the source code as plain text and breaks it into tokens.  
Tokens are small meaningful units like keywords, identifiers, numbers, and symbols.  
This step removes raw text complexity and prepares input for parsing.

### Parser
The parser reads the tokens produced by the lexer and checks if they follow the rules of the language.  
It converts tokens into structured statements and expressions based on grammar rules.

### AST (Abstract Syntax Tree)
The AST represents the logical structure of the program.  
It stores the meaning of code instead of its textual form.  
The interpreter uses the AST to execute programs.

### Interpreter
The interpreter walks through the AST and executes the program.  
It evaluates expressions, stores variables in memory, and controls program flow.

---

## Runtime Behavior

Variables are stored in a simple map during execution.  
Control flow such as loops and conditionals is handled by repeatedly evaluating conditions.

---

## Project Goal

This project focuses on understanding the basics of language design, parsing, and interpretation.  
It is intentionally minimal and not meant for production use.
