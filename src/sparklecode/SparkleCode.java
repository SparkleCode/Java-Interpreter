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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * wrapper around the other classes, to provide a 
 * command line usage of the interpreter
 * @author Will
 */
public class SparkleCode {
  /**
   * interpreter instance, stores state for repl
   */
  private static final Interpreter INTERP = new Interpreter();
  
  /**
   * has the code had a parse error
   */
  static boolean hadError = false;
  
  /**
   * has the code errored during runtime
   */
  static boolean hadRuntimeError;
  
  /**
   * is the code from a repl or a file
   */
  static boolean inRepl = false;

  /**
   * main method to call interpreter
   * @param args the command line arguments
   * @throws java.io.IOException
   */
  public static void main(String[] args) throws IOException {
    runFile("sparkle.sc");
    /*
    if(args.length > 1) {
      System.out.println("Usage: SparkleCode [script]");
    } else if(args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }*/
  }
  
  /**
   * run provided file
   * @param path path to file
   * @throws IOException 
   */
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    
    if(hadError)System.exit(-1);
    if(hadRuntimeError) System.exit(-2);
  }
  
  /**
   * run repl in command line
   * @throws IOException 
   */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);
    
    inRepl = true;
    
    for(;;) {
      System.out.print("> ");
      run(reader.readLine());
      
      hadError = false;
    }
  }
  
  /**
   * run code string
   * @param code code
   */
  private static void run(String code) {
    Scanner scanner = new Scanner(code);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    // Stop if there was a syntax error.
    if (hadError) return;
    
    Resolver resolver = new Resolver(INTERP);
    resolver.resolve(statements);
    
    // Stop if there was a resolution error.
    if (hadError) return;
    
    System.out.println(new AstPrinter().print(statements));
    INTERP.interpret(statements);
  }
  
  /**
   * throw parse error with no token, used during scanning
   * @param line line where error occurred
   * @param message error message
   */
  static void error(int line, String message) {
    report(line, "", message);
  }
  
  /**
   * throw parse error at token
   * @param token error token
   * @param message error message
   */
  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
  /**
   * report parse error
   * @param line which line was the error on
   * @param where string representing code location
   * @param message error message
   */
  static private void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  /**
   * report runtime error
   * @param error error thrown
   */
  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "[line " + error.token.line + "]"); 
    hadRuntimeError = true;
  }
}
