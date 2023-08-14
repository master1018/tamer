public class TelnetInputStream extends FilterInputStream {
    boolean         stickyCRLF = false;
    boolean         seenCR = false;
    public boolean  binaryMode = false;
    public TelnetInputStream(InputStream fd, boolean binary) {
        super(fd);
        binaryMode = binary;
    }
    public void setStickyCRLF(boolean on) {
        stickyCRLF = on;
    }
    public int read() throws IOException {
        if (binaryMode)
            return super.read();
        int c;
        if (seenCR) {
            seenCR = false;
            return '\n';
        }
        if ((c = super.read()) == '\r') {    
            switch (c = super.read()) {
            default:
            case -1:                        
                throw new TelnetProtocolException("misplaced CR in input");
            case 0:                         
                return '\r';
            case '\n':                      
                if (stickyCRLF) {
                    seenCR = true;
                    return '\r';
                } else {
                    return '\n';
                }
            }
        }
        return c;
    }
    public int read(byte bytes[]) throws IOException {
        return read(bytes, 0, bytes.length);
    }
    public int read(byte bytes[], int off, int length) throws IOException {
        if (binaryMode)
            return super.read(bytes, off, length);
        int c;
        int offStart = off;
        while (--length >= 0) {
            c = read();
            if (c == -1)
                break;
            bytes[off++] = (byte)c;
        }
        return (off > offStart) ? off - offStart : -1;
    }
}
