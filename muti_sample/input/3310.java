final public class AuthorizationDataEntry {
    private final int type;
    private final byte[] data;
    public AuthorizationDataEntry(int type, byte[] data) {
        this.type = type;
        this.data = data.clone();
    }
    public int getType() {
        return type;
    }
    public byte[] getData() {
        return data.clone();
    }
    public String toString() {
        return "AuthorizationDataEntry: type="+type+", data=" +
                data.length + " bytes:\n" +
                new sun.misc.HexDumpEncoder().encodeBuffer(data);
    }
}
