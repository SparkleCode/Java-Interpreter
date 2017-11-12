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

package tool;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Small command-line program to generate the classes used to build an ast
 * @author Will
 */
public class generateAst {

  /**
   * @param args the command line arguments
   * @throws java.io.IOException
   */
  public static void main(String[] args) throws IOException {
    String outputDir = "./src/sparklecode";
    
    defineAst(outputDir, "Expr", Arrays.asList(
      "Assign   : Token name, Expr value",
      "Binary   : Expr left, Token operator, Expr right",
      "Grouping : Expr expression",
      "Literal  : Object value",
      "Unary    : Token operator, Expr right",
      "Variable : Token name"
    ));
    
    defineAst(outputDir, "Stmt", Arrays.asList(
      "Block      : List<Stmt> statements",
      "Expression : Expr expression",
      "Print      : Expr expression",
      "Var        : Token name, Expr initializer"
    ));
  }

  /**
   * Write ast classes and visitor to file
   * @param outputDir directory for file
   * @param baseName name of type (Stmt, Expr, etc.)
   * @param types list of classes to generate
   * @throws IOException 
   */
  private static void defineAst(String outputDir, String baseName, 
          List<String> types) throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    
    PrintWriter writer;
    // overwrite existing file
    writer = new PrintWriter(new FileOutputStream(path, false));
    
    writer.println("/* Generated code - do not modify */");
    writer.println();
    writer.println("package sparklecode;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println();
    writer.println("abstract class " + baseName + " {");
    
    defineVisitor(writer, baseName, types);
    
    types.forEach((type) -> {
      String className = type.split(":")[0].trim();
      String fields = type.split(":")[1].trim();
      defineType(writer, baseName, className, fields);
    });
    
    // accept method
    writer.println("");
    writer.println("  abstract <R> R accept(Visitor<R> visitor);");
    
    writer.println("}");
    writer.close();
  }

  /**
   * 
   * @param writer file stream to write to
   * @param baseName name of type (Stmt, Expr, etc.)
   * @param className name of class to generate
   * @param fieldList list of fields for the class
   */
  private static void defineType(PrintWriter writer, 
          String baseName, String className, String fieldList) {
    writer.println();
    writer.println("  static public class " + className + " extends " + baseName + "{");
    
    // constructor
    writer.println("    " + className + "(" + fieldList + ") {");
    
    // Store parameters in fields.
    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = " + name + ";");
    }

    writer.println("    }");
    
    // Visitor pattern.
    writer.println();
    writer.println("    @Override");
    writer.println("    <R> R accept(Visitor<R> visitor) {");
    writer.println("      return visitor.visit" +
        className + baseName + "(this);");
    writer.println("    }");

    // Fields.
    writer.println();
    for (String field : fields) {
      writer.println("    final " + field + ";");
    }

    writer.println("  }");
  }

  /**
   * define visitor interface for class
   * @param writer file stream to write to
   * @param baseName name of type (Stmt, Expr, etc.)
   * @param types list of classes to generate the visitor for
   */
  private static void defineVisitor(PrintWriter writer, 
          String baseName, List<String> types) {
    writer.println("  public interface Visitor<R> {");
    
    types.forEach(type -> {
      String typeName = type.split(":")[0].trim();
      writer.println("    public R visit" + typeName + baseName + "(" +
              typeName + " " + baseName.toLowerCase() + ");");
    });
    
    writer.println("  }");
  }
}
