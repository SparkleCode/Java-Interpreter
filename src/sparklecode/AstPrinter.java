/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sparklecode;

import java.util.List;

/**
 *
 * @author Will
 */
public class AstPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
  String print(Expr expression) {
    return expression.accept(this);
  }
  String print(Stmt statement) {
    return statement.accept(this);
  }
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
  
  private String parenthesize2(String name, Object... parts) {
    StringBuilder builder = new StringBuilder();

    builder.append("(").append(name);

    for (Object part : parts) {
      builder.append(" ");

      if (part instanceof Expr) {
        builder.append(((Expr)part).accept(this));
//> Statements and State omit
      } else if (part instanceof Stmt) {
        builder.append(((Stmt) part).accept(this));
//< Statements and State omit
      } else if (part instanceof Token) {
        builder.append(((Token) part).lexeme);
      } else {
        builder.append(part);
      }
    }
    builder.append(")");

    return builder.toString();
  }
  
  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if(expr.value == null) return "nil";
    return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  @Override
  public String visitExpressionStmt(Stmt.Expression stmt) {
    return parenthesize("exprStmt", stmt.expression);
  }

  @Override
  public String visitPrintStmt(Stmt.Print stmt) {
    return parenthesize("print", stmt.expression);
  }

  @Override
  public String visitVariableExpr(Expr.Variable expr) {
    return expr.name.lexeme;
  }

  @Override
  public String visitVarStmt(Stmt.Var stmt) {
    if (stmt.initializer == null) {
      return parenthesize2("var", stmt.name.lexeme);
    }

    return parenthesize2("var", stmt.name, "=", stmt.initializer);
  }

  @Override
  public String visitAssignExpr(Expr.Assign expr) {
    return parenthesize2("=", expr.name.lexeme, expr.value);
  }

  @Override
  public String visitBlockStmt(Stmt.Block stmt) {
    StringBuilder builder = new StringBuilder();
    builder.append("(block ");

    for (Stmt statement : stmt.statements) {
      builder.append(statement.accept(this));
    }

    builder.append(")");
    return builder.toString();
  }
}
