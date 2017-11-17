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

import java.util.List;

/**
 * Output an inputted ast as a lisp-style string
 * @author Will
 */
public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  /**
   * print expression
   * @param expression expression to print
   * @return string representation of expression
   */
  String print(Expr expression) {
    return expression.accept(this);
  }
  
  /**
   * print statement
   * @param statement statement to print
   * @return  string representation of statement
   */
  String print(Stmt statement) {
    return statement.accept(this);
  }
  
  /**
   * print list of statements
   * @param statements list to be printed
   * @return  string representation of list
   */
  String print(List<Stmt> statements) {
    StringBuilder builder = new StringBuilder();
    
    int idx = -1;
    for(Stmt stmt : statements){
      idx++;
      builder.append(stmt.accept(this));
      if(idx>=statements.size()) builder.append("\n");
    }
    return builder.toString();
  }
  
  /**
   * create lisp-style function call string
   * @param name name of function/group/etc.
   * @param exprs list of expressions, calls AstPrinter recursively
   * @return parenthesised string
   */
  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);
    for (Expr expr : exprs) {
      builder.append(" ");
      builder.append(expr.accept(this));
    }
    builder.append(")");

    return builder.toString();
  }
  
  /**
   * more general form of parenthesise - can take any object not just Exprs
   * recursively visits expressions and statements, adds lexeme of token
   * and adds Object.toString of anything else
   * @param name name of function
   * @param parts list of objects to add to string
   * @see parenthesize
   * @return 
   */
  private String parenthesize2(String name, Object... parts) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);

    for (Object part : parts) {
      builder.append(" ");

      if (part instanceof Expr) {
        builder.append(((Expr)part).accept(this));
      } else if (part instanceof Stmt) {
        builder.append(print(((Stmt) part)));
      } else if (part instanceof Token) {
        builder.append(((Token) part).lexeme);
      } else {
        builder.append(part);
      }
    }
    builder.append(")");

    return builder.toString();
  }
  
  /**
   * binary operator to string
   * @param expr binary operator
   * @return string
   */
  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }
  
  /**
   * group to string
   * @param expr group
   * @return string
   */
  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }
  
  /**
   * literal to string
   * @param expr literal
   * @return string
   */
  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if(expr.value == null) return "nil";
    return expr.value.toString();
  }

  /**
   * unary expression to string
   * @param expr unary expression
   * @return string
   */
  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  /**
   * expression statement to string
   * @param stmt expression statement
   * @return string
   */
  @Override
  public String visitExpressionStmt(Stmt.Expression stmt) {
    return parenthesize("exprStmt", stmt.expression);
  }
 
  /**
   * print statement to string
   * @param stmt print statement
   * @return string
   */
  @Override
  public String visitPrintStmt(Stmt.Print stmt) {
    return parenthesize("print", stmt.expression);
  }
  
  /**
   * variable name expression to string
   * @param expr variable name expression
   * @return string
   */
  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return expr.name.lexeme;
  }

  /**
   * variable declaration statement to string
   * @param stmt variable declaration statement
   * @return string
   */
  @Override
  public String visitVarStmt(Stmt.Var stmt) {
    if (stmt.initializer == null) {
      return parenthesize2("var", stmt.name.lexeme);
    }

    return parenthesize2("var", stmt.name, "=", stmt.initializer);
  }

  
  /**
   * assign expression to string
   * @param expr assign expression
   * @return string
   */
  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    return parenthesize2("=", expr.name.lexeme, expr.value);
  }

  /**
   * block to string
   * @param stmt block
   * @return string
   */
  @Override
  public String visitBlockStmt(Stmt.Block stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(block ");

    stmt.statements.forEach((statement) -> {
      builder.append(statement.accept(this));
    });

    builder.append(")");
    return builder.toString();
  }

  /**
   * if statement to string
   * @param stmt statement
   * @return string
   */
  @Override
  public String visitIfStmt(Stmt.If stmt) {
    return parenthesize2("if", stmt.condition, "then", stmt.thenBranch, "else", stmt.elseBranch);
  }

  /**
   * logical operator to string
   * @param expr expression
   * @return string
   */
  @Override
  public String visitLogicalExpr(Expr.Logical expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  /**
   * while statement to string
   * @param stmt statement
   * @return string
   */
  @Override
  public String visitWhileStmt(Stmt.While stmt) {
    return parenthesize2("while", stmt.condition, "do", stmt.body);
  }

  @Override
  public String visitCallExpr(Expr.Call expr) {
    return parenthesize2("call", expr.callee, expr.arguments);
  }
}
