public abstract class CookieHandler {
    private static CookieHandler systemWideCookieHandler;
    private final static NetPermission getCookieHandlerPermission = new NetPermission(
            "getCookieHandler"); 
    private final static NetPermission setCookieHandlerPermission = new NetPermission(
            "setCookieHandler"); 
    public static CookieHandler getDefault() {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(getCookieHandlerPermission);
        }
        return systemWideCookieHandler;
    }
    public static void setDefault(CookieHandler cHandler) {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(setCookieHandlerPermission);
        }
        systemWideCookieHandler = cHandler;
    }
    public abstract Map<String, List<String>> get(URI uri,
            Map<String, List<String>> requestHeaders) throws IOException;
    public abstract void put(URI uri, Map<String, List<String>> responseHeaders)
            throws IOException;
}
