public class EagerHttpFallback {
    static final int INITIAL_PORT = 7070;
    static final int FALLBACK_PORT = 7071;
    public static void main(String[] args) throws Exception {
        System.setProperty("http.proxyHost", "127.0.0.1");
        System.setProperty("http.proxyPort", Integer.toString(FALLBACK_PORT));
        System.setProperty("sun.rmi.transport.proxy.eagerHttpFallback",
                           "true");
        LocateRegistry.createRegistry(FALLBACK_PORT);
        try {
            LocateRegistry.getRegistry(INITIAL_PORT).list();
        } catch (Exception e) {
            System.err.println(
                "call on registry stub with port " + INITIAL_PORT +
                "did not successfully perform HTTP fallback to " +
                FALLBACK_PORT);
            throw e;
        }
    }
}
