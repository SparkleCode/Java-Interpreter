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
/**
 * Token emited by scanner
 * @author Will
 */
public class Token {
  /**
   * type of token
   */
  public final TokenType type;
  
  /**
   * string scanned to produce token
   */
  public final String lexeme;
  
  /**
   * number / string / etc. value of token
   */
  public final Object literal;
  
  /**
   * where was the token found
   */
  public final int line;
  
  /**
   * create new token
   * @param type type
   * @param lexeme string scanned to make token
   * @param literal data to store on token
   * @param line line token was found in
   */
  public Token(TokenType type, String lexeme, Object literal, int line) {
    this.type = type;
    this.lexeme = lexeme;
    this.literal = literal;
    this.line = line;
  }
  
  /**
   * token to string
   * @return string
   */
  @Override
  public String toString() {
    return "Token(" + type + ", \"" + lexeme + "\""+ (literal==null?"":(", " + literal)) + ")";
  }
}
