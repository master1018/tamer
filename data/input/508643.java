public class FileFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter FILE = new FileFileFilter();
    protected FileFileFilter() {
    }
    public boolean accept(File file) {
        return file.isFile();
    }
}
