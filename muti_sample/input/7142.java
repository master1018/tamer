public class B6529759
{
    public static void main(String[] args) {
        try {
            new java.net.URL(null, "a:", new a());
        } catch (Exception e) {
            if (e.getCause() == null) {
                e.printStackTrace();
                throw new RuntimeException("Failed: Exception has no cause");
            }
        }
    }
    static class a extends java.net.URLStreamHandler {
        protected java.net.URLConnection openConnection(java.net.URL u)  {
            throw new UnsupportedOperationException();
        }
        protected void parseURL(java.net.URL u, String spec, int start, int limit) {
            throw new RuntimeException();
        }
    }
}
