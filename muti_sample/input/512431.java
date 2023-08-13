public class OutputStreamWriter extends Writer {
    private OutputStream out;
    private CharsetEncoder encoder;
    private ByteBuffer bytes = ByteBuffer.allocate(8192);
    public OutputStreamWriter(OutputStream out) {
        super(out);
        this.out = out;
        String encoding = AccessController
                .doPrivileged(new PriviAction<String>(
                        "file.encoding", "ISO8859_1")); 
        encoder = Charset.forName(encoding).newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }
    public OutputStreamWriter(OutputStream out, final String enc)
            throws UnsupportedEncodingException {
        super(out);
        if (enc == null) {
            throw new NullPointerException();
        }
        this.out = out;
        try {
            encoder = Charset.forName(enc).newEncoder();
        } catch (Exception e) {
            throw new UnsupportedEncodingException(enc);
        }
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }
    public OutputStreamWriter(OutputStream out, Charset cs) {
        super(out);
        this.out = out;
        encoder = cs.newEncoder();
        encoder.onMalformedInput(CodingErrorAction.REPLACE);
        encoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
    }
    public OutputStreamWriter(OutputStream out, CharsetEncoder enc) {
        super(out);
        enc.charset();
        this.out = out;
        encoder = enc;
    }
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (encoder != null) {
                encoder.flush(bytes);
                flush();
                out.flush();
                out.close();
                encoder = null;
                bytes = null;
            }
        }
    }
    @Override
    public void flush() throws IOException {
        synchronized (lock) {
            checkStatus();
            int position;
            if ((position = bytes.position()) > 0) {
                bytes.flip();
                out.write(bytes.array(), 0, position);
                bytes.clear();
            }
            out.flush();
        }
    }
    private void checkStatus() throws IOException {
        if (encoder == null) {
            throw new IOException(Msg.getString("K005d")); 
        }
    }
    public String getEncoding() {
        if (encoder == null) {
            return null;
        }
        return HistoricalNamesUtil.getHistoricalName(encoder.charset().name());
    }
    @Override
    public void write(char[] buf, int offset, int count) throws IOException {
        synchronized (lock) {
            checkStatus();
            if (buf == null) {
                throw new NullPointerException(Msg.getString("K0047")); 
            }
            if ((offset | count) < 0 || offset > buf.length - count) {
                throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
            }
            CharBuffer chars = CharBuffer.wrap(buf, offset, count);
            convert(chars);
        }
    }
    private void convert(CharBuffer chars) throws IOException {
        CoderResult result = encoder.encode(chars, bytes, true);
        while (true) {
            if (result.isError()) {
                throw new IOException(result.toString());
            } else if (result.isOverflow()) {
                flush();
                result = encoder.encode(chars, bytes, true);
                continue;
            }
            break;
        }
    }
    @Override
    public void write(int oneChar) throws IOException {
        synchronized (lock) {
            checkStatus();
            CharBuffer chars = CharBuffer.wrap(new char[] { (char) oneChar });
            convert(chars);
        }
    }
    @Override
    public void write(String str, int offset, int count) throws IOException {
        synchronized (lock) {
            if (str == null) {
                throw new NullPointerException(Msg.getString("K0047")); 
            }
            if ((offset | count) < 0 || offset > str.length() - count) {
                throw new StringIndexOutOfBoundsException(Msg.getString("K002f")); 
            }
            checkStatus();
            CharBuffer chars = CharBuffer.wrap(str, offset, count + offset);
            convert(chars);
        }
    }
    @Override boolean checkError() {
        return out.checkError();
    }
}
