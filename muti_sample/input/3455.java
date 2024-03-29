public abstract class CharsetProvider {
    protected CharsetProvider() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
            sm.checkPermission(new RuntimePermission("charsetProvider"));
    }
    public abstract Iterator<Charset> charsets();
    public abstract Charset charsetForName(String charsetName);
}
