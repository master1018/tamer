class CompressOutputStream extends FilterOutputStream
    implements CompressConstants
{
    public CompressOutputStream(OutputStream out) {
        super(out);
    }
    int buf[] = new int[5];
    int bufPos = 0;
    public void write(int b) throws IOException {
        b &= 0xFF;                      
        int pos = codeTable.indexOf((char)b);
        if (pos != -1)
            writeCode(BASE + pos);
        else {
            writeCode(RAW);
            writeCode(b >> 4);
            writeCode(b & 0xF);
        }
    }
    public void write(byte b[], int off, int len) throws IOException {
        for (int i = 0; i < len; i++)
            write(b[off + i]);
    }
    public void flush() throws IOException {
        while (bufPos > 0)
            writeCode(NOP);
    }
    private void writeCode(int c) throws IOException {
        buf[bufPos++] = c;
        if (bufPos == 5) {      
            int pack = (buf[0] << 24) | (buf[1] << 18) | (buf[2] << 12) |
                       (buf[3] << 6) | buf[4];
            out.write((pack >>> 24) & 0xFF);
            out.write((pack >>> 16) & 0xFF);
            out.write((pack >>> 8)  & 0xFF);
            out.write((pack >>> 0)  & 0xFF);
            bufPos = 0;
        }
    }
}
