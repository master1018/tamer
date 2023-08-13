public abstract class ResponseCache {
    private static ResponseCache _defaultResponseCache = null;
    private static NetPermission getResponseCachepermission = new NetPermission(
            "getResponseCache"); 
    private static NetPermission setResponseCachepermission = new NetPermission(
            "setResponseCache"); 
    private static void checkGetResponseCachePermission() {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(getResponseCachepermission);
        }
    }
    private static void checkSetResponseCachePermission() {
        SecurityManager sm = System.getSecurityManager();
        if (null != sm) {
            sm.checkPermission(setResponseCachepermission);
        }
    }
    public ResponseCache() {
        super();
    }
    public static ResponseCache getDefault() {
        checkGetResponseCachePermission();
        return _defaultResponseCache;
    }
    public static void setDefault(ResponseCache responseCache) {
        checkSetResponseCachePermission();
        _defaultResponseCache = responseCache;
    }
    public abstract CacheResponse get(URI uri, String rqstMethod,
            Map<String, List<String>> rqstHeaders) throws IOException;
    public abstract CacheRequest put(URI uri, URLConnection conn)
            throws IOException;
}
