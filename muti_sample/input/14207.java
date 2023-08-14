public final class KerberosPrincipal
    implements java.security.Principal, java.io.Serializable {
    private static final long serialVersionUID = -7374788026156829911L;
    public static final int KRB_NT_UNKNOWN =   0;
    public static final int KRB_NT_PRINCIPAL = 1;
    public static final int KRB_NT_SRV_INST =  2;
    public static final int KRB_NT_SRV_HST =   3;
    public static final int KRB_NT_SRV_XHST =  4;
    public static final int KRB_NT_UID = 5;
    private transient String fullName;
    private transient String realm;
    private transient int nameType;
    private static final char NAME_REALM_SEPARATOR = '@';
    public KerberosPrincipal(String name) {
        PrincipalName krb5Principal = null;
        try {
            krb5Principal = new PrincipalName(name, KRB_NT_PRINCIPAL);
        } catch (KrbException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        nameType = KRB_NT_PRINCIPAL;  
        fullName = krb5Principal.toString();
        realm = krb5Principal.getRealmString();
    }
    public KerberosPrincipal(String name, int nameType) {
        PrincipalName krb5Principal = null;
        try {
            krb5Principal  = new PrincipalName(name,nameType);
        } catch (KrbException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        this.nameType = nameType;
        fullName = krb5Principal.toString();
        realm = krb5Principal.getRealmString();
    }
    public String getRealm() {
        return realm;
    }
    public int hashCode() {
        return getName().hashCode();
    }
    public boolean equals(Object other) {
        if (other == this)
            return true;
        if (! (other instanceof KerberosPrincipal)) {
            return false;
        } else {
            String myFullName = getName();
            String otherFullName = ((KerberosPrincipal) other).getName();
            if (nameType == ((KerberosPrincipal)other).nameType &&
                myFullName.equals(otherFullName)) {
                 return true;
            }
        }
        return false;
    }
    private void writeObject(ObjectOutputStream oos)
        throws IOException {
        PrincipalName krb5Principal = null;
        try {
            krb5Principal  = new PrincipalName(fullName,nameType);
            oos.writeObject(krb5Principal.asn1Encode());
            oos.writeObject(krb5Principal.getRealm().asn1Encode());
        } catch (Exception e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
    private void readObject(ObjectInputStream ois)
         throws IOException, ClassNotFoundException {
        byte[] asn1EncPrincipal = (byte [])ois.readObject();
        byte[] encRealm = (byte [])ois.readObject();
        try {
           PrincipalName krb5Principal = new PrincipalName(new
                                                DerValue(asn1EncPrincipal));
           realm = (new Realm(new DerValue(encRealm))).toString();
           fullName = krb5Principal.toString() + NAME_REALM_SEPARATOR +
                         realm.toString();
           nameType = krb5Principal.getNameType();
        } catch (Exception e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
    public String getName() {
        return fullName;
    }
    public int getNameType() {
        return nameType;
    }
    public String toString() {
        return getName();
    }
}
