class DefaultShellFolder extends ShellFolder {
    DefaultShellFolder(ShellFolder parent, File f) {
        super(parent, f.getAbsolutePath());
    }
    protected Object writeReplace() throws java.io.ObjectStreamException {
        return new File(getPath());
    }
    public File[] listFiles() {
        File[] files = super.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                files[i] = new DefaultShellFolder(this, files[i]);
            }
        }
        return files;
    }
    public boolean isLink() {
        return false; 
    }
    public boolean isHidden() {
        String fileName = getName();
        if (fileName.length() > 0) {
            return (fileName.charAt(0) == '.');
        }
        return false;
    }
    public ShellFolder getLinkLocation() {
        return null; 
    }
    public String getDisplayName() {
        return getName();
    }
    public String getFolderType() {
        if (isDirectory()) {
            return "File Folder"; 
        } else {
            return "File"; 
        }
    }
    public String getExecutableType() {
        return null; 
    }
}
