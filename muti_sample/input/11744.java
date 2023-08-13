public class InitialValue implements Runnable {
    static ThreadLocal<String> other;
    static boolean passed;
    public class MyLocal extends ThreadLocal<String> {
        String val;
        protected String initialValue() {
            other = new ThreadLocal<String>();
            other.set("Other");
            return "Initial";
        }
    }
    public void run() {
        MyLocal l = new MyLocal();
        String s1 = l.get();
        String s2 = other.get();
        if ((s2 != null) && s2.equals("Other")) {
            passed = true;
        }
    }
    public static void main(String[] args) {
        Thread t = new Thread(new InitialValue());
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            throw new RuntimeException("Test Interrupted: failed");
        }
        if (!passed) {
            throw new RuntimeException("Test Failed");
        }
    }
}
