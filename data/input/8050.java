public class ExitOnThrow {
    private static volatile boolean ran = false;
    public static void main(String[] args) throws Exception {
        Cleaner.create(new Object(),
                       new Runnable() {
                               public void run() {
                                   ran = true;
                                   throw new RuntimeException("Foo!");
                               }
                           });
        while (!ran) {
            System.gc();
            Thread.sleep(100);
        }
        System.exit(0);
    }
}
