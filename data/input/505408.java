public class SSLBufferedInput extends SSLInputStream {
    private ByteBuffer in;
    private int bytik;
    private int consumed = 0;
    protected SSLBufferedInput() {}
    protected void setSourceBuffer(ByteBuffer in) {
        consumed = 0;
        this.in = in;
    }
    @Override
    public int available() throws IOException {
        return in.remaining();
    }
    protected int consumed() {
        return consumed;
    }
    @Override
    public int read() throws IOException {
        bytik = in.get() & 0x00FF;
        consumed ++;
        return bytik;
    }
}
