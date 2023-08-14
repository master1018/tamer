public class bug6342301 {
    private static String tempDir;
    public static void main(String[] args) throws Exception {
        tempDir = System.getProperty("java.io.tmpdir");
        if (tempDir.length() == 0) { 
            tempDir = System.getProperty("user.home");
        }
        System.out.println("Temp directory: " + tempDir);
        UIManager.setLookAndFeel(new MetalLookAndFeel());
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                HackedFileChooser openChooser = new HackedFileChooser();
                openChooser.setUI(new MetalFileChooserUI(openChooser));
                openChooser.setCurrentDirectory(new File(tempDir));
            }
        });
    }
    private static class HackedFileChooser extends JFileChooser {
        public void setUI(ComponentUI newUI) {
            super.setUI(newUI);
        }
    }
}
