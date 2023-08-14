class A<T> {
    class B<U> {
        T t;
    }
    static class C {
        {
            B b = null; 
        }
    }
}
