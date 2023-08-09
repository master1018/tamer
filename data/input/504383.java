public class SSLStreamedInput extends SSLInputStream {
    private InputStream in;
    public SSLStreamedInput(InputStream in) {
        this.in = in;
    }
    @Override
    public int available() throws IOException {
        return in.available();
    }
    @Override
    public int read() throws IOException {
        int res = in.read();
        if (res < 0) {
            throw new EndOfSourceException();
        }
        return res;
    }
}
