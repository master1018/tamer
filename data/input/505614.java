public class HttpResponseWriter extends AbstractMessageWriter {
    public HttpResponseWriter(final SessionOutputBuffer buffer,
                              final LineFormatter formatter,
                              final HttpParams params) {
        super(buffer, formatter, params);
    }
    protected void writeHeadLine(final HttpMessage message)
        throws IOException {
        final CharArrayBuffer buffer = lineFormatter.formatStatusLine
            (this.lineBuf, ((HttpResponse) message).getStatusLine());
        this.sessionBuffer.writeLine(buffer);
    }
}
