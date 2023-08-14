public class UUEncoder extends CharacterEncoder {
    private String bufferName;
    private int mode;
    public UUEncoder() {
        bufferName = "encoder.buf";
        mode = 644;
    }
    public UUEncoder(String fname) {
        bufferName = fname;
        mode = 644;
    }
    public UUEncoder(String fname, int newMode) {
        bufferName = fname;
        mode = newMode;
    }
    protected int bytesPerAtom() {
        return (3);
    }
    protected int bytesPerLine() {
        return (45);
    }
    protected void encodeAtom(OutputStream outStream, byte data[], int offset, int len)
        throws IOException {
        byte    a, b = 1, c = 1;
        int     c1, c2, c3, c4;
        a = data[offset];
        if (len > 1) {
            b = data[offset+1];
        }
        if (len > 2) {
            c = data[offset+2];
        }
        c1 = (a >>> 2) & 0x3f;
        c2 = ((a << 4) & 0x30) | ((b >>> 4) & 0xf);
        c3 = ((b << 2) & 0x3c) | ((c >>> 6) & 0x3);
        c4 = c & 0x3f;
        outStream.write(c1 + ' ');
        outStream.write(c2 + ' ');
        outStream.write(c3 + ' ');
        outStream.write(c4 + ' ');
        return;
    }
    protected void encodeLinePrefix(OutputStream outStream, int length)
        throws IOException {
        outStream.write((length & 0x3f) + ' ');
    }
    protected void encodeLineSuffix(OutputStream outStream) throws IOException {
        pStream.println();
    }
    protected void encodeBufferPrefix(OutputStream a) throws IOException {
        super.pStream = new PrintStream(a);
        super.pStream.print("begin "+mode+" ");
        if (bufferName != null) {
            super.pStream.println(bufferName);
        } else {
            super.pStream.println("encoder.bin");
        }
        super.pStream.flush();
    }
    protected void encodeBufferSuffix(OutputStream a) throws IOException {
        super.pStream.println(" \nend");
        super.pStream.flush();
    }
}
