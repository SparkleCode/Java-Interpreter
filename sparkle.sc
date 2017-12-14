class Human {
    feet(){
        return 2
    }
}

class Person < Human {
    init(name) {
        this.name = name
    }
    say() {
        print this.name + " has";
        print super.feet();
        print "feet"
    }
}

var me = Person("John");
me.say();
