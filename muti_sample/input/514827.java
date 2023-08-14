public abstract class CharacterDataImpl extends LeafNodeImpl implements
        CharacterData {
    protected StringBuffer buffer;
    CharacterDataImpl(DocumentImpl document, String data) {
        super(document);
        setData(data);
    }
    public void appendData(String arg) throws DOMException {
        buffer.append(arg);
    }
    public void deleteData(int offset, int count) throws DOMException {
        buffer.delete(offset, offset + count);
    }
    public String getData() throws DOMException {
        return buffer.toString();
    }
    public void appendDataTo(StringBuilder stringBuilder) {
        stringBuilder.append(buffer);
    }
    public int getLength() {
        return buffer.length();
    }
    @Override
    public String getNodeValue() {
        return getData();
    }
    public void insertData(int offset, String arg) throws DOMException {
        try {
            buffer.insert(offset, arg);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, null);
        }
    }
    public void replaceData(int offset, int count, String arg)
            throws DOMException {
        try {
            buffer.replace(offset, offset + count, arg);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, null);
        }
    }
    public void setData(String data) throws DOMException {
        buffer = new StringBuffer(data);
    }
    public String substringData(int offset, int count) throws DOMException {
        try {
            return buffer.substring(offset, offset + count);
        } catch (ArrayIndexOutOfBoundsException ex) {
            throw new DOMException(DOMException.INDEX_SIZE_ERR, null);
        }
    }
}
