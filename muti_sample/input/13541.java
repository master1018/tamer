class GSSContextImpl implements ExtendedGSSContext {
    private final GSSManagerImpl gssManager;
    private final boolean initiator;
    private static final int PRE_INIT = 1;
    private static final int IN_PROGRESS = 2;
    private static final int READY = 3;
    private static final int DELETED = 4;
    private int currentState = PRE_INIT;
    private GSSContextSpi mechCtxt = null;
    private Oid mechOid = null;
    private ObjectIdentifier objId = null;
    private GSSCredentialImpl myCred = null;
    private GSSNameImpl srcName = null;
    private GSSNameImpl targName = null;
    private int reqLifetime = INDEFINITE_LIFETIME;
    private ChannelBinding channelBindings = null;
    private boolean reqConfState = true;
    private boolean reqIntegState = true;
    private boolean reqMutualAuthState = true;
    private boolean reqReplayDetState = true;
    private boolean reqSequenceDetState = true;
    private boolean reqCredDelegState = false;
    private boolean reqAnonState = false;
    private boolean reqDelegPolicyState = false;
    public GSSContextImpl(GSSManagerImpl gssManager, GSSName peer, Oid mech,
                          GSSCredential myCred, int lifetime)
        throws GSSException {
        if ((peer == null) || !(peer instanceof GSSNameImpl)) {
            throw new GSSException(GSSException.BAD_NAME);
        }
        if (mech == null) mech = ProviderList.DEFAULT_MECH_OID;
        this.gssManager = gssManager;
        this.myCred = (GSSCredentialImpl) myCred;  
        reqLifetime = lifetime;
        targName = (GSSNameImpl)peer;
        this.mechOid = mech;
        initiator = true;
    }
    public GSSContextImpl(GSSManagerImpl gssManager, GSSCredential myCred)
        throws GSSException {
        this.gssManager = gssManager;
        this.myCred = (GSSCredentialImpl) myCred; 
        initiator = false;
    }
    public GSSContextImpl(GSSManagerImpl gssManager, byte[] interProcessToken)
        throws GSSException {
        this.gssManager = gssManager;
        mechCtxt = gssManager.getMechanismContext(interProcessToken);
        initiator = mechCtxt.isInitiator();
        this.mechOid = mechCtxt.getMech();
    }
    public byte[] initSecContext(byte inputBuf[], int offset, int len)
        throws GSSException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(600);
        ByteArrayInputStream bin =
            new ByteArrayInputStream(inputBuf, offset, len);
        int size = initSecContext(bin, bos);
        return (size == 0? null : bos.toByteArray());
    }
    public int initSecContext(InputStream inStream,
                              OutputStream outStream) throws GSSException {
        if (mechCtxt != null && currentState != IN_PROGRESS) {
            throw new GSSExceptionImpl(GSSException.FAILURE,
                                   "Illegal call to initSecContext");
        }
        GSSHeader gssHeader = null;
        int inTokenLen = -1;
        GSSCredentialSpi credElement = null;
        boolean firstToken = false;
        try {
            if (mechCtxt == null) {
                if (myCred != null) {
                    try {
                        credElement = myCred.getElement(mechOid, true);
                    } catch (GSSException ge) {
                        if (GSSUtil.isSpNegoMech(mechOid) &&
                            ge.getMajor() == GSSException.NO_CRED) {
                            credElement = myCred.getElement
                                (myCred.getMechs()[0], true);
                        } else {
                            throw ge;
                        }
                    }
                }
                GSSNameSpi nameElement = targName.getElement(mechOid);
                mechCtxt = gssManager.getMechanismContext(nameElement,
                                                          credElement,
                                                          reqLifetime,
                                                          mechOid);
                mechCtxt.requestConf(reqConfState);
                mechCtxt.requestInteg(reqIntegState);
                mechCtxt.requestCredDeleg(reqCredDelegState);
                mechCtxt.requestMutualAuth(reqMutualAuthState);
                mechCtxt.requestReplayDet(reqReplayDetState);
                mechCtxt.requestSequenceDet(reqSequenceDetState);
                mechCtxt.requestAnonymity(reqAnonState);
                mechCtxt.setChannelBinding(channelBindings);
                mechCtxt.requestDelegPolicy(reqDelegPolicyState);
                objId = new ObjectIdentifier(mechOid.toString());
                currentState = IN_PROGRESS;
                firstToken = true;
            } else {
                if (mechCtxt.getProvider().getName().equals("SunNativeGSS") ||
                    GSSUtil.isSpNegoMech(mechOid)) {
                } else {
                    gssHeader = new GSSHeader(inStream);
                    if (!gssHeader.getOid().equals((Object) objId))
                        throw new GSSExceptionImpl
                            (GSSException.DEFECTIVE_TOKEN,
                             "Mechanism not equal to " +
                             mechOid.toString() +
                             " in initSecContext token");
                    inTokenLen = gssHeader.getMechTokenLength();
                }
            }
            byte[] obuf = mechCtxt.initSecContext(inStream, inTokenLen);
            int retVal = 0;
            if (obuf != null) {
                retVal = obuf.length;
                if (mechCtxt.getProvider().getName().equals("SunNativeGSS") ||
                    (!firstToken && GSSUtil.isSpNegoMech(mechOid))) {
                } else {
                    gssHeader = new GSSHeader(objId, obuf.length);
                    retVal += gssHeader.encode(outStream);
                }
                outStream.write(obuf);
            }
            if (mechCtxt.isEstablished())
                currentState = READY;
            return retVal;
        } catch (IOException e) {
            throw new GSSExceptionImpl(GSSException.DEFECTIVE_TOKEN,
                                   e.getMessage());
        }
    }
    public byte[] acceptSecContext(byte inTok[], int offset, int len)
        throws GSSException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(100);
        acceptSecContext(new ByteArrayInputStream(inTok, offset, len),
                         bos);
        byte[] out = bos.toByteArray();
        return (out.length == 0) ? null : out;
    }
    public void acceptSecContext(InputStream inStream,
                                 OutputStream outStream) throws GSSException {
        if (mechCtxt != null && currentState != IN_PROGRESS) {
            throw new GSSExceptionImpl(GSSException.FAILURE,
                                       "Illegal call to acceptSecContext");
        }
        GSSHeader gssHeader = null;
        int inTokenLen = -1;
        GSSCredentialSpi credElement = null;
        try {
            if (mechCtxt == null) {
                gssHeader = new GSSHeader(inStream);
                inTokenLen = gssHeader.getMechTokenLength();
                objId = gssHeader.getOid();
                mechOid = new Oid(objId.toString());
                if (myCred != null) {
                    credElement = myCred.getElement(mechOid, false);
                }
                mechCtxt = gssManager.getMechanismContext(credElement,
                                                          mechOid);
                mechCtxt.setChannelBinding(channelBindings);
                currentState = IN_PROGRESS;
            } else {
                if (mechCtxt.getProvider().getName().equals("SunNativeGSS") ||
                    (GSSUtil.isSpNegoMech(mechOid))) {
                } else {
                    gssHeader = new GSSHeader(inStream);
                    if (!gssHeader.getOid().equals((Object) objId))
                        throw new GSSExceptionImpl
                            (GSSException.DEFECTIVE_TOKEN,
                             "Mechanism not equal to " +
                             mechOid.toString() +
                             " in acceptSecContext token");
                    inTokenLen = gssHeader.getMechTokenLength();
                }
            }
            byte[] obuf = mechCtxt.acceptSecContext(inStream, inTokenLen);
            if (obuf != null) {
                int retVal = obuf.length;
                if (mechCtxt.getProvider().getName().equals("SunNativeGSS") ||
                    (GSSUtil.isSpNegoMech(mechOid))) {
                } else {
                    gssHeader = new GSSHeader(objId, obuf.length);
                    retVal += gssHeader.encode(outStream);
                }
                outStream.write(obuf);
            }
            if (mechCtxt.isEstablished()) {
                currentState = READY;
            }
        } catch (IOException e) {
            throw new GSSExceptionImpl(GSSException.DEFECTIVE_TOKEN,
                                   e.getMessage());
        }
    }
    public boolean isEstablished() {
        if (mechCtxt == null)
            return false;
        else
            return (currentState == READY);
    }
    public int getWrapSizeLimit(int qop, boolean confReq,
                                int maxTokenSize) throws GSSException {
        if (mechCtxt != null)
            return mechCtxt.getWrapSizeLimit(qop, confReq, maxTokenSize);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public byte[] wrap(byte inBuf[], int offset, int len,
                       MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            return mechCtxt.wrap(inBuf, offset, len, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                   "No mechanism context yet!");
    }
    public void wrap(InputStream inStream, OutputStream outStream,
                     MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            mechCtxt.wrap(inStream, outStream, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public byte [] unwrap(byte[] inBuf, int offset, int len,
                          MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            return mechCtxt.unwrap(inBuf, offset, len, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public void unwrap(InputStream inStream, OutputStream outStream,
                       MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            mechCtxt.unwrap(inStream, outStream, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public byte[] getMIC(byte []inMsg, int offset, int len,
                         MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            return mechCtxt.getMIC(inMsg, offset, len, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public void getMIC(InputStream inStream, OutputStream outStream,
                       MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            mechCtxt.getMIC(inStream, outStream, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public void verifyMIC(byte[] inTok, int tokOffset, int tokLen,
                          byte[] inMsg, int msgOffset, int msgLen,
                          MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            mechCtxt.verifyMIC(inTok, tokOffset, tokLen,
                               inMsg, msgOffset, msgLen, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public void verifyMIC(InputStream tokStream, InputStream msgStream,
                          MessageProp msgProp) throws GSSException {
        if (mechCtxt != null)
            mechCtxt.verifyMIC(tokStream, msgStream, msgProp);
        else
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                  "No mechanism context yet!");
    }
    public byte[] export() throws GSSException {
        byte[] result = null;
        if (mechCtxt.isTransferable() &&
            mechCtxt.getProvider().getName().equals("SunNativeGSS")) {
            result = mechCtxt.export();
        }
        return result;
    }
    public void requestMutualAuth(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqMutualAuthState = state;
    }
    public void requestReplayDet(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqReplayDetState = state;
    }
    public void requestSequenceDet(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqSequenceDetState = state;
    }
    public void requestCredDeleg(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqCredDelegState = state;
    }
    public void requestAnonymity(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqAnonState = state;
    }
    public void requestConf(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqConfState = state;
    }
    public void requestInteg(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqIntegState = state;
    }
    public void requestLifetime(int lifetime) throws GSSException {
        if (mechCtxt == null && initiator)
            reqLifetime = lifetime;
    }
    public void setChannelBinding(ChannelBinding channelBindings)
        throws GSSException {
        if (mechCtxt == null)
            this.channelBindings = channelBindings;
    }
    public boolean getCredDelegState() {
        if (mechCtxt != null)
            return mechCtxt.getCredDelegState();
        else
            return reqCredDelegState;
    }
    public boolean getMutualAuthState() {
        if (mechCtxt != null)
            return mechCtxt.getMutualAuthState();
        else
            return reqMutualAuthState;
    }
    public boolean getReplayDetState() {
        if (mechCtxt != null)
            return mechCtxt.getReplayDetState();
        else
            return reqReplayDetState;
    }
    public boolean getSequenceDetState() {
        if (mechCtxt != null)
            return mechCtxt.getSequenceDetState();
        else
            return reqSequenceDetState;
    }
    public boolean getAnonymityState() {
        if (mechCtxt != null)
            return mechCtxt.getAnonymityState();
        else
            return reqAnonState;
    }
    public boolean isTransferable() throws GSSException {
        if (mechCtxt != null)
            return mechCtxt.isTransferable();
        else
            return false;
    }
    public boolean isProtReady() {
        if (mechCtxt != null)
            return mechCtxt.isProtReady();
        else
            return false;
    }
    public boolean getConfState() {
        if (mechCtxt != null)
            return mechCtxt.getConfState();
        else
            return reqConfState;
    }
    public boolean getIntegState() {
        if (mechCtxt != null)
            return mechCtxt.getIntegState();
        else
            return reqIntegState;
    }
    public int getLifetime() {
        if (mechCtxt != null)
            return mechCtxt.getLifetime();
        else
            return reqLifetime;
    }
    public GSSName getSrcName() throws GSSException {
        if (srcName == null) {
            srcName = GSSNameImpl.wrapElement
                (gssManager, mechCtxt.getSrcName());
        }
        return srcName;
    }
    public GSSName getTargName() throws GSSException {
        if (targName == null) {
            targName = GSSNameImpl.wrapElement
                (gssManager, mechCtxt.getTargName());
        }
        return targName;
    }
    public Oid getMech() throws GSSException {
        if (mechCtxt != null) {
            return mechCtxt.getMech();
        }
        return mechOid;
    }
    public GSSCredential getDelegCred() throws GSSException {
        if (mechCtxt == null)
            throw new GSSExceptionImpl(GSSException.NO_CONTEXT,
                                   "No mechanism context yet!");
        GSSCredentialSpi delCredElement = mechCtxt.getDelegCred();
        return (delCredElement == null ?
            null : new GSSCredentialImpl(gssManager, delCredElement));
    }
    public boolean isInitiator() throws GSSException {
        return initiator;
    }
    public void dispose() throws GSSException {
        currentState = DELETED;
        if (mechCtxt != null) {
            mechCtxt.dispose();
            mechCtxt = null;
        }
        myCred = null;
        srcName = null;
        targName = null;
    }
    @Override
    public Object inquireSecContext(InquireType type) throws GSSException {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(new InquireSecContextPermission(type.toString()));
        }
        if (mechCtxt == null) {
            throw new GSSException(GSSException.NO_CONTEXT);
        }
        return mechCtxt.inquireSecContext(type);
    }
    @Override
    public void requestDelegPolicy(boolean state) throws GSSException {
        if (mechCtxt == null && initiator)
            reqDelegPolicyState = state;
    }
    @Override
    public boolean getDelegPolicyState() {
        if (mechCtxt != null)
            return mechCtxt.getDelegPolicyState();
        else
            return reqDelegPolicyState;
    }
}
