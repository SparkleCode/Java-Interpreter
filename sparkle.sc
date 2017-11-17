// test file for interpreter

var a = 0;
var b = 1;

var start = clock();
print "Time start: ";
print start;

for(var count = 0; count <= 200; count = count + 1)  {
    print a;
    var temp = a;
    a = b;
    b = temp + a;
}

var end = clock();
print "Time end: " ;
print end;
print "Time total: ";
print end - start;