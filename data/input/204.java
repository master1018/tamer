public class QDecoderStream extends QPDecoderStream {
    public QDecoderStream(InputStream in) {
        super(in);
    }
    public int read() throws IOException {
        int c = in.read();
        if (c == '_') return ' '; else if (c == '=') {
            ba[0] = (byte) in.read();
            ba[1] = (byte) in.read();
            try {
                return 0;
            } catch (NumberFormatException nex) {
                throw new IOException("Error in QP stream " + nex.getMessage());
            }
        } else return c;
    }
}
