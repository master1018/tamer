public class DefaultFieldParser extends DelegatingFieldParser {
    public DefaultFieldParser() {
        setFieldParser(Field.CONTENT_TRANSFER_ENCODING, new ContentTransferEncodingField.Parser());
        setFieldParser(Field.CONTENT_TYPE, new ContentTypeField.Parser());
        final DateTimeField.Parser dateTimeParser = new DateTimeField.Parser();
        setFieldParser(Field.DATE, dateTimeParser);
        setFieldParser(Field.RESENT_DATE, dateTimeParser);
        final MailboxListField.Parser mailboxListParser = new MailboxListField.Parser();
        setFieldParser(Field.FROM, mailboxListParser);
        setFieldParser(Field.RESENT_FROM, mailboxListParser);
        final MailboxField.Parser mailboxParser = new MailboxField.Parser();
        setFieldParser(Field.SENDER, mailboxParser);
        setFieldParser(Field.RESENT_SENDER, mailboxParser);
        final AddressListField.Parser addressListParser = new AddressListField.Parser();
        setFieldParser(Field.TO, addressListParser);
        setFieldParser(Field.RESENT_TO, addressListParser);
        setFieldParser(Field.CC, addressListParser);
        setFieldParser(Field.RESENT_CC, addressListParser);
        setFieldParser(Field.BCC, addressListParser);
        setFieldParser(Field.RESENT_BCC, addressListParser);
        setFieldParser(Field.REPLY_TO, addressListParser);
    }
}
