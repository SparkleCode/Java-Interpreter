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
import java.util.Arrays;
import java.util.List;
import static sparklecode.TokenType.*;

/**
 * convert list of tokens into statement list
 * @author Will
 */
public class Parser {
  /**
   * exception thrown during parsing
   */
  private static class ParseError extends RuntimeException {}
  
  /**
   * input list of tokens
   */
  private final List<Token> tokens;
  
  /**
   * current token index
   */
  private int current = 0;
  
  /**
   * Create new parser for list of tokens
   * @param tokens list of tokens
   */
  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }
  
  /**
   * convert tokens to list of statements
   * @return 
   */
  List<Stmt> parse() {
    List<Stmt> statments = new ArrayList<>();
    while(!isAtEnd()){
      Stmt d = declaration();
      if(d != null) {
        statments.add(d);
      }
    }
    
    return statments;
  }
  
  /**
   * parse a declaration
   * @return statement
   */
  private Stmt declaration() {
    try {
      if(match(VAR)) return varDeclaration();
      
      return statement();
    } catch(ParseError e) {
      synchronize();
      return null;
    }
  }
  
  /**
   * parse variable declaration (var x = 5; var x;)
   * @return statement
   */
  private Stmt varDeclaration() {
    Token name = consume(IDENTIFIER, "Expected variable name. ");
    
    Expr initialiser = null;
    if(match(EQUAL)) {
      initialiser = expression();
    }
    
    consumeStmtEnd("Expect ';' after variable declaration.");
    return new Stmt.Var(name, initialiser);
  }
  
  /**
   * parse non declaration statement
   * @return statement
   */
  private Stmt statement() {
    if(match(FOR)) return forStatement();
    if(match(IF)) return ifStatement();
    if(match(PRINT)) return printStatement();
    if(match(LEFT_BRACE)) return new Stmt.Block(block());
    if(match(WHILE)) return whileStatement();
    
    return expressionStatement();
  }
  
  /**
   * parse if statement
   * @return if statement
   */
  private Stmt ifStatement() {
    consume(LEFT_PAREN, "Expect ( after if. ");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ) after condition. ");
    
    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if(match(ELSE)){
      elseBranch = statement();
    }
    
    consumeStmtEnd();
    
    return new Stmt.If(condition, thenBranch, elseBranch);
  }
  
  /**
   * parse block statement
   * @return statement
   */
  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();
    
    while(!check(RIGHT_BRACE) && !isAtEnd()){
      statements.add(declaration());
    }
    
    consume(RIGHT_BRACE, "Expected } after block statement");
    consumeStmtEnd();
    
    return statements;
  }
  
  /**
   * parse print statement
   * @return statement
   */
  private Stmt printStatement() {
    Expr value = expression();
    consumeStmtEnd("Expected ';' after value.");
    return new Stmt.Print(value);
  }
  
  /**
   * parse expression statement
   * @return statement
   */
  private Stmt expressionStatement() {
    Expr value = expression();
    consumeStmtEnd("Expected ';' after expression.");
    return new Stmt.Expression(value);
  }
  
  /**
   * parse expression
   * @return expression
   */
  private Expr expression() {
    return assignment();
  }

  /**
   * parse assignment expression
   * @return expression
   */
  private Expr assignment() {
    Expr expr = or();

    if (match(EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable)expr).name;
        return new Expr.Assign(name, value);
      }

      throw error(equals, "Invalid assignment target.");
    }

    return expr;
  }
  
  /**
   * parse logical or || expression 
   * @return expression
   */
  private Expr or() {
    Expr expr = and();

    while (match(OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }
  
  /**
   * parse logical and && expression
   * @return expression
   */
  private Expr and() {
    Expr expr = equality();

    while (match(AND)) {
      Token operator = previous();
      Expr right = equality();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }
    
  /**
   * parse equality or inequality expression
   * @return expression
   */
  private Expr equality() {
    Expr expr = comparison();
    
    while(match(EQUAL_EQUAL, BANG_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }
    
    return expr;
  }
  
  /**
   * match comparison operator expression
   * @return expression
   */
  private Expr comparison() {
    Expr expr = addition();
    
    while(match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
      Token operator = previous();
      Expr right = addition();
      expr = new Expr.Binary(expr, operator, right);
    }
    
    return expr;
  }
  
  /**
   * match addition or subtraction expression
   * @return expression
   */
  private Expr addition() {
    Expr expr = multiplication();

    while (match(MINUS, PLUS)) {
      Token operator = previous();
      Expr right = multiplication();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  /**
   * match multiplication or division expression
   * @return expression
   */
  private Expr multiplication() {
    Expr expr = unary();

    while (match(SLASH, STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }
  
  /**
   * match unary operator expression
   * @return expression
   */
  private Expr unary() {
    if (match(BANG, MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }
  
  /**
   * parses call expression and curried syntax - a()()
   * @return nested call expression
   */
  private Expr call() {
    Expr expr = primary();
    
    while(true) {
      if(match(LEFT_PAREN)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }
    
    return expr;
  }
  
  /**
   * 
   */
  private Expr finishCall(Expr expr) {
    List<Expr> arguments = new ArrayList<>();
    if(!check(RIGHT_PAREN)) {
      do {
        if(arguments.size() >= 8) {
          error(peek(), "Cannot have more than 8 arguments");
        }
        arguments.add(expression());
      } while(match(COMMA));
    }
    
    Token paren = consume(RIGHT_PAREN, "Expect ) after arguments");
    
    return new Expr.Call(expr, paren, arguments);
  }
  /**
   * parse primary expression
   * @return expression
   */
  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

    if (match(NUMBER, STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(LEFT_PAREN)) {
      Expr expr = expression();
      consume(RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }
    
    if (match(IDENTIFIER)) {
      return new Expr.Variable(previous());
    }
    
    throw error(peek(), "Expect expression.");
  }
  
  /**
   * if next token is of input type advance, else throw error
   * @param type type to check for
   * @param message message if error thrown
   * @return token consumed
   */
  private Token consume(TokenType type, String message) {
    if (check(type)) return advance();

    throw error(peek(), message);
  }
  
  /**
   * throw new parse error and report it
   * @param token where the error occurred
   * @param message error message
   * @return error to throw
   */
  private ParseError error(Token token, String message) {
    SparkleCode.error(token, message);
    return new ParseError();
  }
  
  /**
   * recover parser after error, advance until next statement
   */
  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == SEMICOLON) return;

      switch (peek().type) {
        case CLASS:
        case FN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
  
  /**
   * if any of the supplied types equal the type of the next token return true
   * @param types list of types to check for
   * @return boolean
   */
  private boolean match(TokenType ... types) {
    for(TokenType type : types) {
      if(check(type)) {
        advance();
        return true;
      }
    }
    return false;
  }
  
  /**
   * is the next token of the supplied type
   * @param tokenType type
   * @return boolean
   */
  private boolean check(TokenType tokenType) {
    // allow no semi colon after last statement
    // if (isAtEnd() && tokenType == SEMICOLON) return true;
    if (isAtEnd()) return false;
    return peek().type == tokenType;
  }
  
  /**
   * consume token
   * @return token that was consumed
   */
  private Token advance() {
    if (!isAtEnd()) current++;
    return previous();
  }
  
  /**
   * is next token end of file
   * @return boolean
   */
  private boolean isAtEnd() {
    return peek().type == EOF;
  }

  /**
   * get next token
   * @return token
   */
  private Token peek() {
    return tokens.get(current);
  }

  /**
   * get previous token
   * @return previous token
   */
  private Token previous() {
    return tokens.get(current - 1);
  }
  
  /**
   * parse while statement
   * @return while statement
   */
  private Stmt whileStatement() {
    consume(LEFT_PAREN, "Expect ( after while. ");
    Expr condition = expression();
    consume(RIGHT_PAREN, "Expect ) after while condition. ");
    
    Stmt body = statement();
    
    consumeStmtEnd();
    return new Stmt.While(condition, body);
  }
  
  /**
   * parse and desugar for statement
   * @return while statement in block
   */
   private Stmt forStatement() {
    consume(LEFT_PAREN, "Expect ( after for. ");
    
    Stmt initializer;
    if(match(SEMICOLON)) {
      initializer = null;
    } else if(match(VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }
    
    Expr condition = null;
    if(!check(SEMICOLON)) {
      condition = expression();
    }
    consume(SEMICOLON, "Expect ; after for loop initializer. ");
    
    Expr increment = null;
    if (!check(RIGHT_PAREN)) {
      increment = expression();
    }
    consume(RIGHT_PAREN, "Expect ')' after for loop condition. ");
    
    Stmt body = statement();
    
    // add incrementer to body
    if(increment != null){
      body = new Stmt.Block(Arrays.asList(
        body,
        new Stmt.Expression(increment)
      ));
    }
    
    // set while loop condition
    if(condition == null) condition = new Expr.Literal(true); // infinite loop
    body = new Stmt.While(condition, body);
    
    // add initializer in block
    if(initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }
    
    consumeStmtEnd();
    return body;
  }
   
   private void consumeStmtEnd() {
     consumeStmtEnd("", true);
   }
   
   private void consumeStmtEnd(String message) {
     // apparently if       (false) is made true, all semicolons are optional
     consumeStmtEnd(message, false);
   }
   
   /**
    * consume semicolons
    * @param message
    * @param optional
    */
   private void consumeStmtEnd(String message, boolean optional) {
     if(check(RIGHT_BRACE)){}
     else if(match(SEMICOLON)){
       while(true){
         if(!match(SEMICOLON)){
           return;
         }
       }
     } else if(!optional) {
       throw error(peek(), message);
     }
   }
}
