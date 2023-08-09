public class InputStreamReader extends Reader {
    private InputStream in;
    private static final int BUFFER_SIZE = 8192;
    private boolean endOfInput = false;
    CharsetDecoder decoder;
    ByteBuffer bytes = ByteBuffer.allocate(BUFFER_SIZE);
    public InputStreamReader(InputStream in) {
        super(in);
        this.in = in;
        String encoding = AccessController
                .doPrivileged(new PriviAction<String>(
                        "file.encoding", "ISO8859_1")); 
        decoder = Charset.forName(encoding).newDecoder().onMalformedInput(
                CodingErrorAction.REPLACE).onUnmappableCharacter(
                CodingErrorAction.REPLACE);
        bytes.limit(0);
    }
    public InputStreamReader(InputStream in, final String enc)
            throws UnsupportedEncodingException {
        super(in);
        if (enc == null) {
            throw new NullPointerException();
        }
        this.in = in;
        try {
            decoder = Charset.forName(enc).newDecoder().onMalformedInput(
                    CodingErrorAction.REPLACE).onUnmappableCharacter(
                    CodingErrorAction.REPLACE);
        } catch (IllegalArgumentException e) {
            throw (UnsupportedEncodingException)
                    new UnsupportedEncodingException().initCause(e);
        }
        bytes.limit(0);
    }
    public InputStreamReader(InputStream in, CharsetDecoder dec) {
        super(in);
        dec.averageCharsPerByte();
        this.in = in;
        decoder = dec;
        bytes.limit(0);
    }
    public InputStreamReader(InputStream in, Charset charset) {
        super(in);
        this.in = in;
        decoder = charset.newDecoder().onMalformedInput(
                CodingErrorAction.REPLACE).onUnmappableCharacter(
                CodingErrorAction.REPLACE);
        bytes.limit(0);
    }
    @Override
    public void close() throws IOException {
        synchronized (lock) {
            if (decoder != null) {
                decoder.reset();
            }
            decoder = null;
            if (in != null) {
                in.close();
                in = null;
            }
        }
    }
    public String getEncoding() {
        if (!isOpen()) {
            return null;
        }
        return HistoricalNamesUtil.getHistoricalName(decoder.charset().name());
    }
    @Override
    public int read() throws IOException {
        synchronized (lock) {
            if (!isOpen()) {
                throw new IOException(Msg.getString("K0070")); 
            }
            char buf[] = new char[1];
            return read(buf, 0, 1) != -1 ? buf[0] : -1;
        }
    }
    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        synchronized (lock) {
            if (!isOpen()) {
                throw new IOException(Msg.getString("K0070")); 
            }
            if (buf == null) {
                throw new NullPointerException(Msg.getString("K0047")); 
            }
            if ((offset | length) < 0 || offset > buf.length - length) {
                throw new IndexOutOfBoundsException(Msg.getString("K002f")); 
            }
            if (length == 0) {
                return 0;
            }
            CharBuffer out = CharBuffer.wrap(buf, offset, length);
            CoderResult result = CoderResult.UNDERFLOW;
            boolean needInput = !bytes.hasRemaining();
            while (out.hasRemaining()) {
                if (needInput) {
                    try {
                        if ((in.available() == 0) 
                            && (out.position() > offset)) {
                            break;
                        }
                    } catch (IOException e) {
                    }
                    int to_read = bytes.capacity() - bytes.limit();
                    int off = bytes.arrayOffset() + bytes.limit();
                    int was_red = in.read(bytes.array(), off, to_read);
                    if (was_red == -1) {
                        endOfInput = true;
                        break;
                    } else if (was_red == 0) {
                        break;
                    }
                    bytes.limit(bytes.limit() + was_red);
                    needInput = false;
                }
                result = decoder.decode(bytes, out, false);
                if (result.isUnderflow()) {
                    if (bytes.limit() == bytes.capacity()) {
                        bytes.compact();
                        bytes.limit(bytes.position());
                        bytes.position(0);
                    }
                    needInput = true;
                } else {
                    break;
                }
            }
            if (result == CoderResult.UNDERFLOW && endOfInput) {
                result = decoder.decode(bytes, out, true);
                decoder.flush(out);
                decoder.reset();
            }
            if (result.isMalformed()) {
                throw new MalformedInputException(result.length());
            } else if (result.isUnmappable()) {
                throw new UnmappableCharacterException(result.length());
            }
            return out.position() - offset == 0 ? -1 : out.position() - offset;
        }
    }
    private boolean isOpen() {
        return in != null;
    }
    @Override
    public boolean ready() throws IOException {
        synchronized (lock) {
            if (in == null) {
                throw new IOException(Msg.getString("K0070")); 
            }
            try {
                return bytes.hasRemaining() || in.available() > 0;
            } catch (IOException e) {
                return false;
            }
        }
    }
}
