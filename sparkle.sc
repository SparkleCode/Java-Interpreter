// test file for interpreter

var a = 0;
var b = 1;

for(var count = 0; count <= 20; count = count + 1)  {
    print a;
    var temp = a;
    a = b;
    b = temp + a;
}