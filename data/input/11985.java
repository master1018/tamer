public class AuthorityKeyIdentifierExtension extends Extension
implements CertAttrSet<String> {
    public static final String IDENT =
                         "x509.info.extensions.AuthorityKeyIdentifier";
    public static final String NAME = "AuthorityKeyIdentifier";
    public static final String KEY_ID = "key_id";
    public static final String AUTH_NAME = "auth_name";
    public static final String SERIAL_NUMBER = "serial_number";
    private static final byte TAG_ID = 0;
    private static final byte TAG_NAMES = 1;
    private static final byte TAG_SERIAL_NUM = 2;
    private KeyIdentifier       id = null;
    private GeneralNames        names = null;
    private SerialNumber        serialNum = null;
    private void encodeThis() throws IOException {
        if (id == null && names == null && serialNum == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream seq = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        if (id != null) {
            DerOutputStream tmp1 = new DerOutputStream();
            id.encode(tmp1);
            tmp.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                              false, TAG_ID), tmp1);
        }
        try {
            if (names != null) {
                DerOutputStream tmp1 = new DerOutputStream();
                names.encode(tmp1);
                tmp.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                                  true, TAG_NAMES), tmp1);
            }
        } catch (Exception e) {
            throw new IOException(e.toString());
        }
        if (serialNum != null) {
            DerOutputStream tmp1 = new DerOutputStream();
            serialNum.encode(tmp1);
            tmp.writeImplicit(DerValue.createTag(DerValue.TAG_CONTEXT,
                              false, TAG_SERIAL_NUM), tmp1);
        }
        seq.write(DerValue.tag_Sequence, tmp);
        this.extensionValue = seq.toByteArray();
    }
    public AuthorityKeyIdentifierExtension(KeyIdentifier kid, GeneralNames name,
                                           SerialNumber sn)
    throws IOException {
        this.id = kid;
        this.names = name;
        this.serialNum = sn;
        this.extensionId = PKIXExtensions.AuthorityKey_Id;
        this.critical = false;
        encodeThis();
    }
    public AuthorityKeyIdentifierExtension(Boolean critical, Object value)
    throws IOException {
        this.extensionId = PKIXExtensions.AuthorityKey_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding for " +
                                  "AuthorityKeyIdentifierExtension.");
        }
        while ((val.data != null) && (val.data.available() != 0)) {
            DerValue opt = val.data.getDerValue();
            if (opt.isContextSpecific(TAG_ID) && !opt.isConstructed()) {
                if (id != null)
                    throw new IOException("Duplicate KeyIdentifier in " +
                                          "AuthorityKeyIdentifier.");
                opt.resetTag(DerValue.tag_OctetString);
                id = new KeyIdentifier(opt);
            } else if (opt.isContextSpecific(TAG_NAMES) &&
                       opt.isConstructed()) {
                if (names != null)
                    throw new IOException("Duplicate GeneralNames in " +
                                          "AuthorityKeyIdentifier.");
                opt.resetTag(DerValue.tag_Sequence);
                names = new GeneralNames(opt);
            } else if (opt.isContextSpecific(TAG_SERIAL_NUM) &&
                       !opt.isConstructed()) {
                if (serialNum != null)
                    throw new IOException("Duplicate SerialNumber in " +
                                          "AuthorityKeyIdentifier.");
                opt.resetTag(DerValue.tag_Integer);
                serialNum = new SerialNumber(opt);
            } else
                throw new IOException("Invalid encoding of " +
                                      "AuthorityKeyIdentifierExtension.");
        }
    }
    public String toString() {
        String s = super.toString() + "AuthorityKeyIdentifier [\n";
        if (id != null) {
            s += id.toString();     
        }
        if (names != null) {
            s += names.toString() + "\n";
        }
        if (serialNum != null) {
            s += serialNum.toString() + "\n";
        }
        return (s + "]\n");
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            extensionId = PKIXExtensions.AuthorityKey_Id;
            critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(KEY_ID)) {
            if (!(obj instanceof KeyIdentifier)) {
              throw new IOException("Attribute value should be of " +
                                    "type KeyIdentifier.");
            }
            id = (KeyIdentifier)obj;
        } else if (name.equalsIgnoreCase(AUTH_NAME)) {
            if (!(obj instanceof GeneralNames)) {
              throw new IOException("Attribute value should be of " +
                                    "type GeneralNames.");
            }
            names = (GeneralNames)obj;
        } else if (name.equalsIgnoreCase(SERIAL_NUMBER)) {
            if (!(obj instanceof SerialNumber)) {
              throw new IOException("Attribute value should be of " +
                                    "type SerialNumber.");
            }
            serialNum = (SerialNumber)obj;
        } else {
          throw new IOException("Attribute name not recognized by " +
                        "CertAttrSet:AuthorityKeyIdentifier.");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(KEY_ID)) {
            return (id);
        } else if (name.equalsIgnoreCase(AUTH_NAME)) {
            return (names);
        } else if (name.equalsIgnoreCase(SERIAL_NUMBER)) {
            return (serialNum);
        } else {
          throw new IOException("Attribute name not recognized by " +
                        "CertAttrSet:AuthorityKeyIdentifier.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(KEY_ID)) {
            id = null;
        } else if (name.equalsIgnoreCase(AUTH_NAME)) {
            names = null;
        } else if (name.equalsIgnoreCase(SERIAL_NUMBER)) {
            serialNum = null;
        } else {
          throw new IOException("Attribute name not recognized by " +
                        "CertAttrSet:AuthorityKeyIdentifier.");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(KEY_ID);
        elements.addElement(AUTH_NAME);
        elements.addElement(SERIAL_NUMBER);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
}
