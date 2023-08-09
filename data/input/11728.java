public class TestPort {
    public static void main(String[] args) {
        URL url = null;
        try {
            url = new URL("ftp", "java.sun.com", -20, "/pub/");
        } catch (MalformedURLException e) {
            url = null;
        }
        if (url != null)
            throw new RuntimeException("MalformedURLException not thrown!");
        try {
            url = new URL("ftp:
        } catch (MalformedURLException e) {
            url = null;
        }
        if (url != null)
            throw new RuntimeException("MalformedURLException not thrown!");
    }
}
