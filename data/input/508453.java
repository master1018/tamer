public class AttributeTypeAndValue {
    private static final ObjectIdentifier C;
    private static final ObjectIdentifier CN;
    private static final ObjectIdentifier DC;
    private static final ObjectIdentifier DNQ;
    private static final ObjectIdentifier DNQUALIFIER;
    private static final ObjectIdentifier EMAILADDRESS;
    private static final ObjectIdentifier GENERATION;
    private static final ObjectIdentifier GIVENNAME;
    private static final ObjectIdentifier INITIALS;
    private static final ObjectIdentifier L;
    private static final ObjectIdentifier O;
    private static final ObjectIdentifier OU;
    private static final ObjectIdentifier SERIALNUMBER;
    private static final ObjectIdentifier ST;
    private static final ObjectIdentifier STREET;
    private static final ObjectIdentifier SURNAME;
    private static final ObjectIdentifier T;
    private static final ObjectIdentifier UID;
    private static final int CAPACITY;
    private static final int SIZE;
    private static final ObjectIdentifier[][] KNOWN_OIDS;
    private static final HashMap KNOWN_NAMES = new HashMap(30);
    private static final HashMap RFC1779_NAMES = new HashMap(10);
    private static final HashMap RFC2253_NAMES = new HashMap(10);
    private static final HashMap RFC2459_NAMES = new HashMap(10);
    static {
        CAPACITY = 10;
        SIZE = 10;
        KNOWN_OIDS = new ObjectIdentifier[SIZE][CAPACITY];
        C = new ObjectIdentifier(new int[] { 2, 5, 4, 6 }, "C", RFC1779_NAMES); 
        CN = new ObjectIdentifier(new int[] { 2, 5, 4, 3 }, "CN", RFC1779_NAMES); 
        DC = new ObjectIdentifier(
                new int[] { 0, 9, 2342, 19200300, 100, 1, 25 }, "DC", 
                RFC2253_NAMES);
        DNQ = new ObjectIdentifier(new int[] { 2, 5, 4, 46 }, "DNQ", 
                RFC2459_NAMES);
        DNQUALIFIER = new ObjectIdentifier(new int[] { 2, 5, 4, 46 },
                "DNQUALIFIER", RFC2459_NAMES); 
        EMAILADDRESS = new ObjectIdentifier(new int[] { 1, 2, 840, 113549, 1,
                9, 1 }, "EMAILADDRESS", RFC2459_NAMES); 
        GENERATION = new ObjectIdentifier(new int[] { 2, 5, 4, 44 },
                "GENERATION", RFC2459_NAMES); 
        GIVENNAME = new ObjectIdentifier(new int[] { 2, 5, 4, 42 },
                "GIVENNAME", RFC2459_NAMES); 
        INITIALS = new ObjectIdentifier(new int[] { 2, 5, 4, 43 }, "INITIALS", 
                RFC2459_NAMES);
        L = new ObjectIdentifier(new int[] { 2, 5, 4, 7 }, "L", RFC1779_NAMES); 
        O = new ObjectIdentifier(new int[] { 2, 5, 4, 10 }, "O", RFC1779_NAMES); 
        OU = new ObjectIdentifier(new int[] { 2, 5, 4, 11 }, "OU", 
                RFC1779_NAMES);
        SERIALNUMBER = new ObjectIdentifier(new int[] { 2, 5, 4, 5 },
                "SERIALNUMBER", RFC2459_NAMES); 
        ST = new ObjectIdentifier(new int[] { 2, 5, 4, 8 }, "ST", RFC1779_NAMES); 
        STREET = new ObjectIdentifier(new int[] { 2, 5, 4, 9 }, "STREET", 
                RFC1779_NAMES);
        SURNAME = new ObjectIdentifier(new int[] { 2, 5, 4, 4 }, "SURNAME", 
                RFC2459_NAMES);
        T = new ObjectIdentifier(new int[] { 2, 5, 4, 12 }, "T", RFC2459_NAMES); 
        UID = new ObjectIdentifier(
                new int[] { 0, 9, 2342, 19200300, 100, 1, 1 }, "UID", 
                RFC2253_NAMES);
        RFC1779_NAMES.put(CN.getName(), CN);
        RFC1779_NAMES.put(L.getName(), L);
        RFC1779_NAMES.put(ST.getName(), ST);
        RFC1779_NAMES.put(O.getName(), O);
        RFC1779_NAMES.put(OU.getName(), OU);
        RFC1779_NAMES.put(C.getName(), C);
        RFC1779_NAMES.put(STREET.getName(), STREET);
        RFC2253_NAMES.putAll(RFC1779_NAMES);
        RFC2253_NAMES.put(DC.getName(), DC);
        RFC2253_NAMES.put(UID.getName(), UID);
        RFC2459_NAMES.put(DNQ.getName(), DNQ);
        RFC2459_NAMES.put(DNQUALIFIER.getName(), DNQUALIFIER);
        RFC2459_NAMES.put(EMAILADDRESS.getName(), EMAILADDRESS);
        RFC2459_NAMES.put(GENERATION.getName(), GENERATION);
        RFC2459_NAMES.put(GIVENNAME.getName(), GIVENNAME);
        RFC2459_NAMES.put(INITIALS.getName(), INITIALS);
        RFC2459_NAMES.put(SERIALNUMBER.getName(), SERIALNUMBER);
        RFC2459_NAMES.put(SURNAME.getName(), SURNAME);
        RFC2459_NAMES.put(T.getName(), T);
        Iterator it = RFC2253_NAMES.values().iterator();
        while (it.hasNext()) {
            addOID((ObjectIdentifier) it.next());
        }
        it = RFC2459_NAMES.values().iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (!(o == DNQUALIFIER)) {
                addOID((ObjectIdentifier) o);
            }
        }
        KNOWN_NAMES.putAll(RFC2253_NAMES); 
        KNOWN_NAMES.putAll(RFC2459_NAMES);
    }
    private final ObjectIdentifier oid;
    private AttributeValue value;
    private AttributeTypeAndValue(int[] oid, AttributeValue value)
            throws IOException {
        ObjectIdentifier thisOid = getOID(oid);
        if (thisOid == null) {
            thisOid = new ObjectIdentifier(oid);
        }
        this.oid = thisOid;
        this.value = value;
    }
    public AttributeTypeAndValue(String sOid, AttributeValue value)
            throws IOException {
        if (sOid.charAt(0) >= '0' && sOid.charAt(0) <= '9') {
            int[] array = org.apache.harmony.security.asn1.ObjectIdentifier
                    .toIntArray(sOid);
            ObjectIdentifier thisOid = getOID(array);
            if (thisOid == null) {
                thisOid = new ObjectIdentifier(array);
            }
            this.oid = thisOid;
        } else {
            this.oid = (ObjectIdentifier) KNOWN_NAMES.get(Util.toUpperCase(sOid));
            if (this.oid == null) {
                throw new IOException(Messages.getString("security.178", sOid)); 
            }
        }
        this.value = value;
    }
    public void appendName(String attrFormat, StringBuffer buf) {
        boolean hexFormat = false;
        if (X500Principal.RFC1779.equals(attrFormat)) {
            if (RFC1779_NAMES == oid.getGroup()) {
                buf.append(oid.getName());
            } else {
                buf.append(oid.toOIDString());
            }
            buf.append('=');
            if (value.escapedString == value.getHexString()) {
                buf.append(Util.toUpperCase(value.getHexString()));
            } else if (value.escapedString.length() != value.rawString.length()) {
                value.appendQEString(buf);
            } else {
                buf.append(value.escapedString);
            }
        } else {
            Object group = oid.getGroup();
            if (RFC1779_NAMES == group || RFC2253_NAMES == group) {
                buf.append(oid.getName());
                if (X500Principal.CANONICAL.equals(attrFormat)) {
                    int tag = value.getTag();
                    if (!ASN1StringType.UTF8STRING.checkTag(tag)
                            && !ASN1StringType.PRINTABLESTRING.checkTag(tag)
                            && !ASN1StringType.TELETEXSTRING.checkTag(tag)) {
                        hexFormat = true;
                    }
                }
            } else {
                buf.append(oid.toString());
                hexFormat = true;
            }
            buf.append('=');
            if (hexFormat) {
                buf.append(value.getHexString());
            } else {
                if (X500Principal.CANONICAL.equals(attrFormat)) {
                    buf.append(value.makeCanonical());
                } else {
                    buf.append(value.escapedString);
                }
            }
        }
    }
    public ObjectIdentifier getType() {
        return oid;
    }
    public static final ASN1Type attributeValue = new ASN1Type(
            ASN1Constants.TAG_PRINTABLESTRING) {
        public boolean checkTag(int tag) {
            return true;
        }
        public Object decode(BerInputStream in) throws IOException {
            String str = null;
            if (DirectoryString.ASN1.checkTag(in.tag)) {
                str = (String) DirectoryString.ASN1.decode(in);
            } else {
                in.readContent();
            }
            byte[] bytesEncoded = new byte[in.getOffset() - in.getTagOffset()];
            System.arraycopy(in.getBuffer(), in.getTagOffset(), bytesEncoded,
                    0, bytesEncoded.length);
            return new AttributeValue(str, bytesEncoded, in.tag);
        }
        public Object getDecodedObject(BerInputStream in) throws IOException {
            throw new RuntimeException(Messages.getString("security.179")); 
        }
        public void encodeASN(BerOutputStream out) {
            AttributeValue av = (AttributeValue) out.content;
            if (av.encoded != null) {
                out.content = av.encoded;
                out.encodeANY();
            } else {
                out.encodeTag(av.getTag());
                out.content = av.bytes;
                out.encodeString();
            }
        }
        public void setEncodingContent(BerOutputStream out) {
            AttributeValue av = (AttributeValue) out.content;
            if (av.encoded != null) {
                out.length = av.encoded.length;
            } else {
                if (av.getTag() == ASN1Constants.TAG_UTF8STRING) {
                    out.content = av.rawString;
                    ASN1StringType.UTF8STRING.setEncodingContent(out);
                    av.bytes = (byte[]) out.content;
                    out.content = av;
                } else {
                    try {
                        av.bytes = av.rawString.getBytes("UTF-8"); 
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e.getMessage());
                    }
                    out.length = av.bytes.length;
                }
            }
        }
        public void encodeContent(BerOutputStream out) {
            throw new RuntimeException(Messages.getString("security.17A")); 
        }
        public int getEncodedLength(BerOutputStream out) { 
            AttributeValue av = (AttributeValue) out.content;
            if (av.encoded != null) {
                return out.length;
            } else {
                return super.getEncodedLength(out);
            }
        }
    };
    public static final ASN1Sequence ASN1 = new ASN1Sequence(new ASN1Type[] {
            ASN1Oid.getInstance(), attributeValue }) {
        protected Object getDecodedObject(BerInputStream in) throws IOException {
            Object[] values = (Object[]) in.content;
            return new AttributeTypeAndValue((int[]) values[0],
                    (AttributeValue) values[1]);
        }
        protected void getValues(Object object, Object[] values) {
            AttributeTypeAndValue atav = (AttributeTypeAndValue) object;
            values[0] = atav.oid.getOid();
            values[1] = atav.value;
        }
    };
    private static ObjectIdentifier getOID(int[] oid) {
        int index = hashIntArray(oid) % CAPACITY;
        ObjectIdentifier[] list = KNOWN_OIDS[index];
        for (int i = 0; list[i] != null; i++) {
            if (Arrays.equals(oid, list[i].getOid())) {
                return list[i];
            }
        }
        return null;
    }
    private static void addOID(ObjectIdentifier oid) {
        int[] newOid = oid.getOid();
        int index = hashIntArray(newOid) % CAPACITY;
        ObjectIdentifier[] list = KNOWN_OIDS[index];
        int i = 0;
        for (; list[i] != null; i++) {
            if (Arrays.equals(newOid, list[i].getOid())) {
                throw new Error(Messages.getString("security.17B", 
                                oid.getName(), list[i].getName()));
            }
        }
        if (i == (CAPACITY - 1)) {
            throw new Error(Messages.getString("security.17C")); 
        }
        list[i] = oid;
    }
    private static int hashIntArray(int[] oid) {
        int intHash = 0;
        for (int i = 0; i < oid.length && i < 4; i++) {
            intHash += oid[i] << (8 * i); 
        }
        return intHash & 0x7FFFFFFF; 
    }
}
