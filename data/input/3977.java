public class CloseableDisposerRecord implements DisposerRecord {
    private Closeable closeable;
    public CloseableDisposerRecord(Closeable closeable) {
        this.closeable = closeable;
    }
    public synchronized void dispose() {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
            } finally {
                closeable = null;
            }
        }
    }
}
