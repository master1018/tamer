public final class X500Principal implements Serializable, Principal {
    private static final long serialVersionUID = -500463348111345721L;
    public static final String CANONICAL = "CANONICAL"; 
    public static final String RFC1779 = "RFC1779"; 
    public static final String RFC2253 = "RFC2253"; 
    private transient Name dn;
    public X500Principal(byte[] name) {
        super();
        if (name == null) {
            throw new IllegalArgumentException(Messages.getString("auth.00")); 
        }
        try {
            dn = (Name) Name.ASN1.decode(name);
        } catch (IOException e) {
            IllegalArgumentException iae = new IllegalArgumentException(Messages
                    .getString("auth.2B")); 
            iae.initCause(e);
            throw iae;
        }
    }
    public X500Principal(InputStream in) {
        super();
        if (in == null) {
            throw new NullPointerException(Messages.getString("auth.2C")); 
        }
        try {
            dn = (Name) Name.ASN1.decode(in);
        } catch (IOException e) {
            IllegalArgumentException iae = new IllegalArgumentException(Messages
                    .getString("auth.2B")); 
            iae.initCause(e);
            throw iae;
        }
    }
    public X500Principal(String name) {
        super();
        if (name == null) {
            throw new NullPointerException(Messages.getString("auth.00")); 
        }
        try {
            dn = new Name(name);
        } catch (IOException e) {
            IllegalArgumentException iae = new IllegalArgumentException(Messages
                    .getString("auth.2D")); 
            iae.initCause(e);
            throw iae;
        }
    }
    private transient String canonicalName;
    private synchronized String getCanonicalName() {
        if (canonicalName == null) {
            canonicalName = dn.getName(CANONICAL);
        }
        return canonicalName;
    }
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        X500Principal principal = (X500Principal) o;
        return getCanonicalName().equals(principal.getCanonicalName());
    }
    public byte[] getEncoded() {
        byte[] src = dn.getEncoded();
        byte[] dst = new byte[src.length];
        System.arraycopy(src, 0, dst, 0, dst.length);
        return dst;
    }
    public String getName() {
        return dn.getName(RFC2253);
    }
    public String getName(String format) {
        if (CANONICAL.equals(format)) {
            return getCanonicalName();
        }
        return dn.getName(format);
    }
    @Override
    public int hashCode() {
        return getCanonicalName().hashCode();
    }
    @Override
    public String toString() {
        return dn.getName(RFC1779);
    }
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.writeObject(dn.getEncoded());
    }
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        dn = (Name) Name.ASN1.decode((byte[]) in.readObject());
    }
}
