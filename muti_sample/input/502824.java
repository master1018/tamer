public class HiddenFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter HIDDEN  = new HiddenFileFilter();
    public static final IOFileFilter VISIBLE = new NotFileFilter(HIDDEN);
    protected HiddenFileFilter() {
    }
    public boolean accept(File file) {
        return file.isHidden();
    }
}
