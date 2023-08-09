public abstract class A extends Parent implements Parent.Interface, SomeInterface {
    protected A(int a) {
        super();
    }
    public A varargs(Parent... args) {
        return null;
    }
    public void method() {
    }
    public abstract String[] stringArrayMethod() throws java.io.IOException;
    public class Inner {
        int method() {
            return 1;
        }
        int field;
    }
}
