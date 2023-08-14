public class bug6741890 {
    private static final Object mux = new Object();
    private static final int COUNT = 100000;
    public static void main(String[] args) throws Exception {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS) {
            System.out.println("The test is applicable only for Windows. Skipped.");
            return;
        }
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir.length() == 0) { 
            tmpDir = System.getProperty("user.home");
        }
        final ShellFolder tmpFile = ShellFolder.getShellFolder(new File(tmpDir));
        System.out.println("Temp directory: " + tmpDir);
        System.out.println("Stress test was run");
        Thread thread = new Thread() {
            public void run() {
                while (!isInterrupted()) {
                    ShellFolder.invoke(new Callable<Void>() {
                        public Void call() throws Exception {
                            synchronized (mux) {
                                tmpFile.isFileSystem();
                                tmpFile.isLink();
                            }
                            return null;
                        }
                    });
                }
            }
        };
        thread.start();
        for (int i = 0; i < COUNT; i++) {
            synchronized (mux) {
                clearField(tmpFile, "cachedIsLink");
                clearField(tmpFile, "cachedIsFileSystem");
            }
            tmpFile.isFileSystem();
            tmpFile.isLink();
        }
        thread.interrupt();
        thread.join();
        System.out.println("Test passed successfully");
    }
    private static void clearField(Object o, String fieldName) throws Exception {
        Field field = o.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(o, null);
    }
}
