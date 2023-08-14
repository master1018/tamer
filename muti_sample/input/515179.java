public class MessageTestUtils {
    public static Uri contentUri(long attachmentId, EmailContent.Account account) {
        return AttachmentProvider.getAttachmentUri(account.mId, attachmentId);
    }
    public static BodyPart bodyPart(String mimeType, String contentId) throws MessagingException {
        final MimeBodyPart bp = new MimeBodyPart(null, mimeType);
        if (contentId != null) {
            bp.setHeader(MimeHeader.HEADER_CONTENT_ID, contentId);
        }
        return bp;
    }
    public static BodyPart textPart(String mimeType, String text) throws MessagingException {
        final TextBody textBody = new TextBody(text);
        final MimeBodyPart textPart = new MimeBodyPart(textBody);
        textPart.setHeader(MimeHeader.HEADER_CONTENT_TYPE, mimeType);
        return textPart;
    }
    public static BodyPart imagePart(String mimeType, String contentId,
            long attachmentId, LocalStore store) throws MessagingException, IOException {
        final BinaryTempFileBody imageBody = new BinaryTempFileBody();
        final LocalStore.LocalAttachmentBodyPart imagePart =
            store.new LocalAttachmentBodyPart(imageBody, attachmentId);
        imagePart.setHeader(MimeHeader.HEADER_CONTENT_TYPE, mimeType);
        if (contentId != null) {
            imagePart.setHeader(MimeHeader.HEADER_CONTENT_ID, contentId);
        }
        return imagePart;
    }
    public static class MultipartBuilder {
        private final String mContentType;
        private final ArrayList<BodyPart> mParts = new ArrayList<BodyPart>();
        public MultipartBuilder(String mimeType) {
            this(mimeType, "this_is_boundary");
        }
        public MultipartBuilder(String mimeType, String boundary) {
            mContentType = mimeType + "; boundary=" + boundary;
        }
        public MultipartBuilder addBodyPart(final BodyPart bodyPart) {
            mParts.add(bodyPart);
            return this;
        }
        public Multipart build() throws MessagingException {
            final MimeMultipart mp = new MimeMultipart(mContentType);
            for (BodyPart p : mParts) {
                mp.addBodyPart(p);
            }
            return mp;
        }
        public BodyPart buildBodyPart() throws MessagingException {
            final BodyPart bp = new MimeBodyPart();
            bp.setBody(this.build());
            return bp;
        }
    }
    public static class MessageBuilder {
        private Body mBody;
        public MessageBuilder() {
        }
        public MessageBuilder setBody(final Body body) {
            mBody = body;
            return this;
        }
        public Message build() throws MessagingException {
            final MimeMessage msg = new MimeMessage();
            if (mBody == null) {
                throw new MessagingException("body is not specified");
            }
            msg.setBody(mBody);
            return msg;
        }
    }
    public static class TextBuilder {
        final StringBuilder mBuilder = new StringBuilder();
        public TextBuilder(String preamble) {
            mBuilder.append(preamble);
        }
        public TextBuilder addCidImg(String contentId) {
            return addTag("img", "SRC", "cid:" + contentId);
        }
        public TextBuilder addUidImg(Uri contentUri) {
            return addTag("img", "src", contentUri.toString());
        }
        public TextBuilder addTag(String tag, String attribute, String value) {
            return addText(String.format("<%s %s=\"%s\">", tag, attribute, value));
        }
        public TextBuilder addText(String text) {
            mBuilder.append(text);
            return this;
        }
        public String build(String epilogue) {
            mBuilder.append(epilogue);
            return mBuilder.toString();
        }
    }
}
