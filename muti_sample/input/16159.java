public class MemoryLeak {
    public static void main(String[] args) {
        for (int i = 0; i < 1100000; i++) {
            ThreadLocal t = new ThreadLocal();
            t.set(new Object());
            t.set(null);
        }
    }
}
