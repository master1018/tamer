public class SSLEngineDataStream implements DataStream {
    private ByteBuffer[] srcs;
    private int offset;
    private int limit;
    private int available;
    private int consumed;
    protected SSLEngineDataStream() {}
    protected void setSourceBuffers(ByteBuffer[] srcs, int offset, int length) {
        this.srcs = srcs;
        this.offset = offset;
        this.limit = offset+length;
        this.consumed = 0;
        this.available = 0;
        for (int i=offset; i<limit; i++) {
            if (srcs[i] == null) {
                throw new IllegalStateException(
                        "Some of the input parameters are null");
            }
            available += srcs[i].remaining();
        }
    }
    public int available() {
        return available;
    }
    public boolean hasData() {
        return available > 0;
    }
    public byte[] getData(int length) {
        int len = (length < available) ? length : available;
        available -= len;
        consumed += len;
        byte[] res = new byte[len];
        int pos = 0;
        loop:
        for (; offset<limit; offset++) {
            while (srcs[offset].hasRemaining()) {
                res[pos++] = srcs[offset].get();
                len --;
                if (len == 0) {
                    break loop;
                }
            }
        }
        return res;
    }
    protected int consumed() {
        return consumed;
    }
}
