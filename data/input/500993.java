public class MimeStreamParser {
    private static final Log log = LogFactory.getLog(MimeStreamParser.class);
    private static final boolean DEBUG_LOG_MESSAGE = false; 
    private static BitSet fieldChars = null;
    private RootInputStream rootStream = null;
    private LinkedList bodyDescriptors = new LinkedList();
    private ContentHandler handler = null;
    private boolean raw = false;
    static {
        fieldChars = new BitSet();
        for (int i = 0x21; i <= 0x39; i++) {
            fieldChars.set(i);
        }
        for (int i = 0x3b; i <= 0x7e; i++) {
            fieldChars.set(i);
        }
    }
    public MimeStreamParser() {
    }
    public void parse(InputStream is) throws IOException {
        if (DEBUG_LOG_MESSAGE && Email.DEBUG) {
            is = new LoggingInputStream(is, "MIME", true);
        }
        rootStream = new RootInputStream(is);
        parseMessage(rootStream);
    }
    public boolean isRaw() {
        return raw;
    }
    public void setRaw(boolean raw) {
        this.raw = raw;
    }
    public void stop() {
        rootStream.truncate();
    }
    private void parseEntity(InputStream is) throws IOException {
        BodyDescriptor bd = parseHeader(is);
        if (bd.isMultipart()) {
            bodyDescriptors.addFirst(bd);
            handler.startMultipart(bd);
            MimeBoundaryInputStream tempIs = 
                new MimeBoundaryInputStream(is, bd.getBoundary());
            handler.preamble(new CloseShieldInputStream(tempIs));
            tempIs.consume();
            while (tempIs.hasMoreParts()) {
                tempIs = new MimeBoundaryInputStream(is, bd.getBoundary());
                parseBodyPart(tempIs);
                tempIs.consume();
                if (tempIs.parentEOF()) {
                    if (log.isWarnEnabled()) {
                        log.warn("Line " + rootStream.getLineNumber() 
                                + ": Body part ended prematurely. "
                                + "Higher level boundary detected or "
                                + "EOF reached.");
                    }
                    break;
                }
            }
            handler.epilogue(new CloseShieldInputStream(is));
            handler.endMultipart();
            bodyDescriptors.removeFirst();
        } else if (bd.isMessage()) {
            if (bd.isBase64Encoded()) {
                log.warn("base64 encoded message/rfc822 detected");
                is = new EOLConvertingInputStream(
                        new Base64InputStream(is));
            } else if (bd.isQuotedPrintableEncoded()) {
                log.warn("quoted-printable encoded message/rfc822 detected");
                is = new EOLConvertingInputStream(
                        new QuotedPrintableInputStream(is));
            }
            bodyDescriptors.addFirst(bd);
            parseMessage(is);
            bodyDescriptors.removeFirst();
        } else {
            handler.body(bd, new CloseShieldInputStream(is));
        }
        while (is.read() != -1) {
        }
    }
    private void parseMessage(InputStream is) throws IOException {
        if (raw) {
            handler.raw(new CloseShieldInputStream(is));
        } else {
            handler.startMessage();
            parseEntity(is);
            handler.endMessage();
        }
    }
    private void parseBodyPart(InputStream is) throws IOException {
        if (raw) {
            handler.raw(new CloseShieldInputStream(is));
        } else {
            handler.startBodyPart();
            parseEntity(is);
            handler.endBodyPart();
        }
    }
    private BodyDescriptor parseHeader(InputStream is) throws IOException {
        BodyDescriptor bd = new BodyDescriptor(bodyDescriptors.isEmpty() 
                        ? null : (BodyDescriptor) bodyDescriptors.getFirst());
        handler.startHeader();
        int lineNumber = rootStream.getLineNumber();
        StringBuffer sb = new StringBuffer();
        int curr = 0;
        int prev = 0;
        while ((curr = is.read()) != -1) {
            if (curr == '\n' && (prev == '\n' || prev == 0)) {
                sb.deleteCharAt(sb.length() - 1);
                break;
            }
            sb.append((char) curr);
            prev = curr == '\r' ? prev : curr;
        }
        if (curr == -1 && log.isWarnEnabled()) {
            log.warn("Line " + rootStream.getLineNumber()  
                    + ": Unexpected end of headers detected. "
                    + "Boundary detected in header or EOF reached.");
        }
        int start = 0;
        int pos = 0;
        int startLineNumber = lineNumber;
        while (pos < sb.length()) {
            while (pos < sb.length() && sb.charAt(pos) != '\r') {
                pos++;
            }
            if (pos < sb.length() - 1 && sb.charAt(pos + 1) != '\n') {
                pos++;
                continue;
            }
            if (pos >= sb.length() - 2 || fieldChars.get(sb.charAt(pos + 2))) {
                String field = sb.substring(start, pos);
                start = pos + 2;
                int index = field.indexOf(':');
                boolean valid = false;
                if (index != -1 && fieldChars.get(field.charAt(0))) {
                    valid = true;
                    String fieldName = field.substring(0, index).trim();
                    for (int i = 0; i < fieldName.length(); i++) {
                        if (!fieldChars.get(fieldName.charAt(i))) {
                            valid = false;
                            break;
                        }
                    }
                    if (valid) {
                        handler.field(field);
                        bd.addField(fieldName, field.substring(index + 1));
                    }                        
                }
                if (!valid && log.isWarnEnabled()) {
                    log.warn("Line " + startLineNumber 
                            + ": Ignoring invalid field: '" + field.trim() + "'");
                }          
                startLineNumber = lineNumber;
            }
            pos += 2;
            lineNumber++;
        }
        handler.endHeader();
        return bd;
    }
    public void setContentHandler(ContentHandler h) {
        this.handler = h;
    }
}
