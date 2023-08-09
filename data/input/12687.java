public class bug6945316 {
    public static void main(String[] args) throws Exception {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS) {
            System.out.println("The test is suitable only for Windows OS. Skipped.");
            return;
        }
        Toolkit.getDefaultToolkit();
        ShellFolder.get("fileChooserComboBoxFolders");
        final File tempFile = new File("c:\\");
        for (int i = 0; i < 10000; i++) {
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            final Thread thread = new Thread() {
                public void run() {
                    countDownLatch.countDown();
                    ShellFolder.isFileSystemRoot(tempFile);
                }
            };
            thread.start();
            countDownLatch.await();
            thread.interrupt();
        }
    }
}
