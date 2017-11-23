/*
 * The MIT License
 *
 * Copyright 2017 Will.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package sparklecode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;

/**
 * Resolve any variables statically before interpreting code
 * Show static variable usage, etc. errors where possible, before code is run.
 * @author Will
 */
public class Resolver implements Expr.Visitor<Void>, Stmt.Visitor<Void> {

  private enum FunctionType {
    NONE,
    METHOD,
    FUNCTION,
    INITIALIZER
  }
  
  private enum ClassType {
    NONE,
    CLASS
  }

  private final Interpreter interpreter;
  private final Stack<Map<String, Boolean>> scopes = new Stack<>();
  private FunctionType currentFunction = FunctionType.NONE;
  private ClassType currentClass = ClassType.NONE;

  public Resolver(Interpreter interpreter) {
    beginScope();
    this.interpreter = interpreter;
  }

  public void resolve(List<Stmt> statements) {
    statements.forEach(this::resolve);
  }

  private void resolve(Stmt statement) {
    statement.accept(this);
  }

  private void resolve(Expr expr) {
    expr.accept(this);
  }

  private void resolveLocal(Expr expr, Token name) {
    for (int i = scopes.size() - 1; i >= 0; i--) {
      if (scopes.get(i).containsKey(name.lexeme)) {
        interpreter.resolve(expr, scopes.size() - 1 - i);
      }
    }

    // not found, assume global
  }

  private void resolveFunction(Stmt.Function stmt, FunctionType type) {
    FunctionType enclosingFunction = currentFunction;
    currentFunction = type;

    beginScope();
    stmt.parameters.forEach((param) -> {
      declare(param);
      define(param);
    });
    resolve(stmt.body);
    endScope();

    currentFunction = enclosingFunction;
  }

  
  
  private void beginScope() {
    scopes.push(new HashMap<>());
  }

  private void endScope() {
    scopes.pop();
  }

  private void declare(Token name) {
    if (scopes.isEmpty()) {
      return;
    }

    Map<String, Boolean> scope = scopes.peek();

    if (scope.containsKey(name.lexeme)) {
      SparkleCode.error(name,
              "Variable \"" + name.lexeme + "\" already defined in this scope. ");
    }
    scope.put(name.lexeme, false);
  }

  private void define(Token name) {
    if (scopes.isEmpty()) {
      return;
    }
    scopes.peek().put(name.lexeme, true);
  }

  @Override
  public Void visitAssignExpr(Expr.Assign expr) {
    define(expr.name);
    resolve(expr.value);
    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visitBinaryExpr(Expr.Binary expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitCallExpr(Expr.Call expr) {
    resolve(expr.callee);

    expr.arguments.forEach(this::resolve);

    return null;
  }
  
  @Override
  public Void visitGetExpr(Expr.Get expr) {
    resolve(expr.object);
    return null;
  }

  @Override
  public Void visitGroupingExpr(Expr.Grouping expr) {
    resolve(expr.expression);
    return null;
  }

  @Override
  public Void visitLiteralExpr(Expr.Literal expr) {
    return null;
  }

  @Override
  public Void visitLogicalExpr(Expr.Logical expr) {
    resolve(expr.left);
    resolve(expr.right);
    return null;
  }
  
  @Override
  public Void visitSetExpr(Expr.Set expr) {
    resolve(expr.object);
    resolve(expr.value);
    return null;
  }
  
  @Override
  public Void visitThisExpr(Expr.This expr) {
    if(currentClass == ClassType.NONE) {
      SparkleCode.error(expr.keyword, "Cannot use 'this' outside a method. ");
    }
    resolveLocal(expr, expr.keyword);
    return null;
  }
  
  @Override
  public Void visitUnaryExpr(Expr.Unary expr) {
    resolve(expr.right);
    return null;
  }

  @Override
  public Void visitVariableExpr(Expr.Variable expr) {
    if (!scopes.isEmpty()
            && Objects.equals(
                    scopes.peek().get(expr.name.lexeme),
                    Boolean.FALSE)) {
      SparkleCode.error(expr.name, "Cannot refrence local variable before it is initialised. ");
    }

    resolveLocal(expr, expr.name);
    return null;
  }

  @Override
  public Void visitBlockStmt(Stmt.Block stmt) {
    beginScope();
    resolve(stmt.statements);
    endScope();
    return null;
  }
  
  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    declare(stmt.name);
    define(stmt.name);
    
    ClassType enclosingClass = currentClass;
    currentClass = ClassType.CLASS;
    
    beginScope();
    scopes.peek().put("this", true);
    
    stmt.methods.forEach((method) -> {
      FunctionType declaration = FunctionType.METHOD;
      if(method.name.lexeme.equals("init")) {
        declaration = FunctionType.INITIALIZER;
      }
      resolveFunction(method, declaration);
    });
    
    endScope();
    
    currentClass = enclosingClass;
    
    return null;
  }

  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    declare(stmt.name);
    define(stmt.name);

    resolveFunction(stmt, FunctionType.FUNCTION);
    return null;
  }

  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    resolve(stmt.condition);
    resolve(stmt.thenBranch);
    if (stmt.elseBranch != null) {
      resolve(stmt.elseBranch);
    }
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    resolve(stmt.expression);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    if (currentFunction == FunctionType.NONE) {
      SparkleCode.error(stmt.keyword, "Cannot return from top-level code. ");
    }
    if (stmt.value != null) {
      if(currentFunction == FunctionType.INITIALIZER) {
        SparkleCode.error(stmt.keyword, "Cannot return value from initializer");
      }
      resolve(stmt.value);
    }

    return null;
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    declare(stmt.name);
    if (stmt.initializer != null) {
      resolve(stmt.initializer);
      define(stmt.name);
    }
    return null;
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    resolve(stmt.condition);
    resolve(stmt.body);
    return null;
  }
}
