/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sparklecode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static sparklecode.TokenType.*;

/**
 *
 * @author Will
 */
public class Scanner {
  private final String source;
  private final List<Token> tokens;
  
  private int start = 0;
  private int current = 0;
  private int line = 1;
  
  Scanner(String code) {
    this.tokens = new ArrayList<>();
    source = code;
  }
  
  List<Token> scanTokens() {
    while(!isAtEnd()) {
      start = current;
      scanToken();
    }
    
    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }
  
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
          int level = 0;
          while(!isAtEnd()) {
            if(peek() == '/' && peekNext() == '*'){
              level++;
              advance();
            }
            if(peek() == '*' && peekNext() == '/'){
              level--;
              if(level <= -1) break;
              advance();
            }
            advance();
            
            if(isAtEnd()){
              SparkleCode.error(start, "Unterminated block comment");
            }
          }
          match('*');
          match('/');
        } else {
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
  
  private boolean isAtEnd() {
    return current >= source.length();
  }
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
           (c >= 'A' && c <= 'Z') ||
            c == '_';
  }
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }
  
  private char advance() {
    current++;
    return source.charAt(current - 1);
  }
  
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }
  
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }
  
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }
  
  private void addToken(TokenType type) {
    addToken(type, null);
  }
  
  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
  
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
  
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    // See if the identifier is a reserved word.
    String text = source.substring(start, current);

    TokenType type = KEYWORDS.get(text);
    if (type == null) type = IDENTIFIER;
    addToken(type);
  }
}
