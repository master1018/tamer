class B extends A {
    <U extends Object & R<U>> void f() {}
}
class A {
    <T extends Object & R<T>> void f() {}
}
