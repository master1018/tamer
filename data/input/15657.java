public class SpNegoContext implements GSSContextSpi {
    private static final int STATE_NEW = 1;
    private static final int STATE_IN_PROCESS = 2;
    private static final int STATE_DONE = 3;
    private static final int STATE_DELETED = 4;
    private int state = STATE_NEW;
    private boolean credDelegState = false;
    private boolean mutualAuthState = true;
    private boolean replayDetState = true;
    private boolean sequenceDetState = true;
    private boolean confState = true;
    private boolean integState = true;
    private boolean delegPolicyState = false;
    private GSSNameSpi peerName = null;
    private GSSNameSpi myName = null;
    private SpNegoCredElement myCred = null;
    private GSSContext mechContext = null;
    private byte[] DER_mechTypes = null;
    private int lifetime;
    private ChannelBinding channelBinding;
    private boolean initiator;
    private Oid internal_mech = null;
    final private SpNegoMechFactory factory;
    static final boolean DEBUG =
        java.security.AccessController.doPrivileged(
            new sun.security.action.GetBooleanAction
            ("sun.security.spnego.debug")).booleanValue();
    public SpNegoContext(SpNegoMechFactory factory, GSSNameSpi peerName,
                        GSSCredentialSpi myCred,
                        int lifetime) throws GSSException {
        if (peerName == null)
            throw new IllegalArgumentException("Cannot have null peer name");
        if ((myCred != null) && !(myCred instanceof SpNegoCredElement)) {
            throw new IllegalArgumentException("Wrong cred element type");
        }
        this.peerName = peerName;
        this.myCred = (SpNegoCredElement) myCred;
        this.lifetime = lifetime;
        this.initiator = true;
        this.factory = factory;
    }
    public SpNegoContext(SpNegoMechFactory factory, GSSCredentialSpi myCred)
            throws GSSException {
        if ((myCred != null) && !(myCred instanceof SpNegoCredElement)) {
            throw new IllegalArgumentException("Wrong cred element type");
        }
        this.myCred = (SpNegoCredElement) myCred;
        this.initiator = false;
        this.factory = factory;
    }
    public SpNegoContext(SpNegoMechFactory factory, byte [] interProcessToken)
        throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE,
                               -1, "GSS Import Context not available");
    }
    public final void requestConf(boolean value) throws GSSException {
        if (state == STATE_NEW && isInitiator())
            confState  = value;
    }
    public final boolean getConfState() {
        return confState;
    }
    public final void requestInteg(boolean value) throws GSSException {
        if (state == STATE_NEW && isInitiator())
            integState  = value;
    }
    public final void requestDelegPolicy(boolean value) throws GSSException {
        if (state == STATE_NEW && isInitiator())
            delegPolicyState = value;
    }
    public final boolean getIntegState() {
        return integState;
    }
    public final boolean getDelegPolicyState() {
        if (isInitiator() && mechContext != null &&
                mechContext instanceof ExtendedGSSContext &&
                (state == STATE_IN_PROCESS || state == STATE_DONE)) {
            return ((ExtendedGSSContext)mechContext).getDelegPolicyState();
        } else {
            return delegPolicyState;
        }
    }
    public final void requestCredDeleg(boolean value) throws GSSException {
        if (state == STATE_NEW && isInitiator())
            credDelegState  = value;
    }
    public final boolean getCredDelegState() {
        if (isInitiator() && mechContext != null &&
                (state == STATE_IN_PROCESS || state == STATE_DONE)) {
            return mechContext.getCredDelegState();
        } else {
            return credDelegState;
        }
    }
    public final void requestMutualAuth(boolean value) throws GSSException {
        if (state == STATE_NEW && isInitiator()) {
            mutualAuthState  = value;
        }
    }
    public final boolean getMutualAuthState() {
        return mutualAuthState;
    }
    public final Oid getMech() {
        if (isEstablished()) {
            return getNegotiatedMech();
        }
        return (SpNegoMechFactory.GSS_SPNEGO_MECH_OID);
    }
    public final Oid getNegotiatedMech() {
        return (internal_mech);
    }
    public final Provider getProvider() {
        return SpNegoMechFactory.PROVIDER;
    }
    public final void dispose() throws GSSException {
        mechContext = null;
        state = STATE_DELETED;
    }
    public final boolean isInitiator() {
        return initiator;
    }
    public final boolean isProtReady() {
        return (state == STATE_DONE);
    }
    public final byte[] initSecContext(InputStream is, int mechTokenSize)
        throws GSSException {
        byte[] retVal = null;
        NegTokenInit initToken = null;
        byte[] mechToken = null;
        int errorCode = GSSException.FAILURE;
        if (DEBUG) {
            System.out.println("Entered SpNego.initSecContext with " +
                                "state=" + printState(state));
        }
        if (!isInitiator()) {
            throw new GSSException(GSSException.FAILURE, -1,
                "initSecContext on an acceptor GSSContext");
        }
        try {
            if (state == STATE_NEW) {
                state = STATE_IN_PROCESS;
                errorCode = GSSException.NO_CRED;
                Oid[] mechList = getAvailableMechs();
                DER_mechTypes = getEncodedMechs(mechList);
                internal_mech = mechList[0];
                mechToken = GSS_initSecContext(null);
                errorCode = GSSException.DEFECTIVE_TOKEN;
                initToken = new NegTokenInit(DER_mechTypes, getContextFlags(),
                                        mechToken, null);
                if (DEBUG) {
                    System.out.println("SpNegoContext.initSecContext: " +
                                "sending token of type = " +
                                SpNegoToken.getTokenName(initToken.getType()));
                }
                retVal = initToken.getEncoded();
            } else if (state == STATE_IN_PROCESS) {
                errorCode = GSSException.FAILURE;
                if (is == null) {
                    throw new GSSException(errorCode, -1,
                                "No token received from peer!");
                }
                errorCode = GSSException.DEFECTIVE_TOKEN;
                byte[] server_token = new byte[is.available()];
                SpNegoToken.readFully(is, server_token);
                if (DEBUG) {
                    System.out.println("SpNegoContext.initSecContext: " +
                                        "process received token = " +
                                        SpNegoToken.getHexBytes(server_token));
                }
                NegTokenTarg targToken = new NegTokenTarg(server_token);
                if (DEBUG) {
                    System.out.println("SpNegoContext.initSecContext: " +
                                "received token of type = " +
                                SpNegoToken.getTokenName(targToken.getType()));
                }
                internal_mech = targToken.getSupportedMech();
                if (internal_mech == null) {
                    throw new GSSException(errorCode, -1,
                                "supported mechansim from server is null");
                }
                SpNegoToken.NegoResult negoResult = null;
                int result = targToken.getNegotiatedResult();
                switch (result) {
                    case 0:
                        negoResult = SpNegoToken.NegoResult.ACCEPT_COMPLETE;
                        state = STATE_DONE;
                        break;
                    case 1:
                        negoResult = SpNegoToken.NegoResult.ACCEPT_INCOMPLETE;
                        state = STATE_IN_PROCESS;
                        break;
                    case 2:
                        negoResult = SpNegoToken.NegoResult.REJECT;
                        state = STATE_DELETED;
                        break;
                    default:
                        state = STATE_DONE;
                        break;
                }
                errorCode = GSSException.BAD_MECH;
                if (negoResult == SpNegoToken.NegoResult.REJECT) {
                    throw new GSSException(errorCode, -1,
                                        internal_mech.toString());
                }
                errorCode = GSSException.DEFECTIVE_TOKEN;
                if ((negoResult == SpNegoToken.NegoResult.ACCEPT_COMPLETE) ||
                    (negoResult == SpNegoToken.NegoResult.ACCEPT_INCOMPLETE)) {
                    byte[] accept_token = targToken.getResponseToken();
                    if (accept_token == null) {
                        if (!isMechContextEstablished()) {
                            throw new GSSException(errorCode, -1,
                                    "mechanism token from server is null");
                        }
                    } else {
                        mechToken = GSS_initSecContext(accept_token);
                    }
                    if (!GSSUtil.useMSInterop()) {
                        byte[] micToken = targToken.getMechListMIC();
                        if (!verifyMechListMIC(DER_mechTypes, micToken)) {
                            throw new GSSException(errorCode, -1,
                                "verification of MIC on MechList Failed!");
                        }
                    }
                    if (isMechContextEstablished()) {
                        state = STATE_DONE;
                        retVal = mechToken;
                        if (DEBUG) {
                            System.out.println("SPNEGO Negotiated Mechanism = "
                                + internal_mech + " " +
                                GSSUtil.getMechStr(internal_mech));
                        }
                    } else {
                        initToken = new NegTokenInit(null, null,
                                                mechToken, null);
                        if (DEBUG) {
                            System.out.println("SpNegoContext.initSecContext:" +
                                " continue sending token of type = " +
                                SpNegoToken.getTokenName(initToken.getType()));
                        }
                        retVal = initToken.getEncoded();
                    }
                }
            } else {
                if (DEBUG) {
                    System.out.println(state);
                }
            }
            if (DEBUG) {
                if (retVal != null) {
                    System.out.println("SNegoContext.initSecContext: " +
                        "sending token = " + SpNegoToken.getHexBytes(retVal));
                }
            }
        } catch (GSSException e) {
            GSSException gssException =
                        new GSSException(errorCode, -1, e.getMessage());
            gssException.initCause(e);
            throw gssException;
        } catch (IOException e) {
            GSSException gssException =
                new GSSException(GSSException.FAILURE, -1, e.getMessage());
            gssException.initCause(e);
            throw gssException;
        }
        return retVal;
    }
    public final byte[] acceptSecContext(InputStream is, int mechTokenSize)
        throws GSSException {
        byte[] retVal = null;
        SpNegoToken.NegoResult negoResult;
        boolean valid = true;
        if (DEBUG) {
            System.out.println("Entered SpNegoContext.acceptSecContext with " +
                               "state=" +  printState(state));
        }
        if (isInitiator()) {
            throw new GSSException(GSSException.FAILURE, -1,
                                   "acceptSecContext on an initiator " +
                                   "GSSContext");
        }
        try {
            if (state == STATE_NEW) {
                state = STATE_IN_PROCESS;
                byte[] token = new byte[is.available()];
                SpNegoToken.readFully(is, token);
                if (DEBUG) {
                    System.out.println("SpNegoContext.acceptSecContext: " +
                                        "receiving token = " +
                                        SpNegoToken.getHexBytes(token));
                }
                NegTokenInit initToken = new NegTokenInit(token);
                if (DEBUG) {
                    System.out.println("SpNegoContext.acceptSecContext: " +
                                "received token of type = " +
                                SpNegoToken.getTokenName(initToken.getType()));
                }
                Oid[] mechList = initToken.getMechTypeList();
                DER_mechTypes = initToken.getMechTypes();
                if (DER_mechTypes == null) {
                    valid = false;
                }
                byte[] mechToken = initToken.getMechToken();
                Oid[] supported_mechSet = getAvailableMechs();
                Oid mech_wanted =
                        negotiate_mech_type(supported_mechSet, mechList);
                if (mech_wanted == null) {
                    valid = false;
                }
                internal_mech = mech_wanted;
                byte[] accept_token = GSS_acceptSecContext(mechToken);
                if (!GSSUtil.useMSInterop() && valid) {
                    valid = verifyMechListMIC(DER_mechTypes,
                                                initToken.getMechListMIC());
                }
                if (valid) {
                    if (isMechContextEstablished()) {
                        negoResult = SpNegoToken.NegoResult.ACCEPT_COMPLETE;
                        state = STATE_DONE;
                        setContextFlags();
                        if (DEBUG) {
                            System.out.println("SPNEGO Negotiated Mechanism = "
                                + internal_mech + " " +
                                GSSUtil.getMechStr(internal_mech));
                        }
                    } else {
                        negoResult = SpNegoToken.NegoResult.ACCEPT_INCOMPLETE;
                        state = STATE_IN_PROCESS;
                    }
                } else {
                    negoResult = SpNegoToken.NegoResult.REJECT;
                    state = STATE_DONE;
                }
                if (DEBUG) {
                    System.out.println("SpNegoContext.acceptSecContext: " +
                                "mechanism wanted = " + mech_wanted);
                    System.out.println("SpNegoContext.acceptSecContext: " +
                                "negotiated result = " + negoResult);
                }
                NegTokenTarg targToken = new NegTokenTarg(negoResult.ordinal(),
                                mech_wanted, accept_token, null);
                if (DEBUG) {
                    System.out.println("SpNegoContext.acceptSecContext: " +
                                "sending token of type = " +
                                SpNegoToken.getTokenName(targToken.getType()));
                }
                retVal = targToken.getEncoded();
            } else if (state == STATE_IN_PROCESS) {
                byte[] client_token = new byte[is.available()];
                SpNegoToken.readFully(is, client_token);
                byte[] accept_token = GSS_acceptSecContext(client_token);
                if (accept_token == null) {
                    valid = false;
                }
                if (valid) {
                    if (isMechContextEstablished()) {
                        negoResult = SpNegoToken.NegoResult.ACCEPT_COMPLETE;
                        state = STATE_DONE;
                    } else {
                        negoResult = SpNegoToken.NegoResult.ACCEPT_INCOMPLETE;
                        state = STATE_IN_PROCESS;
                    }
                } else {
                    negoResult = SpNegoToken.NegoResult.REJECT;
                    state = STATE_DONE;
                }
                NegTokenTarg targToken = new NegTokenTarg(negoResult.ordinal(),
                                null, accept_token, null);
                if (DEBUG) {
                    System.out.println("SpNegoContext.acceptSecContext: " +
                                "sending token of type = " +
                                SpNegoToken.getTokenName(targToken.getType()));
                }
                retVal = targToken.getEncoded();
            } else {
                if (DEBUG) {
                    System.out.println("AcceptSecContext: state = " + state);
                }
            }
            if (DEBUG) {
                    System.out.println("SpNegoContext.acceptSecContext: " +
                        "sending token = " + SpNegoToken.getHexBytes(retVal));
            }
        } catch (IOException e) {
            GSSException gssException =
                new GSSException(GSSException.FAILURE, -1, e.getMessage());
            gssException.initCause(e);
            throw gssException;
        }
        if (state == STATE_DONE) {
            setContextFlags();
        }
        return retVal;
    }
    private Oid[] getAvailableMechs() {
        if (myCred != null) {
            Oid[] mechs = new Oid[1];
            mechs[0] = myCred.getInternalMech();
            return mechs;
        } else {
            return factory.availableMechs;
        }
    }
    private byte[] getEncodedMechs(Oid[] mechSet)
        throws IOException, GSSException {
        DerOutputStream mech = new DerOutputStream();
        for (int i = 0; i < mechSet.length; i++) {
            byte[] mechType = mechSet[i].getDER();
            mech.write(mechType);
        }
        DerOutputStream mechTypeList = new DerOutputStream();
        mechTypeList.write(DerValue.tag_Sequence, mech);
        byte[] encoded = mechTypeList.toByteArray();
        return encoded;
    }
    private BitArray getContextFlags() {
        BitArray out = new BitArray(7);
        if (getCredDelegState()) out.set(0, true);
        if (getMutualAuthState()) out.set(1, true);
        if (getReplayDetState()) out.set(2, true);
        if (getSequenceDetState()) out.set(3, true);
        if (getConfState()) out.set(5, true);
        if (getIntegState()) out.set(6, true);
        return out;
    }
    private void setContextFlags() {
        if (mechContext != null) {
            if (mechContext.getCredDelegState()) {
                credDelegState = true;
            }
            if (!mechContext.getMutualAuthState()) {
                mutualAuthState = false;
            }
            if (!mechContext.getReplayDetState()) {
                replayDetState = false;
            }
            if (!mechContext.getSequenceDetState()) {
                sequenceDetState = false;
            }
            if (!mechContext.getIntegState()) {
                integState = false;
            }
            if (!mechContext.getConfState()) {
                confState = false;
            }
        }
    }
    private boolean verifyMechListMIC(byte[] mechTypes, byte[] token)
        throws GSSException {
        if (token == null) {
            if (DEBUG) {
                System.out.println("SpNegoContext: no MIC token validation");
            }
            return true;
        }
        if (!mechContext.getIntegState()) {
            if (DEBUG) {
                System.out.println("SpNegoContext: no MIC token validation" +
                        " - mechanism does not support integrity");
            }
            return true;
        }
        boolean valid = false;
        try {
            MessageProp prop = new MessageProp(0, true);
            verifyMIC(token, 0, token.length, mechTypes,
                        0, mechTypes.length, prop);
            valid = true;
        } catch (GSSException e) {
            valid = false;
            if (DEBUG) {
                System.out.println("SpNegoContext: MIC validation failed! " +
                                        e.getMessage());
            }
        }
        return valid;
    }
    private byte[] GSS_initSecContext(byte[] token) throws GSSException {
        byte[] tok = null;
        if (mechContext == null) {
            GSSName serverName =
                factory.manager.createName(peerName.toString(),
                    peerName.getStringNameType(), internal_mech);
            GSSCredential cred = null;
            if (myCred != null) {
                cred = new GSSCredentialImpl(factory.manager,
                    myCred.getInternalCred());
            }
            mechContext =
                factory.manager.createContext(serverName,
                    internal_mech, cred, GSSContext.DEFAULT_LIFETIME);
            mechContext.requestConf(confState);
            mechContext.requestInteg(integState);
            mechContext.requestCredDeleg(credDelegState);
            mechContext.requestMutualAuth(mutualAuthState);
            mechContext.requestReplayDet(replayDetState);
            mechContext.requestSequenceDet(sequenceDetState);
            if (mechContext instanceof ExtendedGSSContext) {
                ((ExtendedGSSContext)mechContext).requestDelegPolicy(
                        delegPolicyState);
            }
        }
        if (token != null) {
            tok = token;
        } else {
            tok = new byte[0];
        }
        byte[] init_token = mechContext.initSecContext(tok, 0, tok.length);
        return init_token;
    }
    private byte[] GSS_acceptSecContext(byte[] token) throws GSSException {
        if (mechContext == null) {
            GSSCredential cred = null;
            if (myCred != null) {
                cred = new GSSCredentialImpl(factory.manager,
                myCred.getInternalCred());
            }
            mechContext =
                factory.manager.createContext(cred);
        }
        byte[] accept_token =
                mechContext.acceptSecContext(token, 0, token.length);
        return accept_token;
    }
    private static Oid negotiate_mech_type(Oid[] supported_mechSet,
                                        Oid[] mechSet) {
        for (int i = 0; i < supported_mechSet.length; i++) {
            for (int j = 0; j < mechSet.length; j++) {
                if (mechSet[j].equals(supported_mechSet[i])) {
                    if (DEBUG) {
                        System.out.println("SpNegoContext: " +
                                "negotiated mechanism = " + mechSet[j]);
                    }
                    return (mechSet[j]);
                }
            }
        }
        return null;
    }
    public final boolean isEstablished() {
        return (state == STATE_DONE);
    }
    public final boolean isMechContextEstablished() {
        if (mechContext != null) {
            return mechContext.isEstablished();
        } else {
            if (DEBUG) {
                System.out.println("The underlying mechansim context has " +
                                        "not been initialized");
            }
            return false;
        }
    }
    public final byte [] export() throws GSSException {
        throw new GSSException(GSSException.UNAVAILABLE, -1,
                               "GSS Export Context not available");
    }
    public final void setChannelBinding(ChannelBinding channelBinding)
        throws GSSException {
        this.channelBinding = channelBinding;
    }
    final ChannelBinding getChannelBinding() {
        return channelBinding;
    }
    public final void requestAnonymity(boolean value) throws GSSException {
    }
    public final boolean getAnonymityState() {
        return false;
    }
    public void requestLifetime(int lifetime) throws GSSException {
        if (state == STATE_NEW && isInitiator())
            this.lifetime = lifetime;
    }
    public final int getLifetime() {
        if (mechContext != null) {
            return mechContext.getLifetime();
        } else {
            return GSSContext.INDEFINITE_LIFETIME;
        }
    }
    public final boolean isTransferable() throws GSSException {
        return false;
    }
    public final void requestSequenceDet(boolean value) throws GSSException {
        if (state == STATE_NEW && isInitiator())
            sequenceDetState  = value;
    }
    public final boolean getSequenceDetState() {
        return sequenceDetState || replayDetState;
    }
    public final void requestReplayDet(boolean value) throws GSSException {
        if (state == STATE_NEW && isInitiator())
            replayDetState  = value;
    }
    public final boolean getReplayDetState() {
        return replayDetState || sequenceDetState;
    }
    public final GSSNameSpi getTargName() throws GSSException {
        if (mechContext != null) {
            GSSNameImpl targName = (GSSNameImpl)mechContext.getTargName();
            peerName = (GSSNameSpi) targName.getElement(internal_mech);
            return peerName;
        } else {
            if (DEBUG) {
                System.out.println("The underlying mechansim context has " +
                                        "not been initialized");
            }
            return null;
        }
    }
    public final GSSNameSpi getSrcName() throws GSSException {
        if (mechContext != null) {
            GSSNameImpl srcName = (GSSNameImpl)mechContext.getSrcName();
            myName = (GSSNameSpi) srcName.getElement(internal_mech);
            return myName;
        } else {
            if (DEBUG) {
                System.out.println("The underlying mechansim context has " +
                                        "not been initialized");
            }
            return null;
        }
    }
    public final GSSCredentialSpi getDelegCred() throws GSSException {
        if (state != STATE_IN_PROCESS && state != STATE_DONE)
            throw new GSSException(GSSException.NO_CONTEXT);
        if (mechContext != null) {
            GSSCredentialImpl delegCred =
                        (GSSCredentialImpl)mechContext.getDelegCred();
            boolean initiate = false;
            if (delegCred.getUsage() == GSSCredential.INITIATE_ONLY) {
                initiate = true;
            }
            GSSCredentialSpi mechCred = (GSSCredentialSpi)
                                delegCred.getElement(internal_mech, initiate);
            SpNegoCredElement cred = new SpNegoCredElement(mechCred);
            return cred.getInternalCred();
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "getDelegCred called in invalid state!");
        }
    }
    public final int getWrapSizeLimit(int qop, boolean confReq,
                                       int maxTokSize) throws GSSException {
        if (mechContext != null) {
            return mechContext.getWrapSizeLimit(qop, confReq, maxTokSize);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "getWrapSizeLimit called in invalid state!");
        }
    }
    public final byte[] wrap(byte inBuf[], int offset, int len,
                             MessageProp msgProp) throws GSSException {
        if (mechContext != null) {
            return mechContext.wrap(inBuf, offset, len, msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "Wrap called in invalid state!");
        }
    }
    public final void wrap(InputStream is, OutputStream os,
                            MessageProp msgProp) throws GSSException {
        if (mechContext != null) {
            mechContext.wrap(is, os, msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "Wrap called in invalid state!");
        }
    }
    public final byte[] unwrap(byte inBuf[], int offset, int len,
                               MessageProp msgProp)
        throws GSSException {
        if (mechContext != null) {
            return mechContext.unwrap(inBuf, offset, len, msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "UnWrap called in invalid state!");
        }
    }
    public final void unwrap(InputStream is, OutputStream os,
                             MessageProp msgProp) throws GSSException {
        if (mechContext != null) {
            mechContext.unwrap(is, os, msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "UnWrap called in invalid state!");
        }
    }
    public final byte[] getMIC(byte []inMsg, int offset, int len,
                               MessageProp msgProp)
        throws GSSException {
        if (mechContext != null) {
            return mechContext.getMIC(inMsg, offset, len, msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "getMIC called in invalid state!");
        }
    }
    public final void getMIC(InputStream is, OutputStream os,
                              MessageProp msgProp) throws GSSException {
        if (mechContext != null) {
            mechContext.getMIC(is, os, msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "getMIC called in invalid state!");
        }
    }
    public final void verifyMIC(byte []inTok, int tokOffset, int tokLen,
                                byte[] inMsg, int msgOffset, int msgLen,
                                MessageProp msgProp)
        throws GSSException {
        if (mechContext != null) {
            mechContext.verifyMIC(inTok, tokOffset, tokLen, inMsg, msgOffset,
                                msgLen,  msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "verifyMIC called in invalid state!");
        }
    }
    public final void verifyMIC(InputStream is, InputStream msgStr,
                                 MessageProp msgProp) throws GSSException {
        if (mechContext != null) {
            mechContext.verifyMIC(is, msgStr, msgProp);
        } else {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                                "verifyMIC called in invalid state!");
        }
    }
    private static String printState(int state) {
        switch (state) {
          case STATE_NEW:
                return ("STATE_NEW");
          case STATE_IN_PROCESS:
                return ("STATE_IN_PROCESS");
          case STATE_DONE:
                return ("STATE_DONE");
          case STATE_DELETED:
                return ("STATE_DELETED");
          default:
                return ("Unknown state " + state);
        }
    }
    public Object inquireSecContext(InquireType type)
            throws GSSException {
        if (mechContext == null) {
            throw new GSSException(GSSException.NO_CONTEXT, -1,
                    "Underlying mech not established.");
        }
        if (mechContext instanceof ExtendedGSSContext) {
            return ((ExtendedGSSContext)mechContext).inquireSecContext(type);
        } else {
            throw new GSSException(GSSException.BAD_MECH, -1,
                    "inquireSecContext not supported by underlying mech.");
        }
    }
}
