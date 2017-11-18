// test file for interpreter

fn makeCounter() {
    var i = 0;
    fn count() {
        i = i + 1;
        return i - 1;
    }
    return count;
}

var counter = makeCounter();
for(var i = 0; i < 20; i = i + 1) {
    print counter();
}