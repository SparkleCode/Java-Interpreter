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

import java.util.List;

/**
 * Representation of user created function object
 * @author Will
 */
public class SparkleFunction implements SparkleCallable {
  private final Stmt.Function declaration;
  private final Environment closure;
  private final boolean isInitializer;

  public SparkleFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.declaration = declaration;
    this.closure = closure;
    this.isInitializer = isInitializer;
  }
  
  
  @Override
  public Object call(Interpreter interp, List<Object> arguments) {
    Environment environment = new Environment(closure);
    for(int i = 0; i < declaration.parameters.size(); i++) {
      environment.define(declaration.parameters.get(i).lexeme,
              arguments.get(i));
    }
    
    try {
      interp.excecuteBlock(declaration.body, environment);
    } catch(Return r) {
      return r.value;
    }
    
    if(isInitializer) return closure.getAt(0, "this");
    
    return null;
  }
  
  public SparkleFunction bind(SparkleInstance instance) {
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new SparkleFunction(declaration, environment, isInitializer);
  };

  @Override
  public int arity() {
    return declaration.parameters.size();
  }
  
  @Override
  public String toString(){
    return "<fn " + declaration.name.lexeme + ">";
  }
}
