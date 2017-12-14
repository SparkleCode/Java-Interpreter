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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Run list of statements
 * @author Will
 */
public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {
  private static final Object UNINITIALIZED = new Object();
  
  /**
   * reference to base environment
   */
  private final Environment globals = new Environment();
  
  /**
   * The current environment
   */
  private Environment env = globals;
  
  /**
   * should expressions be printed explicitly
   */
  private boolean printExpr = false;
  private final Map<Expr, Integer> locals = new HashMap<>();

  /**
   * Constructor to initialise native functions
   */
  public Interpreter() {
    globals.define("clock", new SparkleCallable() {
      @Override
      public Object call(Interpreter interp, List<Object> arguments) {
        return (double)System.currentTimeMillis() / 1000.0;
      }

      @Override
      public int arity() {
        return 0;
      }
    });
  }
  
  public void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }
  
  private Object lookupVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if(distance != null) {
      return env.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }
  
  /**
   * run list of statements in current scope
   * @param statements 
   */
  void interpret(List<Stmt> statements) {
    try {
      printExpr = false;
      int i = 0;
      for(Stmt stmt : statements){
        i++;
        // if last statement enable printing expression statements
        if(i >= statements.size() && SparkleCode.inRepl && i == 0){
          printExpr = true;
        }
        excecute(stmt);
      }
    } catch (RuntimeError error) {
      SparkleCode.runtimeError(error);
    }
  }
  
  /**
   * run binary operator
   * @param expr binary operator
   * @return value of operation
   */
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

  /**
   * run group expression
   * @param expr group expression
   * @return value of contents
   */
  @Override
  public Object visitGroupingExpr(Expr.Grouping expr) {
    return evaluate(expr.expression);
  }

  /**
   * run literal expression
   * @param expr literal
   * @return value of literal
   */
  @Override
  public Object visitLiteralExpr(Expr.Literal expr) {
    return expr.value;
  }
  
  /**
   * run unary operator
   * @param expr operator expression
   * @return value of operation
   */
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
  
  /**
   * run expression statement and optionally print 
   * result of statement (for repl)
   * @param stmt expression statement
   * @return void
   */
  @Override
  public Void visitExpressionStmt(Stmt.Expression stmt) {
    Object o = evaluate(stmt.expression);
    if(printExpr) System.out.println(stringify(o));
    return null;
  }

  /**
   * run print statement
   * @param stmt print statement
   * @return void
   */
  @Override
  public Void visitPrintStmt(Stmt.Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }
  
  /**
   * run visitor of expression
   * @param expr expression
   * @return value of expression
   */
  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }
  
  /**
   * run visitor on statement
   * @param stmt statement
   */
  private void excecute(Stmt stmt) {
    stmt.accept(this);
  }
  
  /**
   * is object boolean true or false
   * @param object object to check if it is true or false
   * @return is true or false
   */
  private boolean isTruthy(Object object) {
    if (object == null) return false;
    if (object instanceof Boolean) return (boolean)object;
    return true;
  }
  
  /**
   * returns a == b, returning false instead of throwing null pointer
   * @param a left object
   * @param b right object
   * @return does a == b
   */
  private boolean isEqual(Object a, Object b) {
    // nil is only equal to nil.
    if (a == null && b == null) return true;
    if (a == null) return false;

    return a.equals(b);
  }
  
  /**
   * is the operand a number
   * @param operator operator operand is used for, for error reporting
   * @param operand operand
   */
  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double) return;
    throw new RuntimeError(operator, "Operand must be a number. Got " + typeof(operator) + ". ");
  }
  
  /**
   * are both operands numbers
   * @param operator operator operand is used for, for error reporting
   * @param left left operand
   * @param right right operand
   * @see checkNumberOperand
   */
  private void checkNumberOperands(Token operator,
                                   Object left, Object right) {
    if (left instanceof Double && right instanceof Double) return;
    
    throw new RuntimeError(operator, "Operands must be numbers. Got " + 
            typeof(left) + " and " + typeof(right) + ". ");
  }
  
  /**
   * return name of class used in SparkleCode
   * @param object object to be named
   * @return name of object
   */
  private String typeof(Object object){
    String type = object.getClass().getSimpleName();
    if(type.equals("Double")) return "number";
    if(type.equals("String")) return "string";
    return type;
  }
  
  /**
   * return string representation of object
   * @param object object
   * @return string representation
   */
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

  /**
   * Get value from environment from variable expression
   * @param expr variable expression
   * @return value
   */
  @Override
  public Object visitVariableExpr(Expr.Variable expr) {
    Object value = lookupVariable(expr.name, expr);
    if(value == UNINITIALIZED){
      throw new RuntimeError(expr.name, 
              "Cannot access uninitialized variable \"" + 
                      expr.name.lexeme + "\". ");
    }
    return value;
  }

  /**
   * run variable definition statement
   * @param stmt statement
   * @return void
   */
  @Override
  public Void visitVarStmt(Stmt.Var stmt) {
    Object value = UNINITIALIZED;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    env.define(stmt.name.lexeme, value);
    return null;
  }

  /**
   * run assignment
   * @param expr expression
   * @return value that is assigned
   */
  @Override
  public Object visitAssignExpr(Expr.Assign expr) {
    Object value = evaluate(expr.value);

    Integer distance = locals.get(expr);
    if (distance != null) {
      env.assignAt(distance, expr.name, value);
    } else {
      globals.assign(expr.name, value);
    }
    return value;
  }
  
  /**
   * run block statement in new environment
   * @param expr block statement
   * @return void
   */
  @Override
  public Void visitBlockStmt(Stmt.Block expr) {
    excecuteBlock(expr.statements, new Environment(env));
    return null;
  }

  /**
   * run a list of statements
   * @param statements list of statements
   * @param environment environment to run the block in
   */
  public void excecuteBlock(List<Stmt> statements, Environment environment) {
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

  /**
   * run if statement
   * @param stmt if statement
   * @return void
   */
  @Override
  public Void visitIfStmt(Stmt.If stmt) {
    if(isTruthy(evaluate(stmt.condition))) {
      excecute(stmt.thenBranch);
    } else if(stmt.elseBranch != null){
      excecute(stmt.elseBranch);
    }
    return null;
  }
   
  /**
   * run logical operator with short circuiting
   * @param expr operator expression
   * @return result
   */
  @Override
  public Object visitLogicalExpr(Expr.Logical expr) {
    Object left = evaluate(expr.left);

    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left)) return left;
    } else {
      if (!isTruthy(left)) return left;
    }

    return evaluate(expr.right);
  }

  /**
   * run while statement
   * @param stmt while statement
   * @return void
   */
  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while(isTruthy(evaluate(stmt.condition))) {
      excecute(stmt.body);
    }
    return null;
  }

  /**
   * call function
   * @param expr function call expression
   * @return return value of function
   */
  @Override
  public Object visitCallExpr(Expr.Call expr) {
   Object callee = evaluate(expr.callee);
   
   if(!(callee instanceof SparkleCallable)) {
     throw new RuntimeError(expr.paren, "Can only call functions and classes. ");
   }
   
   List<Object> arguments = new ArrayList<>();
   expr.arguments.forEach((argument) -> {
     arguments.add(evaluate(argument));
    });
   
    SparkleCallable function = (SparkleCallable)callee;
    
    if(arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " +
              function.arity() + " arguments but got " + 
              arguments.size() + " arguments. ");
    }
    
    return function.call(this, arguments);
  }

  @Override
  public Void visitFunctionStmt(Stmt.Function stmt) {
    SparkleFunction fun = new SparkleFunction(stmt, env, false);
    env.define(stmt.name.lexeme, fun);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if(stmt.value != null) value = evaluate(stmt.value);
    
    throw new Return(value);
  }
  
  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    env.define(stmt.name.lexeme, UNINITIALIZED);
    
    Object superclass = null;
    if(stmt.superclass != null){
      superclass = evaluate(stmt.superclass);
      if(!(superclass instanceof SparkleClass)){
        throw new RuntimeError(stmt.name, "Superclass must be a class. ");
      }
      
      env = new Environment(env);
      env.define("super", superclass);
    }
    
    Map<String, SparkleFunction> methods = new HashMap<>();
    stmt.methods.forEach((method) -> {
      SparkleFunction function = new SparkleFunction(method, env, method.name.lexeme.equals("init"));
      methods.put(method.name.lexeme, function);
    });
    
    SparkleClass klass = new SparkleClass(stmt.name.lexeme,
            (SparkleClass)superclass, methods);
    
    if(superclass != null) {
      env = env.enclosing;
    }
    env.assign(stmt.name, klass);
    return null;
  }
  
  @Override
  public Object visitGetExpr(Expr.Get expr) {
    Object obj = evaluate(expr.object);
    if(obj instanceof SparkleInstance) {
      return ((SparkleInstance) obj).get(expr.name);
    }
    
    throw new RuntimeError(expr.name, "Only instances have properties");
  }

  @Override
  public Object visitSetExpr(Expr.Set expr) {
    Object obj = evaluate(expr.object);
    
    if(!(obj instanceof SparkleInstance)) {
      throw new RuntimeError(expr.name, "Only instances have fields");
    }
    
    Object value = evaluate(expr.value);
    ((SparkleInstance) obj).set(expr.name, value);
    return value;
  }

  @Override
  public Object visitThisExpr(Expr.This expr) {
    return lookupVariable(expr.keyword, expr);
  }

  @Override
  public Object visitSuperExpr(Expr.Super expr) {
    int distance = locals.get(expr);
    SparkleClass superclass = (SparkleClass)env.getAt(distance, "super");
    
    SparkleInstance obj = (SparkleInstance)env.getAt(distance - 1, "this");
    
    SparkleFunction method = superclass.findMethod(obj, expr.method.lexeme);
    
    if (method == null) {
      throw new RuntimeError(expr.method,
          "Undefined property '" + expr.method.lexeme + "'.");
    }
    
    return method;
  }
}