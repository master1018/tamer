public class StringPart extends PartBase {
    private static final Log LOG = LogFactory.getLog(StringPart.class);
    public static final String DEFAULT_CONTENT_TYPE = "text/plain";
    public static final String DEFAULT_CHARSET = "US-ASCII";
    public static final String DEFAULT_TRANSFER_ENCODING = "8bit";
    private byte[] content;
    private String value;
    public StringPart(String name, String value, String charset) {
        super(
            name,
            DEFAULT_CONTENT_TYPE,
            charset == null ? DEFAULT_CHARSET : charset,
            DEFAULT_TRANSFER_ENCODING
        );
        if (value == null) {
            throw new IllegalArgumentException("Value may not be null");
        }
        if (value.indexOf(0) != -1) {
            throw new IllegalArgumentException("NULs may not be present in string parts");
        }
        this.value = value;
    }
    public StringPart(String name, String value) {
        this(name, value, null);
    }
    private byte[] getContent() {
        if (content == null) {
            content = EncodingUtils.getBytes(value, getCharSet());
        }
        return content;
    }
    @Override
    protected void sendData(OutputStream out) throws IOException {
        LOG.trace("enter sendData(OutputStream)");
        out.write(getContent());
    }
    @Override
    protected long lengthOfData() {
        LOG.trace("enter lengthOfData()");
        return getContent().length;
    }
    @Override
    public void setCharSet(String charSet) {
        super.setCharSet(charSet);
        this.content = null;
    }
}
