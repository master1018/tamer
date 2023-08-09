public class HexDumpEncoder extends CharacterEncoder {
    private int offset;
    private int thisLineLength;
    private int currentByte;
    private byte thisLine[] = new byte[16];
    static void hexDigit(PrintStream p, byte x) {
        char c;
        c = (char) ((x >> 4) & 0xf);
        if (c > 9)
            c = (char) ((c-10) + 'A');
        else
            c = (char)(c + '0');
        p.write(c);
        c = (char) (x & 0xf);
        if (c > 9)
            c = (char)((c-10) + 'A');
        else
            c = (char)(c + '0');
        p.write(c);
    }
    protected int bytesPerAtom() {
        return (1);
    }
    protected int bytesPerLine() {
        return (16);
    }
    protected void encodeBufferPrefix(OutputStream o) throws IOException {
        offset = 0;
        super.encodeBufferPrefix(o);
    }
    protected void encodeLinePrefix(OutputStream o, int len) throws IOException {
        hexDigit(pStream, (byte)((offset >>> 8) & 0xff));
        hexDigit(pStream, (byte)(offset & 0xff));
        pStream.print(": ");
        currentByte = 0;
        thisLineLength = len;
    }
    protected void encodeAtom(OutputStream o, byte buf[], int off, int len) throws IOException {
        thisLine[currentByte] = buf[off];
        hexDigit(pStream, buf[off]);
        pStream.print(" ");
        currentByte++;
        if (currentByte == 8)
            pStream.print("  ");
    }
    protected void encodeLineSuffix(OutputStream o) throws IOException {
        if (thisLineLength < 16) {
            for (int i = thisLineLength; i < 16; i++) {
                pStream.print("   ");
                if (i == 7)
                    pStream.print("  ");
            }
        }
        pStream.print(" ");
        for (int i = 0; i < thisLineLength; i++) {
            if ((thisLine[i] < ' ') || (thisLine[i] > 'z')) {
                pStream.print(".");
            } else {
                pStream.write(thisLine[i]);
            }
        }
        pStream.println();
        offset += thisLineLength;
    }
}
