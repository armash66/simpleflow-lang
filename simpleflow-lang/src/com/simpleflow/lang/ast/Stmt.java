package com.simpleflow.lang.ast;

import java.util.List;

import com.simpleflow.lang.lexer.Token;

public abstract class Stmt {

    public interface Visitor<R> {
        R visitAssignStmt(Assign stmt);
        R visitPutStmt(Put stmt);
        R visitPrintStmt(Print stmt);
        R visitBlockStmt(Block stmt);
        R visitIfStmt(If stmt);
        R visitWhileStmt(While stmt);
        R visitFunctionStmt(Function stmt);
        R visitReturnStmt(Return stmt);
        R visitExitStmt(Exit stmt);
        R visitLeaveStmt(Leave stmt);
        R visitNextStmt(Next stmt);
    }

    public abstract <R> R accept(Visitor<R> visitor);

    // --------------------

    public static class Put extends Stmt {
        public final Token name;
        public final Expr initializer;

        public Put(Token name, Expr initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPutStmt(this);
        }
    }

    public static class Print extends Stmt {
        public final Expr expression;

        public Print(Expr expression) {
            this.expression = expression;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStmt(this);
        }
    }

    public static class Block extends Stmt {
        public final List<Stmt> statements;

        public Block(List<Stmt> statements) {
            this.statements = statements;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStmt(this);
        }
    }

    public static class If extends Stmt {
        public final Expr condition;
        public final Stmt thenBranch;
        public final Stmt elseBranch;

        public If(Expr condition, Stmt thenBranch, Stmt elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStmt(this);
        }
    }

    public static class While extends Stmt {
        public final Expr condition;
        public final Stmt body;

        public While(Expr condition, Stmt body) {
            this.condition = condition;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStmt(this);
        }
    }

    public static class Function extends Stmt {
        public final Token name;
        public final List<Token> params;
        public final List<Stmt> body;

        public Function(Token name, List<Token> params, List<Stmt> body) {
            this.name = name;
            this.params = params;
            this.body = body;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitFunctionStmt(this);
        }
    }

    public static class Return extends Stmt {
        public final Token keyword;
        public final Expr value;

        public Return(Token keyword, Expr value) {
            this.keyword = keyword;
            this.value = value;
        }

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitReturnStmt(this);
        }
    }

    public static class Assign extends Stmt {
    public final Token name;
    public final Expr value;

    public Assign(Token name, Expr value) {
        this.name = name;
        this.value = value;
    }

    // ---------------- CONTROL FLOW ----------------

    public static class Exit extends Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExitStmt(this);
        }
    }

    public static class Leave extends Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLeaveStmt(this);
        }
    }

    public static class Next extends Stmt {
        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitNextStmt(this);
        }
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
        return visitor.visitAssignStmt(this);
    }
    }
}
