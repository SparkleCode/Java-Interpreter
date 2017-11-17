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
   * This is an internal class used to store additional information
   * such as whether the variable has been initialised to each
   * object stored in the environment
   */
  public class Val {
    
    /**
     * The value stored in this class
     */
    private Object value;

    /**
     * Does this class have a value (null is a valid value)
     */
    public boolean isInit;
    
    /**
     * the name this value is referred to in the environment map
     */
    public String name;
    
    /**
     * 
     * @param v the value of this class
     * @param i has this class been initialised
     * @param n the name of this value in the environment map
     */
    public Val(Object v, boolean i, String n) {
      value = v;
      isInit = i;
      name = n;
    }
    /**
     * Getter for the value of this class
     * Throws <code> RuntimeError </code> if the value has not been initialised
     * @param n current token, for error reporting
     * @return value of this class
     */
    public Object value(Token n) {
      if(isInit) return value;
      throw new RuntimeError(n, "Attempt to use uninitialised variable, '" + name + "'. ");
    }
    
    /**
     * Setter for the value of this class
     * sets value and <code> isInit </code> to true
     * @param v value to set this class to
     */
    public void value(Object v) {
      isInit = true;
      value = v;
    }
  }
  
  /**
   * Parent environment
   */
  private final Environment enclosing;
  
  /**
   * Map of value names and values
   */
  private final Map<String,Val> values = new HashMap<>();

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
  public void define(String name, Object value) {
    define(name, value, true);
  }
  
  /**
   * Add new value to map
   * @param name name of variable
   * @param value value of variable
   * @param isInit has this variable been initialised (null in 
   *  <code> value </code> could indicate value of null or not initialised)
   */
  public void define(String name, Object value, boolean isInit) {
    values.put(name, new Val(value, isInit, name));
  }
  
  /**
   * Get value from map
   * @param name variable name token, name string stored in lexeme.
   * @return value from map
   */
  public Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      // get value from current map
      return values.get(name.lexeme).value(name);
    }
    
    // get Val class from parent 
    if(enclosing != null) return enclosing.getInt(name).value(name);
    
    // cannot find variable
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
  
  /**
   * Internally used method, has to be public so this method on the parent.
   * Used to get the Val class out of a map from a lower scope.
   * can be accessed
   * @param name token containing name as lexeme
   * @return Val class of value
   */
  public Val getInt(Token name) {
    if (values.containsKey(name.lexeme)) {
      // get Val class from map
      return values.get(name.lexeme);
    }
    
    // get Val class from parent
    if(enclosing != null) return enclosing.getInt(name);
    
    // cannot find variable
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
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
      Val v = getInt(name);
      v.value(value);
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
