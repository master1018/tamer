public abstract class AuthCacheValue implements Serializable {
    public enum Type {
        Proxy,
        Server
    };
    static protected AuthCache cache = new AuthCacheImpl();
    public static void setAuthCache (AuthCache map) {
        cache = map;
    }
    AuthCacheValue() {}
    abstract Type getAuthType ();
    abstract AuthScheme getAuthScheme();
    abstract String getHost ();
    abstract int getPort();
    abstract String getRealm();
    abstract String getPath();
    abstract String getProtocolScheme();
    abstract PasswordAuthentication credentials();
}
