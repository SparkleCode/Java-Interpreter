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
import java.util.List;
import static sparklecode.TokenType.*;

/**
 * convert list of tokens into statement list
 * @author Will
 */
public class Parser {
  private static class ParseError extends RuntimeException {}
  private final List<Token> tokens;
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
    
    consume(SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initialiser);
  }
  
  /**
   * parse non declaration statement
   * @return statement
   */
  private Stmt statement() {
    if(match(PRINT)) return printStatement();
    if(match(LEFT_BRACE)) return new Stmt.Block(block());
    
    return expressionStatement();
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
    return statements;
  }
  
  /**
   * parse print statement
   * @return statement
   */
  private Stmt printStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expected ';' after value.");
    return new Stmt.Print(value);
  }
  
  /**
   * parse expression statement
   * @return statement
   */
  private Stmt expressionStatement() {
    Expr value = expression();
    consume(SEMICOLON, "Expected ';' after expression.");
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
    Expr expr = equality();

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
   * parse equality or inequality expression
   * @return expression
   */
  private Expr equality() {
    Expr expr = comparison();
    
    while(match(BANG, BANG_EQUAL)) {
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

    return primary();
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
    if (isAtEnd() && tokenType == SEMICOLON) return true;
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
}
