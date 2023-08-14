final class GssKrb5Client extends GssKrb5Base implements SaslClient {
    private static final String MY_CLASS_NAME = GssKrb5Client.class.getName();
    private boolean finalHandshake = false;
    private boolean mutual = false;       
    private byte[] authzID;
    GssKrb5Client(String authzID, String protocol, String serverName,
        Map props, CallbackHandler cbh) throws SaslException {
        super(props, MY_CLASS_NAME);
        String service = protocol + "@" + serverName;
        logger.log(Level.FINE, "KRB5CLNT01:Requesting service name: {0}",
            service);
        try {
            GSSManager mgr = GSSManager.getInstance();
            GSSName acceptorName = mgr.createName(service,
                GSSName.NT_HOSTBASED_SERVICE, KRB5_OID);
            GSSCredential credentials = null;
            if (props != null) {
                Object prop = props.get(Sasl.CREDENTIALS);
                if (prop != null && prop instanceof GSSCredential) {
                    credentials = (GSSCredential) prop;
                    logger.log(Level.FINE,
                        "KRB5CLNT01:Using the credentials supplied in " +
                        "javax.security.sasl.credentials");
                }
            }
            secCtx = mgr.createContext(acceptorName,
                KRB5_OID,   
                credentials, 
                GSSContext.INDEFINITE_LIFETIME);
            if (credentials != null) {
                secCtx.requestCredDeleg(true);
            }
            if (props != null) {
                String prop = (String)props.get(Sasl.SERVER_AUTH);
                if (prop != null) {
                    mutual = "true".equalsIgnoreCase(prop);
                }
            }
            secCtx.requestMutualAuth(mutual);
            secCtx.requestConf(true);
            secCtx.requestInteg(true);
        } catch (GSSException e) {
            throw new SaslException("Failure to initialize security context", e);
        }
        if (authzID != null && authzID.length() > 0) {
            try {
                this.authzID = authzID.getBytes("UTF8");
            } catch (IOException e) {
                throw new SaslException("Cannot encode authorization ID", e);
            }
        }
    }
    public boolean hasInitialResponse() {
        return true;
    }
    public byte[] evaluateChallenge(byte[] challengeData) throws SaslException {
        if (completed) {
            throw new IllegalStateException(
                "GSSAPI authentication already complete");
        }
        if (finalHandshake) {
            return doFinalHandshake(challengeData);
        } else {
            try {
                byte[] gssOutToken = secCtx.initSecContext(challengeData,
                    0, challengeData.length);
                if (logger.isLoggable(Level.FINER)) {
                    traceOutput(MY_CLASS_NAME, "evaluteChallenge",
                        "KRB5CLNT02:Challenge: [raw]", challengeData);
                    traceOutput(MY_CLASS_NAME, "evaluateChallenge",
                        "KRB5CLNT03:Response: [after initSecCtx]", gssOutToken);
                }
                if (secCtx.isEstablished()) {
                    finalHandshake = true;
                    if (gssOutToken == null) {
                        return EMPTY;
                    }
                }
                return gssOutToken;
            } catch (GSSException e) {
                throw new SaslException("GSS initiate failed", e);
            }
        }
    }
    private byte[] doFinalHandshake(byte[] challengeData) throws SaslException {
        try {
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(MY_CLASS_NAME, "doFinalHandshake",
                    "KRB5CLNT04:Challenge [raw]:", challengeData);
            }
            if (challengeData.length == 0) {
                return EMPTY;
            }
            byte[] gssOutToken = secCtx.unwrap(challengeData, 0,
                challengeData.length, new MessageProp(0, false));
            if (logger.isLoggable(Level.FINE)) {
                if (logger.isLoggable(Level.FINER)) {
                    traceOutput(MY_CLASS_NAME, "doFinalHandshake",
                        "KRB5CLNT05:Challenge [unwrapped]:", gssOutToken);
                }
                logger.log(Level.FINE, "KRB5CLNT06:Server protections: {0}",
                    new Byte(gssOutToken[0]));
            }
            byte selectedQop = findPreferredMask(gssOutToken[0], qop);
            if (selectedQop == 0) {
                throw new SaslException(
                    "No common protection layer between client and server");
            }
            if ((selectedQop&PRIVACY_PROTECTION) != 0) {
                privacy = true;
                integrity = true;
            } else if ((selectedQop&INTEGRITY_ONLY_PROTECTION) != 0) {
                integrity = true;
            }
            int srvMaxBufSize = networkByteOrderToInt(gssOutToken, 1, 3);
            sendMaxBufSize = (sendMaxBufSize == 0) ? srvMaxBufSize :
                Math.min(sendMaxBufSize, srvMaxBufSize);
            rawSendSize = secCtx.getWrapSizeLimit(JGSS_QOP, privacy,
                sendMaxBufSize);
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
"KRB5CLNT07:Client max recv size: {0}; server max recv size: {1}; rawSendSize: {2}",
                    new Object[] {new Integer(recvMaxBufSize),
                                  new Integer(srvMaxBufSize),
                                  new Integer(rawSendSize)});
            }
            int len = 4;
            if (authzID != null) {
                len += authzID.length;
            }
            byte[] gssInToken = new byte[len];
            gssInToken[0] = selectedQop;
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
            "KRB5CLNT08:Selected protection: {0}; privacy: {1}; integrity: {2}",
                    new Object[]{new Byte(selectedQop),
                                 Boolean.valueOf(privacy),
                                 Boolean.valueOf(integrity)});
            }
            intToNetworkByteOrder(recvMaxBufSize, gssInToken, 1, 3);
            if (authzID != null) {
                System.arraycopy(authzID, 0, gssInToken, 4, authzID.length);
                logger.log(Level.FINE, "KRB5CLNT09:Authzid: {0}", authzID);
            }
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(MY_CLASS_NAME, "doFinalHandshake",
                    "KRB5CLNT10:Response [raw]", gssInToken);
            }
            gssOutToken = secCtx.wrap(gssInToken,
                0, gssInToken.length,
                new MessageProp(0 , false ));
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(MY_CLASS_NAME, "doFinalHandshake",
                    "KRB5CLNT11:Response [after wrap]", gssOutToken);
            }
            completed = true;  
            msgProp = new MessageProp(JGSS_QOP, privacy);
            return gssOutToken;
        } catch (GSSException e) {
            throw new SaslException("Final handshake failed", e);
        }
    }
}
