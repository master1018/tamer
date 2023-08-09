public class InnerTarg {
    static class TheInner {
        void foo() {
            System.out.println("foo");
        }
    }
    public static void main(String args[]) {
        (new InnerTarg()).boo();
    }
    void boo() {
        try {
            ClassLoader.getSystemClassLoader().loadClass("InnerTarg$TheInner");
        } catch (Exception exc) {
            System.out.println("Loading class got " + exc);
        }
        go();
    }
    void go() {
        System.out.println("Inner completing!");
    }
}
