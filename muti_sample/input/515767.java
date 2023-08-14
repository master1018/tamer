public class MimeBodyPart extends BodyPart {
    protected MimeHeader mHeader = new MimeHeader();
    protected MimeHeader mExtendedHeader;
    protected Body mBody;
    protected int mSize;
    private static final Pattern REMOVE_OPTIONAL_BRACKETS = Pattern.compile("^<?([^>]+)>?$");
    private static final Pattern END_OF_LINE = Pattern.compile("\r?\n");
    public MimeBodyPart() throws MessagingException {
        this(null);
    }
    public MimeBodyPart(Body body) throws MessagingException {
        this(body, null);
    }
    public MimeBodyPart(Body body, String mimeType) throws MessagingException {
        if (mimeType != null) {
            setHeader(MimeHeader.HEADER_CONTENT_TYPE, mimeType);
        }
        setBody(body);
    }
    protected String getFirstHeader(String name) throws MessagingException {
        return mHeader.getFirstHeader(name);
    }
    public void addHeader(String name, String value) throws MessagingException {
        mHeader.addHeader(name, value);
    }
    public void setHeader(String name, String value) throws MessagingException {
        mHeader.setHeader(name, value);
    }
    public String[] getHeader(String name) throws MessagingException {
        return mHeader.getHeader(name);
    }
    public void removeHeader(String name) throws MessagingException {
        mHeader.removeHeader(name);
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
        }
        else if (body instanceof TextBody) {
            String contentType = String.format("%s;\n charset=utf-8", getMimeType());
            String name = MimeUtility.getHeaderParameter(getContentType(), "name");
            if (name != null) {
                contentType += String.format(";\n name=\"%s\"", name);
            }
            setHeader(MimeHeader.HEADER_CONTENT_TYPE, contentType);
            setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, "base64");
        }
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
    public boolean isMimeType(String mimeType) throws MessagingException {
        return getMimeType().equals(mimeType);
    }
    public void setSize(int size) {
        this.mSize = size;
    }
    public int getSize() throws MessagingException {
        return mSize;
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
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out), 1024);
        mHeader.writeTo(out);
        writer.write("\r\n");
        writer.flush();
        if (mBody != null) {
            mBody.writeTo(out);
        }
    }
}
