public class MailboxField extends Field {
    private final Mailbox mailbox;
    private final ParseException parseException;
    protected MailboxField(final String name, final String body, final String raw, final Mailbox mailbox, final ParseException parseException) {
        super(name, body, raw);
        this.mailbox = mailbox;
        this.parseException = parseException;
    }
    public Mailbox getMailbox() {
        return mailbox;
    }
    public ParseException getParseException() {
        return parseException;
    }
    public static class Parser implements FieldParser {
        private static Log log = LogFactory.getLog(Parser.class);
        public Field parse(final String name, final String body, final String raw) {
            Mailbox mailbox = null;
            ParseException parseException = null;
            try {
                MailboxList mailboxList = AddressList.parse(body).flatten();
                if (mailboxList.size() > 0) {
                    mailbox = mailboxList.get(0);
                }
            }
            catch (ParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Parsing value '" + body + "': "+ e.getMessage());
                }
                parseException = e;
            }
            return new MailboxField(name, body, raw, mailbox, parseException);
        }
    }
}
