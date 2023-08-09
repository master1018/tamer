public class KerberosKey implements SecretKey, Destroyable {
    private static final long serialVersionUID = -4625402278148246993L;
    private KerberosPrincipal principal;
    private int versionNum;
    private KeyImpl key;
    private transient boolean destroyed = false;
    public KerberosKey(KerberosPrincipal principal,
                       byte[] keyBytes,
                       int keyType,
                       int versionNum) {
        this.principal = principal;
        this.versionNum = versionNum;
        key = new KeyImpl(keyBytes, keyType);
    }
    public KerberosKey(KerberosPrincipal principal,
                       char[] password,
                       String algorithm) {
        this.principal = principal;
        key = new KeyImpl(principal, password, algorithm);
    }
    public final KerberosPrincipal getPrincipal() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return principal;
    }
    public final int getVersionNumber() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return versionNum;
    }
    public final int getKeyType() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return key.getKeyType();
    }
    public final String getAlgorithm() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return key.getAlgorithm();
    }
    public final String getFormat() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return key.getFormat();
    }
    public final byte[] getEncoded() {
        if (destroyed)
            throw new IllegalStateException("This key is no longer valid");
        return key.getEncoded();
    }
    public void destroy() throws DestroyFailedException {
        if (!destroyed) {
            key.destroy();
            principal = null;
            destroyed = true;
        }
    }
    public boolean isDestroyed() {
        return destroyed;
    }
    public String toString() {
        if (destroyed) {
            return "Destroyed Principal";
        }
        return "Kerberos Principal " + principal.toString() +
                "Key Version " + versionNum +
                "key "  + key.toString();
    }
    public int hashCode() {
        int result = 17;
        if (isDestroyed()) {
            return result;
        }
        result = 37 * result + Arrays.hashCode(getEncoded());
        result = 37 * result + getKeyType();
        if (principal != null) {
            result = 37 * result + principal.hashCode();
        }
        return result * 37 + versionNum;
    }
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (! (other instanceof KerberosKey)) {
            return false;
        }
        KerberosKey otherKey = ((KerberosKey) other);
        if (isDestroyed() || otherKey.isDestroyed()) {
            return false;
        }
        if (versionNum != otherKey.getVersionNumber() ||
                getKeyType() != otherKey.getKeyType() ||
                !Arrays.equals(getEncoded(), otherKey.getEncoded())) {
            return false;
        }
        if (principal == null) {
            if (otherKey.getPrincipal() != null) {
                return false;
            }
        } else {
            if (!principal.equals(otherKey.getPrincipal())) {
                return false;
            }
        }
        return true;
    }
}
