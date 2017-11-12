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

import static sparklecode.TokenType.*;

/**
 * Convert string input to list of tokens
 * @author Will
 */
public class Scanner {
  /**
   * source code
   */
  private final String source;
  
  /**
   * output list of tokens
   */
  private final List<Token> tokens;
  
  /**
   * index of character at start of current token
   */
  private int start = 0;
  
  /**
   * index of current location through string
   */
  private int current = 0;
  
  /**
   * the line number of the line that is currently being scanned
   */
  private int line = 1;
  
  /**
   * create a scanner around source code
   * @param code source code
   */
  public Scanner(String code) {
    this.tokens = new ArrayList<>();
    source = code;
  }
  
  /**
   * scan source code
   * @return list of tokens in source code
   */
  List<Token> scanTokens() {
    while(!isAtEnd()) {
      start = current;
      scanToken();
    }
    
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }
  
  /**
   * scan one token and add it to list
   */
  private void scanToken() {
    char c = advance();
    switch(c) {
      case ' ':
      case '\r':
      case '\t':
        // Ignore whitespace.
        break;

      case '\n':
        line++;
        break;
      
      case '(': addToken(LEFT_PAREN); break;
      case ')': addToken(RIGHT_PAREN); break;
      case '{': addToken(LEFT_BRACE); break;
      case '}': addToken(RIGHT_BRACE); break;
      case ',': addToken(COMMA); break;
      case '.': addToken(DOT); break;
      case '-': addToken(MINUS); break;
      case '+': addToken(PLUS); break;
      case ';': addToken(SEMICOLON); break;
      case '*': addToken(STAR); break;
      
      case '!': addToken(match('=') ? BANG_EQUAL : BANG); break;
      case '=': addToken(match('=') ? EQUAL_EQUAL : EQUAL); break;
      case '<': addToken(match('=') ? LESS_EQUAL : LESS); break;
      case '>': addToken(match('=') ? GREATER_EQUAL : GREATER); break;
      
      case '/':
        if (match('/')) {
          // A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else if(match('*')) {
          // multi line block comment
          
          // current level of nesting
          int level = 0;
          while(!isAtEnd()) {
            // new level of nesting
            if(peek() == '/' && peekNext() == '*'){
              level++;
              advance();
            }
            // leave level of nesting
            if(peek() == '*' && peekNext() == '/'){
              level--;
              if(level <= -1) break;
              advance();
            }
            advance();
            
            // if not exited last level of comment and at end of string
            if(isAtEnd()){
              SparkleCode.error(start, "Unterminated block comment");
            }
          }
          match('*');
          match('/');
        } else {
          // divide
          addToken(SLASH);
        }
        break;
      
      case '"': string(); break;
        
      default:
        if(isDigit(c)){
          number();
        } else if(isAlpha(c)){
          identifier();
        } else {
          SparkleCode.error(line, "Unexpected character " + c);
        }
        break;
    }
  }
  
  /**
   * map of strings to their token types
   */
  private static final Map<String, TokenType> KEYWORDS;

  static {
    KEYWORDS = new HashMap<>();
    KEYWORDS.put("and",    AND);
    KEYWORDS.put("class",  CLASS);
    KEYWORDS.put("else",   ELSE);
    KEYWORDS.put("false",  FALSE);
    KEYWORDS.put("for",    FOR);
    KEYWORDS.put("fn",     FN);
    KEYWORDS.put("if",     IF);
    KEYWORDS.put("nil",    NIL);
    KEYWORDS.put("or",     OR);
    KEYWORDS.put("print",  PRINT);
    KEYWORDS.put("return", RETURN);
    KEYWORDS.put("super",  SUPER);
    KEYWORDS.put("this",   THIS);
    KEYWORDS.put("true",   TRUE);
    KEYWORDS.put("var",    VAR);
    KEYWORDS.put("while",  WHILE);
  }
  
  /**
   * has all of the string been consumed?
   * @return boolean
   */
  private boolean isAtEnd() {
    return current >= source.length();
  }
  
  /**
   * is the character in [0-9]
   * @param c character
   * @return boolean
   */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }
  
  /**
   * is the character is [a-zA-Z_]
   * @param c character
   * @return boolean
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }
  
  /**
   * is character in [a-zA-Z0-9_]
   * @param c character
   * @return boolean
   */
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
  
  /**
   * add next character to current token
   * @return character added
   */
  private char advance() {
    current++;
    return source.charAt(current - 1);
  }
  
  /**
   * view next character without consuming it
   * @return character
   */
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
  
  /**
   * view character after next without consuming it
   * @return character
   * @see peek
   */
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }
  
  /**
   * if next character == input consume it and return true, 
   * else don't consume and return false
   * @param expected input character
   * @return boolean
   */
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }
  
  /**
   * add token with no literal
   * @param type token type
   * @see addToken
   */
  private void addToken(TokenType type) {
    addToken(type, null);
  }
  
  /**
   * add token with literal and type, set token value to equal
   * the consumed characters after last token
   * @param type token type
   * @param literal token data
   */
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
  
  /**
   * consume string literal surrounded by "
   */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n') line++;
      advance();
    }

    // Unterminated string.
    if (isAtEnd()) {
      SparkleCode.error(line, "Unterminated string");
      return;
    }

    // The closing ".
    advance();

    // Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }
  
  /**
   * consume decimal or integer number
   */
  private void number() {
    while (isDigit(peek())) advance();

    // Look for a fractional part.
    if (peek() == '.' && isDigit(peekNext())) {
      // Consume the "."
      advance();

      while (isDigit(peek())) advance();
    }

    addToken(NUMBER,
        Double.parseDouble(source.substring(start, current)));
  }
  
  /**
   * consume identifier and see if it is a keyword
   */
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    // See if the identifier is a reserved word.
    String text = source.substring(start, current);

    TokenType type = KEYWORDS.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }
}
