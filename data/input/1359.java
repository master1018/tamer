public class ExampleFileSystemView extends FileSystemView {
    public File createNewFolder(File containingDir) throws IOException {
        File result = new File(containingDir, "New folder");
        if (result.exists()) {
            throw new IOException("Directory 'New folder' exists");
        }
        if (!result.mkdir()) {
            throw new IOException("Cannot create directory");
        }
        return result;
    }
    @Override
    public File[] getRoots() {
        return new File[] { getHomeDirectory() };
    }
    @Override
    public String getSystemDisplayName(File f) {
        String displayName = super.getSystemDisplayName(f);
        return f.isDirectory() ? displayName.toUpperCase() : displayName.
                toLowerCase();
    }
}
