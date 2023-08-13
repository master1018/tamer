public class EOLConvertingOutputStream extends FilterOutputStream {
    int lastChar;
    public EOLConvertingOutputStream(OutputStream out) {
        super(out);
    }
    @Override
    public void write(int oneByte) throws IOException {
        if (oneByte == '\n') {
            if (lastChar != '\r') {
                super.write('\r');
            }
        }
        super.write(oneByte);
        lastChar = oneByte;
    }
    @Override
    public void flush() throws IOException {
        if (lastChar == '\r') {
            super.write('\n');
            lastChar = '\n';
        }
        super.flush();
    }
}
