final class GssKrb5Server extends GssKrb5Base implements SaslServer {
    private static final String MY_CLASS_NAME = GssKrb5Server.class.getName();
    private int handshakeStage = 0;
    private String peer;
    private String authzid;
    private CallbackHandler cbh;
    GssKrb5Server(String protocol, String serverName,
        Map props, CallbackHandler cbh) throws SaslException {
        super(props, MY_CLASS_NAME);
        this.cbh = cbh;
        String service = protocol + "@" + serverName;
        logger.log(Level.FINE, "KRB5SRV01:Using service name: {0}", service);
        try {
            GSSManager mgr = GSSManager.getInstance();
            GSSName serviceName = mgr.createName(service,
                GSSName.NT_HOSTBASED_SERVICE, KRB5_OID);
            GSSCredential cred = mgr.createCredential(serviceName,
                GSSCredential.INDEFINITE_LIFETIME,
                KRB5_OID, GSSCredential.ACCEPT_ONLY);
            secCtx = mgr.createContext(cred);
            if ((allQop&INTEGRITY_ONLY_PROTECTION) != 0) {
                secCtx.requestInteg(true);
            }
            if ((allQop&PRIVACY_PROTECTION) != 0) {
                secCtx.requestConf(true);
            }
        } catch (GSSException e) {
            throw new SaslException("Failure to initialize security context", e);
        }
        logger.log(Level.FINE, "KRB5SRV02:Initialization complete");
    }
    public byte[] evaluateResponse(byte[] responseData) throws SaslException {
        if (completed) {
            throw new SaslException(
                "SASL authentication already complete");
        }
        if (logger.isLoggable(Level.FINER)) {
            traceOutput(MY_CLASS_NAME, "evaluateResponse",
                "KRB5SRV03:Response [raw]:", responseData);
        }
        switch (handshakeStage) {
        case 1:
            return doHandshake1(responseData);
        case 2:
            return doHandshake2(responseData);
        default:
            try {
                byte[] gssOutToken = secCtx.acceptSecContext(responseData,
                    0, responseData.length);
                if (logger.isLoggable(Level.FINER)) {
                    traceOutput(MY_CLASS_NAME, "evaluateResponse",
                        "KRB5SRV04:Challenge: [after acceptSecCtx]", gssOutToken);
                }
                if (secCtx.isEstablished()) {
                    handshakeStage = 1;
                    peer = secCtx.getSrcName().toString();
                    logger.log(Level.FINE, "KRB5SRV05:Peer name is : {0}", peer);
                    if (gssOutToken == null) {
                        return doHandshake1(EMPTY);
                    }
                }
                return gssOutToken;
            } catch (GSSException e) {
                throw new SaslException("GSS initiate failed", e);
            }
        }
    }
    private byte[] doHandshake1(byte[] responseData) throws SaslException {
        try {
            if (responseData != null && responseData.length > 0) {
                throw new SaslException(
                    "Handshake expecting no response data from server");
            }
            byte[] gssInToken = new byte[4];
            gssInToken[0] = allQop;
            intToNetworkByteOrder(recvMaxBufSize, gssInToken, 1, 3);
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
                    "KRB5SRV06:Supported protections: {0}; recv max buf size: {1}",
                    new Object[]{new Byte(allQop),
                                 new Integer(recvMaxBufSize)});
            }
            handshakeStage = 2;  
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(MY_CLASS_NAME, "doHandshake1",
                    "KRB5SRV07:Challenge [raw]", gssInToken);
            }
            byte[] gssOutToken = secCtx.wrap(gssInToken, 0, gssInToken.length,
                new MessageProp(0 , false ));
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(MY_CLASS_NAME, "doHandshake1",
                    "KRB5SRV08:Challenge [after wrap]", gssOutToken);
            }
            return gssOutToken;
        } catch (GSSException e) {
            throw new SaslException("Problem wrapping handshake1", e);
        }
    }
    private byte[] doHandshake2(byte[] responseData) throws SaslException {
        try {
            byte[] gssOutToken = secCtx.unwrap(responseData, 0,
                responseData.length, new MessageProp(0, false));
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(MY_CLASS_NAME, "doHandshake2",
                    "KRB5SRV09:Response [after unwrap]", gssOutToken);
            }
            byte selectedQop = gssOutToken[0];
            if ((selectedQop&allQop) == 0) {
                throw new SaslException("Client selected unsupported protection: "
                    + selectedQop);
            }
            if ((selectedQop&PRIVACY_PROTECTION) != 0) {
                privacy = true;
                integrity = true;
            } else if ((selectedQop&INTEGRITY_ONLY_PROTECTION) != 0) {
                integrity = true;
            }
            msgProp = new MessageProp(JGSS_QOP, privacy);
            int clntMaxBufSize = networkByteOrderToInt(gssOutToken, 1, 3);
            sendMaxBufSize = (sendMaxBufSize == 0) ? clntMaxBufSize :
                Math.min(sendMaxBufSize, clntMaxBufSize);
            rawSendSize = secCtx.getWrapSizeLimit(JGSS_QOP, privacy,
                sendMaxBufSize);
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE,
            "KRB5SRV10:Selected protection: {0}; privacy: {1}; integrity: {2}",
                    new Object[]{new Byte(selectedQop),
                                 Boolean.valueOf(privacy),
                                 Boolean.valueOf(integrity)});
                logger.log(Level.FINE,
"KRB5SRV11:Client max recv size: {0}; server max send size: {1}; rawSendSize: {2}",
                    new Object[] {new Integer(clntMaxBufSize),
                                  new Integer(sendMaxBufSize),
                                  new Integer(rawSendSize)});
            }
            if (gssOutToken.length > 4) {
                try {
                    authzid = new String(gssOutToken, 4,
                        gssOutToken.length - 4, "UTF-8");
                } catch (UnsupportedEncodingException uee) {
                    throw new SaslException ("Cannot decode authzid", uee);
                }
            } else {
                authzid = peer;
            }
            logger.log(Level.FINE, "KRB5SRV12:Authzid: {0}", authzid);
            AuthorizeCallback acb = new AuthorizeCallback(peer, authzid);
            cbh.handle(new Callback[] {acb});
            if (acb.isAuthorized()) {
                authzid = acb.getAuthorizedID();
                completed = true;
            } else {
                throw new SaslException(peer +
                    " is not authorized to connect as " + authzid);
            }
            return null;
        } catch (GSSException e) {
            throw new SaslException("Final handshake step failed", e);
        } catch (IOException e) {
            throw new SaslException("Problem with callback handler", e);
        } catch (UnsupportedCallbackException e) {
            throw new SaslException("Problem with callback handler", e);
        }
    }
    public String getAuthorizationID() {
        if (completed) {
            return authzid;
        } else {
            throw new IllegalStateException("Authentication incomplete");
        }
    }
}
