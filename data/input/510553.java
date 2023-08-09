public class HttpAuthHeader {
    public final static String BASIC_TOKEN = "Basic";
    public final static String DIGEST_TOKEN = "Digest";
    private final static String REALM_TOKEN = "realm";
    private final static String NONCE_TOKEN = "nonce";
    private final static String STALE_TOKEN = "stale";
    private final static String OPAQUE_TOKEN = "opaque";
    private final static String QOP_TOKEN = "qop";
    private final static String ALGORITHM_TOKEN = "algorithm";
    private int mScheme;
    public static final int UNKNOWN = 0;
    public static final int BASIC = 1;
    public static final int DIGEST = 2;
    private boolean mStale;
    private String mRealm;
    private String mNonce;
    private String mOpaque;
    private String mQop;
    private String mAlgorithm;
    private boolean mIsProxy;
    private String mUsername;
    private String mPassword;
    public HttpAuthHeader(String header) {
        if (header != null) {
            parseHeader(header);
        }
    }
    public boolean isProxy() {
        return mIsProxy;
    }
    public void setProxy() {
        mIsProxy = true;
    }
    public String getUsername() {
        return mUsername;
    }
    public void setUsername(String username) {
        mUsername = username;
    }
    public String getPassword() {
        return mPassword;
    }
    public void setPassword(String password) {
        mPassword = password;
    }
    public boolean isBasic () {
        return mScheme == BASIC;
    }
    public boolean isDigest() {
        return mScheme == DIGEST;
    }
    public int getScheme() {
        return mScheme;
    }
    public boolean getStale() {
        return mStale;
    }
    public String getRealm() {
        return mRealm;
    }
    public String getNonce() {
        return mNonce;
    }
    public String getOpaque() {
        return mOpaque;
    }
    public String getQop() {
        return mQop;
    }
    public String getAlgorithm() {
        return mAlgorithm;
    }
    public boolean isSupportedScheme() {
        if (mRealm != null) {
            if (mScheme == BASIC) {
                return true;
            } else {
                if (mScheme == DIGEST) {
                    return
                        mAlgorithm.equals("md5") &&
                        (mQop == null || mQop.equals("auth"));
                }
            }
        }
        return false;
    }
    private void parseHeader(String header) {
        if (HttpLog.LOGV) {
            HttpLog.v("HttpAuthHeader.parseHeader(): header: " + header);
        }
        if (header != null) {
            String parameters = parseScheme(header);
            if (parameters != null) {
                if (mScheme != UNKNOWN) {
                    parseParameters(parameters);
                }
            }
        }
    }
    private String parseScheme(String header) {
        if (header != null) {
            int i = header.indexOf(' ');
            if (i >= 0) {
                String scheme = header.substring(0, i).trim();
                if (scheme.equalsIgnoreCase(DIGEST_TOKEN)) {
                    mScheme = DIGEST;
                    mAlgorithm = "md5";
                } else {
                    if (scheme.equalsIgnoreCase(BASIC_TOKEN)) {
                        mScheme = BASIC;
                    }
                }
                return header.substring(i + 1);
            }
        }
        return null;
    }
    private void parseParameters(String parameters) {
        if (HttpLog.LOGV) {
            HttpLog.v("HttpAuthHeader.parseParameters():" +
                      " parameters: " + parameters);
        }
        if (parameters != null) {
            int i;
            do {
                i = parameters.indexOf(',');
                if (i < 0) {
                    parseParameter(parameters);
                } else {
                    parseParameter(parameters.substring(0, i));
                    parameters = parameters.substring(i + 1);
                }
            } while (i >= 0);
        }
    }
    private void parseParameter(String parameter) {
        if (parameter != null) {
            int i = parameter.indexOf('=');
            if (i >= 0) {
                String token = parameter.substring(0, i).trim();
                String value =
                    trimDoubleQuotesIfAny(parameter.substring(i + 1).trim());
                if (HttpLog.LOGV) {
                    HttpLog.v("HttpAuthHeader.parseParameter():" +
                              " token: " + token +
                              " value: " + value);
                }
                if (token.equalsIgnoreCase(REALM_TOKEN)) {
                    mRealm = value;
                } else {
                    if (mScheme == DIGEST) {
                        parseParameter(token, value);
                    }
                }
            }
        }
    }
    private void parseParameter(String token, String value) {
        if (token != null && value != null) {
            if (token.equalsIgnoreCase(NONCE_TOKEN)) {
                mNonce = value;
                return;
            }
            if (token.equalsIgnoreCase(STALE_TOKEN)) {
                parseStale(value);
                return;
            }
            if (token.equalsIgnoreCase(OPAQUE_TOKEN)) {
                mOpaque = value;
                return;
            }
            if (token.equalsIgnoreCase(QOP_TOKEN)) {
                mQop = value.toLowerCase();
                return;
            }
            if (token.equalsIgnoreCase(ALGORITHM_TOKEN)) {
                mAlgorithm = value.toLowerCase();
                return;
            }
        }
    }
    private void parseStale(String value) {
        if (value != null) {
            if (value.equalsIgnoreCase("true")) {
                mStale = true;
            }
        }
    }
    static private String trimDoubleQuotesIfAny(String value) {
        if (value != null) {
            int len = value.length();
            if (len > 2 &&
                value.charAt(0) == '\"' && value.charAt(len - 1) == '\"') {
                return value.substring(1, len - 1);
            }
        }
        return value;
    }
}
