public class bug6868611 {
    private static final int COUNT = 1000;
    public static void main(String[] args) throws Exception {
        String tempDirProp = System.getProperty("java.io.tmpdir");
        final String tempDir = tempDirProp == null || !new File(tempDirProp).isDirectory() ?
            System.getProperty("user.home") : tempDirProp;
        System.out.println("Temp directory: " + tempDir);
        for (int i = 0; i < 1000; i++) {
            new File(tempDir, "temp" + i).createNewFile();
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                FileSystemView.getFileSystemView().getFiles(new File(tempDir), false);
            }
        });
        for (int i = 0; i < COUNT; i++) {
            Thread thread = new MyThread(tempDir);
            thread.start();
            Thread.sleep((long) (Math.random() * 100));
            thread.interrupt();
            if (i % 100 == 0) {
                System.out.print("*");
            }
        }
        System.out.println();
        for (int i = 0; i < 1000; i++) {
            new File(tempDir, "temp" + i).delete();
        }
    }
    private static class MyThread extends Thread {
        private final String dir;
        private MyThread(String dir) {
            this.dir = dir;
        }
        public void run() {
            FileSystemView fileSystemView = FileSystemView.getFileSystemView();
            fileSystemView.getFiles(new File(dir), false);
        }
    }
}
