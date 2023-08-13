public class NullArguments {
    public static void main(String[] args) {
        ProxySelector ps = ProxySelector.getDefault();
        List p = null;
        boolean ok = false;
        if (ps != null) {
            try {
                p = ps.select(null);
            } catch (IllegalArgumentException iae) {
                System.out.println("OK");
                ok = true;
            }
            if (!ok)
                throw new RuntimeException("Expected IllegalArgumentException!");
            URI uri = null;
            try {
                uri = new URI("http:
            } catch (java.net.URISyntaxException use) {
            }
            SocketAddress sa = new InetSocketAddress("localhost", 80);
            IOException ioe = new IOException("dummy IOE");
            ok = false;
            try {
                ps.connectFailed(uri, sa, null);
            } catch (IllegalArgumentException iae) {
                System.out.println("OK");
                ok = true;
            }
            if (!ok)
                throw new RuntimeException("Expected IllegalArgumentException!");
            ok = false;
            try {
                ps.connectFailed(uri, null, ioe);
            } catch (IllegalArgumentException iae) {
                System.out.println("OK");
                ok = true;
            }
            if (!ok)
                throw new RuntimeException("Expected IllegalArgumentException!");
            ok = false;
            try {
                ps.connectFailed(null, sa, ioe);
            } catch (IllegalArgumentException iae) {
                System.out.println("OK");
                ok = true;
            }
            if (!ok)
                throw new RuntimeException("Expected IllegalArgumentException!");
        }
    }
}
