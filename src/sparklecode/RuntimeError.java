/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sparklecode;

/**
 *
 * @author Will
 */
public class RuntimeError extends RuntimeException {
  final Token token;
  
  RuntimeError(Token token, String message){
    super(message);
    this.token = token;
  }
}
