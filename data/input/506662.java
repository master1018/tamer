public class FilePart extends PartBase {
    public static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";
    public static final String DEFAULT_CHARSET = "ISO-8859-1";
    public static final String DEFAULT_TRANSFER_ENCODING = "binary";
    private static final Log LOG = LogFactory.getLog(FilePart.class);
    protected static final String FILE_NAME = "; filename=";
    private static final byte[] FILE_NAME_BYTES = 
        EncodingUtils.getAsciiBytes(FILE_NAME);
    private PartSource source;
    public FilePart(String name, PartSource partSource, String contentType, String charset) {
        super(
            name, 
            contentType == null ? DEFAULT_CONTENT_TYPE : contentType, 
            charset == null ? "ISO-8859-1" : charset, 
            DEFAULT_TRANSFER_ENCODING
        );
        if (partSource == null) {
            throw new IllegalArgumentException("Source may not be null");
        }
        this.source = partSource;
    }
    public FilePart(String name, PartSource partSource) {
        this(name, partSource, null, null);
    }
    public FilePart(String name, File file) 
    throws FileNotFoundException {
        this(name, new FilePartSource(file), null, null);
    }
    public FilePart(String name, File file, String contentType, String charset) 
    throws FileNotFoundException {
        this(name, new FilePartSource(file), contentType, charset);
    }
    public FilePart(String name, String fileName, File file) 
    throws FileNotFoundException {
        this(name, new FilePartSource(fileName, file), null, null);
    }
    public FilePart(String name, String fileName, File file, String contentType, String charset) 
    throws FileNotFoundException {
        this(name, new FilePartSource(fileName, file), contentType, charset);
    }
    @Override
    protected void sendDispositionHeader(OutputStream out) 
    throws IOException {
        LOG.trace("enter sendDispositionHeader(OutputStream out)");
        super.sendDispositionHeader(out);
        String filename = this.source.getFileName();
        if (filename != null) {
            out.write(FILE_NAME_BYTES);
            out.write(QUOTE_BYTES);
            out.write(EncodingUtils.getAsciiBytes(filename));
            out.write(QUOTE_BYTES);
        }
    }
    @Override
    protected void sendData(OutputStream out) throws IOException {
        LOG.trace("enter sendData(OutputStream out)");
        if (lengthOfData() == 0) {
            LOG.debug("No data to send.");
            return;
        }
        byte[] tmp = new byte[4096];
        InputStream instream = source.createInputStream();
        try {
            int len;
            while ((len = instream.read(tmp)) >= 0) {
                out.write(tmp, 0, len);
            }
        } finally {
            instream.close();
        }
    }
    protected PartSource getSource() {
        LOG.trace("enter getSource()");
        return this.source;
    }
    @Override
    protected long lengthOfData() {
        LOG.trace("enter lengthOfData()");
        return source.getLength();
    }    
}
