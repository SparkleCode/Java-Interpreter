/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 *
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
      "Binary   : Expr left, Token operator, Expr right",
      "Grouping : Expr expression",
      "Literal  : Object value",
      "Unary    : Token operator, Expr right",
      "Variable : Token name"
    ));
    
    defineAst(outputDir, "Stmt", Arrays.asList(
      "Expression : Expr expression",
      "Print      : Expr expression",
      "Var        : Token name, Expr initializer"
    ));
  }

  private static void defineAst(String outputDir, String baseName, 
          List<String> types) throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    
    PrintWriter writer;
    writer = new PrintWriter(new FileOutputStream(path, false));
    
    writer.println("package sparklecode;");
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
