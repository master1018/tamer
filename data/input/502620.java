public class IOUtilities {
    private IOUtilities() {
    }
    public static boolean close(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                return false;
            }
        }
        return true;
    }
}
