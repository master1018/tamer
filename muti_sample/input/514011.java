public class ImpsSession {
    private static final String KEY_CIR_HTTP_ADDRESS = "cirHttpAddress";
    private static final String KEY_CIR_TCP_PORT = "cirTcpPort";
    private static final String KEY_CIR_TCP_ADDRESS = "cirTcpAddress";
    private static final String KEY_CIR_METHOD = "CirMethod";
    private static final String KEY_SERVER_POLL_MIN = "serverPollMin";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_KEEP_ALIVE_TIME = "keepAliveTime";
    private static final String KEY_SESSION_COOKIE = "sessionCookie";
    private static final String KEY_SESSION_ID = "sessionId";
    private static final int DEFAULT_TCP_PORT = 3171;
    private ImpsConnection mConnection;
    private String mId;
    private String mCookie;
    private long mKeepAliveTime;
    private CirMethod mCurrentCirMethod;
    private String mCirTcpAddress;
    private int mCirTcpPort = DEFAULT_TCP_PORT;
    private long mServerPollMin;
    private String mCirHttpAddress;
    private LoginInfo mLoginInfo;
    private boolean mCapablityRequest;
    private List<CirMethod> mSupportedCirMethod;
    private Contact mLoginUser;
    PrimitiveElement mServiceTree;
    private boolean mNew;
    ImpsSession(ImpsConnection connection, LoginInfo info) throws ImException{
        mConnection = connection;
        setLoginInfo(info);
        mNew = true;
        mCookie = ImpsUtils.genSessionCookie();
        mCirTcpPort = DEFAULT_TCP_PORT;
        mServerPollMin = connection.getConfig().getDefaultServerPollMin();
    }
    ImpsSession(ImpsConnection connection, HashMap<String, String> values)
            throws ImException {
        mConnection = connection;
        mNew = false;
        mId = values.get(KEY_SESSION_ID);
        if (mId == null || mId.length() == 0) {
            throw new ImException(ImErrorInfo.INVALID_SESSION_CONTEXT,
                "Missing session id");
        }
        mCookie = values.get(KEY_SESSION_COOKIE);
        if (mCookie == null || mCookie.length() == 0) {
            throw new ImException(ImErrorInfo.INVALID_SESSION_CONTEXT,
                "Missing session cookie");
        }
        try {
            mKeepAliveTime = Long.parseLong(values.get(KEY_KEEP_ALIVE_TIME));
        } catch (NumberFormatException e) {
            throw new ImException(ImErrorInfo.INVALID_SESSION_CONTEXT,
                "Invalid keepAliveTime");
        }
        try {
            mServerPollMin = Long.parseLong(values.get(KEY_SERVER_POLL_MIN));
        } catch (NumberFormatException e) {
            throw new ImException(ImErrorInfo.INVALID_SESSION_CONTEXT,
                "Invalid serverPollMin");
        }
        String username = values.get(KEY_USERNAME);
        String password = values.get(KEY_PASSWORD);
        if (username == null || username.length() == 0 || password == null) {
            throw new ImException(ImErrorInfo.INVALID_SESSION_CONTEXT,
                "Invalid username or password");
        }
        setLoginInfo(new LoginInfo(username, password));
        mCurrentCirMethod = CirMethod.valueOf(values.get(KEY_CIR_METHOD));
        if (mCurrentCirMethod == CirMethod.STCP) {
            mCirTcpAddress = values.get(KEY_CIR_TCP_ADDRESS);
            if (mCirTcpAddress == null || mCirTcpAddress.length() == 0) {
                throw new ImException(ImErrorInfo.INVALID_SESSION_CONTEXT,
                    "Missing CirTcpAddress");
            }
            try {
                mCirTcpPort = Integer.parseInt(values.get(KEY_CIR_TCP_PORT));
            } catch (NumberFormatException e) {
                throw new ImException(ImErrorInfo.INVALID_SESSION_CONTEXT,
                    "Invalid CirTcpPort");
            }
        } else if (mCurrentCirMethod == CirMethod.SHTTP) {
            mCirHttpAddress = values.get(KEY_CIR_HTTP_ADDRESS);
        }
    }
    public HashMap<String, String> getContext() {
        HashMap<String, String> values = new HashMap<String, String>();
        values.put(KEY_SESSION_ID, mId);
        values.put(KEY_SESSION_COOKIE, mCookie);
        values.put(KEY_KEEP_ALIVE_TIME, Long.toString(mKeepAliveTime));
        values.put(KEY_USERNAME, mLoginInfo.getUserName());
        values.put(KEY_PASSWORD, mLoginInfo.getPassword());
        values.put(KEY_SERVER_POLL_MIN, Long.toString(mServerPollMin));
        values.put(KEY_CIR_METHOD, mCurrentCirMethod.name());
        if(mCurrentCirMethod == CirMethod.STCP) {
            values.put(KEY_CIR_TCP_ADDRESS, mCirTcpAddress);
            values.put(KEY_CIR_TCP_PORT, Integer.toString(mCirTcpPort));
        } else if (mCurrentCirMethod == CirMethod.SHTTP) {
            values.put(KEY_CIR_HTTP_ADDRESS, mCirHttpAddress);
        }
        return values;
    }
    public String getID() {
        return mId;
    }
    public void setId(String id) {
        mId = id;
    }
    public String getCookie() {
        return mCookie;
    }
    public String getUserName() {
        return mLoginInfo.getUserName();
    }
    public String getPassword() {
        return mLoginInfo.getPassword();
    }
    public LoginInfo getLoginInfo() {
        return mLoginInfo;
    }
    public long getKeepAliveTime() {
        return mKeepAliveTime;
    }
    public void setKeepAliveTime(long keepAliveTime) {
        mKeepAliveTime = keepAliveTime;
    }
    public boolean isCapablityRequestRequired() {
        return mCapablityRequest || mNew;
    }
    public void setCapablityRequestRequired(boolean required) {
        mCapablityRequest = required;
    }
    public ImpsUserAddress getLoginUserAddress() {
        return (ImpsUserAddress) mLoginUser.getAddress();
    }
    public Contact getLoginUser() {
        return mLoginUser;
    }
    private void setLoginInfo(LoginInfo loginInfo) throws ImException {
        try {
            ImpsAddress address = new ImpsUserAddress(loginInfo.getUserName());
            mLoginUser = new Contact(address, address.getScreenName());
            mLoginInfo = loginInfo;
        } catch (IllegalArgumentException e) {
            throw new ImException(ImErrorInfo.INVALID_USERNAME,
                    "Invalid username");
        }
    }
    public List<CirMethod> getSupportedCirMethods() {
        return mSupportedCirMethod;
    }
    public CirMethod getCurrentCirMethod() {
        return mCurrentCirMethod;
    }
    public void setCurrentCirMethod(CirMethod cirMethod) {
        mCurrentCirMethod = cirMethod;
    }
    public String getCirTcpAddress() {
        return mCirTcpAddress;
    }
    public int getCirTcpPort() {
        return mCirTcpPort;
    }
    public long getServerPollMin() {
        return mServerPollMin;
    }
    public String getCirHttpAddress() {
        return mCirHttpAddress;
    }
    public PrimitiveElement getServiceTree() {
        return mServiceTree;
    }
    public void negotiateCapabilityAsync(AsyncCompletion completion) {
        Primitive capabilityRequest = buildCapabilityRequest();
        AsyncTransaction tx = new AsyncTransaction(
                mConnection.getTransactionManager(), completion) {
            @Override
            public void onResponseOk(Primitive response) {
                extractCapability(response);
            }
            @Override
            public void onResponseError(ImpsErrorInfo error) { }
        };
        tx.sendRequest(capabilityRequest);
    }
    public void negotiateServiceAsync(AsyncCompletion completion) {
        Primitive serviceRequest = buildServiceRequest();
        AsyncTransaction tx = new AsyncTransaction(
                mConnection.getTransactionManager(), completion) {
            @Override
            public void onResponseOk(Primitive response) {
                mServiceTree = response.getElement(ImpsTags.AllFunctions).getFirstChild();
            }
            @Override
            public void onResponseError(ImpsErrorInfo error) { }
        };
        tx.sendRequest(serviceRequest);
    }
    private Primitive buildCapabilityRequest() {
        Primitive capabilityRequest = new Primitive(ImpsTags.ClientCapability_Request);
        PrimitiveElement list = capabilityRequest.addElement(ImpsTags.CapabilityList);
        list.addChild(ImpsTags.ClientType, ImpsClientCapability.getClientType());
        list.addChild(ImpsTags.AcceptedContentLength, Integer
                .toString(ImpsClientCapability.getAcceptedContentLength()));
        list.addChild(ImpsTags.ParserSize,
                Integer.toString(ImpsClientCapability.getParserSize()));
        list.addChild(ImpsTags.MultiTrans,
                Integer.toString(ImpsClientCapability.getMultiTrans()));
        list.addChild(ImpsTags.InitialDeliveryMethod,
                ImpsClientCapability.getInitialDeliveryMethod());
        list.addChild(ImpsTags.ServerPollMin, Long.toString(mServerPollMin));
        for(TransportType supportedBear : ImpsClientCapability.getSupportedBearers()) {
            list.addChild(ImpsTags.SupportedBearer, supportedBear.toString());
        }
        for(CirMethod supportedCirMethod : ImpsClientCapability.getSupportedCirMethods()) {
            list.addChild(ImpsTags.SupportedCIRMethod, supportedCirMethod.toString());
            if (CirMethod.SUDP.equals(supportedCirMethod)) {
                list.addChild(ImpsTags.UDPPort,
                        Integer.toString(mConnection.getConfig().getUdpPort()));
            }
        }
        return capabilityRequest;
    }
    void extractCapability(Primitive capabilityResponse) {
        mSupportedCirMethod = new ArrayList<CirMethod>();
        PrimitiveElement agreedList = capabilityResponse.getContentElement()
                .getFirstChild();
        for (PrimitiveElement element : agreedList.getChildren()) {
            String tag = element.getTagName();
            if (tag.equals(ImpsTags.SupportedCIRMethod)) {
                try {
                    mSupportedCirMethod.add(CirMethod.valueOf(element.getContents()));
                } catch (IllegalArgumentException e) {
                    ImpsLog.log("Unrecognized CIR method " + element.getContents());
                }
            } else if (tag.equals(ImpsTags.TCPAddress)) {
                mCirTcpAddress = element.getContents();
            } else if (tag.equals(ImpsTags.TCPPort)) {
                mCirTcpPort = (int)ImpsUtils.parseLong(element.getContents(),
                        DEFAULT_TCP_PORT);
            } else if (tag.equals(ImpsTags.ServerPollMin)) {
                long defaultPollMin = mConnection.getConfig().getDefaultServerPollMin();
                mServerPollMin = ImpsUtils.parseLong(element.getContents(),
                        defaultPollMin);
                if (mServerPollMin <= 0) {
                    mServerPollMin = defaultPollMin;
                }
            } else if (tag.equals(ImpsTags.CIRHTTPAddress)
                    || tag.equals(ImpsTags.CIRURL)) {
                mCirHttpAddress = element.getChildContents(ImpsTags.URL);
            }
        }
    }
    private Primitive buildServiceRequest() {
        Primitive serviceRequest = new Primitive(ImpsTags.Service_Request);
        PrimitiveElement functions = serviceRequest.addElement(ImpsTags.Functions);
        PrimitiveElement features = functions.addChild(ImpsTags.WVCSPFeat);
        features.addChild(ImpsTags.FundamentalFeat);
        features.addChild(ImpsTags.PresenceFeat);
        features.addChild(ImpsTags.IMFeat);
        features.addChild(ImpsTags.GroupFeat);
        serviceRequest.addElement(ImpsTags.AllFunctionsRequest, true);
        return serviceRequest;
    }
}
