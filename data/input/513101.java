public class TextBody implements Body {
    String mBody;
    public TextBody(String body) {
        this.mBody = body;
    }
    public void writeTo(OutputStream out) throws IOException, MessagingException {
        byte[] bytes = mBody.getBytes("UTF-8");
        out.write(Base64.encode(bytes, Base64.CRLF));
    }
    public String getText() {
        return mBody;
    }
    public InputStream getInputStream() throws MessagingException {
        try {
            byte[] b = mBody.getBytes("UTF-8");
            return new ByteArrayInputStream(b);
        }
        catch (UnsupportedEncodingException usee) {
            return null;
        }
    }
}
