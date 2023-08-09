public class SaslInputStream extends InputStream {
    private static final boolean debug = false;
    private byte[] saslBuffer;  
    private byte[] lenBuf = new byte[4];  
    private byte[] buf = new byte[0];   
    private int bufPos = 0;             
    private InputStream in;             
    private SaslClient sc;
    private int recvMaxBufSize = 65536;
    SaslInputStream(SaslClient sc, InputStream in) throws SaslException {
        super();
        this.in = in;
        this.sc = sc;
        String str = (String) sc.getNegotiatedProperty(Sasl.MAX_BUFFER);
        if (str != null) {
            try {
                recvMaxBufSize = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                throw new SaslException(Sasl.MAX_BUFFER +
                    " property must be numeric string: " + str);
            }
        }
        saslBuffer = new byte[recvMaxBufSize];
    }
    public int read() throws IOException {
        byte[] inBuf = new byte[1];
        int count = read(inBuf, 0, 1);
        if (count > 0) {
            return inBuf[0];
        } else {
            return -1;
        }
    }
    public int read(byte[] inBuf, int start, int count) throws IOException {
        if (bufPos >= buf.length) {
            int actual = fill();   
            while (actual == 0) {  
                actual = fill();
            }
            if (actual == -1) {
                return -1;    
            }
        }
        int avail = buf.length - bufPos;
        if (count > avail) {
            System.arraycopy(buf, bufPos, inBuf, start, avail);
            bufPos = buf.length;
            return avail;
        } else {
            System.arraycopy(buf, bufPos, inBuf, start, count);
            bufPos += count;
            return count;
        }
    }
    private int fill() throws IOException {
        int actual = readFully(lenBuf, 4);
        if (actual != 4) {
            return -1;
        }
        int len = networkByteOrderToInt(lenBuf, 0, 4);
        if (len > recvMaxBufSize) {
            throw new IOException(
                len + "exceeds the negotiated receive buffer size limit:" +
                recvMaxBufSize);
        }
        if (debug) {
            System.err.println("reading " + len + " bytes from network");
        }
        actual = readFully(saslBuffer, len);
        if (actual != len) {
            throw new EOFException("Expecting to read " + len +
                " bytes but got " + actual + " bytes before EOF");
        }
        buf = sc.unwrap(saslBuffer, 0, len);
        bufPos = 0;
        return buf.length;
    }
    private int readFully(byte[] inBuf, int total) throws IOException {
        int count, pos = 0;
        if (debug) {
            System.err.println("readFully " + total + " from " + in);
        }
        while (total > 0) {
            count = in.read(inBuf, pos, total);
            if (debug) {
                System.err.println("readFully read " + count);
            }
            if (count == -1 ) {
                return (pos == 0? -1 : pos);
            }
            pos += count;
            total -= count;
        }
        return pos;
    }
    public int available() throws IOException {
        return buf.length - bufPos;
    }
    public void close() throws IOException {
        SaslException save = null;
        try {
            sc.dispose(); 
        } catch (SaslException e) {
            save = e;
        }
        in.close();  
        if (save != null) {
            throw save;
        }
    }
    private static int networkByteOrderToInt(byte[] buf, int start, int count) {
        if (count > 4) {
            throw new IllegalArgumentException("Cannot handle more than 4 bytes");
        }
        int answer = 0;
        for (int i = 0; i < count; i++) {
            answer <<= 8;
            answer |= ((int)buf[start+i] & 0xff);
        }
        return answer;
    }
}
