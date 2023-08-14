public class InvalidityDateExtension extends Extension
    implements CertAttrSet<String> {
    public static final String NAME = "InvalidityDate";
    public static final String DATE = "date";
    private Date date;
    private void encodeThis() throws IOException {
        if (date == null) {
            this.extensionValue = null;
            return;
        }
        DerOutputStream dos = new DerOutputStream();
        dos.putGeneralizedTime(date);
        this.extensionValue = dos.toByteArray();
    }
    public InvalidityDateExtension(Date date) throws IOException {
        this(false, date);
    }
    public InvalidityDateExtension(boolean critical, Date date)
    throws IOException {
        this.extensionId = PKIXExtensions.InvalidityDate_Id;
        this.critical = critical;
        this.date = date;
        encodeThis();
    }
    public InvalidityDateExtension(Boolean critical, Object value)
    throws IOException {
        this.extensionId = PKIXExtensions.InvalidityDate_Id;
        this.critical = critical.booleanValue();
        this.extensionValue = (byte[]) value;
        DerValue val = new DerValue(this.extensionValue);
        this.date = val.getGeneralizedTime();
    }
    public void set(String name, Object obj) throws IOException {
        if (!(obj instanceof Date)) {
            throw new IOException("Attribute must be of type Date.");
        }
        if (name.equalsIgnoreCase(DATE)) {
            date = (Date) obj;
        } else {
            throw new IOException
                ("Name not supported by InvalidityDateExtension");
        }
        encodeThis();
    }
    public Object get(String name) throws IOException {
        if (name.equalsIgnoreCase(DATE)) {
            if (date == null) {
                return null;
            } else {
                return (new Date(date.getTime()));    
            }
        } else {
            throw new IOException
                ("Name not supported by InvalidityDateExtension");
        }
    }
    public void delete(String name) throws IOException {
        if (name.equalsIgnoreCase(DATE)) {
            date = null;
        } else {
            throw new IOException
                ("Name not supported by InvalidityDateExtension");
        }
        encodeThis();
    }
    public String toString() {
        return super.toString() + "    Invalidity Date: " + String.valueOf(date);
    }
    public void encode(OutputStream out) throws IOException {
        DerOutputStream  tmp = new DerOutputStream();
        if (this.extensionValue == null) {
            this.extensionId = PKIXExtensions.InvalidityDate_Id;
            this.critical = false;
            encodeThis();
        }
        super.encode(tmp);
        out.write(tmp.toByteArray());
    }
    public Enumeration<String> getElements() {
        AttributeNameEnumeration elements = new AttributeNameEnumeration();
        elements.addElement(DATE);
        return elements.elements();
    }
    public String getName() {
        return NAME;
    }
    public static InvalidityDateExtension toImpl(java.security.cert.Extension ext)
        throws IOException {
        if (ext instanceof InvalidityDateExtension) {
            return (InvalidityDateExtension) ext;
        } else {
            return new InvalidityDateExtension
                (Boolean.valueOf(ext.isCritical()), ext.getValue());
        }
    }
}
