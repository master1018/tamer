public abstract class Identity implements Principal, Serializable {
    private static final long serialVersionUID = 3609922007826600659L;
    private String name;
    private PublicKey publicKey;
    private String info = "no additional info"; 
    private IdentityScope scope;
    private Vector<Certificate> certificates;
    protected Identity() {
    }
    public Identity(String name) {
        this.name = name;
    }
    public Identity(String name, IdentityScope scope)
            throws KeyManagementException {
        this(name);
        if (scope != null) {
            scope.addIdentity(this);
            this.scope = scope;
        }
    }
    public void addCertificate(Certificate certificate)
            throws KeyManagementException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("addIdentityCertificate"); 
        }
        PublicKey certPK = certificate.getPublicKey();
        if (publicKey != null) {
            if (!checkKeysEqual(publicKey, certPK)) {
                throw new KeyManagementException(Messages.getString("security.13")); 
            }
        } else {
            publicKey = certPK;
        }
        if (certificates == null) {
            certificates = new Vector<Certificate>();
        }
        certificates.add(certificate);
    }
    private static boolean checkKeysEqual(PublicKey pk1, PublicKey pk2) {
        String format1 = pk1.getFormat();
        String format2;
        if ((pk2 == null)
                || (((format2 = pk2.getFormat()) != null) ^ (format1 != null))
                || ((format1 != null) && !format1.equals(format2))) {
            return false;
        }
        return Arrays.equals(pk1.getEncoded(), pk2.getEncoded());
    }
    public void removeCertificate(Certificate certificate)
            throws KeyManagementException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("removeIdentityCertificate"); 
        }
        if (certificates != null) {
            if (!certificates.contains(certificate)) {
                throw new KeyManagementException("Certificate not found");
            }
            certificates.removeElement(certificate);
        }
    }
    public Certificate[] certificates() {
        if (certificates == null) {
            return new Certificate[0];
        }
        Certificate[] ret = new Certificate[certificates.size()];
        certificates.copyInto(ret);
        return ret;
    }
    protected boolean identityEquals(Identity identity) {
        if (!name.equals(identity.name)) {
            return false;
        }
        if (publicKey == null) {
            return (identity.publicKey == null);
        }
        return checkKeysEqual(publicKey, identity.publicKey);
    }
    public String toString(boolean detailed) {
        String s = toString();
        if (detailed) {
            s += " " + info; 
        }
        return s;
    }
    public final IdentityScope getScope() {
        return scope;
    }
    public void setPublicKey(PublicKey key) throws KeyManagementException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setIdentityPublicKey"); 
        }
        if ((scope != null) && (key != null)) {
            Identity i = scope.getIdentity(key);
            if ((i != null) && (i != this)) {
                throw new KeyManagementException(Messages.getString("security.14")); 
            }
        }
        this.publicKey = key;
        certificates = null;
    }
    public PublicKey getPublicKey() {
        return publicKey;
    }
    public void setInfo(String info) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("setIdentityInfo"); 
        }
        this.info = info;
    }
    public String getInfo() {
        return info;
    }
    @Override
    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Identity)) {
            return false;
        }
        Identity i = (Identity) obj;
        if ((name == i.name || (name != null && name.equals(i.name)))
                && (scope == i.scope || (scope != null && scope.equals(i.scope)))) {
            return true;
        }
        return identityEquals(i);
    }
    public final String getName() {
        return name;
    }
    @Override
    public int hashCode() {
        int hash = 0;
        if (name != null) {
            hash += name.hashCode();
        }
        if (scope != null) {
            hash += scope.hashCode();
        }
        return hash;
    }
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkSecurityAccess("printIdentity");
        }
        String s = (this.name == null ? "" : this.name);
        if (scope != null) {
            s += " [" + scope.getName() + "]";
        }
        return s;
    }
}
