class Bug {
    void f() {
        Stack<?> stack = null;
        String o = stack.pop();
    }
}
