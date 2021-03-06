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
import java.util.Map;

/**
 *
 * @author Will
 */
public class SparkleClass implements SparkleCallable {
  public final String name;
  public final Map<String, SparkleFunction> methods;
  public final SparkleClass superclass;

  public SparkleClass(String name, SparkleClass superclass, Map<String, SparkleFunction> methods) {
    this.name = name;
    this.methods = methods;
    this.superclass = superclass;
  }
  
  @Override
  public String toString() {
    return name;
  }

  @Override
  public Object call(Interpreter interp, List<Object> arguments) {
    SparkleInstance instance = new SparkleInstance(this);
    
    SparkleFunction init = methods.get("init");
    if(init != null){
      init.bind(instance).call(interp, arguments);
    }
    return instance;
  }

  @Override
  public int arity() {
    SparkleFunction init = methods.get("init");
    if(init == null) return 0;
    return init.arity();
  }

  public SparkleFunction findMethod(SparkleInstance instance, String name) {
    if(methods.containsKey(name)) {
      return methods.get(name).bind(instance);
    }
    if(superclass != null){
      return superclass.findMethod(instance, name);
    }
    return null;
  }
}
