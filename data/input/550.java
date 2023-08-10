public class NoFiles implements FileFilter {
    public static final NoFiles NO_FILES = new NoFiles();
    @Override
    public boolean accept(File file) {
        if (file.isDirectory()) {
            return true;
        }
        return false;
    }
}
