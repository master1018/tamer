public class SSLEngineAppData implements org.apache.harmony.xnet.provider.jsse.Appendable {
    byte[] buffer;
    protected SSLEngineAppData() {}
    public void append(byte[] src) {
        if (buffer != null) {
            throw new AlertException(
                AlertProtocol.INTERNAL_ERROR,
                new SSLException("Attempt to override the data"));
        }
        buffer = src;
    }
    protected int placeTo(ByteBuffer[] dsts, int offset, int length) {
        if (buffer == null) {
            return 0;
        }
        int pos = 0;
        int len = buffer.length;
        int rem;
        for (int i=offset; i<offset+length; i++) {
            rem = dsts[i].remaining();
            if (len - pos < rem) {
                dsts[i].put(buffer, pos, len - pos);
                pos = len;
                break;
            }
            dsts[i].put(buffer, pos, rem);
            pos += rem;
        }
        if (pos != len) {
            throw new AlertException(
                AlertProtocol.INTERNAL_ERROR,
                new SSLException(
                    "The received application data could not be fully written"
                    + "into the destination buffers"));
        }
        buffer = null;
        return len;
    }
}
