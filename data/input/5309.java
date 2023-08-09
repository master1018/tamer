public class Test4530962 {
    public static void main(String[] args) throws Exception {
        try {
            test(new A(), new Y(), new Y());
            throw new Error("exception expected");
        }
        catch (NoSuchMethodException exception) {
        }
        catch (Exception exception) {
            throw new Error("unexpected exception", exception);
        }
        test(new B(), new Y(), new Y());
        test(new C(), new Z(), new Z());
        test(new D(), new Z(), new Z());
        test(new E(), new Z(), new Z());
    }
    private static void test(Object target, Object... params) throws Exception {
        new Statement(target, "m", params).execute();
    }
    public static class A {
        public void m(X x1, X x2) {
            throw new Error("A.m(X,X) should not be called");
        }
        public void m(X x1, Y y2) {
            throw new Error("A.m(X,Y) should not be called");
        }
        public void m(Y y1, X x2) {
            throw new Error("A.m(Y,X) should not be called");
        }
    }
    public static class B {
        public void m(X x1, X x2) {
            throw new Error("B.m(X,X) should not be called");
        }
        public void m(X x1, Y y2) {
        }
    }
    public static class C {
        public void m(Y y1, Y y2) {
        }
        public void m(X x1, X x2) {
            throw new Error("C.m(X,X) should not be called");
        }
    }
    public static class D {
        public void m(X x1, X x2) {
            throw new Error("D.m(X,X) should not be called");
        }
        public void m(Y y1, Y y2) {
        }
    }
    public static class E {
        public void m(X x1, X x2) {
        }
    }
    public static class X {
    }
    public static class Y extends X {
    }
    public static class Z extends Y {
    }
}
