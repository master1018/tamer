public final class LdapClient implements PooledConnection {
    private static final int debug = 0;
    static final boolean caseIgnore = true;
    private static final Hashtable defaultBinaryAttrs = new Hashtable(23,0.75f);
    static {
        defaultBinaryAttrs.put("userpassword", Boolean.TRUE);      
        defaultBinaryAttrs.put("javaserializeddata", Boolean.TRUE);
        defaultBinaryAttrs.put("javaserializedobject", Boolean.TRUE);
        defaultBinaryAttrs.put("jpegphoto", Boolean.TRUE);
        defaultBinaryAttrs.put("audio", Boolean.TRUE);  
        defaultBinaryAttrs.put("thumbnailphoto", Boolean.TRUE);
        defaultBinaryAttrs.put("thumbnaillogo", Boolean.TRUE);
        defaultBinaryAttrs.put("usercertificate", Boolean.TRUE);     
        defaultBinaryAttrs.put("cacertificate", Boolean.TRUE);       
        defaultBinaryAttrs.put("certificaterevocationlist", Boolean.TRUE);
        defaultBinaryAttrs.put("authorityrevocationlist", Boolean.TRUE); 
        defaultBinaryAttrs.put("crosscertificatepair", Boolean.TRUE);    
        defaultBinaryAttrs.put("photo", Boolean.TRUE);   
        defaultBinaryAttrs.put("personalsignature", Boolean.TRUE);
        defaultBinaryAttrs.put("x500uniqueidentifier", Boolean.TRUE); 
    }
    private static final String DISCONNECT_OID = "1.3.6.1.4.1.1466.20036";
    boolean isLdapv3;         
    int referenceCount = 1;   
    Connection conn;  
    final private PoolCallback pcb;
    final private boolean pooled;
    private boolean authenticateCalled = false;
    LdapClient(String host, int port, String socketFactory,
        int connectTimeout, int readTimeout, OutputStream trace, PoolCallback pcb)
        throws NamingException {
        if (debug > 0)
            System.err.println("LdapClient: constructor called " + host + ":" + port );
        conn = new Connection(this, host, port, socketFactory, connectTimeout, readTimeout,
            trace);
        this.pcb = pcb;
        pooled = (pcb != null);
    }
    synchronized boolean authenticateCalled() {
        return authenticateCalled;
    }
    synchronized LdapResult
    authenticate(boolean initial, String name, Object pw, int version,
        String authMechanism, Control[] ctls,  Hashtable env)
        throws NamingException {
        authenticateCalled = true;
        try {
            ensureOpen();
        } catch (IOException e) {
            NamingException ne = new CommunicationException();
            ne.setRootCause(e);
            throw ne;
        }
        switch (version) {
        case LDAP_VERSION3_VERSION2:
        case LDAP_VERSION3:
            isLdapv3 = true;
            break;
        case LDAP_VERSION2:
            isLdapv3 = false;
            break;
        default:
            throw new CommunicationException("Protocol version " + version +
                " not supported");
        }
        LdapResult res = null;
        if (authMechanism.equalsIgnoreCase("none") ||
            authMechanism.equalsIgnoreCase("anonymous")) {
            if (!initial ||
                (version == LDAP_VERSION2) ||
                (version == LDAP_VERSION3_VERSION2) ||
                ((ctls != null) && (ctls.length > 0))) {
                try {
                    res = ldapBind(name=null, (byte[])(pw=null), ctls, null,
                        false);
                    if (res.status == LdapClient.LDAP_SUCCESS) {
                        conn.setBound();
                    }
                } catch (IOException e) {
                    NamingException ne =
                        new CommunicationException("anonymous bind failed: " +
                        conn.host + ":" + conn.port);
                    ne.setRootCause(e);
                    throw ne;
                }
            } else {
                res = new LdapResult();
                res.status = LdapClient.LDAP_SUCCESS;
            }
        } else if (authMechanism.equalsIgnoreCase("simple")) {
            byte[] encodedPw = null;
            try {
                encodedPw = encodePassword(pw, isLdapv3);
                res = ldapBind(name, encodedPw, ctls, null, false);
                if (res.status == LdapClient.LDAP_SUCCESS) {
                    conn.setBound();
                }
            } catch (IOException e) {
                NamingException ne =
                    new CommunicationException("simple bind failed: " +
                        conn.host + ":" + conn.port);
                ne.setRootCause(e);
                throw ne;
            } finally {
                if (encodedPw != pw && encodedPw != null) {
                    for (int i = 0; i < encodedPw.length; i++) {
                        encodedPw[i] = 0;
                    }
                }
            }
        } else if (isLdapv3) {
            try {
                res = LdapSasl.saslBind(this, conn, conn.host, name, pw,
                    authMechanism, env, ctls);
                if (res.status == LdapClient.LDAP_SUCCESS) {
                    conn.setBound();
                }
            } catch (IOException e) {
                NamingException ne =
                    new CommunicationException("SASL bind failed: " +
                    conn.host + ":" + conn.port);
                ne.setRootCause(e);
                throw ne;
            }
        } else {
            throw new AuthenticationNotSupportedException(authMechanism);
        }
        if (initial &&
            (res.status == LdapClient.LDAP_PROTOCOL_ERROR) &&
            (version == LdapClient.LDAP_VERSION3_VERSION2) &&
            (authMechanism.equalsIgnoreCase("none") ||
                authMechanism.equalsIgnoreCase("anonymous") ||
                authMechanism.equalsIgnoreCase("simple"))) {
            byte[] encodedPw = null;
            try {
                isLdapv3 = false;
                encodedPw = encodePassword(pw, false);
                res = ldapBind(name, encodedPw, ctls, null, false);
                if (res.status == LdapClient.LDAP_SUCCESS) {
                    conn.setBound();
                }
            } catch (IOException e) {
                NamingException ne =
                    new CommunicationException(authMechanism + ":" +
                        conn.host +     ":" + conn.port);
                ne.setRootCause(e);
                throw ne;
            } finally {
                if (encodedPw != pw && encodedPw != null) {
                    for (int i = 0; i < encodedPw.length; i++) {
                        encodedPw[i] = 0;
                    }
                }
            }
        }
        if (res.status == LdapClient.LDAP_NO_SUCH_OBJECT) {
            throw new AuthenticationException(
                getErrorMessage(res.status, res.errorMessage));
        }
        conn.setV3(isLdapv3);
        return res;
    }
    synchronized public LdapResult ldapBind(String dn, byte[]toServer,
        Control[] bindCtls, String auth, boolean pauseAfterReceipt)
        throws java.io.IOException, NamingException {
        ensureOpen();
        conn.abandonOutstandingReqs(null);
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
        LdapResult res = new LdapResult();
        res.status = LDAP_OPERATIONS_ERROR;
        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
            ber.encodeInt(curMsgId);
            ber.beginSeq(LdapClient.LDAP_REQ_BIND);
                ber.encodeInt(isLdapv3 ? LDAP_VERSION3 : LDAP_VERSION2);
                ber.encodeString(dn, isLdapv3);
                if (auth != null) {
                    ber.beginSeq(Ber.ASN_CONTEXT | Ber.ASN_CONSTRUCTOR | 3);
                        ber.encodeString(auth, isLdapv3);    
                        if (toServer != null) {
                            ber.encodeOctetString(toServer,
                                Ber.ASN_OCTET_STR);
                        }
                    ber.endSeq();
                } else {
                    if (toServer != null) {
                        ber.encodeOctetString(toServer, Ber.ASN_CONTEXT);
                    } else {
                        ber.encodeOctetString(null, Ber.ASN_CONTEXT, 0, 0);
                    }
                }
            ber.endSeq();
            if (isLdapv3) {
                encodeControls(ber, bindCtls);
            }
        ber.endSeq();
        LdapRequest req = conn.writeRequest(ber, curMsgId, pauseAfterReceipt);
        if (toServer != null) {
            ber.reset();        
        }
        BerDecoder rber = conn.readReply(req);
        rber.parseSeq(null);    
        rber.parseInt();        
        if (rber.parseByte() !=  LDAP_REP_BIND) {
            return res;
        }
        rber.parseLength();
        parseResult(rber, res, isLdapv3);
        if (isLdapv3 &&
            (rber.bytesLeft() > 0) &&
            (rber.peekByte() == (Ber.ASN_CONTEXT | 7))) {
            res.serverCreds = rber.parseOctetString((Ber.ASN_CONTEXT | 7), null);
        }
        res.resControls = isLdapv3 ? parseControls(rber) : null;
        conn.removeRequest(req);
        return res;
    }
    boolean usingSaslStreams() {
        return (conn.inStream instanceof SaslInputStream);
    }
    synchronized void incRefCount() {
        ++referenceCount;
        if (debug > 1) {
            System.err.println("LdapClient.incRefCount: " + referenceCount + " " + this);
        }
    }
    private static byte[] encodePassword(Object pw, boolean v3) throws IOException {
        if (pw instanceof char[]) {
            pw = new String((char[])pw);
        }
        if (pw instanceof String) {
            if (v3) {
                return ((String)pw).getBytes("UTF8");
            } else {
                return ((String)pw).getBytes("8859_1");
            }
        } else {
            return (byte[])pw;
        }
    }
    synchronized void close(Control[] reqCtls, boolean hardClose) {
        --referenceCount;
        if (debug > 1) {
            System.err.println("LdapClient: " + this);
            System.err.println("LdapClient: close() called: " + referenceCount);
            (new Throwable()).printStackTrace();
        }
        if (referenceCount <= 0 && conn != null) {
            if (debug > 0) System.err.println("LdapClient: closed connection " + this);
            if (!pooled) {
                conn.cleanup(reqCtls, false);
                conn = null;
            } else {
                if (hardClose) {
                    conn.cleanup(reqCtls, false);
                    conn = null;
                    pcb.removePooledConnection(this);
                } else {
                    pcb.releasePooledConnection(this);
                }
            }
        }
    }
    private void forceClose(boolean cleanPool) {
        referenceCount = 0; 
        if (debug > 1) {
            System.err.println("LdapClient: forceClose() of " + this);
        }
        if (conn != null) {
            if (debug > 0) System.err.println(
                "LdapClient: forced close of connection " + this);
            conn.cleanup(null, false);
            conn = null;
            if (cleanPool) {
                pcb.removePooledConnection(this);
            }
        }
    }
    protected void finalize() {
        if (debug > 0) System.err.println("LdapClient: finalize " + this);
        forceClose(pooled);
    }
    synchronized public void closeConnection() {
        forceClose(false); 
    }
    void processConnectionClosure() {
        if (unsolicited.size() > 0) {
            String msg;
            if (conn != null) {
                msg = conn.host + ":" + conn.port + " connection closed";
            } else {
                msg = "Connection closed";
            }
            notifyUnsolicited(new CommunicationException(msg));
        }
        if (pooled) {
            pcb.removePooledConnection(this);
        }
    }
    static final int SCOPE_BASE_OBJECT = 0;
    static final int SCOPE_ONE_LEVEL = 1;
    static final int SCOPE_SUBTREE = 2;
    LdapResult search(String dn, int scope, int deref, int sizeLimit,
                      int timeLimit, boolean attrsOnly, String attrs[],
                      String filter, int batchSize, Control[] reqCtls,
                      Hashtable binaryAttrs, boolean waitFirstReply,
                      int replyQueueCapacity)
        throws IOException, NamingException {
        ensureOpen();
        LdapResult res = new LdapResult();
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                ber.encodeInt(curMsgId);
                ber.beginSeq(LDAP_REQ_SEARCH);
                    ber.encodeString(dn == null ? "" : dn, isLdapv3);
                    ber.encodeInt(scope, LBER_ENUMERATED);
                    ber.encodeInt(deref, LBER_ENUMERATED);
                    ber.encodeInt(sizeLimit);
                    ber.encodeInt(timeLimit);
                    ber.encodeBoolean(attrsOnly);
                    Filter.encodeFilterString(ber, filter, isLdapv3);
                    ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                        ber.encodeStringArray(attrs, isLdapv3);
                    ber.endSeq();
                ber.endSeq();
                if (isLdapv3) encodeControls(ber, reqCtls);
            ber.endSeq();
         LdapRequest req =
                conn.writeRequest(ber, curMsgId, false, replyQueueCapacity);
         res.msgId = curMsgId;
         res.status = LdapClient.LDAP_SUCCESS; 
         if (waitFirstReply) {
             res = getSearchReply(req, batchSize, res, binaryAttrs);
         }
         return res;
    }
    void clearSearchReply(LdapResult res, Control[] ctls) {
        if (res != null && conn != null) {
            LdapRequest req = conn.findRequest(res.msgId);
            if (req == null) {
                return;
            }
            if (req.hasSearchCompleted()) {
                conn.removeRequest(req);
            } else {
                conn.abandonRequest(req, ctls);
            }
        }
    }
    LdapResult getSearchReply(int batchSize, LdapResult res,
        Hashtable binaryAttrs) throws IOException, NamingException {
        ensureOpen();
        LdapRequest req;
        if ((req = conn.findRequest(res.msgId)) == null) {
            return null;
        }
        return getSearchReply(req, batchSize, res, binaryAttrs);
    }
    private LdapResult getSearchReply(LdapRequest req,
        int batchSize, LdapResult res, Hashtable binaryAttrs)
        throws IOException, NamingException {
        if (batchSize == 0)
            batchSize = Integer.MAX_VALUE;
        if (res.entries != null) {
            res.entries.setSize(0); 
        } else {
            res.entries =
                new Vector(batchSize == Integer.MAX_VALUE ? 32 : batchSize);
        }
        if (res.referrals != null) {
            res.referrals.setSize(0); 
        }
        BerDecoder replyBer;    
        int seq;                
        Attributes lattrs;      
        Attribute la;           
        String DN;              
        LdapEntry le;           
        int[] seqlen;           
        int endseq;             
        for (int i = 0; i < batchSize;) {
            replyBer = conn.readReply(req);
            replyBer.parseSeq(null);                    
            replyBer.parseInt();                        
            seq = replyBer.parseSeq(null);
            if (seq == LDAP_REP_SEARCH) {
                lattrs = new BasicAttributes(caseIgnore);
                DN = replyBer.parseString(isLdapv3);
                le = new LdapEntry(DN, lattrs);
                seqlen = new int[1];
                replyBer.parseSeq(seqlen);
                endseq = replyBer.getParsePosition() + seqlen[0];
                while ((replyBer.getParsePosition() < endseq) &&
                    (replyBer.bytesLeft() > 0)) {
                    la = parseAttribute(replyBer, binaryAttrs);
                    lattrs.put(la);
                }
                le.respCtls = isLdapv3 ? parseControls(replyBer) : null;
                res.entries.addElement(le);
                i++;
            } else if ((seq == LDAP_REP_SEARCH_REF) && isLdapv3) {
                Vector URLs = new Vector(4);
                if (replyBer.peekByte() ==
                    (Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR)) {
                    replyBer.parseSeq(null);
                }
                while ((replyBer.bytesLeft() > 0) &&
                    (replyBer.peekByte() == Ber.ASN_OCTET_STR)) {
                    URLs.addElement(replyBer.parseString(isLdapv3));
                }
                if (res.referrals == null) {
                    res.referrals = new Vector(4);
                }
                res.referrals.addElement(URLs);
                res.resControls = isLdapv3 ? parseControls(replyBer) : null;
            } else if (seq == LDAP_REP_EXTENSION) {
                parseExtResponse(replyBer, res); 
            } else if (seq == LDAP_REP_RESULT) {
                parseResult(replyBer, res, isLdapv3);
                res.resControls = isLdapv3 ? parseControls(replyBer) : null;
                conn.removeRequest(req);
                return res;     
            }
        }
        return res;
    }
    private Attribute parseAttribute(BerDecoder ber, Hashtable binaryAttrs)
        throws IOException {
        int len[] = new int[1];
        int seq = ber.parseSeq(null);
        String attrid = ber.parseString(isLdapv3);
        boolean hasBinaryValues = isBinaryValued(attrid, binaryAttrs);
        Attribute la = new LdapAttribute(attrid);
        if ((seq = ber.parseSeq(len)) == LBER_SET) {
            int attrlen = len[0];
            while (ber.bytesLeft() > 0 && attrlen > 0) {
                try {
                    attrlen -= parseAttributeValue(ber, la, hasBinaryValues);
                } catch (IOException ex) {
                    ber.seek(attrlen);
                    break;
                }
            }
        } else {
            ber.seek(len[0]);
        }
        return la;
    }
    private int parseAttributeValue(BerDecoder ber, Attribute la,
        boolean hasBinaryValues) throws IOException {
        int len[] = new int[1];
        if (hasBinaryValues) {
            la.add(ber.parseOctetString(ber.peekByte(), len));
        } else {
            la.add(ber.parseStringWithTag(Ber.ASN_SIMPLE_STRING, isLdapv3, len));
        }
        return len[0];
    }
    private boolean isBinaryValued(String attrid, Hashtable binaryAttrs) {
        String id = attrid.toLowerCase();
        return ((id.indexOf(";binary") != -1) ||
            defaultBinaryAttrs.containsKey(id) ||
            ((binaryAttrs != null) && (binaryAttrs.containsKey(id))));
    }
    static void parseResult(BerDecoder replyBer, LdapResult res, boolean isLdapv3)
        throws IOException {
        res.status = replyBer.parseEnumeration();
        res.matchedDN = replyBer.parseString(isLdapv3);
        res.errorMessage = replyBer.parseString(isLdapv3);
        if (isLdapv3 &&
            (replyBer.bytesLeft() > 0) &&
            (replyBer.peekByte() == LDAP_REP_REFERRAL)) {
            Vector URLs = new Vector(4);
            int[] seqlen = new int[1];
            replyBer.parseSeq(seqlen);
            int endseq = replyBer.getParsePosition() + seqlen[0];
            while ((replyBer.getParsePosition() < endseq) &&
                (replyBer.bytesLeft() > 0)) {
                URLs.addElement(replyBer.parseString(isLdapv3));
            }
            if (res.referrals == null) {
                res.referrals = new Vector(4);
            }
            res.referrals.addElement(URLs);
        }
    }
    static Vector parseControls(BerDecoder replyBer) throws IOException {
        if ((replyBer.bytesLeft() > 0) && (replyBer.peekByte() == LDAP_CONTROLS)) {
            Vector ctls = new Vector(4);
            String controlOID;
            boolean criticality = false; 
            byte[] controlValue = null;  
            int[] seqlen = new int[1];
            replyBer.parseSeq(seqlen);
            int endseq = replyBer.getParsePosition() + seqlen[0];
            while ((replyBer.getParsePosition() < endseq) &&
                (replyBer.bytesLeft() > 0)) {
                replyBer.parseSeq(null);
                controlOID = replyBer.parseString(true);
                if ((replyBer.bytesLeft() > 0) &&
                    (replyBer.peekByte() == Ber.ASN_BOOLEAN)) {
                    criticality = replyBer.parseBoolean();
                }
                if ((replyBer.bytesLeft() > 0) &&
                    (replyBer.peekByte() == Ber.ASN_OCTET_STR)) {
                    controlValue =
                        replyBer.parseOctetString(Ber.ASN_OCTET_STR, null);
                }
                if (controlOID != null) {
                    ctls.addElement(
                        new BasicControl(controlOID, criticality, controlValue));
                }
            }
            return ctls;
        } else {
            return null;
        }
    }
    private void parseExtResponse(BerDecoder replyBer, LdapResult res)
        throws IOException {
        parseResult(replyBer, res, isLdapv3);
        if ((replyBer.bytesLeft() > 0) &&
            (replyBer.peekByte() == LDAP_REP_EXT_OID)) {
            res.extensionId =
                replyBer.parseStringWithTag(LDAP_REP_EXT_OID, isLdapv3, null);
        }
        if ((replyBer.bytesLeft() > 0) &&
            (replyBer.peekByte() == LDAP_REP_EXT_VAL)) {
            res.extensionValue =
                replyBer.parseOctetString(LDAP_REP_EXT_VAL, null);
        }
        res.resControls = parseControls(replyBer);
    }
    static void encodeControls(BerEncoder ber, Control[] reqCtls)
        throws IOException {
        if ((reqCtls == null) || (reqCtls.length == 0)) {
            return;
        }
        byte[] controlVal;
        ber.beginSeq(LdapClient.LDAP_CONTROLS);
            for (int i = 0; i < reqCtls.length; i++) {
                ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                    ber.encodeString(reqCtls[i].getID(), true); 
                    if (reqCtls[i].isCritical()) {
                        ber.encodeBoolean(true); 
                    }
                    if ((controlVal = reqCtls[i].getEncodedValue()) != null) {
                        ber.encodeOctetString(controlVal, Ber.ASN_OCTET_STR);
                    }
                ber.endSeq();
            }
        ber.endSeq();
    }
    private LdapResult processReply(LdapRequest req,
        LdapResult res, int responseType) throws IOException, NamingException {
        BerDecoder rber = conn.readReply(req);
        rber.parseSeq(null);    
        rber.parseInt();        
        if (rber.parseByte() !=  responseType) {
            return res;
        }
        rber.parseLength();
        parseResult(rber, res, isLdapv3);
        res.resControls = isLdapv3 ? parseControls(rber) : null;
        conn.removeRequest(req);
        return res;     
    }
    static final int ADD = 0;
    static final int DELETE = 1;
    static final int REPLACE = 2;
    LdapResult modify(String dn, int operations[], Attribute attrs[],
                      Control[] reqCtls)
        throws IOException, NamingException {
        ensureOpen();
        LdapResult res = new LdapResult();
        res.status = LDAP_OPERATIONS_ERROR;
        if (dn == null || operations.length != attrs.length)
            return res;
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
            ber.encodeInt(curMsgId);
            ber.beginSeq(LDAP_REQ_MODIFY);
                ber.encodeString(dn, isLdapv3);
                ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                    for (int i = 0; i < operations.length; i++) {
                        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                            ber.encodeInt(operations[i], LBER_ENUMERATED);
                            if ((operations[i] == ADD) && hasNoValue(attrs[i])) {
                                throw new InvalidAttributeValueException(
                                    "'" + attrs[i].getID() + "' has no values.");
                            } else {
                                encodeAttribute(ber, attrs[i]);
                            }
                        ber.endSeq();
                    }
                ber.endSeq();
            ber.endSeq();
            if (isLdapv3) encodeControls(ber, reqCtls);
        ber.endSeq();
        LdapRequest req = conn.writeRequest(ber, curMsgId);
        return processReply(req, res, LDAP_REP_MODIFY);
    }
    private void encodeAttribute(BerEncoder ber, Attribute attr)
        throws IOException, NamingException {
        ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
            ber.encodeString(attr.getID(), isLdapv3);
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR | 1);
                NamingEnumeration enum_ = attr.getAll();
                Object val;
                while (enum_.hasMore()) {
                    val = enum_.next();
                    if (val instanceof String) {
                        ber.encodeString((String)val, isLdapv3);
                    } else if (val instanceof byte[]) {
                        ber.encodeOctetString((byte[])val, Ber.ASN_OCTET_STR);
                    } else if (val == null) {
                    } else {
                        throw new InvalidAttributeValueException(
                            "Malformed '" + attr.getID() + "' attribute value");
                    }
                }
            ber.endSeq();
        ber.endSeq();
    }
    private static boolean hasNoValue(Attribute attr) throws NamingException {
        return attr.size() == 0 || (attr.size() == 1 && attr.get() == null);
    }
    LdapResult add(LdapEntry entry, Control[] reqCtls)
        throws IOException, NamingException {
        ensureOpen();
        LdapResult res = new LdapResult();
        res.status = LDAP_OPERATIONS_ERROR;
        if (entry == null || entry.DN == null)
            return res;
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
        Attribute attr;
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                ber.encodeInt(curMsgId);
                ber.beginSeq(LDAP_REQ_ADD);
                    ber.encodeString(entry.DN, isLdapv3);
                    ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                        NamingEnumeration enum_ = entry.attributes.getAll();
                        while (enum_.hasMore()) {
                            attr = (Attribute)enum_.next();
                            if (hasNoValue(attr)) {
                                throw new InvalidAttributeValueException(
                                    "'" + attr.getID() + "' has no values.");
                            } else {
                                encodeAttribute(ber, attr);
                            }
                        }
                    ber.endSeq();
                ber.endSeq();
                if (isLdapv3) encodeControls(ber, reqCtls);
            ber.endSeq();
        LdapRequest req = conn.writeRequest(ber, curMsgId);
        return processReply(req, res, LDAP_REP_ADD);
    }
    LdapResult delete(String DN, Control[] reqCtls)
        throws IOException, NamingException {
        ensureOpen();
        LdapResult res = new LdapResult();
        res.status = LDAP_OPERATIONS_ERROR;
        if (DN == null)
            return res;
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                ber.encodeInt(curMsgId);
                ber.encodeString(DN, LDAP_REQ_DELETE, isLdapv3);
                if (isLdapv3) encodeControls(ber, reqCtls);
            ber.endSeq();
        LdapRequest req = conn.writeRequest(ber, curMsgId);
        return processReply(req, res, LDAP_REP_DELETE);
    }
    LdapResult moddn(String DN, String newrdn, boolean deleteOldRdn,
                     String newSuperior, Control[] reqCtls)
        throws IOException, NamingException {
        ensureOpen();
        boolean changeSuperior = (newSuperior != null &&
                                  newSuperior.length() > 0);
        LdapResult res = new LdapResult();
        res.status = LDAP_OPERATIONS_ERROR;
        if (DN == null || newrdn == null)
            return res;
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                ber.encodeInt(curMsgId);
                ber.beginSeq(LDAP_REQ_MODRDN);
                    ber.encodeString(DN, isLdapv3);
                    ber.encodeString(newrdn, isLdapv3);
                    ber.encodeBoolean(deleteOldRdn);
                    if(isLdapv3 && changeSuperior) {
                        ber.encodeString(newSuperior, LDAP_SUPERIOR_DN, isLdapv3);
                    }
                ber.endSeq();
                if (isLdapv3) encodeControls(ber, reqCtls);
            ber.endSeq();
        LdapRequest req = conn.writeRequest(ber, curMsgId);
        return processReply(req, res, LDAP_REP_MODRDN);
    }
    LdapResult compare(String DN, String type, String value, Control[] reqCtls)
        throws IOException, NamingException {
        ensureOpen();
        LdapResult res = new LdapResult();
        res.status = LDAP_OPERATIONS_ERROR;
        if (DN == null || type == null || value == null)
            return res;
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                ber.encodeInt(curMsgId);
                ber.beginSeq(LDAP_REQ_COMPARE);
                    ber.encodeString(DN, isLdapv3);
                    ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                        ber.encodeString(type, isLdapv3);
                        byte[] val = isLdapv3 ?
                            value.getBytes("UTF8") : value.getBytes("8859_1");
                        ber.encodeOctetString(
                            Filter.unescapeFilterValue(val, 0, val.length),
                            Ber.ASN_OCTET_STR);
                    ber.endSeq();
                ber.endSeq();
                if (isLdapv3) encodeControls(ber, reqCtls);
            ber.endSeq();
        LdapRequest req = conn.writeRequest(ber, curMsgId);
        return processReply(req, res, LDAP_REP_COMPARE);
    }
    LdapResult extendedOp(String id, byte[] request, Control[] reqCtls,
        boolean pauseAfterReceipt) throws IOException, NamingException {
        ensureOpen();
        LdapResult res = new LdapResult();
        res.status = LDAP_OPERATIONS_ERROR;
        if (id == null)
            return res;
        BerEncoder ber = new BerEncoder();
        int curMsgId = conn.getMsgId();
            ber.beginSeq(Ber.ASN_SEQUENCE | Ber.ASN_CONSTRUCTOR);
                ber.encodeInt(curMsgId);
                ber.beginSeq(LDAP_REQ_EXTENSION);
                    ber.encodeString(id,
                        Ber.ASN_CONTEXT | 0, isLdapv3);
                    if (request != null) {
                        ber.encodeOctetString(request,
                            Ber.ASN_CONTEXT | 1);
                    }
                ber.endSeq();
                encodeControls(ber, reqCtls); 
            ber.endSeq();
        LdapRequest req = conn.writeRequest(ber, curMsgId, pauseAfterReceipt);
        BerDecoder rber = conn.readReply(req);
        rber.parseSeq(null);    
        rber.parseInt();        
        if (rber.parseByte() !=  LDAP_REP_EXTENSION) {
            return res;
        }
        rber.parseLength();
        parseExtResponse(rber, res);
        conn.removeRequest(req);
        return res;     
    }
    static final int LDAP_VERSION3_VERSION2 = 32;
    static final int LDAP_VERSION2 = 0x02;
    static final int LDAP_VERSION3 = 0x03;              
    static final int LDAP_VERSION = LDAP_VERSION3;
    static final int LDAP_REF_FOLLOW = 0x01;            
    static final int LDAP_REF_THROW = 0x02;             
    static final int LDAP_REF_IGNORE = 0x03;            
    static final String LDAP_URL = "ldap:
    static final String LDAPS_URL = "ldaps:
    static final int LBER_BOOLEAN = 0x01;
    static final int LBER_INTEGER = 0x02;
    static final int LBER_BITSTRING = 0x03;
    static final int LBER_OCTETSTRING = 0x04;
    static final int LBER_NULL = 0x05;
    static final int LBER_ENUMERATED = 0x0a;
    static final int LBER_SEQUENCE = 0x30;
    static final int LBER_SET = 0x31;
    static final int LDAP_SUPERIOR_DN = 0x80;
    static final int LDAP_REQ_BIND = 0x60;      
    static final int LDAP_REQ_UNBIND = 0x42;    
    static final int LDAP_REQ_SEARCH = 0x63;    
    static final int LDAP_REQ_MODIFY = 0x66;    
    static final int LDAP_REQ_ADD = 0x68;       
    static final int LDAP_REQ_DELETE = 0x4a;    
    static final int LDAP_REQ_MODRDN = 0x6c;    
    static final int LDAP_REQ_COMPARE = 0x6e;   
    static final int LDAP_REQ_ABANDON = 0x50;   
    static final int LDAP_REQ_EXTENSION = 0x77; 
    static final int LDAP_REP_BIND = 0x61;      
    static final int LDAP_REP_SEARCH = 0x64;    
    static final int LDAP_REP_SEARCH_REF = 0x73;
    static final int LDAP_REP_RESULT = 0x65;    
    static final int LDAP_REP_MODIFY = 0x67;    
    static final int LDAP_REP_ADD = 0x69;       
    static final int LDAP_REP_DELETE = 0x6b;    
    static final int LDAP_REP_MODRDN = 0x6d;    
    static final int LDAP_REP_COMPARE = 0x6f;   
    static final int LDAP_REP_EXTENSION = 0x78; 
    static final int LDAP_REP_REFERRAL = 0xa3;  
    static final int LDAP_REP_EXT_OID = 0x8a;   
    static final int LDAP_REP_EXT_VAL = 0x8b;   
    static final int LDAP_CONTROLS = 0xa0;      
    static final String LDAP_CONTROL_MANAGE_DSA_IT = "2.16.840.1.113730.3.4.2";
    static final String LDAP_CONTROL_PREFERRED_LANG = "1.3.6.1.4.1.1466.20035";
    static final String LDAP_CONTROL_PAGED_RESULTS = "1.2.840.113556.1.4.319";
    static final String LDAP_CONTROL_SERVER_SORT_REQ = "1.2.840.113556.1.4.473";
    static final String LDAP_CONTROL_SERVER_SORT_RES = "1.2.840.113556.1.4.474";
    static final int LDAP_SUCCESS = 0;
    static final int LDAP_OPERATIONS_ERROR = 1;
    static final int LDAP_PROTOCOL_ERROR = 2;
    static final int LDAP_TIME_LIMIT_EXCEEDED = 3;
    static final int LDAP_SIZE_LIMIT_EXCEEDED = 4;
    static final int LDAP_COMPARE_FALSE = 5;
    static final int LDAP_COMPARE_TRUE = 6;
    static final int LDAP_AUTH_METHOD_NOT_SUPPORTED = 7;
    static final int LDAP_STRONG_AUTH_REQUIRED = 8;
    static final int LDAP_PARTIAL_RESULTS = 9;                  
    static final int LDAP_REFERRAL = 10;                        
    static final int LDAP_ADMIN_LIMIT_EXCEEDED = 11;            
    static final int LDAP_UNAVAILABLE_CRITICAL_EXTENSION = 12;  
    static final int LDAP_CONFIDENTIALITY_REQUIRED = 13;        
    static final int LDAP_SASL_BIND_IN_PROGRESS = 14;           
    static final int LDAP_NO_SUCH_ATTRIBUTE = 16;
    static final int LDAP_UNDEFINED_ATTRIBUTE_TYPE = 17;
    static final int LDAP_INAPPROPRIATE_MATCHING = 18;
    static final int LDAP_CONSTRAINT_VIOLATION = 19;
    static final int LDAP_ATTRIBUTE_OR_VALUE_EXISTS = 20;
    static final int LDAP_INVALID_ATTRIBUTE_SYNTAX = 21;
    static final int LDAP_NO_SUCH_OBJECT = 32;
    static final int LDAP_ALIAS_PROBLEM = 33;
    static final int LDAP_INVALID_DN_SYNTAX = 34;
    static final int LDAP_IS_LEAF = 35;
    static final int LDAP_ALIAS_DEREFERENCING_PROBLEM = 36;
    static final int LDAP_INAPPROPRIATE_AUTHENTICATION = 48;
    static final int LDAP_INVALID_CREDENTIALS = 49;
    static final int LDAP_INSUFFICIENT_ACCESS_RIGHTS = 50;
    static final int LDAP_BUSY = 51;
    static final int LDAP_UNAVAILABLE = 52;
    static final int LDAP_UNWILLING_TO_PERFORM = 53;
    static final int LDAP_LOOP_DETECT = 54;
    static final int LDAP_NAMING_VIOLATION = 64;
    static final int LDAP_OBJECT_CLASS_VIOLATION = 65;
    static final int LDAP_NOT_ALLOWED_ON_NON_LEAF = 66;
    static final int LDAP_NOT_ALLOWED_ON_RDN = 67;
    static final int LDAP_ENTRY_ALREADY_EXISTS = 68;
    static final int LDAP_OBJECT_CLASS_MODS_PROHIBITED = 69;
    static final int LDAP_AFFECTS_MULTIPLE_DSAS = 71;           
    static final int LDAP_OTHER = 80;
    static final String[] ldap_error_message = {
        "Success",                                      
        "Operations Error",                             
        "Protocol Error",                               
        "Timelimit Exceeded",                           
        "Sizelimit Exceeded",                           
        "Compare False",                                
        "Compare True",                                 
        "Authentication Method Not Supported",          
        "Strong Authentication Required",               
        null,
        "Referral",                                     
        "Administrative Limit Exceeded",                
        "Unavailable Critical Extension",               
        "Confidentiality Required",                     
        "SASL Bind In Progress",                        
        null,
        "No Such Attribute",                            
        "Undefined Attribute Type",                     
        "Inappropriate Matching",                       
        "Constraint Violation",                         
        "Attribute Or Value Exists",                    
        "Invalid Attribute Syntax",                     
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "No Such Object",                               
        "Alias Problem",                                
        "Invalid DN Syntax",                            
        null,
        "Alias Dereferencing Problem",                  
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "Inappropriate Authentication",                 
        "Invalid Credentials",                          
        "Insufficient Access Rights",                   
        "Busy",                                         
        "Unavailable",                                  
        "Unwilling To Perform",                         
        "Loop Detect",                                  
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "Naming Violation",                             
        "Object Class Violation",                       
        "Not Allowed On Non-leaf",                      
        "Not Allowed On RDN",                           
        "Entry Already Exists",                         
        "Object Class Modifications Prohibited",        
        null,
        "Affects Multiple DSAs",                        
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        "Other",                                        
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    };
    static String getErrorMessage(int errorCode, String errorMessage) {
        String message = "[LDAP: error code " + errorCode;
        if ((errorMessage != null) && (errorMessage.length() != 0)) {
            message = message + " - " + errorMessage + "]";
        } else {
            try {
                if (ldap_error_message[errorCode] != null) {
                    message = message + " - " + ldap_error_message[errorCode] +
                                "]";
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                message = message + "]";
            }
        }
        return message;
    }
    private Vector unsolicited = new Vector(3);
    void addUnsolicited(LdapCtx ctx) {
        if (debug > 0) {
            System.err.println("LdapClient.addUnsolicited" + ctx);
        }
        unsolicited.addElement(ctx);
    }
    void removeUnsolicited(LdapCtx ctx) {
        if (debug > 0) {
            System.err.println("LdapClient.removeUnsolicited" + ctx);
        }
        synchronized (unsolicited) {
            if (unsolicited.size() == 0) {
                return;
            }
            unsolicited.removeElement(ctx);
        }
    }
    void processUnsolicited(BerDecoder ber) {
        if (debug > 0) {
            System.err.println("LdapClient.processUnsolicited");
        }
      synchronized (unsolicited) {
        try {
            LdapResult res = new LdapResult();
            ber.parseSeq(null); 
            ber.parseInt();             
            if (ber.parseByte() != LDAP_REP_EXTENSION) {
                throw new IOException(
                    "Unsolicited Notification must be an Extended Response");
            }
            ber.parseLength();
            parseExtResponse(ber, res);
            if (DISCONNECT_OID.equals(res.extensionId)) {
                forceClose(pooled);
            }
            if (unsolicited.size() > 0) {
                UnsolicitedNotification notice = new UnsolicitedResponseImpl(
                    res.extensionId,
                    res.extensionValue,
                    res.referrals,
                    res.status,
                    res.errorMessage,
                    res.matchedDN,
                    (res.resControls != null) ?
            ((LdapCtx)unsolicited.elementAt(0)).convertControls(res.resControls) :
                    null);
                notifyUnsolicited(notice);
                if (DISCONNECT_OID.equals(res.extensionId)) {
                    notifyUnsolicited(
                        new CommunicationException("Connection closed"));
                }
            }
        } catch (IOException e) {
            if (unsolicited.size() == 0)
                return;  
            NamingException ne = new CommunicationException(
                "Problem parsing unsolicited notification");
            ne.setRootCause(e);
            notifyUnsolicited(ne);
        } catch (NamingException e) {
            notifyUnsolicited(e);
        }
      }
    }
    private void notifyUnsolicited(Object e) {
        for (int i = 0; i < unsolicited.size(); i++) {
            ((LdapCtx)unsolicited.elementAt(i)).fireUnsolicited(e);
        }
        if (e instanceof NamingException) {
            unsolicited.setSize(0);  
        }
    }
    private void ensureOpen() throws IOException {
        if (conn == null || !conn.useable) {
            if (conn != null && conn.closureReason != null) {
                throw conn.closureReason;
            } else {
                throw new IOException("connection closed");
            }
        }
    }
    static LdapClient getInstance(boolean usePool, String hostname, int port,
        String factory, int connectTimeout, int readTimeout, OutputStream trace,
        int version, String authMechanism, Control[] ctls, String protocol,
        String user, Object passwd, Hashtable env) throws NamingException {
        if (usePool) {
            if (LdapPoolManager.isPoolingAllowed(factory, trace,
                    authMechanism, protocol, env)) {
                LdapClient answer = LdapPoolManager.getLdapClient(
                        hostname, port, factory, connectTimeout, readTimeout,
                        trace, version, authMechanism, ctls, protocol, user,
                        passwd, env);
                answer.referenceCount = 1;   
                return answer;
            }
        }
        return new LdapClient(hostname, port, factory, connectTimeout,
                                        readTimeout, trace, null);
    }
}
