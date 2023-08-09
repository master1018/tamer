public class MimeMessage extends Message {
    private MimeHeader mHeader;
    private MimeHeader mExtendedHeader;
    private Address[] mFrom;
    private Address[] mTo;
    private Address[] mCc;
    private Address[] mBcc;
    private Address[] mReplyTo;
    private Date mSentDate;
    private Body mBody;
    protected int mSize;
    private boolean mInhibitLocalMessageId = false;
    private static java.util.Random sRandom = new java.util.Random();
    private static final SimpleDateFormat DATE_FORMAT =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
    private static final Pattern REMOVE_OPTIONAL_BRACKETS = Pattern.compile("^<?([^>]+)>?$");
    private static final Pattern END_OF_LINE = Pattern.compile("\r?\n");
    public MimeMessage() {
        mHeader = null;
    }
    private String generateMessageId() {
        StringBuffer sb = new StringBuffer();
        sb.append("<");
        for (int i = 0; i < 24; i++) {
            int value = sRandom.nextInt() & 31;
            char c = "0123456789abcdefghijklmnopqrstuv".charAt(value);
            sb.append(c);
        }
        sb.append(".");
        sb.append(Long.toString(System.currentTimeMillis()));
        sb.append("@email.android.com>");
        return sb.toString();
    }
    public MimeMessage(InputStream in) throws IOException, MessagingException {
        parse(in);
    }
    protected void parse(InputStream in) throws IOException, MessagingException {
        getMimeHeaders().clear();
        mInhibitLocalMessageId = true;
        mFrom = null;
        mTo = null;
        mCc = null;
        mBcc = null;
        mReplyTo = null;
        mSentDate = null;
        mBody = null;
        MimeStreamParser parser = new MimeStreamParser();
        parser.setContentHandler(new MimeMessageBuilder());
        parser.parse(new EOLConvertingInputStream(in));
    }
    private MimeHeader getMimeHeaders() {
        if (mHeader == null) {
            mHeader = new MimeHeader();
        }
        return mHeader;
    }
    public Date getReceivedDate() throws MessagingException {
        return null;
    }
    public Date getSentDate() throws MessagingException {
        if (mSentDate == null) {
            try {
                DateTimeField field = (DateTimeField)Field.parse("Date: "
                        + MimeUtility.unfoldAndDecode(getFirstHeader("Date")));
                mSentDate = field.getDate();
            } catch (Exception e) {
            }
        }
        return mSentDate;
    }
    public void setSentDate(Date sentDate) throws MessagingException {
        setHeader("Date", DATE_FORMAT.format(sentDate));
        this.mSentDate = sentDate;
    }
    public String getContentType() throws MessagingException {
        String contentType = getFirstHeader(MimeHeader.HEADER_CONTENT_TYPE);
        if (contentType == null) {
            return "text/plain";
        } else {
            return contentType;
        }
    }
    public String getDisposition() throws MessagingException {
        String contentDisposition = getFirstHeader(MimeHeader.HEADER_CONTENT_DISPOSITION);
        if (contentDisposition == null) {
            return null;
        } else {
            return contentDisposition;
        }
    }
    public String getContentId() throws MessagingException {
        String contentId = getFirstHeader(MimeHeader.HEADER_CONTENT_ID);
        if (contentId == null) {
            return null;
        } else {
            return REMOVE_OPTIONAL_BRACKETS.matcher(contentId).replaceAll("$1");
        }
    }
    public String getMimeType() throws MessagingException {
        return MimeUtility.getHeaderParameter(getContentType(), null);
    }
    public int getSize() throws MessagingException {
        return mSize;
    }
    public Address[] getRecipients(RecipientType type) throws MessagingException {
        if (type == RecipientType.TO) {
            if (mTo == null) {
                mTo = Address.parse(MimeUtility.unfold(getFirstHeader("To")));
            }
            return mTo;
        } else if (type == RecipientType.CC) {
            if (mCc == null) {
                mCc = Address.parse(MimeUtility.unfold(getFirstHeader("CC")));
            }
            return mCc;
        } else if (type == RecipientType.BCC) {
            if (mBcc == null) {
                mBcc = Address.parse(MimeUtility.unfold(getFirstHeader("BCC")));
            }
            return mBcc;
        } else {
            throw new MessagingException("Unrecognized recipient type.");
        }
    }
    public void setRecipients(RecipientType type, Address[] addresses) throws MessagingException {
        final int TO_LENGTH = 4;  
        final int CC_LENGTH = 4;  
        final int BCC_LENGTH = 5; 
        if (type == RecipientType.TO) {
            if (addresses == null || addresses.length == 0) {
                removeHeader("To");
                this.mTo = null;
            } else {
                setHeader("To", MimeUtility.fold(Address.toHeader(addresses), TO_LENGTH));
                this.mTo = addresses;
            }
        } else if (type == RecipientType.CC) {
            if (addresses == null || addresses.length == 0) {
                removeHeader("CC");
                this.mCc = null;
            } else {
                setHeader("CC", MimeUtility.fold(Address.toHeader(addresses), CC_LENGTH));
                this.mCc = addresses;
            }
        } else if (type == RecipientType.BCC) {
            if (addresses == null || addresses.length == 0) {
                removeHeader("BCC");
                this.mBcc = null;
            } else {
                setHeader("BCC", MimeUtility.fold(Address.toHeader(addresses), BCC_LENGTH));
                this.mBcc = addresses;
            }
        } else {
            throw new MessagingException("Unrecognized recipient type.");
        }
    }
    public String getSubject() throws MessagingException {
        return MimeUtility.unfoldAndDecode(getFirstHeader("Subject"));
    }
    public void setSubject(String subject) throws MessagingException {
        final int HEADER_NAME_LENGTH = 9;     
        setHeader("Subject", MimeUtility.foldAndEncode2(subject, HEADER_NAME_LENGTH));
    }
    public Address[] getFrom() throws MessagingException {
        if (mFrom == null) {
            String list = MimeUtility.unfold(getFirstHeader("From"));
            if (list == null || list.length() == 0) {
                list = MimeUtility.unfold(getFirstHeader("Sender"));
            }
            mFrom = Address.parse(list);
        }
        return mFrom;
    }
    public void setFrom(Address from) throws MessagingException {
        final int FROM_LENGTH = 6;  
        if (from != null) {
            setHeader("From", MimeUtility.fold(from.toHeader(), FROM_LENGTH));
            this.mFrom = new Address[] {
                    from
                };
        } else {
            this.mFrom = null;
        }
    }
    public Address[] getReplyTo() throws MessagingException {
        if (mReplyTo == null) {
            mReplyTo = Address.parse(MimeUtility.unfold(getFirstHeader("Reply-to")));
        }
        return mReplyTo;
    }
    public void setReplyTo(Address[] replyTo) throws MessagingException {
        final int REPLY_TO_LENGTH = 10;  
        if (replyTo == null || replyTo.length == 0) {
            removeHeader("Reply-to");
            mReplyTo = null;
        } else {
            setHeader("Reply-to", MimeUtility.fold(Address.toHeader(replyTo), REPLY_TO_LENGTH));
            mReplyTo = replyTo;
        }
    }
    @Override
    public void setMessageId(String messageId) throws MessagingException {
        setHeader("Message-ID", messageId);
    }
    @Override
    public String getMessageId() throws MessagingException {
        String messageId = getFirstHeader("Message-ID");
        if (messageId == null && !mInhibitLocalMessageId) {
            messageId = generateMessageId();
            setMessageId(messageId);
        }
        return messageId;
    }
    public void saveChanges() throws MessagingException {
        throw new MessagingException("saveChanges not yet implemented");
    }
    public Body getBody() throws MessagingException {
        return mBody;
    }
    public void setBody(Body body) throws MessagingException {
        this.mBody = body;
        if (body instanceof com.android.email.mail.Multipart) {
            com.android.email.mail.Multipart multipart = ((com.android.email.mail.Multipart)body);
            multipart.setParent(this);
            setHeader(MimeHeader.HEADER_CONTENT_TYPE, multipart.getContentType());
            setHeader("MIME-Version", "1.0");
        }
        else if (body instanceof TextBody) {
            setHeader(MimeHeader.HEADER_CONTENT_TYPE, String.format("%s;\n charset=utf-8",
                    getMimeType()));
            setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
        }
    }
    protected String getFirstHeader(String name) throws MessagingException {
        return getMimeHeaders().getFirstHeader(name);
    }
    public void addHeader(String name, String value) throws MessagingException {
        getMimeHeaders().addHeader(name, value);
    }
    public void setHeader(String name, String value) throws MessagingException {
        getMimeHeaders().setHeader(name, value);
    }
    public String[] getHeader(String name) throws MessagingException {
        return getMimeHeaders().getHeader(name);
    }
    public void removeHeader(String name) throws MessagingException {
        getMimeHeaders().removeHeader(name);
        if ("Message-ID".equalsIgnoreCase(name)) {
            mInhibitLocalMessageId = true;
        }
    }
    public void setExtendedHeader(String name, String value) throws MessagingException {
        if (value == null) {
            if (mExtendedHeader != null) {
                mExtendedHeader.removeHeader(name);
            }
            return;
        }
        if (mExtendedHeader == null) {
            mExtendedHeader = new MimeHeader(); 
        }
        mExtendedHeader.setHeader(name, END_OF_LINE.matcher(value).replaceAll(""));
    }
    public String getExtendedHeader(String name) throws MessagingException {
        if (mExtendedHeader == null) {
            return null;
        }
        return mExtendedHeader.getFirstHeader(name);
    }
    public void setExtendedHeaders(String headers) throws MessagingException {
        if (TextUtils.isEmpty(headers)) {
            mExtendedHeader = null;
        } else {
            mExtendedHeader = new MimeHeader();
            for (String header : END_OF_LINE.split(headers)) {
                String[] tokens = header.split(":", 2);
                if (tokens.length != 2) {
                    throw new MessagingException("Illegal extended headers: " + headers);
                }
                mExtendedHeader.setHeader(tokens[0].trim(), tokens[1].trim());
            }
        }
    }
    public String getExtendedHeaders() {
        if (mExtendedHeader != null) {
            return mExtendedHeader.writeToString();
        }
        return null;
    }
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);
        getMessageId();
        getMimeHeaders().writeTo(out);
        writer.write("\r\n");
        writer.flush();
        if (mBody != null) {
            mBody.writeTo(out);
        }
    }
    public InputStream getInputStream() throws MessagingException {
        return null;
    }
    class MimeMessageBuilder implements ContentHandler {
        private Stack stack = new Stack();
        public MimeMessageBuilder() {
        }
        private void expect(Class c) {
            if (!c.isInstance(stack.peek())) {
                throw new IllegalStateException("Internal stack error: " + "Expected '"
                        + c.getName() + "' found '" + stack.peek().getClass().getName() + "'");
            }
        }
        public void startMessage() {
            if (stack.isEmpty()) {
                stack.push(MimeMessage.this);
            } else {
                expect(Part.class);
                try {
                    MimeMessage m = new MimeMessage();
                    ((Part)stack.peek()).setBody(m);
                    stack.push(m);
                } catch (MessagingException me) {
                    throw new Error(me);
                }
            }
        }
        public void endMessage() {
            expect(MimeMessage.class);
            stack.pop();
        }
        public void startHeader() {
            expect(Part.class);
        }
        public void field(String fieldData) {
            expect(Part.class);
            try {
                String[] tokens = fieldData.split(":", 2);
                ((Part)stack.peek()).addHeader(tokens[0], tokens[1].trim());
            } catch (MessagingException me) {
                throw new Error(me);
            }
        }
        public void endHeader() {
            expect(Part.class);
        }
        public void startMultipart(BodyDescriptor bd) {
            expect(Part.class);
            Part e = (Part)stack.peek();
            try {
                MimeMultipart multiPart = new MimeMultipart(e.getContentType());
                e.setBody(multiPart);
                stack.push(multiPart);
            } catch (MessagingException me) {
                throw new Error(me);
            }
        }
        public void body(BodyDescriptor bd, InputStream in) throws IOException {
            expect(Part.class);
            Body body = MimeUtility.decodeBody(in, bd.getTransferEncoding());
            try {
                ((Part)stack.peek()).setBody(body);
            } catch (MessagingException me) {
                throw new Error(me);
            }
        }
        public void endMultipart() {
            stack.pop();
        }
        public void startBodyPart() {
            expect(MimeMultipart.class);
            try {
                MimeBodyPart bodyPart = new MimeBodyPart();
                ((MimeMultipart)stack.peek()).addBodyPart(bodyPart);
                stack.push(bodyPart);
            } catch (MessagingException me) {
                throw new Error(me);
            }
        }
        public void endBodyPart() {
            expect(BodyPart.class);
            stack.pop();
        }
        public void epilogue(InputStream is) throws IOException {
            expect(MimeMultipart.class);
            StringBuffer sb = new StringBuffer();
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char)b);
            }
        }
        public void preamble(InputStream is) throws IOException {
            expect(MimeMultipart.class);
            StringBuffer sb = new StringBuffer();
            int b;
            while ((b = is.read()) != -1) {
                sb.append((char)b);
            }
            try {
                ((MimeMultipart)stack.peek()).setPreamble(sb.toString());
            } catch (MessagingException me) {
                throw new Error(me);
            }
        }
        public void raw(InputStream is) throws IOException {
            throw new UnsupportedOperationException("Not supported");
        }
    }
}
