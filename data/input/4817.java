public class ABCInputStream extends InputStream {
    int len;
    int chunk;
    int count = 0;
    char next = firstChar();
    ABCInputStream(int len) {
        this(len, len);
    }
    ABCInputStream(int len, int chunk) {
        this.len = len;
        this.chunk = chunk;
    }
    static char firstChar() {
        return 'a';
    }
    static char nextChar(char c) {
        if (c == 'z')
            return '0';
        else if (c == '9')
            return 'a';
        else
            return (char)(c + 1);
    }
    public int read() {
        if (count >= len)
            return -1;
        char c = next;
        next = nextChar(c);
        count++;
        return (byte) c;
    }
    public int read(byte buf[], int off, int len) {
        int n = (len > chunk) ? chunk : len;
        for (int i = off; i < off + n; i++) {
            int c = read();
            if (c == -1) {
                if (i > off)
                    return i - off;
                else
                    return -1;
            }
            buf[i] = (byte) c;
        }
        return n;
    }
    public int available() {
        int remaining = len - count;
        return (remaining > chunk) ? chunk : remaining;
    }
    public void close() throws IOException {
        if (len == 0)
            throw new IOException("Already closed");
        len = 0;
    }
}
