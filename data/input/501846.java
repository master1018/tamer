public class BodyDescriptor {
    private static Log log = LogFactory.getLog(BodyDescriptor.class);
    private String mimeType = "text/plain";
    private String boundary = null;
    private String charset = "us-ascii";
    private String transferEncoding = "7bit";
    private Map parameters = new HashMap();
    private boolean contentTypeSet = false;
    private boolean contentTransferEncSet = false;
    public BodyDescriptor() {
        this(null);
    }
    public BodyDescriptor(BodyDescriptor parent) {
        if (parent != null && parent.isMimeType("multipart/digest")) {
            mimeType = "message/rfc822";
        } else {
            mimeType = "text/plain";
        }
    }
    public void addField(String name, String value) {
        name = name.trim().toLowerCase();
        if (name.equals("content-transfer-encoding") && !contentTransferEncSet) {
            contentTransferEncSet = true;
            value = value.trim().toLowerCase();
            if (value.length() > 0) {
                transferEncoding = value;
            }
        } else if (name.equals("content-type") && !contentTypeSet) {
            contentTypeSet = true;
            value = value.trim();
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < value.length(); i++) {
                char c = value.charAt(i);
                if (c == '\r' || c == '\n') {
                    continue;
                }
                sb.append(c);
            }
            Map params = getHeaderParams(sb.toString());
            String main = (String) params.get("");
            if (main != null) {
                main = main.toLowerCase().trim();
                int index = main.indexOf('/');
                boolean valid = false;
                if (index != -1) {
                    String type = main.substring(0, index).trim();
                    String subtype = main.substring(index + 1).trim();
                    if (type.length() > 0 && subtype.length() > 0) {
                        main = type + "/" + subtype;
                        valid = true;
                    }
                }
                if (!valid) {
                    main = null;
                }
            }
            String b = (String) params.get("boundary");
            if (main != null 
                    && ((main.startsWith("multipart/") && b != null) 
                            || !main.startsWith("multipart/"))) {
                mimeType = main;
            }
            if (isMultipart()) {
                boundary = b;
            }
            String c = (String) params.get("charset");
            if (c != null) {
                c = c.trim();
                if (c.length() > 0) {
                    charset = c.toLowerCase();
                }
            }
            parameters.putAll(params);
            parameters.remove("");
            parameters.remove("boundary");
            parameters.remove("charset");
        }
    }
    private Map getHeaderParams(String headerValue) {
        Map result = new HashMap();
        String main;
        String rest;
        if (headerValue.indexOf(";") == -1) {
            main = headerValue;
            rest = null;
        } else {
            main = headerValue.substring(0, headerValue.indexOf(";"));
            rest = headerValue.substring(main.length() + 1);
        }
        result.put("", main);
        if (rest != null) {
            char[] chars = rest.toCharArray();
            StringBuffer paramName = new StringBuffer();
            StringBuffer paramValue = new StringBuffer();
            final byte READY_FOR_NAME = 0;
            final byte IN_NAME = 1;
            final byte READY_FOR_VALUE = 2;
            final byte IN_VALUE = 3;
            final byte IN_QUOTED_VALUE = 4;
            final byte VALUE_DONE = 5;
            final byte ERROR = 99;
            byte state = READY_FOR_NAME;
            boolean escaped = false;
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                switch (state) {
                    case ERROR:
                        if (c == ';')
                            state = READY_FOR_NAME;
                        break;
                    case READY_FOR_NAME:
                        if (c == '=') {
                            log.error("Expected header param name, got '='");
                            state = ERROR;
                            break;
                        }
                        paramName = new StringBuffer();
                        paramValue = new StringBuffer();
                        state = IN_NAME;
                    case IN_NAME:
                        if (c == '=') {
                            if (paramName.length() == 0)
                                state = ERROR;
                            else
                                state = READY_FOR_VALUE;
                            break;
                        }
                        paramName.append(c);
                        break;
                    case READY_FOR_VALUE:
                        boolean fallThrough = false;
                        switch (c) {
                            case ' ':
                            case '\t':
                                break;  
                            case '"':
                                state = IN_QUOTED_VALUE;
                                break;
                            default:
                                state = IN_VALUE;
                                fallThrough = true;
                                break;
                        }
                        if (!fallThrough)
                            break;
                    case IN_VALUE:
                        fallThrough = false;
                        switch (c) {
                            case ';':
                            case ' ':
                            case '\t':
                                result.put(
                                   paramName.toString().trim().toLowerCase(),
                                   paramValue.toString().trim());
                                state = VALUE_DONE;
                                fallThrough = true;
                                break;
                            default:
                                paramValue.append(c);
                                break;
                        }
                        if (!fallThrough)
                            break;
                    case VALUE_DONE:
                        switch (c) {
                            case ';':
                                state = READY_FOR_NAME;
                                break;
                            case ' ':
                            case '\t':
                                break;
                            default:
                                state = ERROR;
                                break;
                        }
                        break;
                    case IN_QUOTED_VALUE:
                        switch (c) {
                            case '"':
                                if (!escaped) {
                                    result.put(
                                            paramName.toString().trim().toLowerCase(),
                                            paramValue.toString());
                                    state = VALUE_DONE;
                                } else {
                                    escaped = false;
                                    paramValue.append(c);                                    
                                }
                                break;
                            case '\\':
                                if (escaped) {
                                    paramValue.append('\\');
                                }
                                escaped = !escaped;
                                break;
                            default:
                                if (escaped) {
                                    paramValue.append('\\');
                                }
                                escaped = false;
                                paramValue.append(c);
                                break;
                        }
                        break;
                }
            }
            if (state == IN_VALUE) {
                result.put(
                        paramName.toString().trim().toLowerCase(),
                        paramValue.toString().trim());
            }
        }
        return result;
    }
    public boolean isMimeType(String mimeType) {
        return this.mimeType.equals(mimeType.toLowerCase());
    }
    public boolean isMessage() {
        return mimeType.equals("message/rfc822");
    }
    public boolean isMultipart() {
        return mimeType.startsWith("multipart/");
    }
    public String getMimeType() {
        return mimeType;
    }
    public String getBoundary() {
        return boundary;
    }
    public String getCharset() {
        return charset;
    }
    public Map getParameters() {
        return parameters;
    }
    public String getTransferEncoding() {
        return transferEncoding;
    }
    public boolean isBase64Encoded() {
        return "base64".equals(transferEncoding);
    }
    public boolean isQuotedPrintableEncoded() {
        return "quoted-printable".equals(transferEncoding);
    }
    public String toString() {
        return mimeType;
    }
}
