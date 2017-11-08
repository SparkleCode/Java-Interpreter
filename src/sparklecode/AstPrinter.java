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
    statements.forEach((stmt) -> {
      builder.append(stmt.accept(this));
      builder.append("\n");
    });
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
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public String visitVarStmt(Stmt.Var stmt) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
}
