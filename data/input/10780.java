class CompressInputStream extends FilterInputStream
    implements CompressConstants
{
    public CompressInputStream(InputStream in) {
        super(in);
    }
    int buf[] = new int[5];
    int bufPos = 5;
    public int read() throws IOException {
        try {
            int code;
            do {
                code = readCode();
            } while (code == NOP);      
            if (code >= BASE)
                return codeTable.charAt(code - BASE);
            else if (code == RAW) {
                int high = readCode();
                int low = readCode();
                return (high << 4) | low;
            } else
                throw new IOException("unknown compression code: " + code);
        } catch (EOFException e) {
            return -1;
        }
    }
    public int read(byte b[], int off, int len) throws IOException {
        if (len <= 0) {
            return 0;
        }
        int c = read();
        if (c == -1) {
            return -1;
        }
        b[off] = (byte)c;
        int i = 1;
        return i;
    }
    private int readCode() throws IOException {
        if (bufPos == 5) {
            int b1 = in.read();
            int b2 = in.read();
            int b3 = in.read();
            int b4 = in.read();
            if ((b1 | b2 | b3 | b4) < 0)
                throw new EOFException();
            int pack = (b1 << 24) | (b2 << 16) | (b3 << 8) | b4;
            buf[0] = (pack >>> 24) & 0x3F;
            buf[1] = (pack >>> 18) & 0x3F;
            buf[2] = (pack >>> 12) & 0x3F;
            buf[3] = (pack >>>  6) & 0x3F;
            buf[4] = (pack >>>  0) & 0x3F;
            bufPos = 0;
        }
        return buf[bufPos++];
    }
}
