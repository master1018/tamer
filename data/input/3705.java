public final class Sun extends Provider {
    private static final long serialVersionUID = 6440182097568097204L;
    private static final String INFO = "SUN " +
    "(DSA key/parameter generation; DSA signing; SHA-1, MD5 digests; " +
    "SecureRandom; X.509 certificates; JKS keystore; PKIX CertPathValidator; " +
    "PKIX CertPathBuilder; LDAP, Collection CertStores, JavaPolicy Policy; " +
    "JavaLoginConfig Configuration)";
    public Sun() {
        super("SUN", 1.7, INFO);
        if (System.getSecurityManager() == null) {
            SunEntries.putEntries(this);
        } else {
            Map<Object, Object> map = new LinkedHashMap<>();
            SunEntries.putEntries(map);
            AccessController.doPrivileged(new PutAllAction(this, map));
        }
    }
}
