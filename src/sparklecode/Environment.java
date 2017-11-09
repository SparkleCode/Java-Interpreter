/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sparklecode;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Will
 */
public class Environment {
  public class Val {
    private Object value;
    public boolean isInit;
    public String name;
    Val(Object v, boolean i, String n) {
      value = v;
      isInit = i;
      name = n;
    }
    Object value(Token n) {
      if(isInit) return value;
      throw new RuntimeError(n, "Attempt to use uninitialised variable, '" + name + "'. ");
    }
    void value(Token n, Object v) {
      isInit = true;
      value = v;
    }
  }
  final Environment enclosing;
  private final Map<String,Val> values = new HashMap<>();

  public Environment() {
    enclosing = null;
  }
  
  public Environment(Environment e) {
    enclosing = e;
  }
  
  void define(String name, Object value, boolean isInit) {
    values.put(name, new Val(value, isInit, name));
  }
  
  Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme).value(name);
    }
    
    if(enclosing != null) return enclosing.getInt(name).value(name);
    
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
  public Val getInt(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }
    
    if(enclosing != null) return enclosing.getInt(name);
    
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
  
  void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      Val v = getInt(name);
      v.value(name, value);
      return;
    }

    if(enclosing != null){
      enclosing.assign(name, value);
      return;
    }
    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }
}
