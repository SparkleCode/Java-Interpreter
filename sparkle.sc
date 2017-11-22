class Person {
    init(name) {
        this.name = name
    }
    say() {
        print this.name
    }
}

var me = Person("Me");
me.say();

var you = Person("You");
you.say();
you.name = "Your name";
you.say()
