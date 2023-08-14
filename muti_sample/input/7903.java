public class StreamDecoder extends Reader
{
    private static final int MIN_BYTE_BUFFER_SIZE = 32;
    private static final int DEFAULT_BYTE_BUFFER_SIZE = 8192;
    private volatile boolean isOpen = true;
    private void ensureOpen() throws IOException {
        if (!isOpen)
            throw new IOException("Stream closed");
    }
    private boolean haveLeftoverChar = false;
    private char leftoverChar;
    public static StreamDecoder forInputStreamReader(InputStream in,
                                                     Object lock,
                                                     String charsetName)
        throws UnsupportedEncodingException
    {
        String csn = charsetName;
        if (csn == null)
            csn = Charset.defaultCharset().name();
        try {
            if (Charset.isSupported(csn))
                return new StreamDecoder(in, lock, Charset.forName(csn));
        } catch (IllegalCharsetNameException x) { }
        throw new UnsupportedEncodingException (csn);
    }
    public static StreamDecoder forInputStreamReader(InputStream in,
                                                     Object lock,
                                                     Charset cs)
    {
        return new StreamDecoder(in, lock, cs);
    }
    public static StreamDecoder forInputStreamReader(InputStream in,
                                                     Object lock,
                                                     CharsetDecoder dec)
    {
        return new StreamDecoder(in, lock, dec);
    }
    public static StreamDecoder forDecoder(ReadableByteChannel ch,
                                           CharsetDecoder dec,
                                           int minBufferCap)
    {
        return new StreamDecoder(ch, dec, minBufferCap);
    }
    public String getEncoding() {
        if (isOpen())
            return encodingName();
        return null;
    }
    public int read() throws IOException {
        return read0();
    }
    private int read0() throws IOException {
        synchronized (lock) {
            if (haveLeftoverChar) {
                haveLeftoverChar = false;
                return leftoverChar;
            }
            char cb[] = new char[2];
            int n = read(cb, 0, 2);
            switch (n) {
            case -1:
                return -1;
            case 2:
                leftoverChar = cb[1];
                haveLeftoverChar = true;
            case 1:
                return cb[0];
            default:
                assert false : n;
                return -1;
            }
        }
    }
    public int read(char cbuf[], int offset, int length) throws IOException {
        int off = offset;
        int len = length;
        synchronized (lock) {
            ensureOpen();
            if ((off < 0) || (off > cbuf.length) || (len < 0) ||
                ((off + len) > cbuf.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            }
            if (len == 0)
                return 0;
            int n = 0;
            if (haveLeftoverChar) {
                cbuf[off] = leftoverChar;
                off++; len--;
                haveLeftoverChar = false;
                n = 1;
                if ((len == 0) || !implReady())
                    return n;
            }
            if (len == 1) {
                int c = read0();
                if (c == -1)
                    return (n == 0) ? -1 : n;
                cbuf[off] = (char)c;
                return n + 1;
            }
            return n + implRead(cbuf, off, off + len);
        }
    }
    public boolean ready() throws IOException {
        synchronized (lock) {
            ensureOpen();
            return haveLeftoverChar || implReady();
        }
    }
    public void close() throws IOException {
        synchronized (lock) {
            if (!isOpen)
                return;
            implClose();
            isOpen = false;
        }
    }
    private boolean isOpen() {
        return isOpen;
    }
    private static volatile boolean channelsAvailable = true;
    private static FileChannel getChannel(FileInputStream in) {
        if (!channelsAvailable)
            return null;
        try {
            return in.getChannel();
        } catch (UnsatisfiedLinkError x) {
            channelsAvailable = false;
            return null;
        }
    }
    private Charset cs;
    private CharsetDecoder decoder;
    private ByteBuffer bb;
    private InputStream in;
    private ReadableByteChannel ch;
    StreamDecoder(InputStream in, Object lock, Charset cs) {
        this(in, lock,
         cs.newDecoder()
         .onMalformedInput(CodingErrorAction.REPLACE)
         .onUnmappableCharacter(CodingErrorAction.REPLACE));
    }
    StreamDecoder(InputStream in, Object lock, CharsetDecoder dec) {
        super(lock);
        this.cs = dec.charset();
        this.decoder = dec;
        if (false && in instanceof FileInputStream) {
        ch = getChannel((FileInputStream)in);
        if (ch != null)
            bb = ByteBuffer.allocateDirect(DEFAULT_BYTE_BUFFER_SIZE);
        }
        if (ch == null) {
        this.in = in;
        this.ch = null;
        bb = ByteBuffer.allocate(DEFAULT_BYTE_BUFFER_SIZE);
        }
        bb.flip();                      
    }
    StreamDecoder(ReadableByteChannel ch, CharsetDecoder dec, int mbc) {
        this.in = null;
        this.ch = ch;
        this.decoder = dec;
        this.cs = dec.charset();
        this.bb = ByteBuffer.allocate(mbc < 0
                                  ? DEFAULT_BYTE_BUFFER_SIZE
                                  : (mbc < MIN_BYTE_BUFFER_SIZE
                                     ? MIN_BYTE_BUFFER_SIZE
                                     : mbc));
        bb.flip();
    }
    private int readBytes() throws IOException {
        bb.compact();
        try {
        if (ch != null) {
            int n = ch.read(bb);
            if (n < 0)
                return n;
        } else {
            int lim = bb.limit();
            int pos = bb.position();
            assert (pos <= lim);
            int rem = (pos <= lim ? lim - pos : 0);
            assert rem > 0;
            int n = in.read(bb.array(), bb.arrayOffset() + pos, rem);
            if (n < 0)
                return n;
            if (n == 0)
                throw new IOException("Underlying input stream returned zero bytes");
            assert (n <= rem) : "n = " + n + ", rem = " + rem;
            bb.position(pos + n);
        }
        } finally {
        bb.flip();
        }
        int rem = bb.remaining();
            assert (rem != 0) : rem;
            return rem;
    }
    int implRead(char[] cbuf, int off, int end) throws IOException {
        assert (end - off > 1);
        CharBuffer cb = CharBuffer.wrap(cbuf, off, end - off);
        if (cb.position() != 0)
        cb = cb.slice();
        boolean eof = false;
        for (;;) {
        CoderResult cr = decoder.decode(bb, cb, eof);
        if (cr.isUnderflow()) {
            if (eof)
                break;
            if (!cb.hasRemaining())
                break;
            if ((cb.position() > 0) && !inReady())
                break;          
            int n = readBytes();
            if (n < 0) {
                eof = true;
                if ((cb.position() == 0) && (!bb.hasRemaining()))
                    break;
                decoder.reset();
            }
            continue;
        }
        if (cr.isOverflow()) {
            assert cb.position() > 0;
            break;
        }
        cr.throwException();
        }
        if (eof) {
        decoder.reset();
        }
        if (cb.position() == 0) {
            if (eof)
                return -1;
            assert false;
        }
        return cb.position();
    }
    String encodingName() {
        return ((cs instanceof HistoricallyNamedCharset)
            ? ((HistoricallyNamedCharset)cs).historicalName()
            : cs.name());
    }
    private boolean inReady() {
        try {
        return (((in != null) && (in.available() > 0))
                || (ch instanceof FileChannel)); 
        } catch (IOException x) {
        return false;
        }
    }
    boolean implReady() {
            return bb.hasRemaining() || inReady();
    }
    void implClose() throws IOException {
        if (ch != null)
        ch.close();
        else
        in.close();
    }
}
