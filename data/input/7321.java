class Warn3 {
    void f(Class<?>... args) {}
    void g(Class... args) {
        f(args);
    }
}
