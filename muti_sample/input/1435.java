class SaslOutputStream extends FilterOutputStream {
    private static final boolean debug = false;
    private byte[] lenBuf = new byte[4];  
    private int rawSendSize = 65536;
    private SaslClient sc;
    SaslOutputStream(SaslClient sc, OutputStream out) throws SaslException {
        super(out);
        this.sc = sc;
        if (debug) {
            System.err.println("SaslOutputStream: " + out);
        }
        String str = (String) sc.getNegotiatedProperty(Sasl.RAW_SEND_SIZE);
        if (str != null) {
            try {
                rawSendSize = Integer.parseInt(str);
            } catch (NumberFormatException e) {
                throw new SaslException(Sasl.RAW_SEND_SIZE +
                    " property must be numeric string: " + str);
            }
        }
    }
    public void write(int b) throws IOException {
        byte[] buffer = new byte[1];
        buffer[0] = (byte)b;
        write(buffer, 0, 1);
    }
    public void write(byte[] buffer, int offset, int total) throws IOException {
        int count;
        byte[] wrappedToken, saslBuffer;
        if (debug) {
            System.err.println("Total size: " + total);
        }
        for (int i = 0; i < total; i += rawSendSize) {
            count = (total - i) < rawSendSize ? (total - i) : rawSendSize;
            wrappedToken = sc.wrap(buffer, offset+i, count);
            intToNetworkByteOrder(wrappedToken.length, lenBuf, 0, 4);
            if (debug) {
                System.err.println("sending size: " + wrappedToken.length);
            }
            out.write(lenBuf, 0, 4);
            out.write(wrappedToken, 0, wrappedToken.length);
        }
    }
    public void close() throws IOException {
        SaslException save = null;
        try {
            sc.dispose();  
        } catch (SaslException e) {
            save = e;
        }
        super.close();  
        if (save != null) {
            throw save;
        }
    }
    private static void intToNetworkByteOrder(int num, byte[] buf, int start,
        int count) {
        if (count > 4) {
            throw new IllegalArgumentException("Cannot handle more than 4 bytes");
        }
        for (int i = count-1; i >= 0; i--) {
            buf[start+i] = (byte)(num & 0xff);
            num >>>= 8;
        }
    }
}
