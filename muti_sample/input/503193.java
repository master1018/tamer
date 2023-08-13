public class Rfc822Output {
    private static final Pattern PATTERN_START_OF_LINE = Pattern.compile("(?m)^");
    private static final Pattern PATTERN_ENDLINE_CRLF = Pattern.compile("\r\n");
    static final SimpleDateFormat mDateFormat =
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US);
     static String buildBodyText(Context context, Message message,
            boolean appendQuotedText) {
        Body body = Body.restoreBodyWithMessageId(context, message.mId);
        if (body == null) {
            return null;
        }
        String text = body.mTextContent;
        int flags = message.mFlags;
        boolean isReply = (flags & Message.FLAG_TYPE_REPLY) != 0;
        boolean isForward = (flags & Message.FLAG_TYPE_FORWARD) != 0;
        String intro = body.mIntroText == null ? "" : body.mIntroText;
        if (!appendQuotedText) {
            if (isReply) {
                text += intro;
            } else if (isForward) {
                text += "\r\n";
            }
            return text;
        }
        String quotedText = body.mTextReply;
        if (quotedText != null) {
            Matcher matcher = PATTERN_ENDLINE_CRLF.matcher(quotedText);
            quotedText = matcher.replaceAll("\n");
        }
        if (isReply) {
            text += intro;
            if (quotedText != null) {
                Matcher matcher = PATTERN_START_OF_LINE.matcher(quotedText);
                text += matcher.replaceAll(">");
            }
        } else if (isForward) {
            text += intro;
            if (quotedText != null) {
                text += quotedText;
            }
        }
        return text;
    }
    public static void writeTo(Context context, long messageId, OutputStream out,
            boolean appendQuotedText, boolean sendBcc) throws IOException, MessagingException {
        Message message = Message.restoreMessageWithId(context, messageId);
        if (message == null) {
            return;
        }
        OutputStream stream = new BufferedOutputStream(out, 1024);
        Writer writer = new OutputStreamWriter(stream);
        String date = mDateFormat.format(new Date(message.mTimeStamp));
        writeHeader(writer, "Date", date);
        writeEncodedHeader(writer, "Subject", message.mSubject);
        writeHeader(writer, "Message-ID", message.mMessageId);
        writeAddressHeader(writer, "From", message.mFrom);
        writeAddressHeader(writer, "To", message.mTo);
        writeAddressHeader(writer, "Cc", message.mCc);
        if (sendBcc) {
            writeAddressHeader(writer, "Bcc", message.mBcc);
        }
        writeAddressHeader(writer, "Reply-To", message.mReplyTo);
        writeHeader(writer, "MIME-Version", "1.0");
        String text = buildBodyText(context, message, appendQuotedText);
        Uri uri = ContentUris.withAppendedId(Attachment.MESSAGE_ID_URI, messageId);
        Cursor attachmentsCursor = context.getContentResolver().query(uri,
                Attachment.CONTENT_PROJECTION, null, null, null);
        try {
            int attachmentCount = attachmentsCursor.getCount();
            boolean multipart = attachmentCount > 0;
            String multipartBoundary = null;
            String multipartType = "mixed";
            if (!multipart) {
                if (text != null) {
                    writeTextWithHeaders(writer, stream, text);
                } else {
                    writer.write("\r\n");       
                }
            } else {
                multipartBoundary = "--_com.android.email_" + System.nanoTime();
                attachmentsCursor.moveToFirst();
                if (attachmentCount == 1) {
                    int flags = attachmentsCursor.getInt(Attachment.CONTENT_FLAGS_COLUMN);
                    if ((flags & Attachment.FLAG_ICS_ALTERNATIVE_PART) != 0) {
                        multipartType = "alternative";
                    }
                }
                writeHeader(writer, "Content-Type",
                        "multipart/" + multipartType + "; boundary=\"" + multipartBoundary + "\"");
                writer.write("\r\n");
                if (text != null) {
                    writeBoundary(writer, multipartBoundary, false);
                    writeTextWithHeaders(writer, stream, text);
                }
                do {
                    writeBoundary(writer, multipartBoundary, false);
                    Attachment attachment =
                        Attachment.getContent(attachmentsCursor, Attachment.class);
                    writeOneAttachment(context, writer, stream, attachment);
                    writer.write("\r\n");
                } while (attachmentsCursor.moveToNext());
                writeBoundary(writer, multipartBoundary, true);
            }
        } finally {
            attachmentsCursor.close();
        }
        writer.flush();
        out.flush();
    }
    private static void writeOneAttachment(Context context, Writer writer, OutputStream out,
            Attachment attachment) throws IOException, MessagingException {
        writeHeader(writer, "Content-Type",
                attachment.mMimeType + ";\n name=\"" + attachment.mFileName + "\"");
        writeHeader(writer, "Content-Transfer-Encoding", "base64");
        if ((attachment.mFlags & Attachment.FLAG_ICS_ALTERNATIVE_PART) == 0) {
            writeHeader(writer, "Content-Disposition",
                    "attachment;"
                    + "\n filename=\"" + attachment.mFileName + "\";"
                    + "\n size=" + Long.toString(attachment.mSize));
        }
        writeHeader(writer, "Content-ID", attachment.mContentId);
        writer.append("\r\n");
        InputStream inStream = null;
        try {
            if (attachment.mContentBytes != null) {
                inStream = new ByteArrayInputStream(attachment.mContentBytes);
            } else {
                Uri fileUri = Uri.parse(attachment.mContentUri);
                inStream = context.getContentResolver().openInputStream(fileUri);
            }
            writer.flush();
            Base64OutputStream base64Out = new Base64OutputStream(
                out, Base64.CRLF | Base64.NO_CLOSE);
            IOUtils.copy(inStream, base64Out);
            base64Out.close();
            out.write('\r');
            out.write('\n');
            out.flush();
        }
        catch (FileNotFoundException fnfe) {
        }
        catch (IOException ioe) {
            throw new MessagingException("Invalid attachment.", ioe);
        }
    }
    private static void writeHeader(Writer writer, String name, String value) throws IOException {
        if (value != null && value.length() > 0) {
            writer.append(name);
            writer.append(": ");
            writer.append(value);
            writer.append("\r\n");
        }
    }
    private static void writeEncodedHeader(Writer writer, String name, String value)
            throws IOException {
        if (value != null && value.length() > 0) {
            writer.append(name);
            writer.append(": ");
            writer.append(MimeUtility.foldAndEncode2(value, name.length() + 2));
            writer.append("\r\n");
        }
    }
    private static void writeAddressHeader(Writer writer, String name, String value)
            throws IOException {
        if (value != null && value.length() > 0) {
            writer.append(name);
            writer.append(": ");
            writer.append(MimeUtility.fold(Address.packedToHeader(value), name.length() + 2));
            writer.append("\r\n");
        }
    }
    private static void writeBoundary(Writer writer, String boundary, boolean end)
            throws IOException {
        writer.append("--");
        writer.append(boundary);
        if (end) {
            writer.append("--");
        }
        writer.append("\r\n");
    }
    private static void writeTextWithHeaders(Writer writer, OutputStream out, String text)
            throws IOException {
        writeHeader(writer, "Content-Type", "text/plain; charset=utf-8");
        writeHeader(writer, "Content-Transfer-Encoding", "base64");
        writer.write("\r\n");
        byte[] bytes = text.getBytes("UTF-8");
        writer.flush();
        out.write(Base64.encode(bytes, Base64.CRLF));
    }
}
