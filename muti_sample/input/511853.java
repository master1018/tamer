abstract class ExpatAttributes implements Attributes {
    private static final String CDATA = "CDATA";
    public abstract int getLength();
    abstract int getParserPointer();
    public abstract int getPointer();
    public String getURI(int index) {
        if (index < 0 || index >= getLength()) {
            return null;
        }
        return getURI(getParserPointer(), getPointer(), index);
    }
    public String getLocalName(int index) {
        return (index < 0 || index >= getLength())
                ? null
                : getLocalName(getParserPointer(), getPointer(), index);
    }
    public String getQName(int index) {
        return (index < 0 || index >= getLength())
                ? null
                : getQName(getParserPointer(), getPointer(), index);
    }
    public String getType(int index) {
        return (index < 0 || index >= getLength()) ? null : CDATA;
    }
    public String getValue(int index) {
        return (index < 0 || index >= getLength())
                ? null
                : getValue(getPointer(), index);
    }
    public int getIndex(String uri, String localName) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        if (localName == null) {
            throw new NullPointerException("local name");
        }
        int pointer = getPointer();
        if (pointer == 0) {
            return -1;
        }
        return getIndex(pointer, uri, localName);
    }
    public int getIndex(String qName) {
        if (qName == null) {
            throw new NullPointerException("uri");
        }
        int pointer = getPointer();
        if (pointer == 0) {
            return -1;
        }
        return getIndex(pointer, qName);
    }
    public String getType(String uri, String localName) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        if (localName == null) {
            throw new NullPointerException("local name");
        }
        return getIndex(uri, localName) == -1 ? null : CDATA;
    }
    public String getType(String qName) {
        return getIndex(qName) == -1 ? null : CDATA;
    }
    public String getValue(String uri, String localName) {
        if (uri == null) {
            throw new NullPointerException("uri");
        }
        if (localName == null) {
            throw new NullPointerException("local name");
        }
        int pointer = getPointer();
        if (pointer == 0) {
            return null;
        }
        return getValue(pointer, uri, localName);
    }
    public String getValue(String qName) {
        if (qName == null) {
            throw new NullPointerException("qName");
        }
        int pointer = getPointer();
        if (pointer == 0) {
            return null;
        }
        return getValue(pointer, qName);
    }
    static native String getURI(int pointer, int attributePointer, int index);
    static native String getLocalName(int pointer, int attributePointer,
            int index);
    static native String getQName(int pointer, int attributePointer,
            int index);
    static native String getValue(int attributePointer, int index);
    static native int getIndex(int attributePointer, String uri,
            String localName);
    static native int getIndex(int attributePointer, String qName);
    static native String getValue(int attributePointer,
            String uri, String localName);
    static native String getValue(int attributePointer, String qName);
    static native void freeAttributes(int pointer);
}
