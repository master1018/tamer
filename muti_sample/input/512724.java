public abstract class Part {
    private static final Log LOG = LogFactory.getLog(Part.class);
    protected static final String BOUNDARY = "----------------314159265358979323846";
    protected static final byte[] BOUNDARY_BYTES = EncodingUtils.getAsciiBytes(BOUNDARY);
    private static final byte[] DEFAULT_BOUNDARY_BYTES = BOUNDARY_BYTES;    
    protected static final String CRLF = "\r\n";
    protected static final byte[] CRLF_BYTES = EncodingUtils.getAsciiBytes(CRLF);
    protected static final String QUOTE = "\"";
    protected static final byte[] QUOTE_BYTES = 
      EncodingUtils.getAsciiBytes(QUOTE);
    protected static final String EXTRA = "--";
    protected static final byte[] EXTRA_BYTES = 
      EncodingUtils.getAsciiBytes(EXTRA);
    protected static final String CONTENT_DISPOSITION = "Content-Disposition: form-data; name=";
    protected static final byte[] CONTENT_DISPOSITION_BYTES = 
      EncodingUtils.getAsciiBytes(CONTENT_DISPOSITION);
    protected static final String CONTENT_TYPE = "Content-Type: ";
    protected static final byte[] CONTENT_TYPE_BYTES = 
      EncodingUtils.getAsciiBytes(CONTENT_TYPE);
    protected static final String CHARSET = "; charset=";
    protected static final byte[] CHARSET_BYTES = 
      EncodingUtils.getAsciiBytes(CHARSET);
    protected static final String CONTENT_TRANSFER_ENCODING = "Content-Transfer-Encoding: ";
    protected static final byte[] CONTENT_TRANSFER_ENCODING_BYTES = 
      EncodingUtils.getAsciiBytes(CONTENT_TRANSFER_ENCODING);
    public static String getBoundary() {
        return BOUNDARY;
    }
    private byte[] boundaryBytes;
    public abstract String getName();
    public abstract String getContentType();
    public abstract String getCharSet();
    public abstract String getTransferEncoding();
    protected byte[] getPartBoundary() {
        if (boundaryBytes == null) {
            return DEFAULT_BOUNDARY_BYTES;
        } else {
            return boundaryBytes;            
        }
    }
    void setPartBoundary(byte[] boundaryBytes) {
        this.boundaryBytes = boundaryBytes;
    }
    public boolean isRepeatable() {
        return true;
    }
    protected void sendStart(OutputStream out) throws IOException {
        LOG.trace("enter sendStart(OutputStream out)");
        out.write(EXTRA_BYTES);
        out.write(getPartBoundary());
        out.write(CRLF_BYTES);
    }
    protected void sendDispositionHeader(OutputStream out) throws IOException {
        LOG.trace("enter sendDispositionHeader(OutputStream out)");
        out.write(CONTENT_DISPOSITION_BYTES);
        out.write(QUOTE_BYTES);
        out.write(EncodingUtils.getAsciiBytes(getName()));
        out.write(QUOTE_BYTES);
    }
     protected void sendContentTypeHeader(OutputStream out) throws IOException {
        LOG.trace("enter sendContentTypeHeader(OutputStream out)");
        String contentType = getContentType();
        if (contentType != null) {
            out.write(CRLF_BYTES);
            out.write(CONTENT_TYPE_BYTES);
            out.write(EncodingUtils.getAsciiBytes(contentType));
            String charSet = getCharSet();
            if (charSet != null) {
                out.write(CHARSET_BYTES);
                out.write(EncodingUtils.getAsciiBytes(charSet));
            }
        }
    }
     protected void sendTransferEncodingHeader(OutputStream out) throws IOException {
        LOG.trace("enter sendTransferEncodingHeader(OutputStream out)");
        String transferEncoding = getTransferEncoding();
        if (transferEncoding != null) {
            out.write(CRLF_BYTES);
            out.write(CONTENT_TRANSFER_ENCODING_BYTES);
            out.write(EncodingUtils.getAsciiBytes(transferEncoding));
        }
    }
    protected void sendEndOfHeader(OutputStream out) throws IOException {
        LOG.trace("enter sendEndOfHeader(OutputStream out)");
        out.write(CRLF_BYTES);
        out.write(CRLF_BYTES);
    }
    protected abstract void sendData(OutputStream out) throws IOException;
    protected abstract long lengthOfData() throws IOException;
    protected void sendEnd(OutputStream out) throws IOException {
        LOG.trace("enter sendEnd(OutputStream out)");
        out.write(CRLF_BYTES);
    }
    public void send(OutputStream out) throws IOException {
        LOG.trace("enter send(OutputStream out)");
        sendStart(out);
        sendDispositionHeader(out);
        sendContentTypeHeader(out);
        sendTransferEncodingHeader(out);
        sendEndOfHeader(out);
        sendData(out);
        sendEnd(out);
    }
    public long length() throws IOException {
        LOG.trace("enter length()");
        if (lengthOfData() < 0) {
            return -1;
        }
        ByteArrayOutputStream overhead = new ByteArrayOutputStream();
        sendStart(overhead);
        sendDispositionHeader(overhead);
        sendContentTypeHeader(overhead);
        sendTransferEncodingHeader(overhead);
        sendEndOfHeader(overhead);
        sendEnd(overhead);
        return overhead.size() + lengthOfData();
    }
    @Override
    public String toString() {
        return this.getName();
    }
    public static void sendParts(OutputStream out, final Part[] parts)
        throws IOException {
        sendParts(out, parts, DEFAULT_BOUNDARY_BYTES);
    }
    public static void sendParts(OutputStream out, Part[] parts, byte[] partBoundary)
        throws IOException {
        if (parts == null) {
            throw new IllegalArgumentException("Parts may not be null"); 
        }
        if (partBoundary == null || partBoundary.length == 0) {
            throw new IllegalArgumentException("partBoundary may not be empty");
        }
        for (int i = 0; i < parts.length; i++) {
            parts[i].setPartBoundary(partBoundary);
            parts[i].send(out);
        }
        out.write(EXTRA_BYTES);
        out.write(partBoundary);
        out.write(EXTRA_BYTES);
        out.write(CRLF_BYTES);
    }
    public static long getLengthOfParts(Part[] parts)
    throws IOException {
        return getLengthOfParts(parts, DEFAULT_BOUNDARY_BYTES);
    }
    public static long getLengthOfParts(Part[] parts, byte[] partBoundary) throws IOException {
        LOG.trace("getLengthOfParts(Parts[])");
        if (parts == null) {
            throw new IllegalArgumentException("Parts may not be null"); 
        }
        long total = 0;
        for (int i = 0; i < parts.length; i++) {
            parts[i].setPartBoundary(partBoundary);
            long l = parts[i].length();
            if (l < 0) {
                return -1;
            }
            total += l;
        }
        total += EXTRA_BYTES.length;
        total += partBoundary.length;
        total += EXTRA_BYTES.length;
        total += CRLF_BYTES.length;
        return total;
    }        
}
