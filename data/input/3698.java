public class UUDecoder extends CharacterDecoder {
    public String bufferName;
    public int mode;
    protected int bytesPerAtom() {
        return (3);
    }
    protected int bytesPerLine() {
        return (45);
    }
    private byte decoderBuffer[] = new byte[4];
    protected void decodeAtom(PushbackInputStream inStream, OutputStream outStream, int l)
        throws IOException {
        int i, c1, c2, c3, c4;
        int a, b, c;
        StringBuffer x = new StringBuffer();
        for (i = 0; i < 4; i++) {
            c1 = inStream.read();
            if (c1 == -1) {
                throw new CEStreamExhausted();
            }
            x.append((char)c1);
            decoderBuffer[i] = (byte) ((c1 - ' ') & 0x3f);
        }
        a = ((decoderBuffer[0] << 2) & 0xfc) | ((decoderBuffer[1] >>> 4) & 3);
        b = ((decoderBuffer[1] << 4) & 0xf0) | ((decoderBuffer[2] >>> 2) & 0xf);
        c = ((decoderBuffer[2] << 6) & 0xc0) | (decoderBuffer[3] & 0x3f);
        outStream.write((byte)(a & 0xff));
        if (l > 1) {
            outStream.write((byte)( b & 0xff));
        }
        if (l > 2) {
            outStream.write((byte)(c&0xff));
        }
    }
    protected void decodeBufferPrefix(PushbackInputStream inStream, OutputStream outStream) throws IOException {
        int     c;
        StringBuffer q = new StringBuffer(32);
        String r;
        boolean sawNewLine;
        sawNewLine = true;
        while (true) {
            c = inStream.read();
            if (c == -1) {
                throw new CEFormatException("UUDecoder: No begin line.");
            }
            if ((c == 'b')  && sawNewLine){
                c = inStream.read();
                if (c == 'e') {
                    break;
                }
            }
            sawNewLine = (c == '\n') || (c == '\r');
        }
        while ((c != '\n') && (c != '\r')) {
            c = inStream.read();
            if (c == -1) {
                throw new CEFormatException("UUDecoder: No begin line.");
            }
            if ((c != '\n') && (c != '\r')) {
                q.append((char)c);
            }
        }
        r = q.toString();
        if (r.indexOf(' ') != 3) {
                throw new CEFormatException("UUDecoder: Malformed begin line.");
        }
        mode = Integer.parseInt(r.substring(4,7));
        bufferName = r.substring(r.indexOf(' ',6)+1);
        if (c == '\r') {
            c = inStream.read ();
            if ((c != '\n') && (c != -1))
                inStream.unread (c);
        }
    }
    protected int decodeLinePrefix(PushbackInputStream inStream, OutputStream outStream) throws IOException {
        int     c;
        c = inStream.read();
        if (c == ' ') {
            c = inStream.read(); 
            c = inStream.read(); 
            if ((c != '\n') && (c != -1))
                inStream.unread (c);
            throw new CEStreamExhausted();
        } else if (c == -1) {
            throw new CEFormatException("UUDecoder: Short Buffer.");
        }
        c = (c - ' ') & 0x3f;
        if (c > bytesPerLine()) {
            throw new CEFormatException("UUDecoder: Bad Line Length.");
        }
        return (c);
    }
    protected void decodeLineSuffix(PushbackInputStream inStream, OutputStream outStream) throws IOException {
        int c;
        while (true) {
            c = inStream.read();
            if (c == -1) {
                throw new CEStreamExhausted();
            }
            if (c == '\n') {
                break;
            }
            if (c == '\r') {
                c = inStream.read();
                if ((c != '\n') && (c != -1)) {
                    inStream.unread (c);
                }
                break;
            }
        }
    }
    protected void decodeBufferSuffix(PushbackInputStream inStream, OutputStream outStream) throws IOException  {
        int     c;
        c = inStream.read(decoderBuffer);
        if ((decoderBuffer[0] != 'e') || (decoderBuffer[1] != 'n') ||
            (decoderBuffer[2] != 'd')) {
            throw new CEFormatException("UUDecoder: Missing 'end' line.");
        }
    }
}
