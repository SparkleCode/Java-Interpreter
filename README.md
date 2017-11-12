# Sparkle Code

based on the Lox language (http://www.craftinginterpreters.com/control-flow.html

## building
build using netbeans java project

## Todo

  - not instead of !
  - block statement only after if, while, etc.
  - proper support for optional trailing semicolon (currently mandatory unless last token in file)

<!--
# Features
continuations
decorators
dynamic typing
exeptions
extension methods
first class functions
functional
functions can be called infix
hashmaps
infinite recursion
keyword arguments
module system
multiple return, assignment
operators are functions
strong typing
traits

# inspiration
ruby
rust
python
c++
javascript

# implementation stages
## 1 - core language
 - continuations
 - infinite recursion
 - strong typing
 - dynamic typing
 - first class functions
 - keyword arguments
 - operators are functions

## 2 - syntaxical additions
 - const
 - protected, public, private
 - string interpolation
 - split functions
 - adding operators
 - functions can be called infix
 - multiple return, assignment
 - extension methods
 - module system
 - exeptions
 - decorators
 - traits
 - operator definition

## 3 - standard library
 - functional
 - hashmaps

## 4 - Future
 - pattern matching
 - bignums
 - grapheme cluster strings
 - function overloading and operator overloading

# examples 
```
print("hello, world");

let x = fn(hello = "world") {  //assign x to un-named lambda, set default value of hello to world
  print("hello, ${hello}");
};

let x = fn loop(n) { //assign x to named lambda, for recursion, name not accessible outside function
  if n > 1 then {
    loop(n - 1);
  }
  print(n);
}

[1,2,3].reduce(\+,0); //operator as function

namespace funcs {
  fn hi(name) {
    print(name);
  };
}
funcs.hi("Bob");

fn run(code) {
  code();
}
run {  // block after function name adds block as last argument to function, if only block parents not required
  print("hi");
}

fn run2(arg,code){
  code(arg);
}
run2(5) { |arg| // block takes input
  print(arg);
}
run2(5,fn(arg) { // equivalent to above, blocks are just shorter and look better, only for use as argument
  print(arg); 
});
times(2) { |i| }

function i(cond, block, |e|, block2) { // |e| in function definiton defines keyword, not argument
  if cond then { block() } else { block2() };
}
i(5 == 6){

} e {

}

while(true) {

}
repeat(5) { |i|
  //1,2,3,4,5
}
repeat(5,2,-1) { |i|
  //5,4,3,2
}

@infix(2) // precedence 2
fn add(a, b) { 
  return a + b;
}
5 add 6;

@prefix(2)
fn not(a) { // cannot use multi part function
  return !a;
}
not true;

@mixfix(3,2) // 3 args, mixfix ignores function name, uses function keyword
fn ife(a,|?|,b,|:|,c){
  if a then { return b } else { return c }
}
5==6 ? 5 : 6;

let const x = 5; // read only

// all classes inherit from object
class x {
  let x = 5;  // private by default
  let private x = 5; // only accessible from inside type
  let public x = 5;  // accessible outside and inside type
  let protected x = 5; // accessible inside and by extended type
  let private const x = 5;
  let public const x = 5;
  let protected const x = 5;
  constructor(self,super) { // run when type created

  }
  fn x(self, super) { ... } // not in any trait
}

impl iterable for x { //class x implements iterable trait
  fn next(self, super) { ... }
};

type y extends x {
  constructor(self,super) { // running super initialises x
    // self and super are always passed as first two arguments to function inside type
    
  }
}

x::iterable.next() //next from iterable trait
x.next() //next from any trait or not in a trait

trait iterable {  // like interface in java
  fn next() {}; // iterable types require next method
}

extend type x with {}; //add methods to type x
extend trait iterable with {}; //add methods to any type with trait iterable


export(x,y); //export x and y from file as namespace
let fs = import('fs'); //get exported namespace from module

throw "ERROR";

try {

} catch {

};

let x = fn(text,sep = " ") {
  ...
};

x(sep = ":", text = "hi"); //keyword arguments

let x = Hash();
x.Add("5","6");

/*
  async calls can use continuations to avoid callbacks
*/
let x = fetch("https://example.com");
if(x.Errored) ...
x.Data ...

{

} //blocks for scope, let is block scoped

let x = fn {
  return 5, 6;  //return 2 values
}
let y = x(); // y is array
let a, b = x(); // a is 5, b is 6
a, b = b, a // swap values - a and b already defined so let is not needed
```-->