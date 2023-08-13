public class ChunkedInputStream extends InputStream {
    private SessionInputBuffer in;
    private final CharArrayBuffer buffer;
    private int chunkSize;
    private int pos;
    private boolean bof = true;
    private boolean eof = false;
    private boolean closed = false;
    private Header[] footers = new Header[] {};
    public ChunkedInputStream(final SessionInputBuffer in) {
        super();
        if (in == null) {
            throw new IllegalArgumentException("Session input buffer may not be null");
        }
        this.in = in;
        this.pos = 0;
        this.buffer = new CharArrayBuffer(16);
    }
    public int read() throws IOException {
        if (this.closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        if (this.eof) {
            return -1;
        } 
        if (this.pos >= this.chunkSize) {
            nextChunk();
            if (this.eof) { 
                return -1;
            }
        }
        pos++;
        return in.read();
    }
    public int read (byte[] b, int off, int len) throws IOException {
        if (closed) {
            throw new IOException("Attempted read from closed stream.");
        }
        if (eof) { 
            return -1;
        }
        if (pos >= chunkSize) {
            nextChunk();
            if (eof) { 
                return -1;
            }
        }
        len = Math.min(len, chunkSize - pos);
        int count = in.read(b, off, len);
        pos += count;
        return count;
    }
    public int read (byte[] b) throws IOException {
        return read(b, 0, b.length);
    }
    private void nextChunk() throws IOException {
        chunkSize = getChunkSize();
        if (chunkSize < 0) {
            throw new MalformedChunkCodingException("Negative chunk size");
        }
        bof = false;
        pos = 0;
        if (chunkSize == 0) {
            eof = true;
            parseTrailerHeaders();
        }
    }
    private int getChunkSize() throws IOException {
        if (!bof) {
            int cr = in.read();
            int lf = in.read();
            if ((cr != HTTP.CR) || (lf != HTTP.LF)) { 
                throw new MalformedChunkCodingException(
                    "CRLF expected at end of chunk");
            }
        }
        this.buffer.clear();
        int i = this.in.readLine(this.buffer);
        if (i == -1) {
            throw new MalformedChunkCodingException(
                    "Chunked stream ended unexpectedly");
        }
        int separator = this.buffer.indexOf(';');
        if (separator < 0) {
            separator = this.buffer.length();
        }
        try {
            return Integer.parseInt(this.buffer.substringTrimmed(0, separator), 16);
        } catch (NumberFormatException e) {
            throw new MalformedChunkCodingException("Bad chunk header");
        }
    }
    private void parseTrailerHeaders() throws IOException {
        try {
            this.footers = AbstractMessageParser.parseHeaders
                (in, -1, -1, null);
        } catch (HttpException e) {
            IOException ioe = new MalformedChunkCodingException("Invalid footer: " 
                    + e.getMessage());
            ExceptionUtils.initCause(ioe, e); 
            throw ioe;
        }
    }
    public void close() throws IOException {
        if (!closed) {
            try {
                if (!eof) {
                    exhaustInputStream(this);
                }
            } finally {
                eof = true;
                closed = true;
            }
        }
    }
    public Header[] getFooters() {
        return (Header[])this.footers.clone();
    }
    static void exhaustInputStream(final InputStream inStream) throws IOException {
        byte buffer[] = new byte[1024];
        while (inStream.read(buffer) >= 0) {
            ;
        }
    }
}
