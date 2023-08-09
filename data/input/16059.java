public class SelectAndClose {
    static Selector selector;
    static boolean awakened = false;
    static boolean closed = false;
    public static void main(String[] args) throws Exception {
        selector = Selector.open();
        new Thread(new Runnable() {
                public void run() {
                    try {
                        selector.select();
                        awakened = true;
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }
            }).start();
        Thread.sleep(3000);
        new Thread(new Runnable() {
                public void run() {
                    try {
                        selector.close();
                        closed = true;
                    } catch (IOException e) {
                        System.err.println(e);
                    }
                }
            }).start();
        Thread.sleep(3000);
        if (!awakened)
            selector.wakeup();
        if (!awakened)
            throw new RuntimeException("Select did not wake up");
        if (!closed)
            throw new RuntimeException("Selector did not close");
    }
}
