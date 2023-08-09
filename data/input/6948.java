public class PKCS10Attributes implements DerEncoder {
    private Hashtable<String, PKCS10Attribute> map =
                        new Hashtable<String, PKCS10Attribute>(3);
    public PKCS10Attributes() { }
    public PKCS10Attributes(PKCS10Attribute[] attrs) {
        for (int i = 0; i < attrs.length; i++) {
            map.put(attrs[i].getAttributeId().toString(), attrs[i]);
        }
    }
    public PKCS10Attributes(DerInputStream in) throws IOException {
        DerValue[] attrs = in.getSet(3, true);
        if (attrs == null)
            throw new IOException("Illegal encoding of attributes");
        for (int i = 0; i < attrs.length; i++) {
            PKCS10Attribute attr = new PKCS10Attribute(attrs[i]);
            map.put(attr.getAttributeId().toString(), attr);
        }
    }
    public void encode(OutputStream out) throws IOException {
        derEncode(out);
    }
    public void derEncode(OutputStream out) throws IOException {
        Collection<PKCS10Attribute> allAttrs = map.values();
        PKCS10Attribute[] attribs =
                allAttrs.toArray(new PKCS10Attribute[map.size()]);
        DerOutputStream attrOut = new DerOutputStream();
        attrOut.putOrderedSetOf(DerValue.createTag(DerValue.TAG_CONTEXT,
                                                   true, (byte)0),
                                attribs);
        out.write(attrOut.toByteArray());
    }
    public void setAttribute(String name, Object obj) {
        if (obj instanceof PKCS10Attribute) {
            map.put(name, (PKCS10Attribute)obj);
        }
    }
    public Object getAttribute(String name) {
        return map.get(name);
    }
    public void deleteAttribute(String name) {
        map.remove(name);
    }
    public Enumeration<PKCS10Attribute> getElements() {
        return (map.elements());
    }
    public Collection<PKCS10Attribute> getAttributes() {
        return (Collections.unmodifiableCollection(map.values()));
    }
    public boolean equals(Object other) {
        if (this == other)
            return true;
        if (!(other instanceof PKCS10Attributes))
            return false;
        Collection<PKCS10Attribute> othersAttribs =
                ((PKCS10Attributes)other).getAttributes();
        PKCS10Attribute[] attrs =
            othersAttribs.toArray(new PKCS10Attribute[othersAttribs.size()]);
        int len = attrs.length;
        if (len != map.size())
            return false;
        PKCS10Attribute thisAttr, otherAttr;
        String key = null;
        for (int i=0; i < len; i++) {
            otherAttr = attrs[i];
            key = otherAttr.getAttributeId().toString();
            if (key == null)
                return false;
            thisAttr = map.get(key);
            if (thisAttr == null)
                return false;
            if (! thisAttr.equals(otherAttr))
                return false;
        }
        return true;
    }
    public int hashCode() {
        return map.hashCode();
    }
    public String toString() {
        String s = map.size() + "\n" + map.toString();
        return s;
    }
}
