public class PKCS9Attributes {
    private final Hashtable<ObjectIdentifier, PKCS9Attribute> attributes =
        new Hashtable<ObjectIdentifier, PKCS9Attribute>(3);
    private final Hashtable<ObjectIdentifier, ObjectIdentifier> permittedAttributes;
    private final byte[] derEncoding;
    private boolean ignoreUnsupportedAttributes = false;
    public PKCS9Attributes(ObjectIdentifier[] permittedAttributes,
                           DerInputStream in) throws IOException {
        if (permittedAttributes != null) {
            this.permittedAttributes =
                new Hashtable<ObjectIdentifier, ObjectIdentifier>(
                                                permittedAttributes.length);
            for (int i = 0; i < permittedAttributes.length; i++)
                this.permittedAttributes.put(permittedAttributes[i],
                                             permittedAttributes[i]);
        } else {
            this.permittedAttributes = null;
        }
        derEncoding = decode(in);
    }
    public PKCS9Attributes(DerInputStream in) throws IOException {
        this(in, false);
    }
    public PKCS9Attributes(DerInputStream in,
        boolean ignoreUnsupportedAttributes) throws IOException {
        this.ignoreUnsupportedAttributes = ignoreUnsupportedAttributes;
        derEncoding = decode(in);
        permittedAttributes = null;
    }
    public PKCS9Attributes(PKCS9Attribute[] attribs)
    throws IllegalArgumentException, IOException {
        ObjectIdentifier oid;
        for (int i=0; i < attribs.length; i++) {
            oid = attribs[i].getOID();
            if (attributes.containsKey(oid))
                throw new IllegalArgumentException(
                          "PKCSAttribute " + attribs[i].getOID() +
                          " duplicated while constructing " +
                          "PKCS9Attributes.");
            attributes.put(oid, attribs[i]);
        }
        derEncoding = generateDerEncoding();
        permittedAttributes = null;
    }
    private byte[] decode(DerInputStream in) throws IOException {
        DerValue val = in.getDerValue();
        byte[] derEncoding = val.toByteArray();
        derEncoding[0] = DerValue.tag_SetOf;
        DerInputStream derIn = new DerInputStream(derEncoding);
        DerValue[] derVals = derIn.getSet(3,true);
        PKCS9Attribute attrib;
        ObjectIdentifier oid;
        boolean reuseEncoding = true;
        for (int i=0; i < derVals.length; i++) {
            try {
                attrib = new PKCS9Attribute(derVals[i]);
            } catch (ParsingException e) {
                if (ignoreUnsupportedAttributes) {
                    reuseEncoding = false; 
                    continue; 
                } else {
                    throw e;
                }
            }
            oid = attrib.getOID();
            if (attributes.get(oid) != null)
                throw new IOException("Duplicate PKCS9 attribute: " + oid);
            if (permittedAttributes != null &&
                !permittedAttributes.containsKey(oid))
                throw new IOException("Attribute " + oid +
                                      " not permitted in this attribute set");
            attributes.put(oid, attrib);
        }
        return reuseEncoding ? derEncoding : generateDerEncoding();
    }
    public void encode(byte tag, OutputStream out) throws IOException {
        out.write(tag);
        out.write(derEncoding, 1, derEncoding.length -1);
    }
    private byte[] generateDerEncoding() throws IOException {
        DerOutputStream out = new DerOutputStream();
        Object[] attribVals = attributes.values().toArray();
        out.putOrderedSetOf(DerValue.tag_SetOf,
                            castToDerEncoder(attribVals));
        return out.toByteArray();
    }
    public byte[] getDerEncoding() throws IOException {
        return derEncoding.clone();
    }
    public PKCS9Attribute getAttribute(ObjectIdentifier oid) {
        return attributes.get(oid);
    }
    public PKCS9Attribute getAttribute(String name) {
        return attributes.get(PKCS9Attribute.getOID(name));
    }
    public PKCS9Attribute[] getAttributes() {
        PKCS9Attribute[] attribs = new PKCS9Attribute[attributes.size()];
        ObjectIdentifier oid;
        int j = 0;
        for (int i=1; i < PKCS9Attribute.PKCS9_OIDS.length &&
                      j < attribs.length; i++) {
            attribs[j] = getAttribute(PKCS9Attribute.PKCS9_OIDS[i]);
            if (attribs[j] != null)
                j++;
        }
        return attribs;
    }
    public Object getAttributeValue(ObjectIdentifier oid)
    throws IOException {
        try {
            Object value = getAttribute(oid).getValue();
            return value;
        } catch (NullPointerException ex) {
            throw new IOException("No value found for attribute " + oid);
        }
    }
    public Object getAttributeValue(String name) throws IOException {
        ObjectIdentifier oid = PKCS9Attribute.getOID(name);
        if (oid == null)
            throw new IOException("Attribute name " + name +
                                  " not recognized or not supported.");
        return getAttributeValue(oid);
    }
    public String toString() {
        StringBuffer buf = new StringBuffer(200);
        buf.append("PKCS9 Attributes: [\n\t");
        ObjectIdentifier oid;
        PKCS9Attribute value;
        boolean first = true;
        for (int i = 1; i < PKCS9Attribute.PKCS9_OIDS.length; i++) {
            value = getAttribute(PKCS9Attribute.PKCS9_OIDS[i]);
            if (value == null) continue;
            if (first)
                first = false;
            else
                buf.append(";\n\t");
            buf.append(value.toString());
        }
        buf.append("\n\t] (end PKCS9 Attributes)");
        return buf.toString();
    }
    static DerEncoder[] castToDerEncoder(Object[] objs) {
        DerEncoder[] encoders = new DerEncoder[objs.length];
        for (int i=0; i < encoders.length; i++)
            encoders[i] = (DerEncoder) objs[i];
        return encoders;
    }
}
