public class TelnetOutputStream extends BufferedOutputStream {
    boolean         stickyCRLF = false;
    boolean         seenCR = false;
    public boolean  binaryMode = false;
    public TelnetOutputStream(OutputStream fd, boolean binary) {
        super(fd);
        binaryMode = binary;
    }
    public void setStickyCRLF(boolean on) {
        stickyCRLF = on;
    }
    public void write(int c) throws IOException {
        if (binaryMode) {
            super.write(c);
            return;
        }
        if (seenCR) {
            if (c != '\n')
                super.write(0);
            super.write(c);
            if (c != '\r')
                seenCR = false;
        } else { 
            if (c == '\n') {
                super.write('\r');
                super.write('\n');
                return;
            }
            if (c == '\r') {
                if (stickyCRLF)
                    seenCR = true;
                else {
                    super.write('\r');
                    c = 0;
                }
            }
            super.write(c);
        }
    }
    public void write(byte bytes[], int off, int length) throws IOException {
        if (binaryMode) {
            super.write(bytes, off, length);
            return;
        }
        while (--length >= 0) {
            write(bytes[off++]);
        }
    }
}
