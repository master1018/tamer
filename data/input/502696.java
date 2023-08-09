public class Rfc822OutputTests extends ProviderTestCase2<EmailProvider> {
    private static final String SENDER = "sender@android.com";
    private static final String REPLYTO = "replyto@android.com";
    private static final String RECIPIENT_TO = "recipient-to@android.com";
    private static final String RECIPIENT_CC = "recipient-cc@android.com";
    private static final String RECIPIENT_BCC = "recipient-bcc@android.com";
    private static final String SUBJECT = "This is the subject";
    private static final String BODY = "This is the body.  This is also the body.";
    private static final String TEXT = "Here is some new text.";
    private static final String REPLY_BODY_SHORT = "\n\n" + SENDER + " wrote:\n\n";
    private static final String REPLY_BODY = REPLY_BODY_SHORT + ">" + BODY;
    private Context mMockContext;
    public Rfc822OutputTests () {
        super(EmailProvider.class, EmailProvider.EMAIL_AUTHORITY);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockContext = getMockContext();
    }
    public void testBuildBodyTextWithReply() {
        Message msg = new Message();
        msg.mText = "";
        msg.mFrom = SENDER;
        msg.mFlags = Message.FLAG_TYPE_REPLY;
        msg.mTextReply = BODY;
        msg.mIntroText = REPLY_BODY_SHORT;
        msg.save(mMockContext);
        String body = Rfc822Output.buildBodyText(mMockContext, msg, true);
        assertEquals(REPLY_BODY, body);
        msg.mId = -1;
        msg.mTextReply = null;
        msg.save(mMockContext);
        body = Rfc822Output.buildBodyText(mMockContext, msg, true);
        assertEquals(REPLY_BODY_SHORT, body);
    }
    public void testBuildBodyTextWithoutReply() {
        Message msg = new Message();
        msg.mText = TEXT;
        msg.mFrom = SENDER;
        msg.mFlags = Message.FLAG_TYPE_REPLY;
        msg.mTextReply = BODY;
        msg.mIntroText = REPLY_BODY_SHORT;
        msg.save(mMockContext);
        String body = Rfc822Output.buildBodyText(mMockContext, msg, false);
        assertEquals(TEXT + REPLY_BODY_SHORT, body);
        msg.mId = -1;
        msg.mTextReply = null;
        msg.save(mMockContext);
        body = Rfc822Output.buildBodyText(mMockContext, msg, false);
        assertEquals(TEXT + REPLY_BODY_SHORT, body);
    }
    public void testWriteToText() throws IOException, MessagingException {
        Message msg = new Message();
        msg.mText = TEXT;
        msg.mFrom = SENDER;
        msg.save(mMockContext);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Rfc822Output.writeTo(mMockContext, msg.mId, byteStream, false, false);
        ByteArrayInputStream messageInputStream =
            new ByteArrayInputStream(byteStream.toByteArray());
        org.apache.james.mime4j.message.Message mimeMessage =
            new org.apache.james.mime4j.message.Message(messageInputStream);
        checkMimeVersion(mimeMessage);
        assertFalse(mimeMessage.isMultipart());
        assertEquals("text/plain", mimeMessage.getMimeType());
    }
    @SuppressWarnings("unchecked")
    public void testWriteToAlternativePart() throws IOException, MessagingException {
        Message msg = new Message();
        msg.mText = TEXT;
        msg.mFrom = SENDER;
        msg.mAttachments = new ArrayList<Attachment>();
        Attachment att = new Attachment();
        att.mContentBytes = "__CONTENT__".getBytes("UTF-8");
        att.mFlags = Attachment.FLAG_ICS_ALTERNATIVE_PART;
        att.mMimeType = "text/calendar";
        att.mFileName = "invite.ics";
        msg.mAttachments.add(att);
        msg.save(mMockContext);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Rfc822Output.writeTo(mMockContext, msg.mId, byteStream, false, false);
        ByteArrayInputStream messageInputStream =
            new ByteArrayInputStream(byteStream.toByteArray());
        org.apache.james.mime4j.message.Message mimeMessage =
            new org.apache.james.mime4j.message.Message(messageInputStream);
        checkMimeVersion(mimeMessage);
        assertTrue(mimeMessage.isMultipart());
        Header header = mimeMessage.getHeader();
        Field contentType = header.getField("content-type");
        assertTrue(contentType.getBody().contains("multipart/alternative"));
        Multipart multipart = (Multipart)mimeMessage.getBody();
        List<Body> partList = multipart.getBodyParts();
        assertEquals(2, partList.size());
        Entity part = (Entity)partList.get(0);
        assertEquals("text/plain", part.getMimeType());
        part = (Entity)partList.get(1);
        assertEquals("text/calendar", part.getMimeType());
        header = part.getHeader();
        assertNull(header.getField("content-disposition"));
    }
    public void testWriteToMixedPart() throws IOException, MessagingException {
        Message msg = new Message();
        msg.mText = TEXT;
        msg.mFrom = SENDER;
        msg.mAttachments = new ArrayList<Attachment>();
        Attachment att = new Attachment();
        att.mContentBytes = "<html>Hi</html>".getBytes("UTF-8");
        att.mMimeType = "text/html";
        att.mFileName = "test.html";
        msg.mAttachments.add(att);
        msg.save(mMockContext);
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        Rfc822Output.writeTo(mMockContext, msg.mId, byteStream, false, false);
        ByteArrayInputStream messageInputStream =
            new ByteArrayInputStream(byteStream.toByteArray());
        org.apache.james.mime4j.message.Message mimeMessage =
            new org.apache.james.mime4j.message.Message(messageInputStream);
        checkMimeVersion(mimeMessage);
        assertTrue(mimeMessage.isMultipart());
        Header header = mimeMessage.getHeader();
        Field contentType = header.getField("content-type");
        assertTrue(contentType.getBody().contains("multipart/mixed"));
        Multipart multipart = (Multipart)mimeMessage.getBody();
        List<Body> partList = multipart.getBodyParts();
        assertEquals(2, partList.size());
        Entity part = (Entity)partList.get(0);
        assertEquals("text/plain", part.getMimeType());
        part = (Entity)partList.get(1);
        assertEquals("text/html", part.getMimeType());
        header = part.getHeader();
        assertNotNull(header.getField("content-disposition"));
    }
    private void checkMimeVersion(org.apache.james.mime4j.message.Message mimeMessage) {
        Header header = mimeMessage.getHeader();
        Field contentType = header.getField("MIME-VERSION");
        assertTrue(contentType.getBody().equals("1.0"));
    }
}
