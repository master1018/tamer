public class bug6713352 {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                String tempDir = System.getProperty("java.io.tmpdir");
                if (tempDir == null || !new File(tempDir).isDirectory()) {
                    tempDir = System.getProperty("user.home");
                }
                MyFileSystemView systemView = new MyFileSystemView();
                synchronized (systemView) { 
                    new JFileChooser(systemView);
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        System.out.println("Try to get Invokers lock");
                        ShellFolder.getShellFolder(new File(tempDir)).listFiles(true);
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private static class MyFileSystemView extends FileSystemView {
        public File createNewFolder(File containingDir) throws IOException {
            return null;
        }
        public File[] getFiles(File dir, boolean useFileHiding) {
            System.out.println("getFiles start");
            File[] result;
            synchronized (this) {
                result = super.getFiles(dir, useFileHiding);
            }
            System.out.println("getFiles finished");
            return result;
        }
        public synchronized Boolean isTraversable(File f) {
            return super.isTraversable(f);
        }
    }
}
