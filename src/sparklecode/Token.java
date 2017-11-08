/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sparklecode;
/**
 *
 * @author Will
 */
public class Token {
  final TokenType type;
  final String lexeme;
  final Object literal;
  final int line;
  
  Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }
  
  @Override
  public String toString() {
    return "Token(" + type + ", \"" + lexeme + "\""+ (literal==null?"":(", " + literal)) + ")";
  }
}
