public class ABCOutputStream extends OutputStream {
    int len;
    int count = 0;
    char next = ABCInputStream.firstChar();
    ABCOutputStream(int len) {
        this.len = len;
    }
    public void write(int c) throws IOException {
        if (count >= len)
            throw new IOException("Wrote too many characters");
        if (c != next)
            throw new IOException("Wrong character written: "
                                  + "Expected '" + next + "', "
                                  + "got '" + (char)c + "'");
        next = ABCInputStream.nextChar(next);
        count++;
    }
    public void write(byte buf[], int off, int len) throws IOException {
        for (int i = off; i < off + len; i++)
            write(buf[i]);
    }
    public void close() throws IOException {
        if (len == 0)
            throw new IOException("Already closed");
        else if (count < len)
            throw new IOException("Wrote too few characters");
        len = 0;
    }
}
