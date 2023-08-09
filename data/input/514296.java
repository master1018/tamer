public class EmailHtmlUtil {
    private static final Pattern PLAIN_TEXT_TO_ESCAPE = Pattern.compile("[<>&]| {2,}|\r?\n");
    public static String resolveInlineImage(
            ContentResolver resolver, long accountId, String text, Part part, int depth)
        throws MessagingException {
        if (depth >= 10 || text == null) {
            return text;
        }
        String contentType = MimeUtility.unfoldAndDecode(part.getContentType());
        String contentId = part.getContentId();
        if (contentType.startsWith("image/") &&
            contentId != null &&
            part instanceof LocalAttachmentBodyPart) {
            LocalAttachmentBodyPart attachment = (LocalAttachmentBodyPart)part;
            Uri attachmentUri =
                AttachmentProvider.getAttachmentUri(accountId, attachment.getAttachmentId());
            Uri contentUri =
                AttachmentProvider.resolveAttachmentIdToContentUri(resolver, attachmentUri);
            String contentIdRe = "\\s+(?i)src=\"cid(?-i):\\Q" + contentId + "\\E\"";
            text = text.replaceAll(contentIdRe, " src=\"" + contentUri + "\""); 
        }
        if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart)part.getBody();
            for (int i = 0; i < mp.getCount(); i++) {
                text = resolveInlineImage(resolver, accountId, text, mp.getBodyPart(i), depth + 1);
            }
        }
        return text;
    }
    public static String escapeCharacterToDisplay(String text) {
        Pattern pattern = PLAIN_TEXT_TO_ESCAPE;
        Matcher match = pattern.matcher(text);
        if (match.find()) {
            StringBuilder out = new StringBuilder();
            int end = 0;
            do {
                int start = match.start();
                out.append(text.substring(end, start));
                end = match.end();
                int c = text.codePointAt(start);
                if (c == ' ') {
                    for (int i = 1, n = end - start; i < n; ++i) {
                        out.append("&nbsp;");
                    }
                    out.append(' ');
                } else if (c == '\r' || c == '\n') {
                    out.append("<br>");
                } else if (c == '<') {
                    out.append("&lt;");
                } else if (c == '>') {
                    out.append("&gt;");
                } else if (c == '&') {
                    out.append("&amp;");
                }
            } while (match.find());
            out.append(text.substring(end));
            text = out.toString();
        }        
        return text;
    }
}
