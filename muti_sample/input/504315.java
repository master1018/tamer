public class MimeUtility {
    private final static Pattern PATTERN_CR_OR_LF = Pattern.compile("\r|\n");
    public static String unfold(String s) {
        if (s == null) {
            return null;
        }
        Matcher patternMatcher = PATTERN_CR_OR_LF.matcher(s);
        if (patternMatcher.find()) {
            patternMatcher.reset();
            s = patternMatcher.replaceAll("");
        }
        return s;
    }
    public static String decode(String s) {
        if (s == null) {
            return null;
        }
        return DecoderUtil.decodeEncodedWords(s);
    }
    public static String unfoldAndDecode(String s) {
        return decode(unfold(s));
    }
    public static String foldAndEncode(String s) {
        return s;
    }
    public static String foldAndEncode2(String s, int usedCharacters) {
        String encoded = EncoderUtil.encodeIfNecessary(s, EncoderUtil.Usage.TEXT_TOKEN,
                usedCharacters);
        return fold(encoded, usedCharacters);
    }
    public static String fold(String s, int usedCharacters) {
        final int maxCharacters = 76;
        final int length = s.length();
        if (usedCharacters + length <= maxCharacters)
            return s;
        StringBuilder sb = new StringBuilder();
        int lastLineBreak = -usedCharacters;
        int wspIdx = indexOfWsp(s, 0);
        while (true) {
            if (wspIdx == length) {
                sb.append(s.substring(Math.max(0, lastLineBreak)));
                return sb.toString();
            }
            int nextWspIdx = indexOfWsp(s, wspIdx + 1);
            if (nextWspIdx - lastLineBreak > maxCharacters) {
                sb.append(s.substring(Math.max(0, lastLineBreak), wspIdx));
                sb.append("\r\n");
                lastLineBreak = wspIdx;
            }
            wspIdx = nextWspIdx;
        }
    }
    private static int indexOfWsp(String s, int fromIndex) {
        final int len = s.length();
        for (int index = fromIndex; index < len; index++) {
            char c = s.charAt(index);
            if (c == ' ' || c == '\t')
                return index;
        }
        return len;
    }
    public static String getHeaderParameter(String header, String name) {
        if (header == null) {
            return null;
        }
        String[] parts = unfold(header).split(";");
        if (name == null) {
            return parts[0].trim();
        }
        String lowerCaseName = name.toLowerCase();
        for (String part : parts) {
            if (part.trim().toLowerCase().startsWith(lowerCaseName)) {
                String parameter = part.split("=", 2)[1].trim();
                if (parameter.startsWith("\"") && parameter.endsWith("\"")) {
                    return parameter.substring(1, parameter.length() - 1);
                }
                else {
                    return parameter;
                }
            }
        }
        return null;
    }
    public static Part findFirstPartByMimeType(Part part, String mimeType)
            throws MessagingException {
        if (part.getBody() instanceof Multipart) {
            Multipart multipart = (Multipart)part.getBody();
            for (int i = 0, count = multipart.getCount(); i < count; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                Part ret = findFirstPartByMimeType(bodyPart, mimeType);
                if (ret != null) {
                    return ret;
                }
            }
        }
        else if (part.getMimeType().equalsIgnoreCase(mimeType)) {
            return part;
        }
        return null;
    }
    public static Part findPartByContentId(Part part, String contentId) throws Exception {
        if (part.getBody() instanceof Multipart) {
            Multipart multipart = (Multipart)part.getBody();
            for (int i = 0, count = multipart.getCount(); i < count; i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                Part ret = findPartByContentId(bodyPart, contentId);
                if (ret != null) {
                    return ret;
                }
            }
        }
        String cid = part.getContentId();
        if (contentId.equals(cid)) {
            return part;
        }
        return null;
    }
    public static String getTextFromPart(Part part) {
        try {
            if (part != null && part.getBody() != null) {
                InputStream in = part.getBody().getInputStream();
                String mimeType = part.getMimeType();
                if (mimeType != null && MimeUtility.mimeTypeMatches(mimeType, "text
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    IOUtils.copy(in, out);
                    in.close();
                    in = null;      
                    String charset = getHeaderParameter(part.getContentType(), "charset");
                    if (charset != null) {
                        charset = CharsetUtil.toJavaCharset(charset);
                    }
                    if (charset == null) {
                        charset = "ASCII";
                    }
                    String result = out.toString(charset);
                    out.close();
                    return result;
                }
            }
        }
        catch (OutOfMemoryError oom) {
            Log.e(Email.LOG_TAG, "Unable to getTextFromPart " + oom.toString());
        }
        catch (Exception e) {
            Log.e(Email.LOG_TAG, "Unable to getTextFromPart " + e.toString());
        }
        return null;
    }
    public static boolean mimeTypeMatches(String mimeType, String matchAgainst) {
        Pattern p = Pattern.compile(matchAgainst.replaceAll("\\*", "\\.\\*"),
                Pattern.CASE_INSENSITIVE);
        return p.matcher(mimeType).matches();
    }
    public static boolean mimeTypeMatches(String mimeType, String[] matchAgainst) {
        for (String matchType : matchAgainst) {
            if (mimeTypeMatches(mimeType, matchType)) {
                return true;
            }
        }
        return false;
    }
    public static Body decodeBody(InputStream in, String contentTransferEncoding)
            throws IOException {
        if (contentTransferEncoding != null) {
            contentTransferEncoding =
                MimeUtility.getHeaderParameter(contentTransferEncoding, null);
            if ("quoted-printable".equalsIgnoreCase(contentTransferEncoding)) {
                in = new QuotedPrintableInputStream(in);
            }
            else if ("base64".equalsIgnoreCase(contentTransferEncoding)) {
                in = new Base64InputStream(in, Base64.DEFAULT);
            }
        }
        BinaryTempFileBody tempBody = new BinaryTempFileBody();
        OutputStream out = tempBody.getOutputStream();
        IOUtils.copy(in, out);
        out.close();
        return tempBody;
    }
    public static void collectParts(Part part, ArrayList<Part> viewables,
            ArrayList<Part> attachments) throws MessagingException {
        String disposition = part.getDisposition();
        String dispositionType = null;
        String dispositionFilename = null;
        if (disposition != null) {
            dispositionType = MimeUtility.getHeaderParameter(disposition, null);
            dispositionFilename = MimeUtility.getHeaderParameter(disposition, "filename");
        }
        boolean attachment = ("attachment".equalsIgnoreCase(dispositionType))
                || (dispositionFilename != null)
                && (!"inline".equalsIgnoreCase(dispositionType));
        if (part.getBody() instanceof Multipart) {
            Multipart mp = (Multipart)part.getBody();
            for (int i = 0; i < mp.getCount(); i++) {
                collectParts(mp.getBodyPart(i), viewables, attachments);
            }
        }
        else if (part.getBody() instanceof Message) {
            Message message = (Message)part.getBody();
            collectParts(message, viewables, attachments);
        }
        else if ((!attachment) && (part.getMimeType().equalsIgnoreCase("text/html"))) {
            viewables.add(part);
        }
        else if ((!attachment) && (part.getMimeType().equalsIgnoreCase("text/plain"))) {
            viewables.add(part);
        }
        else {
            attachments.add(part);
        }
    }
}
