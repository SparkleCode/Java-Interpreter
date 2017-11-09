/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
 *
 * @author Will
 */
public class SparkleCode {
  private static final Interpreter interp = new Interpreter();
  static boolean hadError = false;
  static boolean hadRuntimeError;
  static boolean inRepl = false;

  /**
   * @param args the command line arguments
   * @throws java.io.IOException
   */
  public static void main(String[] args) throws IOException {
    if(args.length > 1) {
      System.out.println("Usage: SparkleCode [script]");
    } else if(args.length == 1) {
      runFile(args[0]);
    } else {
      runPrompt();
    }
  }
  
  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
    run(new String(bytes, Charset.defaultCharset()));
    
    if(hadError)System.exit(-1);
    if(hadRuntimeError) System.exit(-2);
  }
  
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
  
  private static void run(String code) {
    Scanner scanner = new Scanner(code);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    // Stop if there was a syntax error.
    if (hadError) return;
    
    interp.interpret(statements);
    System.out.println(new AstPrinter().print(statements));
  }
  
  static void error(int line, String message) {
    report(line, "", message);
  }
  static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }
  
  static private void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
    hadError = false;
  }

  static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() + "[line " + error.token.line + "]"); 
    hadRuntimeError = true;
  }
}
