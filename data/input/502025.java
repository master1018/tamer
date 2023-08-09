public class ImapResponseParser {
    private static boolean DEBUG_LOG_RAW_STREAM = false;
    private final static SimpleDateFormat DATE_TIME_FORMAT =
            new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss Z", Locale.US);
    private final PeekableInputStream mIn;
    private InputStream mActiveLiteral;
    private final DiscourseLogger mDiscourseLogger;
    public ImapResponseParser(InputStream in, DiscourseLogger discourseLogger) {
        if (DEBUG_LOG_RAW_STREAM && Config.LOGD && Email.DEBUG) {
            in = new LoggingInputStream(in);
        }
        this.mIn = new PeekableInputStream(in);
        mDiscourseLogger = discourseLogger;
    }
    private int readByte() throws IOException {
        int ret = mIn.read();
        if (ret != -1) {
            mDiscourseLogger.addReceivedByte(ret);
        }
        return ret;
    }
    public ImapResponse readResponse() throws IOException {
        try {
            ImapResponse response = new ImapResponse();
            if (mActiveLiteral != null) {
                while (mActiveLiteral.read() != -1)
                    ;
                mActiveLiteral = null;
            }
            int ch = mIn.peek();
            if (ch == '*') {
                parseUntaggedResponse();
                readTokens(response);
            } else if (ch == '+') {
                response.mCommandContinuationRequested =
                        parseCommandContinuationRequest();
                readTokens(response);
            } else {
                response.mTag = parseTaggedResponse();
                readTokens(response);
            }
            if (Config.LOGD) {
                if (Email.DEBUG) {
                    Log.d(Email.LOG_TAG, "<<< " + response.toString());
                }
            }
            return response;
        } catch (RuntimeException e) {
            onParseError(e);
            throw e;
        } catch (IOException e) {
            onParseError(e);
            throw e;
        }
    }
    private void onParseError(Exception e) {
        try {
            for (int i = 0; i < 4; i++) {
                int b = readByte();
                if (b == -1 || b == '\n') {
                    break;
                }
            }
        } catch (IOException ignore) {
        }
        Log.w(Email.LOG_TAG, "Exception detected: " + e.getMessage());
        mDiscourseLogger.logLastDiscourse();
    }
    private void readTokens(ImapResponse response) throws IOException {
        response.clear();
        Object token;
        while ((token = readToken()) != null) {
            if (response != null) {
                response.add(token);
            }
            if (mActiveLiteral != null) {
                break;
            }
        }
        response.mCompleted = token == null;
    }
    public Object readToken() throws IOException {
        while (true) {
            Object token = parseToken();
            if (token == null || !(token.equals(")") || token.equals("]"))) {
                return token;
            }
        }
    }
    private Object parseToken() throws IOException {
        if (mActiveLiteral != null) {
            while (mActiveLiteral.read() != -1)
                ;
            mActiveLiteral = null;
        }
        while (true) {
            int ch = mIn.peek();
            if (ch == '(') {
                return parseList('(', ")");
            } else if (ch == ')') {
                expect(')');
                return ")";
            } else if (ch == '[') {
                return parseList('[', "]");
            } else if (ch == ']') {
                expect(']');
                return "]";
            } else if (ch == '"') {
                return parseQuoted();
            } else if (ch == '{') {
                mActiveLiteral = parseLiteral();
                return mActiveLiteral;
            } else if (ch == ' ') {
                expect(' ');
            } else if (ch == '\r') {
                expect('\r');
                expect('\n');
                return null;
            } else if (ch == '\n') {
                expect('\n');
                return null;
            } else {
                return parseAtom();
            }
        }
    }
    private boolean parseCommandContinuationRequest() throws IOException {
        expect('+');
        expect(' ');
        return true;
    }
    private void parseUntaggedResponse() throws IOException {
        expect('*');
        expect(' ');
    }
    private String parseTaggedResponse() throws IOException {
        String tag = readStringUntil(' ');
        return tag;
    }
    private ImapList parseList(char opener, String closer) throws IOException {
        expect(opener);
        ImapList list = new ImapList();
        Object token;
        while (true) {
            token = parseToken();
            if (token == null) {
                break;
            } else if (token instanceof InputStream) {
                list.add(token);
                break;
            } else if (token.equals(closer)) {
                break;
            } else {
                list.add(token);
            }
        }
        return list;
    }
    private String parseAtom() throws IOException {
        StringBuffer sb = new StringBuffer();
        int ch;
        while (true) {
            ch = mIn.peek();
            if (ch == -1) {
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, "parseAtom(): end of stream reached");
                }
                throw new IOException("parseAtom(): end of stream reached");
            } else if (ch == '(' || ch == ')' || ch == '{' || ch == ' ' ||
                    ch == ']' ||
                    ch == '%' ||
                    ch == '"' || (ch >= 0x00 && ch <= 0x1f) || ch == 0x7f) {
                if (sb.length() == 0) {
                    throw new IOException(String.format("parseAtom(): (%04x %c)", ch, ch));
                }
                return sb.toString();
            } else {
                sb.append((char)readByte());
            }
        }
    }
    private InputStream parseLiteral() throws IOException {
        expect('{');
        int size = Integer.parseInt(readStringUntil('}'));
        expect('\r');
        expect('\n');
        FixedLengthInputStream fixed = new FixedLengthInputStream(mIn, size);
        return fixed;
    }
    private String parseQuoted() throws IOException {
        expect('"');
        return readStringUntil('"');
    }
    private String readStringUntil(char end) throws IOException {
        StringBuffer sb = new StringBuffer();
        int ch;
        while ((ch = readByte()) != -1) {
            if (ch == end) {
                return sb.toString();
            } else {
                sb.append((char)ch);
            }
        }
        if (Config.LOGD && Email.DEBUG) {
            Log.d(Email.LOG_TAG, "readQuotedString(): end of stream reached");
        }
        throw new IOException("readQuotedString(): end of stream reached");
    }
    private int expect(char ch) throws IOException {
        int d;
        if ((d = readByte()) != ch) {
            if (d == -1 && Config.LOGD && Email.DEBUG) {
                Log.d(Email.LOG_TAG, "expect(): end of stream reached");
            }
            throw new IOException(String.format("Expected %04x (%c) but got %04x (%c)", (int)ch,
                    ch, d, (char)d));
        }
        return d;
    }
    public class ImapList extends ArrayList<Object> {
        public ImapList getList(int index) {
            return (ImapList)get(index);
        }
        public ImapList getListOrNull(int index) {
            if (index < size()) {
                Object list = get(index);
                if (list instanceof ImapList) {
                    return (ImapList) list;
                }
            }
            return null;
        }
        public String getString(int index) {
            return (String)get(index);
        }
        public String getStringOrNull(int index) {
            if (index < size()) {
                Object string = get(index);
                if (string instanceof String) {
                    return (String) string;
                }
            }
            return null;
        }
        public InputStream getLiteral(int index) {
            return (InputStream)get(index);
        }
        public int getNumber(int index) {
            return Integer.parseInt(getString(index));
        }
        public Date getDate(int index) throws MessagingException {
            try {
                return DATE_TIME_FORMAT.parse(getString(index));
            } catch (ParseException pe) {
                throw new MessagingException("Unable to parse IMAP datetime", pe);
            }
        }
        public Object getKeyedValue(Object key) {
            for (int i = 0, count = size(); i < count; i++) {
                if (get(i).equals(key)) {
                    return get(i + 1);
                }
            }
            return null;
        }
        public ImapList getKeyedList(Object key) {
            return (ImapList)getKeyedValue(key);
        }
        public String getKeyedString(Object key) {
            return (String)getKeyedValue(key);
        }
        public InputStream getKeyedLiteral(Object key) {
            return (InputStream)getKeyedValue(key);
        }
        public int getKeyedNumber(Object key) {
            return Integer.parseInt(getKeyedString(key));
        }
        public Date getKeyedDate(Object key) throws MessagingException {
            try {
                String value = getKeyedString(key);
                if (value == null) {
                    return null;
                }
                return DATE_TIME_FORMAT.parse(value);
            } catch (ParseException pe) {
                throw new MessagingException("Unable to parse IMAP datetime", pe);
            }
        }
    }
    public class ImapResponse extends ImapList {
        private boolean mCompleted;
        boolean mCommandContinuationRequested;
        String mTag;
        public boolean completed() {
            return mCompleted;
        }
        public void nailDown() throws IOException {
            int last = size() - 1;
            if (last >= 0) {
                Object o = get(last);
                if (o instanceof FixedLengthInputStream) {
                    FixedLengthInputStream is = (FixedLengthInputStream) o;
                    byte[] buffer = new byte[is.available()];
                    is.read(buffer);
                    set(last, new String(buffer));
                }
            }
        }
        public void appendAll(ImapResponse other) {
            addAll(other);
            mCompleted = other.mCompleted;
        }
        public boolean more() throws IOException {
            if (mCompleted) {
                return false;
            }
            readTokens(this);
            return true;
        }
        public String getAlertText() {
            if (size() > 1) {
                ImapList alertList = this.getListOrNull(1);
                if (alertList != null) {
                    String responseCode = alertList.getStringOrNull(0);
                    if ("ALERT".equalsIgnoreCase(responseCode)) {
                        StringBuffer sb = new StringBuffer();
                        for (int i = 2, count = size(); i < count; i++) {
                            if (i > 2) {
                                sb.append(' ');
                            }
                            sb.append(get(i).toString());
                        }
                        return sb.toString();
                    }
                }
            }
            return null;
        }
        @Override
        public String toString() {
            return "#" + mTag + "# " + super.toString();
        }
    }
}
