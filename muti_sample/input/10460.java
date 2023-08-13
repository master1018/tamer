public abstract class MemoryCredentialsCache extends CredentialsCache {
    private static CredentialsCache getCCacheInstance(PrincipalName p) {
        return null;
    }
    private static CredentialsCache getCCacheInstance(PrincipalName p, File cacheFile) {
        return null;
    }
    public abstract boolean exists(String cache);
    public abstract void update(Credentials c);
    public abstract void save() throws IOException, KrbException;
    public abstract Credentials[] getCredsList();
    public abstract Credentials getCreds(PrincipalName sname, Realm srealm) ;
    public abstract PrincipalName getPrimaryPrincipal();
}
