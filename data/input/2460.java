public class bug6550546 {
    public static void main(String[] args) throws Exception {
        if (OSInfo.getOSType() != OSInfo.OSType.WINDOWS) {
            System.out.println("The test is suitable only for Windows, skipped.");
            return;
        }
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                File[] files = (File[]) ShellFolder.get("fileChooserComboBoxFolders");
                for (File file : files) {
                    if (file instanceof ShellFolder && ((ShellFolder) file).isLink()) {
                        throw new RuntimeException("Link shouldn't be in FileChooser combobox, " + file.getPath());
                    }
                }
            }
        });
    }
}
