public class BasicConstraintsExtension extends Extension
implements CertAttrSet<String> {
    public static final String IDENT = "x509.info.extensions.BasicConstraints";
    public static final String NAME = "BasicConstraints";
    public static final String IS_CA = "is_ca";
    public static final String PATH_LEN = "path_len";
    private boolean     ca = false;
    private int pathLen = -1;
    private void encodeThis() throws IOException {
        DerOutputStream out = new DerOutputStream();
        DerOutputStream tmp = new DerOutputStream();
        if (ca) {
            tmp.putBoolean(ca);
            if (pathLen >= 0) {
                tmp.putInteger(pathLen);
            }
        }
        out.write(DerValue.tag_Sequence, tmp);
        this.extensionValue = out.toByteArray();
    }
    public BasicConstraintsExtension(boolean ca, int len) throws IOException {
        this(Boolean.valueOf(ca), ca, len);
    }
    public BasicConstraintsExtension(Boolean critical, boolean ca, int len)
    throws IOException {
        this.ca = ca;
        this.pathLen = len;
        this.extensionId = PKIXExtensions.BasicConstraints_Id;
        this.critical = critical.booleanValue();
        encodeThis();
    }
     public BasicConstraintsExtension(Boolean critical, Object value)
         throws IOException
    {
         this.extensionId = PKIXExtensions.BasicConstraints_Id;
         this.critical = critical.booleanValue();
         this.extensionValue = (byte[]) value;
         DerValue val = new DerValue(this.extensionValue);
         if (val.tag != DerValue.tag_Sequence) {
             throw new IOException("Invalid encoding of BasicConstraints");
         }
         if (val.data == null || val.data.available() == 0) {
             return;
         }
         DerValue opt = val.data.getDerValue();
         if (opt.tag != DerValue.tag_Boolean) {
             return;
         }
         this.ca = opt.getBoolean();
         if (val.data.available() == 0) {
             this.pathLen = Integer.MAX_VALUE;
             return;
         }
         opt = val.data.getDerValue();
         if (opt.tag != DerValue.tag_Integer) {
             throw new IOException("Invalid encoding of BasicConstraints");
         }
         this.pathLen = opt.getInteger();
     }
     public String toString() {
         String s = super.toString() + "BasicConstraints:[\n";
         s += ((ca) ? ("  CA:true") : ("  CA:false")) + "\n";
         if (pathLen >= 0) {
             s += "  PathLen:" + pathLen + "\n";
         } else {
             s += "  PathLen: undefined\n";
         }
         return (s + "]\n");
     }
     public void encode(OutputStream out) throws IOException {
         DerOutputStream tmp = new DerOutputStream();
         if (extensionValue == null) {
             this.extensionId = PKIXExtensions.BasicConstraints_Id;
             if (ca) {
                 critical = true;
             } else {
                 critical = false;
             }
             encodeThis();
         }
         super.encode(tmp);
         out.write(tmp.toByteArray());
     }
    public void set(String name, Object obj) throws IOException {
        if (name.equalsIgnoreCase(IS_CA)) {
            if (!(obj instanceof Boolean)) {
              throw new IOException("Attribute value should be of type Boolean.");
            }
            ca = ((Boolean)obj).booleanValue();
        } else if (name.equalsIgnoreCase(PATH_LEN)) {
            if (!(obj instanceof Integer)) {
              throw new IOException("Attribute value should be of type Integer.");
            }
            pathLen = ((Integer)obj).intValue();
        } else {
          throw new IOException("Attribute name not recognized by " +
                                "CertAttrSet:BasicConstraints.");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(IS_CA)) {
            return (Boolean.valueOf(ca));
        } else if (name.equalsIgnoreCase(PATH_LEN)) {
            return (Integer.valueOf(pathLen));
        } else {
          throw new IOException("Attribute name not recognized by " +
                                "CertAttrSet:BasicConstraints.");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(IS_CA)) {
            ca = false;
        } else if (name.equalsIgnoreCase(PATH_LEN)) {
            pathLen = -1;
        } else {
          throw new IOException("Attribute name not recognized by " +
                                "CertAttrSet:BasicConstraints.");
        }
        encodeThis();
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(IS_CA);
        elements.addElement(PATH_LEN);
        return (elements.elements());
    }
    public String getName() {
        return (NAME);
    }
}
