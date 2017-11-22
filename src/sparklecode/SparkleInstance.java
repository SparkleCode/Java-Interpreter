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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Will
 */
public class SparkleInstance {
  private final SparkleClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  public SparkleInstance(SparkleClass klass) {
    this.klass = klass;
  }
  
  public Object get(Token name) {
    if(fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }
    
    SparkleFunction method = klass.findMethod(this, name.lexeme);
    if(method != null) return method;
    
    throw new RuntimeError(name, "Undefined property '" + name.lexeme + "'. ");
  }
  
  @Override
  public String toString() {
    return klass.name + " Instance";
  }

  void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }
}
