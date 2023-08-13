public class bug4150029 extends JApplet {
    private boolean res;
    public void init() {
        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir.length() == 0) {
            tmpDir = System.getProperty("user.home");
        }
        System.out.println("Temp directory: " + tmpDir);
        File testDir = new File(tmpDir, "testDir");
        testDir.mkdir();
        File subDir = new File(testDir, "subDir");
        subDir.mkdir();
        System.out.println("Created directory: " + testDir);
        System.out.println("Created sub-directory: " + subDir);
        JFileChooser fileChooser = new JFileChooser(testDir);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        try {
            res = fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION ||
                    testDir.getCanonicalPath().equals(fileChooser.getSelectedFile().getCanonicalPath());
        } catch (IOException e) {
            res = false;
            e.printStackTrace();
        }
        try {
            subDir.delete();
            testDir.delete();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    public void destroy() {
        if (!res) {
            throw new RuntimeException("BackSpace keyboard button does not lead to parent directory");
        }
    }
}
