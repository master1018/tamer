public class QuotedPrintableInputStream extends InputStream {
    private static Log log = LogFactory.getLog(QuotedPrintableInputStream.class);
    private InputStream stream;
    ByteQueue byteq = new ByteQueue();
    ByteQueue pushbackq = new ByteQueue();
    private byte state = 0;
    public QuotedPrintableInputStream(InputStream stream) {
        this.stream = stream;
    }
    public void close() throws IOException {
        stream.close();
    }
    public int read() throws IOException {
        fillBuffer();
        if (byteq.count() == 0)
            return -1;
        else {
            byte val = byteq.dequeue();
            if (val >= 0)
                return val;
            else
                return val & 0xFF;
        }
    }
    private void populatePushbackQueue() throws IOException {
        if (pushbackq.count() != 0)
            return;
        while (true) {
            int i = stream.read();
            switch (i) {
                case -1:
                    pushbackq.clear();  
                    return;
                case ' ':
                case '\t':
                    pushbackq.enqueue((byte)i);
                    break;
                case '\r':
                case '\n':
                    pushbackq.clear();  
                    pushbackq.enqueue((byte)i);
                    return;
                default:
                    pushbackq.enqueue((byte)i);
                    return;
            }
        }
    }
    private void fillBuffer() throws IOException {
        byte msdChar = 0;  
        while (byteq.count() == 0) {
            if (pushbackq.count() == 0) {
                populatePushbackQueue();
                if (pushbackq.count() == 0)
                    return;
            }
            byte b = (byte)pushbackq.dequeue();
            switch (state) {
                case 0:  
                    if (b != '=') {
                        byteq.enqueue(b);
                        break;  
                    } else {
                        state = 1;
                        break;
                    }
                case 1:  
                    if (b == '\r') {
                        state = 2;
                        break;
                    } else if ((b >= '0' && b <= '9') || (b >= 'A' && b <= 'F') || (b >= 'a' && b <= 'f')) {
                        state = 3;
                        msdChar = b;  
                        break;
                    } else if (b == '=') {
                        if (log.isWarnEnabled()) {
                            log.warn("Malformed MIME; got ==");
                        }
                        byteq.enqueue((byte)'=');
                        break;
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Malformed MIME; expected \\r or "
                                    + "[0-9A-Z], got " + b);
                        }
                        state = 0;
                        byteq.enqueue((byte)'=');
                        byteq.enqueue(b);
                        break;
                    }
                case 2:  
                    if (b == '\n') {
                        state = 0;
                        break;
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Malformed MIME; expected " 
                                    + (int)'\n' + ", got " + b);
                        }
                        state = 0;
                        byteq.enqueue((byte)'=');
                        byteq.enqueue((byte)'\r');
                        byteq.enqueue(b);
                        break;
                    }
                case 3:  
                    if ((b >= '0' && b <= '9') || (b >= 'A' && b <= 'F') || (b >= 'a' && b <= 'f')) {
                        byte msd = asciiCharToNumericValue(msdChar);
                        byte low = asciiCharToNumericValue(b);
                        state = 0;
                        byteq.enqueue((byte)((msd << 4) | low));
                        break;
                    } else {
                        if (log.isWarnEnabled()) {
                            log.warn("Malformed MIME; expected "
                                     + "[0-9A-Z], got " + b);
                        }
                        state = 0;
                        byteq.enqueue((byte)'=');
                        byteq.enqueue(msdChar);
                        byteq.enqueue(b);
                        break;
                    }
                default:  
                    log.error("Illegal state: " + state);
                    state = 0;
                    byteq.enqueue(b);
                    break;
            }
        }
    }
    private byte asciiCharToNumericValue(byte c) {
        if (c >= '0' && c <= '9') {
            return (byte)(c - '0');
        } else if (c >= 'A' && c <= 'Z') {
            return (byte)(0xA + (c - 'A'));
        } else if (c >= 'a' && c <= 'z') {
            return (byte)(0xA + (c - 'a'));
        } else {
            throw new IllegalArgumentException((char) c 
                    + " is not a hexadecimal digit");
        }
    }
}
