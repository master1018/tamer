public class EmptyFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter EMPTY = new EmptyFileFilter();
    public static final IOFileFilter NOT_EMPTY = new NotFileFilter(EMPTY);
    protected EmptyFileFilter() {
    }
    public boolean accept(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            return (files == null || files.length == 0);
        } else {
            return (file.length() == 0);
        }
    }
}
