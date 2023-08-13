public class SpecifyHandler {
    public static void main(String args[]) throws Exception {
        URLStreamHandler handler = getFileHandler();
        URL url1 = new URL("file", "", -1, "/bogus/index.html", handler);
        URL url2 = new URL(null, "file:
    }
    private static URLStreamHandler getFileHandler() throws Exception {
        Class c = Class.forName("sun.net.www.protocol.file.Handler");
        return (URLStreamHandler)c.newInstance();
    }
}
