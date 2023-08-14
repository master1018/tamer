public class ContentTransferEncodingField extends Field {
    public static final String ENC_7BIT = "7bit";
    public static final String ENC_8BIT = "8bit";
    public static final String ENC_BINARY = "binary";
    public static final String ENC_QUOTED_PRINTABLE = "quoted-printable";
    public static final String ENC_BASE64 = "base64";
    private String encoding;
    protected ContentTransferEncodingField(String name, String body, String raw, String encoding) {
        super(name, body, raw);
        this.encoding = encoding;
    }
    public String getEncoding() {
        return encoding;
    }
    public static String getEncoding(ContentTransferEncodingField f) {
        if (f != null && f.getEncoding().length() != 0) {
            return f.getEncoding();
        }
        return ENC_7BIT;
    }
    public static class Parser implements FieldParser {
        public Field parse(final String name, final String body, final String raw) {
            final String encoding = body.trim().toLowerCase();
            return new ContentTransferEncodingField(name, body, raw, encoding);
        }
    }
}
