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
 * Where all values and their names are stored.
 * Contains wrapper methods around get, define and assign to check for errors.
 * @author Will
 */
public class Environment {  
  /**
   * Parent environment
   */
  private final Environment enclosing;
  
  /**
   * Map of value names and values
   */
  private final Map<String, Object> values = new HashMap<>();

  /**
   * new global environment
   */
  public Environment() {
    enclosing = null;
  }
  
  /**
   * new environment that shadows its variables over parent, can also
   * access values from parent
   * @param e parent environment
   */
  public Environment(Environment e) {
    enclosing = e;
  }
  
  /**
   * Add new value to map, assuming the value has been initialised
   * @param name name of variable
   * @param value value of variable
   */
  
  /**
   * Add new value to map
   * @param name name of variable
   * @param value value of variable
   */
  public void define(String name, Object value) {
    values.put(name, value);
  }
  
  /**
   * Get value from map
   * @param name variable name token, name string stored in lexeme.
   * @return value from map
   */
  public Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      // get value from current map
      return values.get(name.lexeme);
    }
    
    // get object from parent 
    if(enclosing != null) return enclosing.get(name);
    
    // cannot find variable
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'. ");
  }
  
  public Object getAt(int distance, String name) {
    return ancestor(distance).values.get(name);
  }
  
  public Environment ancestor(int distance) {
    Environment environment = this;
    for(int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }
    return environment;
  }
  
  public void assignAt(int distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }
  
  /**
   * set value of variable in map.
   * throws error if the value cannot be found
   * @param name token containing name as lexeme
   * @param value value to set the variable to
   */
  public void assign(Token name, Object value) {
    // value in this environment
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }
    
    // value not in this environment but might be in parent
    if(enclosing != null){
      enclosing.assign(name, value);
      return;
    }
    
    // cannot find value
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
}
