public final class KerberosString {
    public static final boolean MSNAME = AccessController.doPrivileged(
            new GetBooleanAction("sun.security.krb5.msinterop.kstring"));
    private final String s;
    public KerberosString(String s) {
        this.s = s;
    }
    public KerberosString(DerValue der) throws IOException {
        if (der.tag != DerValue.tag_GeneralString) {
            throw new IOException(
                "KerberosString's tag is incorrect: " + der.tag);
        }
        s = new String(der.getDataBytes(), MSNAME?"UTF8":"ASCII");
    }
    public String toString() {
        return s;
    }
    public DerValue toDerValue() throws IOException {
        return new DerValue(DerValue.tag_GeneralString,
                s.getBytes(MSNAME?"UTF8":"ASCII"));
    }
}
