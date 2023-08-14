public class BadDottedIPAddress {
    public static void main(String args[]) throws Exception {
        String host = "999.999.999.999";
        boolean exc_thrown = false;
        try {
            InetAddress ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            exc_thrown = true;
        }
        if (!exc_thrown) {
            throw new Exception("UnknownHostException was not thrown for: "
                + host);
        }
        host = "[]";
        exc_thrown = false;
        try {
            InetAddress ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            exc_thrown = true;
        } catch (Exception e) {
        }
        if (!exc_thrown) {
            throw new Exception("UnknownHostException was not thrown for: "
                + host);
        }
        host = "[127.0.0.1]";
        exc_thrown = false;
        try {
            InetAddress ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            exc_thrown = true;
        } catch (Exception e) {
        }
        if (!exc_thrown) {
            throw new Exception("UnknownHostException was not thrown for: "
                + host);
        }
        host = "[localhost]";
        exc_thrown = false;
        try {
            InetAddress ia = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            exc_thrown = true;
        } catch (Exception e) {
        }
        if (!exc_thrown) {
            throw new Exception("UnknownHostException was not thrown for: "
                + host);
        }
    }
}
