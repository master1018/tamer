public class bug6688203 {
    public static void main(String[] args) {
        FileSystemView.getFileSystemView();
        int startCount = UIManager.getPropertyChangeListeners().length;
        for (int i = 0; i < 100; i++) {
            FileSystemView.getFileSystemView();
        }
        if (startCount != UIManager.getPropertyChangeListeners().length) {
            throw new RuntimeException("New listeners were added into UIManager");
        }
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();
        File file = new File("Some file");
        for (UIManager.LookAndFeelInfo lafInfo : UIManager.getInstalledLookAndFeels()) {
            try {
                UIManager.setLookAndFeel(lafInfo.getClassName());
            } catch (Exception e) {
                System.out.println("Cannot set LAF " + lafInfo.getName());
                continue;
            }
            fileSystemView.getSystemDisplayName(file);
            try {
                Field field = FileSystemView.class.getDeclaredField("useSystemExtensionHiding");
                field.setAccessible(true);
                Boolean value = field.getBoolean(fileSystemView);
                if (value != UIManager.getDefaults().getBoolean("FileChooser.useSystemExtensionHiding")) {
                    throw new RuntimeException("Invalid cached value of the FileSystemView.useSystemExtensionHiding field");
                }
            } catch (Exception e) {
                throw new RuntimeException("Cannot read the FileSystemView.useSystemExtensionHiding field", e);
            }
        }
    }
}
