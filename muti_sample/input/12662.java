public class StreamFinalizer {
    private ImageInputStream stream;
    public StreamFinalizer(ImageInputStream stream) {
        this.stream = stream;
    }
    protected void finalize() throws Throwable {
        try {
            stream.close();
        } catch (IOException e) {
        } finally {
            stream = null;
            super.finalize();
        }
    }
}
