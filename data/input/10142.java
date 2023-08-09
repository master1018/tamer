final class DigestMD5Client extends DigestMD5Base implements SaslClient {
    private static final String MY_CLASS_NAME = DigestMD5Client.class.getName();
    private static final String CIPHER_PROPERTY =
        "com.sun.security.sasl.digest.cipher";
    private static final String[] DIRECTIVE_KEY = {
        "realm",      
        "qop",        
        "algorithm",  
        "nonce",      
        "maxbuf",     
        "charset",    
        "cipher",     
        "rspauth",    
        "stale",      
    };
    private static final int REALM = 0;
    private static final int QOP = 1;
    private static final int ALGORITHM = 2;
    private static final int NONCE = 3;
    private static final int MAXBUF = 4;
    private static final int CHARSET = 5;
    private static final int CIPHER = 6;
    private static final int RESPONSE_AUTH = 7;
    private static final int STALE = 8;
    private int nonceCount; 
    private String specifiedCipher;  
    private byte[] cnonce;        
    private String username;
    private char[] passwd;
    private byte[] authzidBytes;  
    DigestMD5Client(String authzid, String protocol, String serverName,
        Map props, CallbackHandler cbh) throws SaslException {
        super(props, MY_CLASS_NAME, 2, protocol + "/" + serverName, cbh);
        if (authzid != null) {
            this.authzid = authzid;
            try {
                authzidBytes = authzid.getBytes("UTF8");
            } catch (UnsupportedEncodingException e) {
                throw new SaslException(
                    "DIGEST-MD5: Error encoding authzid value into UTF-8", e);
            }
        }
        if (props != null) {
            specifiedCipher = (String)props.get(CIPHER_PROPERTY);
            logger.log(Level.FINE, "DIGEST60:Explicitly specified cipher: {0}",
                specifiedCipher);
        }
   }
    public boolean hasInitialResponse() {
        return false;
    }
    public byte[] evaluateChallenge(byte[] challengeData) throws SaslException {
        if (challengeData.length > MAX_CHALLENGE_LENGTH) {
            throw new SaslException(
                "DIGEST-MD5: Invalid digest-challenge length. Got:  " +
                challengeData.length + " Expected < " + MAX_CHALLENGE_LENGTH);
        }
        byte[][] challengeVal;
        switch (step) {
        case 2:
            List<byte[]> realmChoices = new ArrayList<byte[]>(3);
            challengeVal = parseDirectives(challengeData, DIRECTIVE_KEY,
                realmChoices, REALM);
            try {
                processChallenge(challengeVal, realmChoices);
                checkQopSupport(challengeVal[QOP], challengeVal[CIPHER]);
                ++step;
                return generateClientResponse(challengeVal[CHARSET]);
            } catch (SaslException e) {
                step = 0;
                clearPassword();
                throw e; 
            } catch (IOException e) {
                step = 0;
                clearPassword();
                throw new SaslException("DIGEST-MD5: Error generating " +
                    "digest response-value", e);
            }
        case 3:
            try {
                challengeVal = parseDirectives(challengeData, DIRECTIVE_KEY,
                    null, REALM);
                validateResponseValue(challengeVal[RESPONSE_AUTH]);
                if (integrity && privacy) {
                    secCtx = new DigestPrivacy(true );
                } else if (integrity) {
                    secCtx = new DigestIntegrity(true );
                }
                return null; 
            } finally {
                clearPassword();
                step = 0;  
                completed = true;
            }
        default:
            throw new SaslException("DIGEST-MD5: Client at illegal state");
        }
    }
    private void processChallenge(byte[][] challengeVal, List<byte[]> realmChoices)
        throws SaslException, UnsupportedEncodingException {
        if (challengeVal[CHARSET] != null) {
            if (!"utf-8".equals(new String(challengeVal[CHARSET], encoding))) {
                throw new SaslException("DIGEST-MD5: digest-challenge format " +
                    "violation. Unrecognised charset value: " +
                    new String(challengeVal[CHARSET]));
            } else {
                encoding = "UTF8";
                useUTF8 = true;
            }
        }
        if (challengeVal[ALGORITHM] == null) {
            throw new SaslException("DIGEST-MD5: Digest-challenge format " +
                "violation: algorithm directive missing");
        } else if (!"md5-sess".equals(new String(challengeVal[ALGORITHM], encoding))) {
            throw new SaslException("DIGEST-MD5: Digest-challenge format " +
                "violation. Invalid value for 'algorithm' directive: " +
                challengeVal[ALGORITHM]);
        }
        if (challengeVal[NONCE] == null) {
            throw new SaslException("DIGEST-MD5: Digest-challenge format " +
                "violation: nonce directive missing");
        } else {
            nonce = challengeVal[NONCE];
        }
        try {
            String[] realmTokens = null;
            if (challengeVal[REALM] != null) {
                if (realmChoices == null || realmChoices.size() <= 1) {
                    negotiatedRealm = new String(challengeVal[REALM], encoding);
                } else {
                    realmTokens = new String[realmChoices.size()];
                    for (int i = 0; i < realmTokens.length; i++) {
                        realmTokens[i] =
                            new String(realmChoices.get(i), encoding);
                    }
                }
            }
            NameCallback ncb = authzid == null ?
                new NameCallback("DIGEST-MD5 authentication ID: ") :
                new NameCallback("DIGEST-MD5 authentication ID: ", authzid);
            PasswordCallback pcb =
                new PasswordCallback("DIGEST-MD5 password: ", false);
            if (realmTokens == null) {
                RealmCallback tcb =
                    (negotiatedRealm == null? new RealmCallback("DIGEST-MD5 realm: ") :
                        new RealmCallback("DIGEST-MD5 realm: ", negotiatedRealm));
                cbh.handle(new Callback[] {tcb, ncb, pcb});
                negotiatedRealm = tcb.getText();
                if (negotiatedRealm == null) {
                    negotiatedRealm = "";
                }
            } else {
                RealmChoiceCallback ccb = new RealmChoiceCallback(
                    "DIGEST-MD5 realm: ",
                    realmTokens,
                    0, false);
                cbh.handle(new Callback[] {ccb, ncb, pcb});
                negotiatedRealm = realmTokens[ccb.getSelectedIndexes()[0]];
            }
            passwd = pcb.getPassword();
            pcb.clearPassword();
            username = ncb.getName();
        } catch (UnsupportedCallbackException e) {
            throw new SaslException("DIGEST-MD5: Cannot perform callback to " +
                "acquire realm, authentication ID or password", e);
        } catch (IOException e) {
            throw new SaslException(
                "DIGEST-MD5: Error acquiring realm, authentication ID or password", e);
        }
        if (username == null || passwd == null) {
            throw new SaslException(
                "DIGEST-MD5: authentication ID and password must be specified");
        }
        int srvMaxBufSize =
            (challengeVal[MAXBUF] == null) ? DEFAULT_MAXBUF
            : Integer.parseInt(new String(challengeVal[MAXBUF], encoding));
        sendMaxBufSize =
            (sendMaxBufSize == 0) ? srvMaxBufSize
            : Math.min(sendMaxBufSize, srvMaxBufSize);
    }
    private void checkQopSupport(byte[] qopInChallenge, byte[] ciphersInChallenge)
        throws IOException {
        String qopOptions;
        if (qopInChallenge == null) {
            qopOptions = "auth";
        } else {
            qopOptions = new String(qopInChallenge, encoding);
        }
        String[] serverQopTokens = new String[3];
        byte[] serverQop = parseQop(qopOptions, serverQopTokens,
            true );
        byte serverAllQop = combineMasks(serverQop);
        switch (findPreferredMask(serverAllQop, qop)) {
        case 0:
            throw new SaslException("DIGEST-MD5: No common protection " +
                "layer between client and server");
        case NO_PROTECTION:
            negotiatedQop = "auth";
            break;
        case INTEGRITY_ONLY_PROTECTION:
            negotiatedQop = "auth-int";
            integrity = true;
            rawSendSize = sendMaxBufSize - 16;
            break;
        case PRIVACY_PROTECTION:
            negotiatedQop = "auth-conf";
            privacy = integrity = true;
            rawSendSize = sendMaxBufSize - 26;
            checkStrengthSupport(ciphersInChallenge);
            break;
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DIGEST61:Raw send size: {0}",
                new Integer(rawSendSize));
        }
     }
    private void checkStrengthSupport(byte[] ciphersInChallenge)
        throws IOException {
        if (ciphersInChallenge == null) {
            throw new SaslException("DIGEST-MD5: server did not specify " +
                "cipher to use for 'auth-conf'");
        }
        String cipherOptions = new String(ciphersInChallenge, encoding);
        StringTokenizer parser = new StringTokenizer(cipherOptions, ", \t\n");
        int tokenCount = parser.countTokens();
        String token = null;
        byte[] serverCiphers = { UNSET,
                                 UNSET,
                                 UNSET,
                                 UNSET,
                                 UNSET };
        String[] serverCipherStrs = new String[serverCiphers.length];
        for (int i = 0; i < tokenCount; i++) {
            token = parser.nextToken();
            for (int j = 0; j < CIPHER_TOKENS.length; j++) {
                if (token.equals(CIPHER_TOKENS[j])) {
                    serverCiphers[j] |= CIPHER_MASKS[j];
                    serverCipherStrs[j] = token; 
                    logger.log(Level.FINE, "DIGEST62:Server supports {0}", token);
                }
            }
        }
        byte[] clntCiphers = getPlatformCiphers();
        byte inter = 0;
        for (int i = 0; i < serverCiphers.length; i++) {
            serverCiphers[i] &= clntCiphers[i];
            inter |= serverCiphers[i];
        }
        if (inter == UNSET) {
            throw new SaslException(
                "DIGEST-MD5: Client supports none of these cipher suites: " +
                cipherOptions);
        }
        negotiatedCipher = findCipherAndStrength(serverCiphers, serverCipherStrs);
        if (negotiatedCipher == null) {
            throw new SaslException("DIGEST-MD5: Unable to negotiate " +
                "a strength level for 'auth-conf'");
        }
        logger.log(Level.FINE, "DIGEST63:Cipher suite: {0}", negotiatedCipher);
    }
    private String findCipherAndStrength(byte[] supportedCiphers,
        String[] tokens) {
        byte s;
        for (int i = 0; i < strength.length; i++) {
            if ((s=strength[i]) != 0) {
                for (int j = 0; j < supportedCiphers.length; j++) {
                    if (s == supportedCiphers[j] &&
                        (specifiedCipher == null ||
                            specifiedCipher.equals(tokens[j]))) {
                        switch (s) {
                        case HIGH_STRENGTH:
                            negotiatedStrength = "high";
                            break;
                        case MEDIUM_STRENGTH:
                            negotiatedStrength = "medium";
                            break;
                        case LOW_STRENGTH:
                            negotiatedStrength = "low";
                            break;
                        }
                        return tokens[j];
                    }
                }
            }
        }
        return null;  
    }
    private byte[] generateClientResponse(byte[] charset) throws IOException {
        ByteArrayOutputStream digestResp = new ByteArrayOutputStream();
        if (useUTF8) {
            digestResp.write("charset=".getBytes(encoding));
            digestResp.write(charset);
            digestResp.write(',');
        }
        digestResp.write(("username=\"" +
            quotedStringValue(username) + "\",").getBytes(encoding));
        if (negotiatedRealm.length() > 0) {
            digestResp.write(("realm=\"" +
                quotedStringValue(negotiatedRealm) + "\",").getBytes(encoding));
        }
        digestResp.write("nonce=\"".getBytes(encoding));
        writeQuotedStringValue(digestResp, nonce);
        digestResp.write('"');
        digestResp.write(',');
        nonceCount = getNonceCount(nonce);
        digestResp.write(("nc=" +
            nonceCountToHex(nonceCount) + ",").getBytes(encoding));
        cnonce = generateNonce();
        digestResp.write("cnonce=\"".getBytes(encoding));
        writeQuotedStringValue(digestResp, cnonce);
        digestResp.write("\",".getBytes(encoding));
        digestResp.write(("digest-uri=\"" + digestUri + "\",").getBytes(encoding));
        digestResp.write("maxbuf=".getBytes(encoding));
        digestResp.write(String.valueOf(recvMaxBufSize).getBytes(encoding));
        digestResp.write(',');
        try {
            digestResp.write("response=".getBytes(encoding));
            digestResp.write(generateResponseValue("AUTHENTICATE",
                digestUri, negotiatedQop, username,
                negotiatedRealm, passwd, nonce, cnonce,
                nonceCount, authzidBytes));
            digestResp.write(',');
        } catch (Exception e) {
            throw new SaslException(
                "DIGEST-MD5: Error generating response value", e);
        }
        digestResp.write(("qop=" + negotiatedQop).getBytes(encoding));
        if (negotiatedCipher != null) {
            digestResp.write((",cipher=\"" + negotiatedCipher + "\"").getBytes(encoding));
        }
        if (authzidBytes != null) {
            digestResp.write(",authzid=\"".getBytes(encoding));
            writeQuotedStringValue(digestResp, authzidBytes);
            digestResp.write("\"".getBytes(encoding));
        }
        if (digestResp.size() > MAX_RESPONSE_LENGTH) {
            throw new SaslException ("DIGEST-MD5: digest-response size too " +
                "large. Length: "  + digestResp.size());
        }
        return digestResp.toByteArray();
     }
    private void validateResponseValue(byte[] fromServer) throws SaslException {
        if (fromServer == null) {
            throw new SaslException("DIGEST-MD5: Authenication failed. " +
                "Expecting 'rspauth' authentication success message");
        }
        try {
            byte[] expected = generateResponseValue("",
                digestUri, negotiatedQop, username, negotiatedRealm,
                passwd, nonce, cnonce,  nonceCount, authzidBytes);
            if (!Arrays.equals(expected, fromServer)) {
                throw new SaslException(
                    "Server's rspauth value does not match what client expects");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new SaslException(
                "Problem generating response value for verification", e);
        } catch (IOException e) {
            throw new SaslException(
                "Problem generating response value for verification", e);
        }
    }
    private static int getNonceCount(byte[] nonceValue) {
        return 1;
    }
    private void clearPassword() {
        if (passwd != null) {
            for (int i = 0; i < passwd.length; i++) {
                passwd[i] = 0;
            }
            passwd = null;
        }
    }
}
