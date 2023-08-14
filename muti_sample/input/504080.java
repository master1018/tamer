public class MimeBoundaryInputStream extends InputStream {
    private PushbackInputStream s = null;
    private byte[] boundary = null;
    private boolean first = true;
    private boolean eof = false;
    private boolean parenteof = false;
    private boolean moreParts = true;
    public MimeBoundaryInputStream(InputStream s, String boundary) 
            throws IOException {
        this.s = new PushbackInputStream(s, boundary.length() + 4);
        boundary = "--" + boundary;
        this.boundary = new byte[boundary.length()];
        for (int i = 0; i < this.boundary.length; i++) {
            this.boundary[i] = (byte) boundary.charAt(i);
        }
        int b = read();
        if (b != -1) {
            this.s.unread(b);
        }
    }
    public void close() throws IOException {
        s.close();
    }
    public boolean hasMoreParts() {
        return moreParts;
    }
    public boolean parentEOF() {
        return parenteof;
    }
    public void consume() throws IOException {
        while (read() != -1) {
        }
    }
    public int read() throws IOException {
        if (eof) {
            return -1;
        }
        if (first) {
            first = false;
            if (matchBoundary()) {
                return -1;
            }
        }
        int b1 = s.read();
        int b2 = s.read();
        if (b1 == '\r' && b2 == '\n') {
            if (matchBoundary()) {
                return -1;
            }
        }
        if (b2 != -1) {
            s.unread(b2);
        }
        parenteof = b1 == -1;
        eof = parenteof;
        return b1;
    }
    private boolean matchBoundary() throws IOException {
        for (int i = 0; i < boundary.length; i++) {
            int b = s.read();
            if (b != boundary[i]) {
                if (b != -1) {
                    s.unread(b);
                }
                for (int j = i - 1; j >= 0; j--) {
                    s.unread(boundary[j]);
                }
                return false;
            }
        }
        int prev = s.read();
        int curr = s.read();
        moreParts = !(prev == '-' && curr == '-');
        do {
            if (curr == '\n' && prev == '\r') {
                break;
            }
            prev = curr;
        } while ((curr = s.read()) != -1);
        if (curr == -1) {
            moreParts = false;
            parenteof = true;
        }
        eof = true;
        return true;
    }
}
