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
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private Environment env = new Environment();
  private boolean printExpr = false;
  
  void interpret(List<Stmt> statements) {
    try {
      printExpr = false;
      int i = 0;
      for(Stmt stmt : statements){
        i++;
        if(i >= statements.size()){
          printExpr = true;
        }
        excecute(stmt);
      }
    } catch (RuntimeError error) {
      SparkleCode.runtimeError(error);
    }
  }
  @Override
  public Object visitBinaryExpr(Expr.Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right); 

    switch (expr.operator.type) {
      case BANG_EQUAL: return !isEqual(left, right);
      case EQUAL_EQUAL: return isEqual(left, right);
      case GREATER:
        checkNumberOperands(expr.operator, left, right);
        return (double)left > (double)right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left >= (double)right;
      case LESS:
        checkNumberOperands(expr.operator, left, right);
        return (double)left < (double)right;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator, left, right);
        return (double)left <= (double)right;
      case MINUS:
        checkNumberOperands(expr.operator, left, right);
        return (double)left - (double)right;
      case PLUS:
        if (left instanceof Double && right instanceof Double) {
          return (double)left + (double)right;
        } 

        if (left instanceof String && right instanceof String) {
          return (String)left + (String)right;
        }
        
        throw new RuntimeError(expr.operator,
            "Operands must be two numbers or two strings. Got " + 
                    typeof(left) + " and " + typeof(right) + ". ");
      case SLASH:
        if(right.equals((double)0))
          throw new RuntimeError(expr.operator, "Divide by 0 error. ");
        checkNumberOperands(expr.operator, left, right);
        return (double)left / (double)right;
      case STAR:
        checkNumberOperands(expr.operator, left, right);
        return (double)left * (double)right;
    }

    // Unreachable.
    return null;
  }

  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }
  
  @Override
  public Object visitUnaryExpr(Expr.Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -(double)right;
    }

    // Unreachable.
    return null;
  }
  
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    Object o = evaluate(stmt.expression);
    if(printExpr) System.out.println(stringify(o));
    return null;
  }

  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }
  
  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }
  private void excecute(Stmt stmt) {
    stmt.accept(this);
  }
  
  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  }
  private boolean isEqual(Object a, Object b) {
    // nil is only equal to nil.
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }
  
  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number. Got " + typeof(operator) + ". ");
  }
  
  private void checkNumberOperands(Token operator,
                                   Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    
    throw new RuntimeError(operator, "Operands must be numbers. Got " + 
            typeof(left) + " and " + typeof(right) + ". ");
  }
  
  private String typeof(Object object){
    String type = object.getClass().getSimpleName();
    if(type.equals("Double")) return "number";
    if(type.equals("String")) return "string";
    return type;
  }
  
  private String stringify(Object object) {
    if (object == null) return "nil";

    // Hack. Work around Java adding ".0" to integer-valued doubles.
    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }
      return text;
    }

    return object.toString();
  }

  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    return env.get(expr.name);
  }

  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    env.define(stmt.name.lexeme, value, stmt.initializer != null);
    return null;
  }

  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);

    env.assign(expr.name, value);
    return value;
  }
  
  @Override
  public Void visitBlockStmt(Stmt.Block expr) {
    excecuteBlock(expr.statements, new Environment(env));
    return null;
  }

  private void excecuteBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.env;
    try {
      this.env = environment;
      statements.forEach((stmt) -> {
        excecute(stmt);
      });
    } finally {
      this.env = previous;
    }
  }
}
