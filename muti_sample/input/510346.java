public class ImpsConnection extends ImConnection {
    ImpsConnectionConfig mConfig;
    DataChannel mDataChannel;
    private CirChannel mCirChannel;
    private PrimitiveDispatcherThread mDispatcherThread;
    ImpsSession mSession;
    ImpsTransactionManager mTransactionManager;
    private ImpsChatSessionManager mChatSessionManager;
    private ImpsContactListManager mContactListManager;
    private ImpsChatGroupManager   mChatGroupManager;
    private boolean mReestablishing;
    public ImpsConnection(ImpsConnectionConfig config) {
        super();
        mConfig = config;
        mTransactionManager = new ImpsTransactionManager(this);
        mChatSessionManager = new ImpsChatSessionManager(this);
        mContactListManager = new ImpsContactListManager(this);
        mChatGroupManager   = new ImpsChatGroupManager(this);
    }
    ImpsConnectionConfig getConfig() {
        return mConfig;
    }
    synchronized void shutdownOnError(ImErrorInfo error) {
        if(mState == DISCONNECTED) {
            return;
        }
        if (mCirChannel != null) {
            mCirChannel.shutdown();
        }
        if (mDispatcherThread != null) {
            mDispatcherThread.shutdown();
        }
        if (mDataChannel != null) {
            mDataChannel.shutdown();
        }
        if (mContactListManager != null && !mReestablishing) {
            mContactListManager.reset();
        }
        setState(mReestablishing ? SUSPENDED: DISCONNECTED, error);
        mReestablishing = false;
    }
    void shutdown(){
        shutdownOnError(null);
    }
    @Override
    public int getCapability() {
        return CAPABILITY_GROUP_CHAT | CAPABILITY_SESSION_REESTABLISHMENT;
    }
    @Override
    public void loginAsync(LoginInfo loginInfo) {
        if (!checkAndSetState(DISCONNECTED)) {
            return;
        }
        try {
            mSession = new ImpsSession(this, loginInfo);
        } catch (ImException e) {
            setState(DISCONNECTED, e.getImError());
            return;
        }
        doLogin();
    }
    @Override
    public void reestablishSessionAsync(
            HashMap<String, String> cookie) {
        if (!checkAndSetState(SUSPENDED)) {
            return;
        }
        if (mDataChannel.resume()) {
            try {
                setupCIRChannel();
            } catch(ImException e) {}
            setState(LOGGED_IN, null);
        } else {
            mReestablishing = true;
            try {
                mSession = new ImpsSession(this, cookie);
            } catch (ImException e) {
                setState(DISCONNECTED, e.getImError());
                return;
            }
            doLogin();
        }
    }
    @Override
    public void networkTypeChanged() {
        if (mCirChannel != null) {
            mCirChannel.reconnect();
        }
    }
    private synchronized boolean checkAndSetState(int state) {
        if(mState != state){
            return false;
        }
        setState(LOGGING_IN, null);
        return true;
    }
    private void doLogin() {
        try {
            if (mConfig.useSmsAuth()) {
                mDataChannel = new SmsDataChannel(this);
            } else {
                mDataChannel = createDataChannel();
            }
            mDataChannel.connect();
        } catch (ImException e) {
            ImErrorInfo error = e.getImError();
            if(error == null){
                error = new ImErrorInfo(ImErrorInfo.UNKNOWN_LOGIN_ERROR,
                        e.getMessage());
            }
            shutdownOnError(error);
            return;
        }
        mDispatcherThread = new PrimitiveDispatcherThread(mDataChannel);
        mDispatcherThread.start();
        LoginTransaction login = new LoginTransaction();
        login.startAuthenticate();
    }
    @Override
    public HashMap<String, String> getSessionContext() {
        if(mState != LOGGED_IN) {
            return null;
        } else {
            return mSession.getContext();
        }
    }
    class LoginTransaction extends MultiPhaseTransaction {
        LoginTransaction() {
            super(mTransactionManager);
        }
        public void startAuthenticate() {
            Primitive login = buildBasicLoginReq();
            if (mConfig.use4wayLogin()) {
                String[] supportedDigestSchema = mConfig.getPasswordDigest().getSupportedDigestSchema();
                for (String element : supportedDigestSchema) {
                    login.addElement(ImpsTags.DigestSchema, element);
                }
            } else {
                login.addElement(ImpsTags.Password, mSession.getPassword());
            }
            sendRequest(login);
        }
        @Override
        public TransactionStatus processResponse(Primitive response) {
            if (response.getElement(ImpsTags.SessionID) != null) {
                String sessionId = response.getElementContents(ImpsTags.SessionID);
                String keepAliveTime = response.getElementContents(ImpsTags.KeepAliveTime);
                String capablityReqeust = response.getElementContents(ImpsTags.CapabilityRequest);
                long keepAlive = ImpsUtils.parseLong(keepAliveTime,
                        mConfig.getDefaultKeepAliveInterval());
                keepAlive -= 5;
                mSession.setId(sessionId);
                mSession.setKeepAliveTime(keepAlive);
                mSession.setCapablityRequestRequired(ImpsUtils.isTrue(capablityReqeust));
                onAuthenticated();
                return TransactionStatus.TRANSACTION_COMPLETED;
            } else {
                return sendSecondLogin(response);
            }
        }
        @Override
        public TransactionStatus processResponseError(ImpsErrorInfo error) {
            if (error.getCode() == ImpsConstants.STATUS_UNAUTHORIZED
                    && error.getPrimitive() != null) {
                if (mConfig.use4wayLogin()) {
                    return sendSecondLogin(error.getPrimitive());
                } else {
                    shutdownOnError(new ImErrorInfo(409, "Invalid password"));
                    return TransactionStatus.TRANSACTION_COMPLETED;
                }
            } else if(error.getCode() == ImpsConstants.STATUS_COULD_NOT_RECOVER_SESSION) {
                LoginInfo loginInfo = mSession.getLoginInfo();
                try {
                    mSession = new ImpsSession(ImpsConnection.this, loginInfo);
                } catch (ImException ignore) {
                }
                startAuthenticate();
                return TransactionStatus.TRANSACTION_COMPLETED;
            } else {
                shutdownOnError(error);
                return TransactionStatus.TRANSACTION_COMPLETED;
            }
        }
        private TransactionStatus sendSecondLogin(Primitive res) {
            try {
                Primitive secondLogin = buildBasicLoginReq();
                String nonce = res.getElementContents(ImpsTags.Nonce);
                String digestSchema = res.getElementContents(ImpsTags.DigestSchema);
                String digestBytes = mConfig.getPasswordDigest().digest(digestSchema, nonce,
                        mSession.getPassword());
                secondLogin.addElement(ImpsTags.DigestBytes, digestBytes);
                sendRequest(secondLogin);
                return TransactionStatus.TRANSACTION_CONTINUE;
            } catch (ImException e) {
                ImpsLog.logError(e);
                shutdownOnError(new ImErrorInfo(ImErrorInfo.UNKNOWN_ERROR, e.toString()));
                return TransactionStatus.TRANSACTION_COMPLETED;
            }
        }
        private void onAuthenticated() {
            if (mState == LOGGING_OUT) {
                sendLogoutRequest();
                return;
            }
            if (mConfig.useSmsAuth()
                    && mConfig.getDataChannelBinding() != TransportType.SMS) {
                try {
                    DataChannel dataChannel = createDataChannel();
                    dataChannel.connect();
                    mDataChannel.shutdown();
                    mDataChannel = dataChannel;
                    mDispatcherThread.changeDataChannel(dataChannel);
                } catch (ImException e) {
                    logoutAsync();
                    return;
                }
            }
            if(mSession.isCapablityRequestRequired()) {
                mSession.negotiateCapabilityAsync(new AsyncCompletion(){
                    public void onComplete() {
                        onCapabilityNegotiated();
                    }
                    public void onError(ImErrorInfo error) {
                        shutdownOnError(error);
                    }
                });
            } else {
                onCapabilityNegotiated();
            }
        }
        void onCapabilityNegotiated() {
            mDataChannel.setServerMinPoll(mSession.getServerPollMin());
            if(getConfig().getCirChannelBinding() != CirMethod.NONE) {
                try {
                    setupCIRChannel();
                } catch (ImException e) {
                    shutdownOnError(new ImErrorInfo(
                            ImErrorInfo.UNSUPPORTED_CIR_CHANNEL, e.toString()));
                    return;
                }
            }
            mSession.negotiateServiceAsync(new AsyncCompletion(){
                public void onComplete() {
                    onServiceNegotiated();
                }
                public void onError(ImErrorInfo error) {
                    shutdownOnError(error);
                }
            });
        }
        void onServiceNegotiated() {
            mDataChannel.startKeepAlive(mSession.getKeepAliveTime());
            retrieveUserPresenceAsync(new AsyncCompletion() {
                public void onComplete() {
                    setState(LOGGED_IN, null);
                    if (mReestablishing) {
                        ImpsContactListManager listMgr=  (ImpsContactListManager) getContactListManager();
                        listMgr.subscribeToAllListAsync();
                        mReestablishing = false;
                    }
                }
                public void onError(ImErrorInfo error) {
                    onComplete();
                }
            });
        }
    }
    @Override
    public void logoutAsync() {
        setState(LOGGING_OUT, null);
        if(mCirChannel != null) {
            mCirChannel.shutdown();
            mCirChannel = null;
        }
        if (mSession.getID() != null) {
            sendLogoutRequest();
        }
    }
    void sendLogoutRequest() {
        AsyncCompletion completion = new AsyncCompletion() {
            public void onComplete() {
                shutdown();
            }
            public void onError(ImErrorInfo error) {
                shutdown();
            }
        };
        AsyncTransaction tx = new SimpleAsyncTransaction(mTransactionManager,
                completion);
        Primitive logoutPrimitive = new Primitive(ImpsTags.Logout_Request);
        tx.sendRequest(logoutPrimitive);
    }
    public ImpsSession getSession() {
        return mSession;
    }
    @Override
    public Contact getLoginUser() {
        if(mSession == null){
            return null;
        }
        Contact loginUser = mSession.getLoginUser();
        loginUser.setPresence(getUserPresence());
        return loginUser;
    }
    @Override
    public int[] getSupportedPresenceStatus() {
        return mConfig.getPresenceMapping().getSupportedPresenceStatus();
    }
    public ImpsTransactionManager getTransactionManager() {
        return mTransactionManager;
    }
    @Override
    public ChatSessionManager getChatSessionManager() {
        return mChatSessionManager;
    }
    @Override
    public ContactListManager getContactListManager() {
        return mContactListManager;
    }
    @Override
    public ChatGroupManager getChatGroupManager() {
        return mChatGroupManager;
    }
    void sendPrimitive(Primitive primitive) {
        mDataChannel.sendPrimitive(primitive);
    }
    void sendPollingRequest() {
        Primitive pollingRequest = new Primitive(ImpsTags.Polling_Request);
        pollingRequest.setSession(getSession().getID());
        mDataChannel.sendPrimitive(pollingRequest);
    }
    private DataChannel createDataChannel() throws ImException {
        TransportType dataChannelBinding = mConfig.getDataChannelBinding();
        if (dataChannelBinding == TransportType.HTTP) {
            return new HttpDataChannel(this);
        } else if (dataChannelBinding == TransportType.SMS) {
            return new SmsDataChannel(this);
        } else {
            throw new ImException("Unsupported data channel binding");
        }
    }
    void setupCIRChannel() throws ImException {
        if(mConfig.getDataChannelBinding() == TransportType.SMS) {
            return;
        }
        CirMethod cirMethod = mSession.getCurrentCirMethod();
        if (cirMethod == null) {
            cirMethod = mConfig.getCirChannelBinding();
            if (!mSession.getSupportedCirMethods().contains(cirMethod)) {
                cirMethod = CirMethod.SHTTP;
            }
            mSession.setCurrentCirMethod(cirMethod);
        }
        if (cirMethod == CirMethod.SHTTP) {
            mCirChannel = new HttpCirChannel(this, mDataChannel);
        } else if (cirMethod == CirMethod.STCP) {
            mCirChannel = new TcpCirChannel(this);
        } else if (cirMethod == CirMethod.SSMS) {
            mCirChannel = new SmsCirChannel(this);
        } else if (cirMethod == CirMethod.NONE) {
        } else {
            throw new ImException(ImErrorInfo.UNSUPPORTED_CIR_CHANNEL,
                    "Unsupported CIR channel binding");
        }
        if(mCirChannel != null) {
            mCirChannel.connect();
        }
    }
    private class PrimitiveDispatcherThread extends Thread {
        private boolean stopped;
        private DataChannel mChannel;
        public PrimitiveDispatcherThread(DataChannel channel)
        {
            super("ImpsPrimitiveDispatcher");
            mChannel = channel;
        }
        public void changeDataChannel(DataChannel channel) {
            mChannel = channel;
            interrupt();
        }
        @Override
        public void run() {
            Primitive primitive = null;
            while (!stopped) {
                try {
                    primitive = mChannel.receivePrimitive();
                } catch (InterruptedException e) {
                    if (stopped) {
                        break;
                    }
                    primitive = null;
                }
                if (primitive != null) {
                    try {
                        processIncomingPrimitive(primitive);
                    } catch (Throwable t) {
                        ImpsLog.logError("ImpsDispatcher: uncaught Throwable", t);
                    }
                }
            }
        }
        void shutdown() {
            stopped = true;
            interrupt();
        }
    }
    void processIncomingPrimitive(Primitive primitive) {
        if (primitive.getCir() != null && ImpsUtils.isFalse(primitive.getCir())) {
            if(mCirChannel != null) {
                mCirChannel.shutdown();
            }
            try {
                setupCIRChannel();
            } catch (ImException e) {
                e.printStackTrace();
            }
        }
        if (primitive.getPoll() != null && ImpsUtils.isTrue(primitive.getPoll())) {
            sendPollingRequest();
        }
        if (primitive.getType().equals(ImpsTags.Disconnect)) {
            if (mState != LOGGING_OUT) {
                ImErrorInfo error = ImpsUtils.checkResultError(primitive);
                shutdownOnError(error);
                return;
            }
        }
        if (primitive.getTransactionMode() == TransactionMode.Response) {
            ImpsErrorInfo error = ImpsUtils.checkResultError(primitive);
            if (error != null) {
                int code = error.getCode();
                if (code == ImpsErrorInfo.SESSION_EXPIRED
                        || code == ImpsErrorInfo.FORCED_LOGOUT
                        || code == ImpsErrorInfo.INVALID_SESSION) {
                    shutdownOnError(error);
                    return;
                }
            }
        }
        if (primitive.getTransactionID() != null) {
            mTransactionManager.notifyIncomingPrimitive(primitive);
        }
    }
    @Override
    protected void doUpdateUserPresenceAsync(Presence presence) {
        ArrayList<PrimitiveElement> presenceSubList = ImpsPresenceUtils.buildUpdatePresenceElems(
                mUserPresence, presence, mConfig.getPresenceMapping());
        Primitive request = buildUpdatePresenceReq(presenceSubList);
        final Presence newPresence = new Presence(presence);
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager) {
            @Override
            public void onResponseOk(Primitive response) {
                savePresenceChange(newPresence);
                notifyUserPresenceUpdated();
            }
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                notifyUpdateUserPresenceError(error);
            }
        };
        tx.sendRequest(request);
    }
    void savePresenceChange(Presence newPresence) {
        mUserPresence.setStatusText(newPresence.getStatusText());
        mUserPresence.setStatus(newPresence.getStatus());
        mUserPresence.setAvatar(newPresence.getAvatarData(), newPresence.getAvatarType());
    }
    void retrieveUserPresenceAsync(final AsyncCompletion completion) {
        Primitive request = new Primitive(ImpsTags.GetPresence_Request);
        request.addElement(this.getSession().getLoginUserAddress().toPrimitiveElement());
        AsyncTransaction tx = new AsyncTransaction(mTransactionManager){
            @Override
            public void onResponseOk(Primitive response) {
                PrimitiveElement presence = response.getElement(ImpsTags.Presence);
                PrimitiveElement presenceSubList = presence.getChild(ImpsTags.PresenceSubList);
                mUserPresence = ImpsPresenceUtils.extractPresence(presenceSubList,
                        mConfig.getPresenceMapping());
                if(mUserPresence.getStatus() == Presence.OFFLINE) {
                    mUserPresence.setStatus(Presence.AVAILABLE);
                }
                compareAndUpdateClientInfo();
            }
            @Override
            public void onResponseError(ImpsErrorInfo error) {
                mUserPresence = new Presence(Presence.AVAILABLE, "", null,
                        null, Presence.CLIENT_TYPE_MOBILE, ImpsUtils.getClientInfo());
                completion.onError(error);
            }
            private void compareAndUpdateClientInfo() {
                if (!ImpsUtils.getClientInfo().equals(mUserPresence.getExtendedInfo())) {
                    updateClientInfoAsync(completion);
                    return;
                }
                completion.onComplete();
            }
        };
        tx.sendRequest(request);
    }
    void updateClientInfoAsync(AsyncCompletion completion) {
        Primitive updatePresenceRequest = buildUpdatePresenceReq(buildClientInfoElem());
        AsyncTransaction tx = new SimpleAsyncTransaction(mTransactionManager,
                completion);
        tx.sendRequest(updatePresenceRequest);
    }
    private Primitive buildUpdatePresenceReq(PrimitiveElement presence) {
        ArrayList<PrimitiveElement> presences = new ArrayList<PrimitiveElement>();
        presences.add(presence);
        return buildUpdatePresenceReq(presences);
    }
    private Primitive buildUpdatePresenceReq(ArrayList<PrimitiveElement> presences) {
        Primitive updatePresenceRequest = new Primitive(ImpsTags.UpdatePresence_Request);
        PrimitiveElement presenceSubList = updatePresenceRequest
                .addElement(ImpsTags.PresenceSubList);
        presenceSubList.setAttribute(ImpsTags.XMLNS, mConfig.getPresenceNs());
        for (PrimitiveElement presence : presences) {
            presenceSubList.addChild(presence);
        }
        return updatePresenceRequest;
    }
    private PrimitiveElement buildClientInfoElem() {
        PrimitiveElement clientInfo = new PrimitiveElement(ImpsTags.ClientInfo);
        clientInfo.addChild(ImpsTags.Qualifier, true);
        Map<String, String> map = ImpsUtils.getClientInfo();
        for (Map.Entry<String, String> item : map.entrySet()) {
            clientInfo.addChild(item.getKey(), item.getValue());
        }
        return clientInfo;
    }
    Primitive buildBasicLoginReq() {
        Primitive login = new Primitive(ImpsTags.Login_Request);
        login.addElement(ImpsTags.UserID, mSession.getUserName());
        PrimitiveElement clientId = login.addElement(ImpsTags.ClientID);
        clientId.addChild(ImpsTags.URL, mConfig.getClientId());
        if (mConfig.getMsisdn() != null) {
            clientId.addChild(ImpsTags.MSISDN, mConfig.getMsisdn());
        }
        login.addElement(ImpsTags.TimeToLive,
                Integer.toString(mConfig.getDefaultKeepAliveInterval() + 5));
        login.addElement(ImpsTags.SessionCookie, mSession.getCookie());
        return login;
    }
    @Override
    synchronized public void suspend() {
        setState(SUSPENDING, null);
        if (mCirChannel != null) {
            mCirChannel.shutdown();
        }
        if (mDataChannel != null) {
            mDataChannel.suspend();
        }
        setState(SUSPENDED, null);
    }
}
