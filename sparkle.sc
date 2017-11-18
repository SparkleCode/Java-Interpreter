// test file for interpreter

fn fibonacci(n) {
    if (n <= 1) return n;
    return fibonacci(n - 2) + fibonacci(n - 1)
}

print fibonacci(20);
print fibonacci