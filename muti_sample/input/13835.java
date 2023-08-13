public class CertificateSerialNumber implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.serialNumber";
    public static final String NAME = "serialNumber";
    public static final String NUMBER = "number";
    private SerialNumber        serial;
    public CertificateSerialNumber(BigInteger num) {
      this.serial = new SerialNumber(num);
    }
    public CertificateSerialNumber(int num) {
      this.serial = new SerialNumber(num);
    }
    public CertificateSerialNumber(DerInputStream in) throws IOException {
        serial = new SerialNumber(in);
    }
    public CertificateSerialNumber(InputStream in) throws IOException {
        serial = new SerialNumber(in);
    }
    public CertificateSerialNumber(DerValue val) throws IOException {
        serial = new SerialNumber(val);
    }
    public String toString() {
        if (serial == null) return "";
        return (serial.toString());
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        serial.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof SerialNumber)) {
            throw new IOException("Attribute must be of type SerialNumber.");
        }
        if (name.equalsIgnoreCase(NUMBER)) {
            serial = (SerialNumber)obj;
        } else {
            throw new IOException("Attribute name not recognized by " +
                                "CertAttrSet:CertificateSerialNumber.");
        }
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(NUMBER)) {
            return (serial);
        } else {
            throw new IOException("Attribute name not recognized by " +
                                "CertAttrSet:CertificateSerialNumber.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(NUMBER)) {
            serial = null;
        } else {
            throw new IOException("Attribute name not recognized by " +
                                "CertAttrSet:CertificateSerialNumber.");
        }
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(NUMBER);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
}
