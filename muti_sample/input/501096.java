public class CustomPasswordDigest implements PasswordDigest {
    private PasswordDigest mPasswordDigest;
    public CustomPasswordDigest(String pluginPath, String implClass) throws ImException {
        PathClassLoader classLoader = new PathClassLoader(pluginPath,
                getClass().getClassLoader());
        try {
            Class<?> cls = classLoader.loadClass(implClass);
            mPasswordDigest = (PasswordDigest)cls.newInstance();
        } catch (ClassNotFoundException e) {
            throw new ImException(e);
        } catch (IllegalAccessException e) {
            throw new ImException(e);
        } catch (InstantiationException e) {
            throw new ImException(e);
        }
    }
    public String digest(String schema, String nonce, String password) throws ImException {
        return mPasswordDigest.digest(schema, nonce, password);
    }
    public String[] getSupportedDigestSchema() {
        return mPasswordDigest.getSupportedDigestSchema();
    }
}
