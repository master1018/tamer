class ABCReader extends Reader {
    int len;
    int count = 0;
    char next = 'a';
    ABCReader(int len) {
        this.len = len;
    }
    public int read() {
        if (count >= len)
            return -1;
        char c = next;
        if (next == 'z')
            next = '0';
        else if (next == '9')
            next = 'a';
        else
            next++;
        count++;
        return c;
    }
    public int read(char cbuf[], int off, int len) {
        for (int i = off; i < off + len; i++) {
            int c = read();
            if (c == -1) {
                if (i > off)
                    return i - off;
                else
                    return -1;
            }
            cbuf[i] = (char) c;
        }
        return len;
    }
    public void close() {
        len = 0;
    }
}
