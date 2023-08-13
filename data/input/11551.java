public class OpenConnection {
    public static void main(String[] args) throws IOException {
        System.setSecurityManager( new SecurityManager() );
        URL u = new URL("http:
        try {
            URLConnection con = u.openConnection(Proxy.NO_PROXY);
        } catch (UnknownHostException ex) {
            return;
        }
    }
}
