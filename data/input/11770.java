public final class SunRsaSign extends Provider {
    private static final long serialVersionUID = 866040293550393045L;
    public SunRsaSign() {
        super("SunRsaSign", 1.7d, "Sun RSA signature provider");
        if (System.getSecurityManager() == null) {
            SunRsaSignEntries.putEntries(this);
        } else {
            Map<Object, Object> map = new HashMap<>();
            SunRsaSignEntries.putEntries(map);
            AccessController.doPrivileged(new PutAllAction(this, map));
        }
    }
}
