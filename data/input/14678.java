class SilentUnchecked {
    void f(Class c) {
        g(c);
    }
    void g(Class<?> c) {
    }
}
