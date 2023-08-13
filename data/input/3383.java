public class ExtendedKeyUsageExtension extends Extension
implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.ExtendedKeyUsage";
    public static final String NAME = "ExtendedKeyUsage";
    public static final String USAGES = "usages";
    private static final Map <ObjectIdentifier, String> map =
            new HashMap <ObjectIdentifier, String> ();
    private static final int[] anyExtendedKeyUsageOidData = {2, 5, 29, 37, 0};
    private static final int[] serverAuthOidData = {1, 3, 6, 1, 5, 5, 7, 3, 1};
    private static final int[] clientAuthOidData = {1, 3, 6, 1, 5, 5, 7, 3, 2};
    private static final int[] codeSigningOidData = {1, 3, 6, 1, 5, 5, 7, 3, 3};
    private static final int[] emailProtectionOidData = {1, 3, 6, 1, 5, 5, 7, 3, 4};
    private static final int[] ipsecEndSystemOidData = {1, 3, 6, 1, 5, 5, 7, 3, 5};
    private static final int[] ipsecTunnelOidData = {1, 3, 6, 1, 5, 5, 7, 3, 6};
    private static final int[] ipsecUserOidData = {1, 3, 6, 1, 5, 5, 7, 3, 7};
    private static final int[] timeStampingOidData = {1, 3, 6, 1, 5, 5, 7, 3, 8};
    private static final int[] OCSPSigningOidData = {1, 3, 6, 1, 5, 5, 7, 3, 9};
    static {
        map.put(ObjectIdentifier.newInternal(anyExtendedKeyUsageOidData), "anyExtendedKeyUsage");
        map.put(ObjectIdentifier.newInternal(serverAuthOidData), "serverAuth");
        map.put(ObjectIdentifier.newInternal(clientAuthOidData), "clientAuth");
        map.put(ObjectIdentifier.newInternal(codeSigningOidData), "codeSigning");
        map.put(ObjectIdentifier.newInternal(emailProtectionOidData), "emailProtection");
        map.put(ObjectIdentifier.newInternal(ipsecEndSystemOidData), "ipsecEndSystem");
        map.put(ObjectIdentifier.newInternal(ipsecTunnelOidData), "ipsecTunnel");
        map.put(ObjectIdentifier.newInternal(ipsecUserOidData), "ipsecUser");
        map.put(ObjectIdentifier.newInternal(timeStampingOidData), "timeStamping");
        map.put(ObjectIdentifier.newInternal(OCSPSigningOidData), "OCSPSigning");
    };
    private Vector<ObjectIdentifier> keyUsages;
    private void encodeThis() throws IOException {
        if (keyUsages == null || keyUsages.isEmpty()) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream os = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        for (int i = 0; i < keyUsages.size(); i++) {
            tmp.putOID(keyUsages.elementAt(i));
        }
        os.write(DerValue.tag_Sequence, tmp);
        this.extensionValue = os.toByteArray();
    }
    public ExtendedKeyUsageExtension(Vector<ObjectIdentifier> keyUsages)
    throws IOException {
        this(Boolean.FALSE, keyUsages);
    }
    public ExtendedKeyUsageExtension(Boolean critical, Vector<ObjectIdentifier> keyUsages)
    throws IOException {
        this.keyUsages = keyUsages;
        this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = critical.booleanValue();
        encodeThis();
    }
    public ExtendedKeyUsageExtension(Boolean critical, Object value)
    throws IOException {
        this.extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        if (val.tag != DerValue.tag_Sequence) {
            throw new IOException("Invalid encoding for " +
                                   "ExtendedKeyUsageExtension.");
        }
        keyUsages = new Vector<ObjectIdentifier>();
        while (val.data.available() != 0) {
            DerValue seq = val.data.getDerValue();
            ObjectIdentifier usage = seq.getOID();
            keyUsages.addElement(usage);
        }
    }
    public String toString() {
        if (keyUsages == null) return "";
        String usage = "  ";
        boolean first = true;
        for (ObjectIdentifier oid: keyUsages) {
            if(!first) {
                usage += "\n  ";
            }
            String result = map.get(oid);
            if (result != null) {
                usage += result;
            } else {
                usage += oid.toString();
            }
            first = false;
        }
        return super.toString() + "ExtendedKeyUsages [\n"
               + usage + "\n]\n";
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream tmp = new DerOutputStream();
        if (extensionValue == null) {
          extensionId = PKIXExtensions.ExtendedKeyUsage_Id;
          critical = false;
          encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(USAGES)) {
            if (!(obj instanceof Vector)) {
                throw new IOException("Attribute value should be of type Vector.");
            }
            this.keyUsages = (Vector<ObjectIdentifier>)obj;
        } else {
          throw new IOException("Attribute name [" + name +
                                "] not recognized by " +
                                "CertAttrSet:ExtendedKeyUsageExtension.");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(USAGES)) {
            return keyUsages;
        } else {
          throw new IOException("Attribute name [" + name +
                                "] not recognized by " +
                                "CertAttrSet:ExtendedKeyUsageExtension.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(USAGES)) {
            keyUsages = null;
        } else {
          throw new IOException("Attribute name [" + name +
                                "] not recognized by " +
                                "CertAttrSet:ExtendedKeyUsageExtension.");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(USAGES);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
    public List<String> getExtendedKeyUsage() {
        List<String> al = new ArrayList<String>(keyUsages.size());
        for (ObjectIdentifier oid : keyUsages) {
            al.add(oid.toString());
        }
        return al;
    }
}
