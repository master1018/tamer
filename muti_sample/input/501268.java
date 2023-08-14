public class Name {
    private volatile byte[] encoded;
    private String rfc1779String;
    private String rfc2253String;
    private String canonicalString;
    private List rdn;
    public Name(byte[] encoding) throws IOException {
        DerInputStream in = new DerInputStream(encoding);
        if (in.getEndOffset() != encoding.length) {
            throw new IOException(Messages.getString("security.111")); 
        }
        ASN1.decode(in);
        this.rdn = (List) in.content;
    }
    public Name(String name) throws IOException {
        rdn = new DNParser(name).parse();
    }
    private Name(List rdn) {
        this.rdn = rdn;
    }
    public X500Principal getX500Principal(){
        return new X500Principal(getName0(X500Principal.RFC2253));
    }
    public String getName(String format) {
        if (X500Principal.RFC1779.equals(format)) {
            if (rfc1779String == null) {
                rfc1779String = getName0(format);
            }
            return rfc1779String;
        } else if (X500Principal.RFC2253.equals(format)) {
            if (rfc2253String == null) {
                rfc2253String = getName0(format);
            }
            return rfc2253String;
        } else if (X500Principal.CANONICAL.equals(format)) {
            if (canonicalString == null) {
                canonicalString = getName0(format);
            }
            return canonicalString;
        }
        else if (X500Principal.RFC1779.equalsIgnoreCase(format)) {
            if (rfc1779String == null) {
                rfc1779String = getName0(X500Principal.RFC1779);
            }
            return rfc1779String;
        } else if (X500Principal.RFC2253.equalsIgnoreCase(format)) {
            if (rfc2253String == null) {
                rfc2253String = getName0(X500Principal.RFC2253);
            }
            return rfc2253String;
        } else if (X500Principal.CANONICAL.equalsIgnoreCase(format)) {
            if (canonicalString == null) {
                canonicalString = getName0(X500Principal.CANONICAL);
            }
            return canonicalString;
        } else {
            throw new IllegalArgumentException(Messages.getString("security.177", format)); 
        }
    }
    private String getName0(String format) {
        StringBuffer name = new StringBuffer();
        for (int i = rdn.size() - 1; i >= 0; i--) {
            List atavList = (List) rdn.get(i);
            if (X500Principal.CANONICAL == format) {
                List sortedList = new LinkedList(atavList);
                Collections.sort(sortedList,
                        new AttributeTypeAndValueComparator());
                atavList = sortedList;
            }
            Iterator it = atavList.iterator();
            while (it.hasNext()) {
                AttributeTypeAndValue _ava = (AttributeTypeAndValue) it.next();
                _ava.appendName(format, name);
                if (it.hasNext()) {
                    if (X500Principal.RFC1779 == format) {
                        name.append(" + "); 
                    } else {
                        name.append('+');
                    }
                }
            }
            if (i != 0) {
                name.append(',');
                if (format == X500Principal.RFC1779) {
                    name.append(' ');
                }
            }
        }
        String sName = name.toString();
        if (X500Principal.CANONICAL.equals(format)) {
            sName = sName.toLowerCase(Locale.US);
        }
        return sName;
    }
    public byte[] getEncoded() {
        if (encoded == null) {
            encoded = ASN1.encode(this);
        }
        return encoded;
    }
    public static final ASN1SetOf ASN1_RDN = new ASN1SetOf(
            AttributeTypeAndValue.ASN1);
    public static final ASN1SequenceOf ASN1 = new ASN1SequenceOf(ASN1_RDN) {
        public Object getDecodedObject(BerInputStream in) {
            return new Name((List) in.content);
        }
        public Collection getValues(Object object) {
            return ((Name) object).rdn; 
        }
    };
}
