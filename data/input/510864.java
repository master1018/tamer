public class CanWriteFileFilter extends AbstractFileFilter implements Serializable {
    public static final IOFileFilter CAN_WRITE = new CanWriteFileFilter();
    public static final IOFileFilter CANNOT_WRITE = new NotFileFilter(CAN_WRITE);
    protected CanWriteFileFilter() {
    }
    public boolean accept(File file) {
        return file.canWrite();
    }
}
