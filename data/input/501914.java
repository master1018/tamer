public class ExtensionValue {
    protected byte[] encoding;
    public ExtensionValue() { }
    public ExtensionValue(byte[] encoding) {
        this.encoding = encoding;
    }
    public byte[] getEncoded() {
        return encoding;
    }
    public void dumpValue(StringBuffer buffer, String prefix) {
        buffer.append(prefix).append("Unparseable extension value:\n"); 
        if (encoding == null) {
            encoding = getEncoded();
        }
        if (encoding == null) {
            buffer.append("NULL\n"); 
        } else {
            buffer.append(Array.toString(encoding, prefix));
        }
    }
    public void dumpValue(StringBuffer buffer) {
        dumpValue(buffer, ""); 
    };
}
