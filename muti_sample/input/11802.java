class ShellFolderManager {
    public ShellFolder createShellFolder(File file) throws FileNotFoundException {
        return new DefaultShellFolder(null, file);
    }
    public Object get(String key) {
        if (key.equals("fileChooserDefaultFolder")) {
            File homeDir = new File(System.getProperty("user.home"));
            try {
                return createShellFolder(homeDir);
            } catch (FileNotFoundException e) {
                return homeDir;
            }
        } else if (key.equals("roots")) {
            return File.listRoots();
        } else if (key.equals("fileChooserComboBoxFolders")) {
            return get("roots");
        } else if (key.equals("fileChooserShortcutPanelFolders")) {
            return new File[] { (File)get("fileChooserDefaultFolder") };
        }
        return null;
    }
    public boolean isComputerNode(File dir) {
        return false;
    }
    public boolean isFileSystemRoot(File dir) {
        if (dir instanceof ShellFolder && !((ShellFolder) dir).isFileSystem()) {
            return false;
        }
        return (dir.getParentFile() == null);
    }
    protected ShellFolder.Invoker createInvoker() {
        return new DirectInvoker();
    }
    private static class DirectInvoker implements ShellFolder.Invoker {
        public <T> T invoke(Callable<T> task) throws Exception {
            return task.call();
        }
    }
}
