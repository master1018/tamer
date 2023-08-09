public class Synchronize extends Thread {
    private static final int loop = 500;
    public void run() {
        try {
            System.out.println("attempting to access configuration...");
            Configuration config = Configuration.getConfiguration();
            AppConfigurationEntry[] entries = config.getAppConfigurationEntry
                                                        ("Synchronize");
            config.refresh();
            System.out.println("done accessing configuration...");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }
    public static void main(String[] args) {
        try {
            Synchronize[] threads = new Synchronize[loop];
            for (int i = 0; i < loop; i++) {
                threads[i] = new Synchronize();
            }
            for (int i = 0; i < loop; i++) {
                threads[i].start();
            }
            for (int i = 0; i < loop; i++) {
                threads[i].join();
            }
            System.out.println("test passed");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("test failed");
            throw new SecurityException(e.toString());
        }
    }
}
