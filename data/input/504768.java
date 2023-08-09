public class DateTimeField extends Field {
    private Date date;
    private ParseException parseException;
    protected DateTimeField(String name, String body, String raw, Date date, ParseException parseException) {
        super(name, body, raw);
        this.date = date;
        this.parseException = parseException;
    }
    public Date getDate() {
        return date;
    }
    public ParseException getParseException() {
        return parseException;
    }
    public static class Parser implements FieldParser {
        private static Log log = LogFactory.getLog(Parser.class);
        public Field parse(final String name, final String body, final String raw) {
            Date date = null;
            ParseException parseException = null;
            try {
                date = DateTime.parse(body).getDate();
            }
            catch (ParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Parsing value '" + body + "': "+ e.getMessage());
                }
                parseException = e;
            }
            return new DateTimeField(name, body, raw, date, parseException);
        }
    }
}
