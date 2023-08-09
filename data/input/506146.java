public class DigestScheme extends RFC2617Scheme {
    private static final char[] HEXADECIMAL = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 
        'e', 'f'
    };
    private boolean complete;
    private static final String NC = "00000001"; 
    private static final int QOP_MISSING = 0;
    private static final int QOP_AUTH_INT = 1;
    private static final int QOP_AUTH = 2;
    private int qopVariant = QOP_MISSING;
    private String cnonce;
    public DigestScheme() {
        super();
        this.complete = false;
    }
    @Override
    public void processChallenge(
            final Header header) throws MalformedChallengeException {
        super.processChallenge(header);
        if (getParameter("realm") == null) {
            throw new MalformedChallengeException("missing realm in challange");
        }
        if (getParameter("nonce") == null) {
            throw new MalformedChallengeException("missing nonce in challange");   
        }
        boolean unsupportedQop = false;
        String qop = getParameter("qop");
        if (qop != null) {
            StringTokenizer tok = new StringTokenizer(qop,",");
            while (tok.hasMoreTokens()) {
                String variant = tok.nextToken().trim();
                if (variant.equals("auth")) {
                    qopVariant = QOP_AUTH;
                    break; 
                } else if (variant.equals("auth-int")) {
                    qopVariant = QOP_AUTH_INT;               
                } else {
                    unsupportedQop = true;
                }     
            }
        }        
        if (unsupportedQop && (qopVariant == QOP_MISSING)) {
            throw new MalformedChallengeException("None of the qop methods is supported");   
        }
        this.cnonce = null;
        this.complete = true;
    }
    public boolean isComplete() {
        String s = getParameter("stale");
        if ("true".equalsIgnoreCase(s)) {
            return false;
        } else {
            return this.complete;
        }
    }
    public String getSchemeName() {
        return "digest";
    }
    public boolean isConnectionBased() {
        return false;    
    }
    public void overrideParamter(final String name, final String value) {
        getParameters().put(name, value);
    }
    private String getCnonce() {
        if (this.cnonce == null) {
            this.cnonce = createCnonce();
        }
        return this.cnonce; 
    }
    public Header authenticate(
            final Credentials credentials, 
            final HttpRequest request) throws AuthenticationException {
        if (credentials == null) {
            throw new IllegalArgumentException("Credentials may not be null");
        }
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        getParameters().put("methodname", request.getRequestLine().getMethod());
        getParameters().put("uri", request.getRequestLine().getUri());
        String charset = getParameter("charset");
        if (charset == null) {
            charset = AuthParams.getCredentialCharset(request.getParams());
            getParameters().put("charset", charset);
        }
        String digest = createDigest(credentials);
        return createDigestHeader(credentials, digest);
    }
    private static MessageDigest createMessageDigest(
            final String digAlg) throws UnsupportedDigestAlgorithmException {
        try {
            return MessageDigest.getInstance(digAlg);
        } catch (Exception e) {
            throw new UnsupportedDigestAlgorithmException(
              "Unsupported algorithm in HTTP Digest authentication: "
               + digAlg);
        }
    }
    private String createDigest(final Credentials credentials) throws AuthenticationException {
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String method = getParameter("methodname");
        String algorithm = getParameter("algorithm");
        if (uri == null) {
            throw new IllegalStateException("URI may not be null");
        }
        if (realm == null) {
            throw new IllegalStateException("Realm may not be null");
        }
        if (nonce == null) {
            throw new IllegalStateException("Nonce may not be null");
        }
        if (algorithm == null) {
            algorithm = "MD5";
        }
        String charset = getParameter("charset");
        if (charset == null) {
            charset = "ISO-8859-1";
        }
        if (qopVariant == QOP_AUTH_INT) {
            throw new AuthenticationException(
                "Unsupported qop in HTTP Digest authentication");   
        }
        MessageDigest md5Helper = createMessageDigest("MD5");
        String uname = credentials.getUserPrincipal().getName();
        String pwd = credentials.getPassword();
        StringBuilder tmp = new StringBuilder(uname.length() + realm.length() + pwd.length() + 2);
        tmp.append(uname);
        tmp.append(':');
        tmp.append(realm);
        tmp.append(':');
        tmp.append(pwd);
        String a1 = tmp.toString();
        if(algorithm.equals("MD5-sess")) {
            String cnonce = getCnonce();
            String tmp2=encode(md5Helper.digest(EncodingUtils.getBytes(a1, charset)));
            StringBuilder tmp3 = new StringBuilder(tmp2.length() + nonce.length() + cnonce.length() + 2);
            tmp3.append(tmp2);
            tmp3.append(':');
            tmp3.append(nonce);
            tmp3.append(':');
            tmp3.append(cnonce);
            a1 = tmp3.toString();
        } else if (!algorithm.equals("MD5")) {
            throw new AuthenticationException("Unhandled algorithm " + algorithm + " requested");
        }
        String md5a1 = encode(md5Helper.digest(EncodingUtils.getBytes(a1, charset)));
        String a2 = null;
        if (qopVariant == QOP_AUTH_INT) {
        } else {
            a2 = method + ':' + uri;
        }
        String md5a2 = encode(md5Helper.digest(EncodingUtils.getAsciiBytes(a2)));
        String serverDigestValue;
        if (qopVariant == QOP_MISSING) {
            StringBuilder tmp2 = new StringBuilder(md5a1.length() + nonce.length() + md5a2.length());
            tmp2.append(md5a1);
            tmp2.append(':');
            tmp2.append(nonce);
            tmp2.append(':');
            tmp2.append(md5a2);
            serverDigestValue = tmp2.toString();
        } else {
            String qopOption = getQopVariantString();
            String cnonce = getCnonce();
            StringBuilder tmp2 = new StringBuilder(md5a1.length() + nonce.length()
                + NC.length() + cnonce.length() + qopOption.length() + md5a2.length() + 5);
            tmp2.append(md5a1);
            tmp2.append(':');
            tmp2.append(nonce);
            tmp2.append(':');
            tmp2.append(NC);
            tmp2.append(':');
            tmp2.append(cnonce);
            tmp2.append(':');
            tmp2.append(qopOption);
            tmp2.append(':');
            tmp2.append(md5a2); 
            serverDigestValue = tmp2.toString();
        }
        String serverDigest =
            encode(md5Helper.digest(EncodingUtils.getAsciiBytes(serverDigestValue)));
        return serverDigest;
    }
    private Header createDigestHeader(
            final Credentials credentials, 
            final String digest) throws AuthenticationException {
        CharArrayBuffer buffer = new CharArrayBuffer(128);
        if (isProxy()) {
            buffer.append(AUTH.PROXY_AUTH_RESP);
        } else {
            buffer.append(AUTH.WWW_AUTH_RESP);
        }
        buffer.append(": Digest ");
        String uri = getParameter("uri");
        String realm = getParameter("realm");
        String nonce = getParameter("nonce");
        String opaque = getParameter("opaque");
        String response = digest;
        String algorithm = getParameter("algorithm");
        String uname = credentials.getUserPrincipal().getName();
        List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>(20);
        params.add(new BasicNameValuePair("username", uname));
        params.add(new BasicNameValuePair("realm", realm));
        params.add(new BasicNameValuePair("nonce", nonce));
        params.add(new BasicNameValuePair("uri", uri));
        params.add(new BasicNameValuePair("response", response));
        if (qopVariant != QOP_MISSING) {
            params.add(new BasicNameValuePair("qop", getQopVariantString()));
            params.add(new BasicNameValuePair("nc", NC));
            params.add(new BasicNameValuePair("cnonce", getCnonce()));
        }
        if (algorithm != null) {
            params.add(new BasicNameValuePair("algorithm", algorithm));
        }    
        if (opaque != null) {
            params.add(new BasicNameValuePair("opaque", opaque));
        }
        for (int i = 0; i < params.size(); i++) {
            BasicNameValuePair param = params.get(i);
            if (i > 0) {
                buffer.append(", ");
            }
            boolean noQuotes = "nc".equals(param.getName()) ||
                               "qop".equals(param.getName());
            BasicHeaderValueFormatter.DEFAULT
                .formatNameValuePair(buffer, param, !noQuotes);
        }
        return new BufferedHeader(buffer);
    }
    private String getQopVariantString() {
        String qopOption;
        if (qopVariant == QOP_AUTH_INT) {
            qopOption = "auth-int";   
        } else {
            qopOption = "auth";
        }
        return qopOption;            
    }
    private static String encode(byte[] binaryData) {
        if (binaryData.length != 16) {
            return null;
        } 
        char[] buffer = new char[32];
        for (int i = 0; i < 16; i++) {
            int low = (binaryData[i] & 0x0f);
            int high = ((binaryData[i] & 0xf0) >> 4);
            buffer[i * 2] = HEXADECIMAL[high];
            buffer[(i * 2) + 1] = HEXADECIMAL[low];
        }
        return new String(buffer);
    }
    public static String createCnonce() {
        String cnonce;
        MessageDigest md5Helper = createMessageDigest("MD5");
        cnonce = Long.toString(System.currentTimeMillis());
        cnonce = encode(md5Helper.digest(EncodingUtils.getAsciiBytes(cnonce)));
        return cnonce;
    }
}
