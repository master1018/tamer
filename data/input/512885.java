public class DelegatingFieldParser implements FieldParser {
    private Map parsers = new HashMap();
    private FieldParser defaultParser = new UnstructuredField.Parser();
    public void setFieldParser(final String name, final FieldParser parser) {
        parsers.put(name.toLowerCase(), parser);
    }
    public FieldParser getParser(final String name) {
        final FieldParser field = (FieldParser) parsers.get(name.toLowerCase());
        if(field==null) {
            return defaultParser;
        }
        return field;
    }
    public Field parse(final String name, final String body, final String raw) {
        final FieldParser parser = getParser(name);
        return parser.parse(name, body, raw);
    }
}
