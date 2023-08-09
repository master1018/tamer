public class ProxyWriter extends FilterWriter {
    public ProxyWriter(Writer proxy) {
        super(proxy);
    }
    public void write(int idx) throws IOException {
        out.write(idx);
    }
    public void write(char[] chr) throws IOException {
        out.write(chr);
    }
    public void write(char[] chr, int st, int end) throws IOException {
        out.write(chr, st, end);
    }
    public void write(String str) throws IOException {
        out.write(str);
    }
    public void write(String str, int st, int end) throws IOException {
        out.write(str, st, end);
    }
    public void flush() throws IOException {
        out.flush();
    }
    public void close() throws IOException {
        out.close();
    }
}
