public class IIOByteBuffer {
    private byte[] data;
    private int offset;
    private int length;
    public IIOByteBuffer(byte[] data, int offset, int length) {
        this.data = data;
        this.offset = offset;
        this.length = length;
    }
    public byte[] getData() {
        return data;
    }
    public int getLength() {
        return length;
    }
    public int getOffset() {
        return offset;
    }
    public void setData(byte[] data) {
        this.data = data;
    }
    public void setLength(int length) {
        this.length = length;
    }
    public void setOffset(int offset) {
        this.offset = offset;
    }
}
