class LogCleanupReader extends Reader {
    private Reader reader;
    private char[] buffer = new char[4096];
    private int bufferCount;
    private int bufferOffset;
    private char[] line = new char[1024];
    private int index;
    private int length;
    private char[] one = new char[1];
    LogCleanupReader(Reader r) {
        reader = r;
    }
    static final private Matcher pattern = Pattern.compile(".+ compile_id='[0-9]+'.*( compile_id='[0-9]+)").matcher("");
    static final private Matcher pattern2 = Pattern.compile("' (C[12]) compile_id=").matcher("");
    static final private Matcher pattern3 = Pattern.compile("'(destroy_vm)/").matcher("");
    private void fill() throws IOException {
        rawFill();
        if (length != -1) {
            boolean changed = false;
            String s = new String(line, 0, length);
            String orig = s;
            pattern2.reset(s);
            if (pattern2.find()) {
                s = s.substring(0, pattern2.start(1)) + s.substring(pattern2.end(1) + 1);
                changed = true;
            }
            pattern.reset(s);
            if (pattern.lookingAt()) {
                s = s.substring(0, pattern.start(1)) + s.substring(pattern.end(1) + 1);
                changed = true;
            }
            pattern3.reset(s);
            if (pattern3.find()) {
                s = s.substring(0, pattern3.start(1)) + s.substring(pattern3.end(1));
                changed = true;
            }
            if (changed) {
                s.getChars(0, s.length(), line, 0);
                length = s.length();
            }
        }
    }
    private void rawFill() throws IOException {
        if (bufferCount == -1) {
            length = -1;
            return;
        }
        int i = 0;
        boolean fillNonEOL = true;
        outer:
        while (true) {
            if (fillNonEOL) {
                int p;
                for (p = bufferOffset; p < bufferCount; p++) {
                    char c = buffer[p];
                    if (c == '\r' || c == '\n') {
                        bufferOffset = p;
                        fillNonEOL = false;
                        continue outer;
                    }
                    if (i >= line.length) {
                        char[] newLine = new char[line.length * 2];
                        System.arraycopy(line, 0, newLine, 0, line.length);
                        line = newLine;
                    }
                    line[i++] = c;
                }
                bufferOffset = p;
            } else {
                int p;
                for (p = bufferOffset; p < bufferCount; p++) {
                    char c = buffer[p];
                    if (c != '\r' && c != '\n') {
                        bufferOffset = p;
                        length = i;
                        index = 0;
                        return;
                    }
                    line[i++] = c;
                }
                bufferOffset = p;
            }
            if (bufferCount == -1) {
                if (i == 0) {
                    length = -1;
                } else {
                    length = i;
                }
                index = 0;
                return;
            }
            if (bufferOffset != bufferCount) {
                System.out.println(bufferOffset);
                System.out.println(bufferCount);
                throw new InternalError("how did we get here");
            }
            bufferCount = reader.read(buffer, 0, buffer.length);
            bufferOffset = 0;
        }
    }
    public int read() throws java.io.IOException {
        read(one, 0, 1);
        return one[0];
    }
    public int read(char[] buffer) throws java.io.IOException {
        return read(buffer, 0, buffer.length);
    }
    public int read(char[] b, int off, int len) throws java.io.IOException {
        if (length == -1) {
            return -1;
        }
        if (index == length) {
            fill();
            if (length == -1) {
                return -1;
            }
        }
        int n = Math.min(length - index, Math.min(b.length - off, len));
        System.arraycopy(line, index, b, off, n);
        index += n;
        return n;
    }
    public long skip(long n) throws java.io.IOException {
        long result = n;
        while (n-- > 0) read();
        return result;
    }
    public boolean ready() throws java.io.IOException {
        return reader.ready() || (line != null && length > 0);
    }
    public boolean markSupported() {
        return false;
    }
    public void mark(int unused) throws java.io.IOException {
        throw new UnsupportedOperationException("mark not supported");
    }
    public void reset() throws java.io.IOException {
        reader.reset();
        line = null;
        index = 0;
    }
    public void close() throws java.io.IOException {
        reader.close();
        line = null;
        index = 0;
    }
}
