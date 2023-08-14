public class Test6932496 {
    static class A {
        volatile boolean flag = false;
    }
    static void m() {
        try {
        } finally {
            A a = new A();
            a.flag = true;
        }
    }
    static public void main(String[] args) {
        m();
    }
}
