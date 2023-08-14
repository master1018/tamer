public class FileMap {
    public static void main(String[] args) {
        try {
            File f = File.createTempFile("test", null);
            f.deleteOnExit();
            String s = f.getAbsolutePath();
            s = s.startsWith("/") ? s : "/" + s;
            URL url = new URL("file:
            InputStream in = url.openStream();
            in.close();
            url = new URL("file:
            in = url.openStream();
            in.close();
        } catch (java.io.FileNotFoundException fnfe) {
            throw new RuntimeException("failed to recognize localhost");
        } catch (Exception ex) {
            throw new RuntimeException("Unexpected exception: " + ex);
        }
    }
}
