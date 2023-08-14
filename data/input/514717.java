public class LineNumberInputStream extends FilterInputStream {
    private int lineNumber;
    private int markedLineNumber = -1;
    private int lastChar = -1;
    private int markedLastChar;
    public LineNumberInputStream(InputStream in) {
        super(in);
    }
    @Override
    public int available() throws IOException {
        return in.available() / 2 + (lastChar == -1 ? 0 : 1);
    }
    public int getLineNumber() {
        return lineNumber;
    }
    @Override
    public void mark(int readlimit) {
        in.mark(readlimit);
        markedLineNumber = lineNumber;
        markedLastChar = lastChar;
    }
    @SuppressWarnings("fallthrough")
    @Override
    public int read() throws IOException {
        int currentChar = lastChar;
        if (currentChar == -1) {
            currentChar = in.read();
        } else {
            lastChar = -1;
        }
        switch (currentChar) {
            case '\r':
                currentChar = '\n';
                lastChar = in.read();
                if (lastChar == '\n') {
                    lastChar = -1;
                }
            case '\n':
                lineNumber++;
        }
        return currentChar;
    }
    @Override
    public int read(byte[] buffer, int offset, int length) throws IOException {
        if (offset > buffer.length || offset < 0) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K002e", offset)); 
        } 
        if (length < 0 || length > buffer.length - offset) {
            throw new ArrayIndexOutOfBoundsException(Msg.getString("K0031", length)); 
        }
        for (int i = 0; i < length; i++) {
            int currentChar;
            try {
                currentChar = read();
            } catch (IOException e) {
                if (i != 0) {
                    return i;
                }
                throw e;
            }
            if (currentChar == -1) {
                return i == 0 ? -1 : i;
            }
            buffer[offset + i] = (byte) currentChar;
        }
        return length;
    }
    @Override
    public void reset() throws IOException {
        in.reset();
        lineNumber = markedLineNumber;
        lastChar = markedLastChar;
    }
    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }
    @Override
    public long skip(long count) throws IOException {
        if (count <= 0) {
            return 0;
        }
        for (int i = 0; i < count; i++) {
            int currentChar = read();
            if (currentChar == -1) {
                return i;
            }
        }
        return count;
    }
}
