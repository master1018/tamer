final class DigestMD5Server extends DigestMD5Base implements SaslServer {
    private static final String MY_CLASS_NAME = DigestMD5Server.class.getName();
    private static final String UTF8_DIRECTIVE = "charset=utf-8,";
    private static final String ALGORITHM_DIRECTIVE = "algorithm=md5-sess";
    private static final int NONCE_COUNT_VALUE = 1;
    private static final String UTF8_PROPERTY =
        "com.sun.security.sasl.digest.utf8";
    private static final String REALM_PROPERTY =
        "com.sun.security.sasl.digest.realm";
    private static final String[] DIRECTIVE_KEY = {
        "username",    
        "realm",       
        "nonce",       
        "cnonce",      
        "nonce-count", 
        "qop",         
        "digest-uri",  
        "response",    
        "maxbuf",      
        "charset",     
        "cipher",      
        "authzid",     
        "auth-param",  
    };
    private static final int USERNAME = 0;
    private static final int REALM = 1;
    private static final int NONCE = 2;
    private static final int CNONCE = 3;
    private static final int NONCE_COUNT = 4;
    private static final int QOP = 5;
    private static final int DIGEST_URI = 6;
    private static final int RESPONSE = 7;
    private static final int MAXBUF = 8;
    private static final int CHARSET = 9;
    private static final int CIPHER = 10;
    private static final int AUTHZID = 11;
    private static final int AUTH_PARAM = 12;
    private String specifiedQops;
    private byte[] myCiphers;
    private List<String> serverRealms;
    DigestMD5Server(String protocol, String serverName, Map props,
        CallbackHandler cbh) throws SaslException {
        super(props, MY_CLASS_NAME, 1, protocol + "/" + serverName, cbh);
        serverRealms = new ArrayList<String>();
        useUTF8 = true;  
        if (props != null) {
            specifiedQops = (String) props.get(Sasl.QOP);
            if ("false".equals((String) props.get(UTF8_PROPERTY))) {
                useUTF8 = false;
                logger.log(Level.FINE, "DIGEST80:Server supports ISO-Latin-1");
            }
            String realms = (String) props.get(REALM_PROPERTY);
            if (realms != null) {
                StringTokenizer parser = new StringTokenizer(realms, ", \t\n");
                int tokenCount = parser.countTokens();
                String token = null;
                for (int i = 0; i < tokenCount; i++) {
                    token = parser.nextToken();
                    logger.log(Level.FINE, "DIGEST81:Server supports realm {0}",
                        token);
                    serverRealms.add(token);
                }
            }
        }
        encoding = (useUTF8 ? "UTF8" : "8859_1");
        if (serverRealms.size() == 0) {
            serverRealms.add(serverName);
        }
    }
    public  byte[] evaluateResponse(byte[] response) throws SaslException {
        if (response.length > MAX_RESPONSE_LENGTH) {
            throw new SaslException(
                "DIGEST-MD5: Invalid digest response length. Got:  " +
                response.length + " Expected < " + MAX_RESPONSE_LENGTH);
        }
        byte[] challenge;
        switch (step) {
        case 1:
            if (response.length != 0) {
                throw new SaslException(
                    "DIGEST-MD5 must not have an initial response");
            }
            String supportedCiphers = null;
            if ((allQop&PRIVACY_PROTECTION) != 0) {
                myCiphers = getPlatformCiphers();
                StringBuffer buf = new StringBuffer();
                for (int i = 0; i < CIPHER_TOKENS.length; i++) {
                    if (myCiphers[i] != 0) {
                        if (buf.length() > 0) {
                            buf.append(',');
                        }
                        buf.append(CIPHER_TOKENS[i]);
                    }
                }
                supportedCiphers = buf.toString();
            }
            try {
                challenge = generateChallenge(serverRealms, specifiedQops,
                    supportedCiphers);
                step = 3;
                return challenge;
            } catch (UnsupportedEncodingException e) {
                throw new SaslException(
                    "DIGEST-MD5: Error encoding challenge", e);
            } catch (IOException e) {
                throw new SaslException(
                    "DIGEST-MD5: Error generating challenge", e);
            }
        case 3:
            try {
                byte[][] responseVal = parseDirectives(response, DIRECTIVE_KEY,
                    null, REALM);
                challenge = validateClientResponse(responseVal);
            } catch (SaslException e) {
                throw e;
            } catch (UnsupportedEncodingException e) {
                throw new SaslException(
                    "DIGEST-MD5: Error validating client response", e);
            } finally {
                step = 0;  
            }
            completed = true;
            if (integrity && privacy) {
                secCtx = new DigestPrivacy(false );
            } else if (integrity) {
                secCtx = new DigestIntegrity(false );
            }
            return challenge;
        default:
            throw new SaslException("DIGEST-MD5: Server at illegal state");
        }
    }
    private byte[] generateChallenge(List<String> realms, String qopStr,
        String cipherStr) throws UnsupportedEncodingException, IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        for (int i = 0; realms != null && i < realms.size(); i++) {
            out.write("realm=\"".getBytes(encoding));
            writeQuotedStringValue(out, realms.get(i).getBytes(encoding));
            out.write('"');
            out.write(',');
        }
        out.write(("nonce=\"").getBytes(encoding));
        nonce = generateNonce();
        writeQuotedStringValue(out, nonce);
        out.write('"');
        out.write(',');
        if (qopStr != null) {
            out.write(("qop=\"").getBytes(encoding));
            writeQuotedStringValue(out, qopStr.getBytes(encoding));
            out.write('"');
            out.write(',');
        }
        if (recvMaxBufSize != DEFAULT_MAXBUF) {
            out.write(("maxbuf=\"" + recvMaxBufSize + "\",").getBytes(encoding));
        }
        if (useUTF8) {
            out.write(UTF8_DIRECTIVE.getBytes(encoding));
        }
        if (cipherStr != null) {
            out.write("cipher=\"".getBytes(encoding));
            writeQuotedStringValue(out, cipherStr.getBytes(encoding));
            out.write('"');
            out.write(',');
        }
        out.write(ALGORITHM_DIRECTIVE.getBytes(encoding));
        return out.toByteArray();
    }
    private byte[] validateClientResponse(byte[][] responseVal)
        throws SaslException, UnsupportedEncodingException {
        if (responseVal[CHARSET] != null) {
            if (!useUTF8 ||
                !"utf-8".equals(new String(responseVal[CHARSET], encoding))) {
                throw new SaslException("DIGEST-MD5: digest response format " +
                    "violation. Incompatible charset value: " +
                    new String(responseVal[CHARSET]));
            }
        }
        int clntMaxBufSize =
            (responseVal[MAXBUF] == null) ? DEFAULT_MAXBUF
            : Integer.parseInt(new String(responseVal[MAXBUF], encoding));
        sendMaxBufSize = ((sendMaxBufSize == 0) ? clntMaxBufSize :
            Math.min(sendMaxBufSize, clntMaxBufSize));
        String username;
        if (responseVal[USERNAME] != null) {
            username = new String(responseVal[USERNAME], encoding);
            logger.log(Level.FINE, "DIGEST82:Username: {0}", username);
        } else {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Missing username.");
        }
        negotiatedRealm = ((responseVal[REALM] != null) ?
            new String(responseVal[REALM], encoding) : "");
        logger.log(Level.FINE, "DIGEST83:Client negotiated realm: {0}",
            negotiatedRealm);
        if (!serverRealms.contains(negotiatedRealm)) {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Nonexistent realm: " + negotiatedRealm);
        }
        if (responseVal[NONCE] == null) {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Missing nonce.");
        }
        byte[] nonceFromClient = responseVal[NONCE];
        if (!Arrays.equals(nonceFromClient, nonce)) {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Mismatched nonce.");
        }
        if (responseVal[CNONCE] == null) {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Missing cnonce.");
        }
        byte[] cnonce = responseVal[CNONCE];
        if (responseVal[NONCE_COUNT] != null &&
            NONCE_COUNT_VALUE != Integer.parseInt(
                new String(responseVal[NONCE_COUNT], encoding), 16)) {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Nonce count does not match: " +
                new String(responseVal[NONCE_COUNT]));
        }
        negotiatedQop = ((responseVal[QOP] != null) ?
            new String(responseVal[QOP], encoding) : "auth");
        logger.log(Level.FINE, "DIGEST84:Client negotiated qop: {0}",
            negotiatedQop);
        byte cQop;
        if (negotiatedQop.equals("auth")) {
            cQop = NO_PROTECTION;
        } else if (negotiatedQop.equals("auth-int")) {
            cQop = INTEGRITY_ONLY_PROTECTION;
            integrity = true;
            rawSendSize = sendMaxBufSize - 16;
        } else if (negotiatedQop.equals("auth-conf")) {
            cQop = PRIVACY_PROTECTION;
            integrity = privacy = true;
            rawSendSize = sendMaxBufSize - 26;
        } else {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Invalid QOP: " + negotiatedQop);
        }
        if ((cQop&allQop) == 0) {
            throw new SaslException("DIGEST-MD5: server does not support " +
                " qop: " + negotiatedQop);
        }
        if (privacy) {
            negotiatedCipher = ((responseVal[CIPHER] != null) ?
                new String(responseVal[CIPHER], encoding) : null);
            if (negotiatedCipher == null) {
                throw new SaslException("DIGEST-MD5: digest response format " +
                    "violation. No cipher specified.");
            }
            int foundCipher = -1;
            logger.log(Level.FINE, "DIGEST85:Client negotiated cipher: {0}",
                negotiatedCipher);
            for (int j = 0; j < CIPHER_TOKENS.length; j++) {
                if (negotiatedCipher.equals(CIPHER_TOKENS[j]) &&
                    myCiphers[j] != 0) {
                    foundCipher = j;
                    break;
                }
            }
            if (foundCipher == -1) {
                throw new SaslException("DIGEST-MD5: server does not " +
                    "support cipher: " + negotiatedCipher);
            }
            if ((CIPHER_MASKS[foundCipher]&HIGH_STRENGTH) != 0) {
                negotiatedStrength = "high";
            } else if ((CIPHER_MASKS[foundCipher]&MEDIUM_STRENGTH) != 0) {
                negotiatedStrength = "medium";
            } else {
                negotiatedStrength = "low";
            }
            logger.log(Level.FINE, "DIGEST86:Negotiated strength: {0}",
                negotiatedStrength);
        }
        String digestUriFromResponse = ((responseVal[DIGEST_URI]) != null ?
            new String(responseVal[DIGEST_URI], encoding) : null);
        if (digestUriFromResponse != null) {
            logger.log(Level.FINE, "DIGEST87:digest URI: {0}",
                digestUriFromResponse);
        }
        if (digestUri.equalsIgnoreCase(digestUriFromResponse)) {
            digestUri = digestUriFromResponse; 
        } else {
            throw new SaslException("DIGEST-MD5: digest response format " +
                "violation. Mismatched URI: " + digestUriFromResponse +
                "; expecting: " + digestUri);
        }
        byte[] responseFromClient = responseVal[RESPONSE];
        if (responseFromClient == null) {
            throw new SaslException("DIGEST-MD5: digest response format " +
                " violation. Missing response.");
        }
        byte[] authzidBytes;
        String authzidFromClient = ((authzidBytes=responseVal[AUTHZID]) != null?
            new String(authzidBytes, encoding) : username);
        if (authzidBytes != null) {
            logger.log(Level.FINE, "DIGEST88:Authzid: {0}",
                new String(authzidBytes));
        }
        char[] passwd;
        try {
            RealmCallback rcb = new RealmCallback("DIGEST-MD5 realm: ",
                negotiatedRealm);
            NameCallback ncb = new NameCallback("DIGEST-MD5 authentication ID: ",
                username);
            PasswordCallback pcb =
                new PasswordCallback("DIGEST-MD5 password: ", false);
            cbh.handle(new Callback[] {rcb, ncb, pcb});
            passwd = pcb.getPassword();
            pcb.clearPassword();
        } catch (UnsupportedCallbackException e) {
            throw new SaslException(
                "DIGEST-MD5: Cannot perform callback to acquire password", e);
        } catch (IOException e) {
            throw new SaslException(
                "DIGEST-MD5: IO error acquiring password", e);
        }
        if (passwd == null) {
            throw new SaslException(
                "DIGEST-MD5: cannot acquire password for " + username +
                " in realm : " + negotiatedRealm);
        }
        try {
            byte[] expectedResponse;
            try {
                expectedResponse = generateResponseValue("AUTHENTICATE",
                    digestUri, negotiatedQop, username, negotiatedRealm,
                    passwd, nonce ,
                    cnonce, NONCE_COUNT_VALUE, authzidBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new SaslException(
                    "DIGEST-MD5: problem duplicating client response", e);
            } catch (IOException e) {
                throw new SaslException(
                    "DIGEST-MD5: problem duplicating client response", e);
            }
            if (!Arrays.equals(responseFromClient, expectedResponse)) {
                throw new SaslException("DIGEST-MD5: digest response format " +
                    "violation. Mismatched response.");
            }
            try {
                AuthorizeCallback acb =
                    new AuthorizeCallback(username, authzidFromClient);
                cbh.handle(new Callback[]{acb});
                if (acb.isAuthorized()) {
                    authzid = acb.getAuthorizedID();
                } else {
                    throw new SaslException("DIGEST-MD5: " + username +
                        " is not authorized to act as " + authzidFromClient);
                }
            } catch (SaslException e) {
                throw e;
            } catch (UnsupportedCallbackException e) {
                throw new SaslException(
                    "DIGEST-MD5: Cannot perform callback to check authzid", e);
            } catch (IOException e) {
                throw new SaslException(
                    "DIGEST-MD5: IO error checking authzid", e);
            }
            return generateResponseAuth(username, passwd, cnonce,
                NONCE_COUNT_VALUE, authzidBytes);
        } finally {
            for (int i = 0; i < passwd.length; i++) {
                passwd[i] = 0;
            }
        }
    }
    private byte[] generateResponseAuth(String username, char[] passwd,
        byte[] cnonce, int nonceCount, byte[] authzidBytes) throws SaslException {
        try {
            byte[] responseValue = generateResponseValue("",
                digestUri, negotiatedQop, username, negotiatedRealm,
                passwd, nonce, cnonce, nonceCount, authzidBytes);
            byte[] challenge = new byte[responseValue.length + 8];
            System.arraycopy("rspauth=".getBytes(encoding), 0, challenge, 0, 8);
            System.arraycopy(responseValue, 0, challenge, 8,
                responseValue.length );
            return challenge;
        } catch (NoSuchAlgorithmException e) {
            throw new SaslException("DIGEST-MD5: problem generating response", e);
        } catch (IOException e) {
            throw new SaslException("DIGEST-MD5: problem generating response", e);
        }
    }
    public String getAuthorizationID() {
        if (completed) {
            return authzid;
        } else {
            throw new IllegalStateException(
                "DIGEST-MD5 server negotiation not complete");
        }
    }
}
