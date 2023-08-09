public abstract class AbstractMessageWriter implements HttpMessageWriter {
    protected final SessionOutputBuffer sessionBuffer;    
    protected final CharArrayBuffer lineBuf;
    protected final LineFormatter lineFormatter;
    public AbstractMessageWriter(final SessionOutputBuffer buffer,
                                 final LineFormatter formatter,
                                 final HttpParams params) {
        super();
        if (buffer == null) {
            throw new IllegalArgumentException("Session input buffer may not be null");
        }
        this.sessionBuffer = buffer;
        this.lineBuf = new CharArrayBuffer(128);
        this.lineFormatter = (formatter != null) ?
            formatter : BasicLineFormatter.DEFAULT;
    }
    protected abstract void writeHeadLine(HttpMessage message)
        throws IOException
        ;
    public void write(
            final HttpMessage message) throws IOException, HttpException {
        if (message == null) {
            throw new IllegalArgumentException("HTTP message may not be null");
        }
        writeHeadLine(message);
        for (Iterator it = message.headerIterator(); it.hasNext(); ) {
            Header header = (Header) it.next();
            this.sessionBuffer.writeLine
                (lineFormatter.formatHeader(this.lineBuf, header));
        }
        this.lineBuf.clear();
        this.sessionBuffer.writeLine(this.lineBuf);
    }
}
