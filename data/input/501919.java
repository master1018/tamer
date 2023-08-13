public class UnstructuredField extends Field {
    private String value;
    protected UnstructuredField(String name, String body, String raw, String value) {
        super(name, body, raw);
        this.value = value;
    }
    public String getValue() {
        return value;
    }
    public static class Parser implements FieldParser {
        public Field parse(final String name, final String body, final String raw) {
            final String value = DecoderUtil.decodeEncodedWords(body);
            return new UnstructuredField(name, body, raw, value);
        }
    }
}
