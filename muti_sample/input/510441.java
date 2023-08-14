public class ContentTypeField extends Field {
    public static final String TYPE_MULTIPART_PREFIX = "multipart/";
    public static final String TYPE_MULTIPART_DIGEST = "multipart/digest";
    public static final String TYPE_TEXT_PLAIN = "text/plain";
    public static final String TYPE_MESSAGE_RFC822 = "message/rfc822";
    public static final String PARAM_BOUNDARY = "boundary";
    public static final String PARAM_CHARSET = "charset";
    private String mimeType = "";
    private Map parameters = null;
    private ParseException parseException;
    protected ContentTypeField(String name, String body, String raw, String mimeType, Map parameters, ParseException parseException) {
        super(name, body, raw);
        this.mimeType = mimeType;
        this.parameters = parameters;
        this.parseException = parseException;
    }
    public ParseException getParseException() {
        return parseException;
    }
    public String getMimeType() {
        return mimeType;
    }
    public static String getMimeType(ContentTypeField child, 
                                     ContentTypeField parent) {
        if (child == null || child.getMimeType().length() == 0 
                || child.isMultipart() && child.getBoundary() == null) {
            if (parent != null && parent.isMimeType(TYPE_MULTIPART_DIGEST)) {
                return TYPE_MESSAGE_RFC822;
            } else {
                return TYPE_TEXT_PLAIN;
            }
        }
        return child.getMimeType();
    }
    public String getParameter(String name) {
        return parameters != null 
                    ? (String) parameters.get(name.toLowerCase())
                    : null;
    }
    public Map getParameters() {
        return parameters != null 
                    ? Collections.unmodifiableMap(parameters)
                    : Collections.EMPTY_MAP;
    }
    public String getBoundary() {
        return getParameter(PARAM_BOUNDARY);
    }
    public String getCharset() {
        return getParameter(PARAM_CHARSET);
    }
    public static String getCharset(ContentTypeField f) {
        if (f != null) {
            if (f.getCharset() != null && f.getCharset().length() > 0) {
                return f.getCharset();
            }
        }
        return "us-ascii";
    }
    public boolean isMimeType(String mimeType) {
        return this.mimeType.equalsIgnoreCase(mimeType);
    }
    public boolean isMultipart() {
        return mimeType.startsWith(TYPE_MULTIPART_PREFIX);
    }
    public static class Parser implements FieldParser {
        private static Log log = LogFactory.getLog(Parser.class);
        public Field parse(final String name, final String body, final String raw) {
            ParseException parseException = null;
            String mimeType = "";
            Map parameters = null;
            ContentTypeParser parser = new ContentTypeParser(new StringReader(body));
            try {
                parser.parseAll();
            }
            catch (ParseException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Parsing value '" + body + "': "+ e.getMessage());
                }
                parseException = e;
            }
            catch (TokenMgrError e) {
                if (log.isDebugEnabled()) {
                    log.debug("Parsing value '" + body + "': "+ e.getMessage());
                }
                parseException = new ParseException(e.getMessage());
            }
            try {
                final String type = parser.getType();
                final String subType = parser.getSubType();
                if (type != null && subType != null) {
                    mimeType = (type + "/" + parser.getSubType()).toLowerCase();
                    ArrayList paramNames = parser.getParamNames();
                    ArrayList paramValues = parser.getParamValues();
                    if (paramNames != null && paramValues != null) {
                        for (int i = 0; i < paramNames.size() && i < paramValues.size(); i++) {
                            if (parameters == null)
                                parameters = new HashMap((int)(paramNames.size() * 1.3 + 1));
                            String paramName = ((String)paramNames.get(i)).toLowerCase();
                            String paramValue = ((String)paramValues.get(i));
                            parameters.put(paramName, paramValue);
                        }
                    }
                }
            }
            catch (NullPointerException npe) {
            }
            return new ContentTypeField(name, body, raw, mimeType, parameters, parseException);
        }
    }
}
