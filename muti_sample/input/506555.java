public class LineNumberReader extends BufferedReader {
    private int lineNumber;
    private int markedLineNumber = -1;
    private boolean lastWasCR;
    private boolean markedLastWasCR;
    public LineNumberReader(Reader in) {
        super(in);
    }
    public LineNumberReader(Reader in, int size) {
        super(in, size);
    }
    public int getLineNumber() {
        synchronized (lock) {
            return lineNumber;
        }
    }
    @Override
    public void mark(int readlimit) throws IOException {
        synchronized (lock) {
            super.mark(readlimit);
            markedLineNumber = lineNumber;
            markedLastWasCR = lastWasCR;
        }
    }
    @SuppressWarnings("fallthrough")
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            int ch = super.read();
            if (ch == '\n' && lastWasCR) {
                ch = super.read();
            }
            lastWasCR = false;
            switch (ch) {
                case '\r':
                    ch = '\n';
                    lastWasCR = true;
                case '\n':
                    lineNumber++;
            }
            return ch;
        }
    }
    @Override
    public int read(char[] buffer, int offset, int count) throws IOException {
        synchronized (lock) {
            int read = super.read(buffer, offset, count);
            if (read == -1) {
                return -1;
            }
            for (int i = 0; i < read; i++) {
                char ch = buffer[offset + i];
                if (ch == '\r') {
                    lineNumber++;
                    lastWasCR = true;
                } else if (ch == '\n') {
                    if (!lastWasCR) {
                        lineNumber++;
                    }
                    lastWasCR = false;
                } else {
                    lastWasCR = false;
                }
            }
            return read;
        }
    }
    @Override
    public String readLine() throws IOException {
        synchronized (lock) {
            if (lastWasCR) {
                chompNewline();
                lastWasCR = false;
            }
            String result = super.readLine();
            if (result != null) {
                lineNumber++;
            }
            return result;
        }
    }
    @Override
    public void reset() throws IOException {
        synchronized (lock) {
            super.reset();
            lineNumber = markedLineNumber;
            lastWasCR = markedLastWasCR;
        }
    }
    public void setLineNumber(int lineNumber) {
        synchronized (lock) {
            this.lineNumber = lineNumber;
        }
    }
    @Override
    public long skip(long count) throws IOException {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        synchronized (lock) {
            for (int i = 0; i < count; i++) {
                if (read() == -1) {
                    return i;
                }
            }
            return count;
        }
    }
}
