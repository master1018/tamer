public class EasSyncService extends AbstractSyncService {
    public static final boolean DEBUG_GAL_SERVICE = false;
    private static final String EMAIL_WINDOW_SIZE = "5";
    public static final String PIM_WINDOW_SIZE = "4";
    private static final String WHERE_ACCOUNT_KEY_AND_SERVER_ID =
        MailboxColumns.ACCOUNT_KEY + "=? and " + MailboxColumns.SERVER_ID + "=?";
    private static final String WHERE_ACCOUNT_AND_SYNC_INTERVAL_PING =
        MailboxColumns.ACCOUNT_KEY + "=? and " + MailboxColumns.SYNC_INTERVAL +
        '=' + Mailbox.CHECK_INTERVAL_PING;
    private static final String AND_FREQUENCY_PING_PUSH_AND_NOT_ACCOUNT_MAILBOX = " AND " +
        MailboxColumns.SYNC_INTERVAL + " IN (" + Mailbox.CHECK_INTERVAL_PING +
        ',' + Mailbox.CHECK_INTERVAL_PUSH + ") AND " + MailboxColumns.TYPE + "!=\"" +
        Mailbox.TYPE_EAS_ACCOUNT_MAILBOX + '\"';
    private static final String WHERE_PUSH_HOLD_NOT_ACCOUNT_MAILBOX =
        MailboxColumns.ACCOUNT_KEY + "=? and " + MailboxColumns.SYNC_INTERVAL +
        '=' + Mailbox.CHECK_INTERVAL_PUSH_HOLD;
    static private final int CHUNK_SIZE = 16*1024;
    static private final String PING_COMMAND = "Ping";
    static private final int COMMAND_TIMEOUT = 30*SECONDS;
    static private final int CONNECTION_TIMEOUT = 20*SECONDS;
    static private final int WATCHDOG_TIMEOUT_ALLOWANCE = 30*SECONDS;
    static private final int ACCOUNT_MAILBOX_SLEEP_TIME = 20*MINUTES;
    static private final String ACCOUNT_MAILBOX_SLEEP_TEXT =
        "Account mailbox sleeping for " + (ACCOUNT_MAILBOX_SLEEP_TIME / MINUTES) + "m";
    static private final String AUTO_DISCOVER_SCHEMA_PREFIX =
        "http:
    static private final String AUTO_DISCOVER_PAGE = "/autodiscover/autodiscover.xml";
    static private final int AUTO_DISCOVER_REDIRECT_CODE = 451;
    static public final String EAS_12_POLICY_TYPE = "MS-EAS-Provisioning-WBXML";
    static public final String EAS_2_POLICY_TYPE = "MS-WAP-Provisioning-XML";
    static private final int PING_MINUTES = 60; 
    static private final int PING_FUDGE_LOW = 10;
    static private final int PING_STARTING_HEARTBEAT = (8*PING_MINUTES)-PING_FUDGE_LOW;
    static private final int PING_HEARTBEAT_INCREMENT = 3*PING_MINUTES;
    static private final int MAX_LOOPING_COUNT = 100;
    static private final int PROTOCOL_PING_STATUS_COMPLETED = 1;
    static private final int POST_LOCK_TIMEOUT = 10*SECONDS;
    static private final int MAX_PING_FAILURES = 1;
    static private final int PING_FALLBACK_INBOX = 5;
    static private final int PING_FALLBACK_PIM = 25;
    static private final int HTTP_NEED_PROVISIONING = 449;
    static private final String PROVISION_STATUS_OK = "1";
    static private final String PROVISION_STATUS_PARTIAL = "2";
    public String mProtocolVersion = Eas.DEFAULT_PROTOCOL_VERSION;
    public Double mProtocolVersionDouble;
    protected String mDeviceId = null;
     String mDeviceType = "Android";
     String mAuthString = null;
    private String mCmdString = null;
    public String mHostAddress;
    public String mUserName;
    public String mPassword;
    private boolean mSsl = true;
    private boolean mTrustSsl = false;
    public ContentResolver mContentResolver;
    private String[] mBindArguments = new String[2];
    private ArrayList<String> mPingChangeList;
    private volatile HttpPost mPendingPost = null;
     int mPingForceHeartbeat = 2*PING_MINUTES;
     int mPingMinHeartbeat = (5*PING_MINUTES)-PING_FUDGE_LOW;
     int mPingMaxHeartbeat = (17*PING_MINUTES)-PING_FUDGE_LOW;
     int mPingHeartbeat = PING_STARTING_HEARTBEAT;
    private int mPingHighWaterMark = 0;
     boolean mPingHeartbeatDropped = false;
    private boolean mPostAborted = false;
    private boolean mPostReset = false;
    public boolean mIsValid = true;
    public EasSyncService(Context _context, Mailbox _mailbox) {
        super(_context, _mailbox);
        mContentResolver = _context.getContentResolver();
        if (mAccount == null) {
            mIsValid = false;
            return;
        }
        HostAuth ha = HostAuth.restoreHostAuthWithId(_context, mAccount.mHostAuthKeyRecv);
        if (ha == null) {
            mIsValid = false;
            return;
        }
        mSsl = (ha.mFlags & HostAuth.FLAG_SSL) != 0;
        mTrustSsl = (ha.mFlags & HostAuth.FLAG_TRUST_ALL_CERTIFICATES) != 0;
    }
    private EasSyncService(String prefix) {
        super(prefix);
    }
    public EasSyncService() {
        this("EAS Validation");
    }
    @Override
    public boolean alarm() {
        HttpPost post;
        if (mThread == null) return true;
        String threadName = mThread.getName();
        synchronized(getSynchronizer()) {
            post = mPendingPost;
            if (post != null) {
                if (Eas.USER_LOG) {
                    URI uri = post.getURI();
                    if (uri != null) {
                        String query = uri.getQuery();
                        if (query == null) {
                            query = "POST";
                        }
                        userLog(threadName, ": Alert, aborting ", query);
                    } else {
                        userLog(threadName, ": Alert, no URI?");
                    }
                }
                mPostAborted = true;
                post.abort();
            } else {
                userLog("Alert, no pending POST");
                return true;
            }
        }
        try {
            Thread.sleep(POST_LOCK_TIMEOUT);
        } catch (InterruptedException e) {
        }
        State s = mThread.getState();
        if (Eas.USER_LOG) {
            userLog(threadName + ": State = " + s.name());
        }
        synchronized (getSynchronizer()) {
            if ((s != State.TERMINATED) && (mPendingPost != null) && (mPendingPost == post)) {
                mStop = true;
                mThread.interrupt();
                userLog("Interrupting...");
                return false;
            }
        }
        return true;
    }
    @Override
    public void reset() {
        synchronized(getSynchronizer()) {
            if (mPendingPost != null) {
                URI uri = mPendingPost.getURI();
                if (uri != null) {
                    String query = uri.getQuery();
                    if (query.startsWith("Cmd=Ping")) {
                        userLog("Reset, aborting Ping");
                        mPostReset = true;
                        mPendingPost.abort();
                    }
                }
            }
        }
    }
    @Override
    public void stop() {
        mStop = true;
        synchronized(getSynchronizer()) {
            if (mPendingPost != null) {
                mPendingPost.abort();
            }
        }
    }
    protected boolean isAuthError(int code) {
        return (code == HttpStatus.SC_UNAUTHORIZED) || (code == HttpStatus.SC_FORBIDDEN);
    }
    protected boolean isProvisionError(int code) {
        return (code == HTTP_NEED_PROVISIONING) || (code == HttpStatus.SC_FORBIDDEN);
    }
    private void setupProtocolVersion(EasSyncService service, Header versionHeader)
            throws MessagingException {
        String supportedVersions = versionHeader.getValue();
        userLog("Server supports versions: ", supportedVersions);
        String[] supportedVersionsArray = supportedVersions.split(",");
        String ourVersion = null;
        for (String version: supportedVersionsArray) {
            if (version.equals(Eas.SUPPORTED_PROTOCOL_EX2003) ||
                    version.equals(Eas.SUPPORTED_PROTOCOL_EX2007)) {
                ourVersion = version;
            }
        }
        if (ourVersion == null) {
            Log.w(TAG, "No supported EAS versions: " + supportedVersions);
            throw new MessagingException(MessagingException.PROTOCOL_VERSION_UNSUPPORTED);
        } else {
            service.mProtocolVersion = ourVersion;
            service.mProtocolVersionDouble = Double.parseDouble(ourVersion);
            if (service.mAccount != null) {
                service.mAccount.mProtocolVersion = ourVersion;
            }
        }
    }
    @Override
    public void validateAccount(String hostAddress, String userName, String password, int port,
            boolean ssl, boolean trustCertificates, Context context) throws MessagingException {
        try {
            userLog("Testing EAS: ", hostAddress, ", ", userName, ", ssl = ", ssl ? "1" : "0");
            EasSyncService svc = new EasSyncService("%TestAccount%");
            svc.mContext = context;
            svc.mHostAddress = hostAddress;
            svc.mUserName = userName;
            svc.mPassword = password;
            svc.mSsl = ssl;
            svc.mTrustSsl = trustCertificates;
            svc.mDeviceId = "validate";
            HttpResponse resp = svc.sendHttpClientOptions();
            int code = resp.getStatusLine().getStatusCode();
            userLog("Validation (OPTIONS) response: " + code);
            if (code == HttpStatus.SC_OK) {
                Header commands = resp.getFirstHeader("MS-ASProtocolCommands");
                Header versions = resp.getFirstHeader("ms-asprotocolversions");
                if (commands == null || versions == null) {
                    userLog("OPTIONS response without commands or versions; reporting I/O error");
                    throw new MessagingException(MessagingException.IOERROR);
                }
                setupProtocolVersion(svc, versions);
                Serializer s = new Serializer();
                userLog("Validate: try folder sync");
                s.start(Tags.FOLDER_FOLDER_SYNC).start(Tags.FOLDER_SYNC_KEY).text("0")
                    .end().end().done();
                resp = svc.sendHttpClientPost("FolderSync", s.toByteArray());
                code = resp.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_FORBIDDEN || code == HTTP_NEED_PROVISIONING) {
                    userLog("Validate: provisioning required");
                    if (svc.canProvision() != null) {
                        userLog("Validate: provisioning is possible");
                        throw new MessagingException(MessagingException.SECURITY_POLICIES_REQUIRED);
                    } else
                        userLog("Validate: provisioning not possible");
                        throw new MessagingException(
                                MessagingException.SECURITY_POLICIES_UNSUPPORTED);
                } else if (code == HttpStatus.SC_NOT_FOUND) {
                    userLog("Wrong address or bad protocol version");
                    throw new MessagingException(MessagingException.PROTOCOL_VERSION_UNSUPPORTED);
                } else if (code != HttpStatus.SC_OK) {
                    userLog("Unexpected response for FolderSync: ", code);
                    throw new MessagingException(MessagingException.UNSPECIFIED_EXCEPTION);
                }
                userLog("Validation successful");
                return;
            }
            if (isAuthError(code)) {
                userLog("Authentication failed");
                throw new AuthenticationFailedException("Validation failed");
            } else {
                userLog("Validation failed, reporting I/O error: ", code);
                throw new MessagingException(MessagingException.IOERROR);
            }
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause != null && cause instanceof CertificateException) {
                userLog("CertificateException caught: ", e.getMessage());
                throw new MessagingException(MessagingException.GENERAL_SECURITY);
            }
            userLog("IOException caught: ", e.getMessage());
            throw new MessagingException(MessagingException.IOERROR);
        }
    }
    private HttpPost getRedirect(HttpResponse resp, HttpPost post) {
        Header locHeader = resp.getFirstHeader("X-MS-Location");
        if (locHeader != null) {
            String loc = locHeader.getValue();
            if (loc != null && loc.startsWith("http")) {
                post.setURI(URI.create(loc));
                return post;
            }
        }
        return null;
    }
    private HttpResponse postAutodiscover(HttpClient client, HttpPost post, boolean canRetry)
            throws IOException, MessagingException {
        userLog("Posting autodiscover to: " + post.getURI());
        HttpResponse resp = executePostWithTimeout(client, post, COMMAND_TIMEOUT);
        int code = resp.getStatusLine().getStatusCode();
        if (code == AUTO_DISCOVER_REDIRECT_CODE) {
            post = getRedirect(resp, post);
            if (post != null) {
                userLog("Posting autodiscover to redirect: " + post.getURI());
                return executePostWithTimeout(client, post, COMMAND_TIMEOUT);
            }
        } else if (code == HttpStatus.SC_UNAUTHORIZED) {
            if (canRetry && mUserName.contains("@")) {
                int atSignIndex = mUserName.indexOf('@');
                mUserName = mUserName.substring(0, atSignIndex);
                cacheAuthAndCmdString();
                userLog("401 received; trying username: ", mUserName);
                post.removeHeaders("Authorization");
                post.setHeader("Authorization", mAuthString);
                return postAutodiscover(client, post, false);
            }
            throw new MessagingException(MessagingException.AUTHENTICATION_FAILED);
        } else if (code != HttpStatus.SC_OK) {
            userLog("Code: " + code + ", throwing IOException");
            throw new IOException();
        }
        return resp;
    }
    public Bundle tryAutodiscover(String userName, String password) throws RemoteException {
        XmlSerializer s = Xml.newSerializer();
        ByteArrayOutputStream os = new ByteArrayOutputStream(1024);
        HostAuth hostAuth = new HostAuth();
        Bundle bundle = new Bundle();
        bundle.putInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE,
                MessagingException.NO_ERROR);
        try {
            s.setOutput(os, "UTF-8");
            s.startDocument("UTF-8", false);
            s.startTag(null, "Autodiscover");
            s.attribute(null, "xmlns", AUTO_DISCOVER_SCHEMA_PREFIX + "requestschema/2006");
            s.startTag(null, "Request");
            s.startTag(null, "EMailAddress").text(userName).endTag(null, "EMailAddress");
            s.startTag(null, "AcceptableResponseSchema");
            s.text(AUTO_DISCOVER_SCHEMA_PREFIX + "responseschema/2006");
            s.endTag(null, "AcceptableResponseSchema");
            s.endTag(null, "Request");
            s.endTag(null, "Autodiscover");
            s.endDocument();
            String req = os.toString();
            mUserName = userName;
            mPassword = password;
            cacheAuthAndCmdString();
            int amp = userName.indexOf('@');
            if (amp < 0) {
                throw new RemoteException();
            }
            String domain = userName.substring(amp + 1);
            HttpPost post = new HttpPost("https:
            setHeaders(post, false);
            post.setHeader("Content-Type", "text/xml");
            post.setEntity(new StringEntity(req));
            HttpClient client = getHttpClient(COMMAND_TIMEOUT);
            HttpResponse resp;
            try {
                resp = postAutodiscover(client, post, true );
            } catch (IOException e1) {
                userLog("IOException in autodiscover; trying alternate address");
                post.setURI(URI.create("https:
                resp = postAutodiscover(client, post, true );
            }
            int code = resp.getStatusLine().getStatusCode();
            userLog("Code: " + code);
            if (code != HttpStatus.SC_OK) return null;
            HttpEntity e = resp.getEntity();
            InputStream is = e.getContent();
            try {
                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(is, "UTF-8");
                int type = parser.getEventType();
                if (type == XmlPullParser.START_DOCUMENT) {
                    type = parser.next();
                    if (type == XmlPullParser.START_TAG) {
                        String name = parser.getName();
                        if (name.equals("Autodiscover")) {
                            hostAuth = new HostAuth();
                            parseAutodiscover(parser, hostAuth);
                            if (hostAuth.mAddress != null) {
                                hostAuth.mLogin = mUserName;
                                hostAuth.mPassword = mPassword;
                                hostAuth.mPort = 443;
                                hostAuth.mProtocol = "eas";
                                hostAuth.mFlags =
                                    HostAuth.FLAG_SSL | HostAuth.FLAG_AUTHENTICATE;
                                bundle.putParcelable(
                                        EmailServiceProxy.AUTO_DISCOVER_BUNDLE_HOST_AUTH, hostAuth);
                            } else {
                                bundle.putInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE,
                                        MessagingException.UNSPECIFIED_EXCEPTION);
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e1) {
            }
       } catch (IllegalArgumentException e) {
             bundle.putInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE,
                     MessagingException.UNSPECIFIED_EXCEPTION);
       } catch (IllegalStateException e) {
            bundle.putInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE,
                    MessagingException.UNSPECIFIED_EXCEPTION);
       } catch (IOException e) {
            userLog("IOException in Autodiscover", e);
            bundle.putInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE,
                    MessagingException.IOERROR);
        } catch (MessagingException e) {
            bundle.putInt(EmailServiceProxy.AUTO_DISCOVER_BUNDLE_ERROR_CODE,
                    MessagingException.AUTHENTICATION_FAILED);
        }
        return bundle;
    }
    void parseServer(XmlPullParser parser, HostAuth hostAuth)
            throws XmlPullParserException, IOException {
        boolean mobileSync = false;
        while (true) {
            int type = parser.next();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("Server")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("Type")) {
                    if (parser.nextText().equals("MobileSync")) {
                        mobileSync = true;
                    }
                } else if (mobileSync && name.equals("Url")) {
                    String url = parser.nextText().toLowerCase();
                    if (url.startsWith("https:
                            url.endsWith("/microsoft-server-activesync")) {
                        int lastSlash = url.lastIndexOf('/');
                        hostAuth.mAddress = url.substring(8, lastSlash);
                        userLog("Autodiscover, server: " + hostAuth.mAddress);
                    }
                }
            }
        }
    }
    void parseSettings(XmlPullParser parser, HostAuth hostAuth)
            throws XmlPullParserException, IOException {
        while (true) {
            int type = parser.next();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("Settings")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("Server")) {
                    parseServer(parser, hostAuth);
                }
            }
        }
    }
    void parseAction(XmlPullParser parser, HostAuth hostAuth)
            throws XmlPullParserException, IOException {
        while (true) {
            int type = parser.next();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("Action")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("Error")) {
                } else if (name.equals("Redirect")) {
                    Log.d(TAG, "Redirect: " + parser.nextText());
                } else if (name.equals("Settings")) {
                    parseSettings(parser, hostAuth);
                }
            }
        }
    }
    void parseUser(XmlPullParser parser, HostAuth hostAuth)
            throws XmlPullParserException, IOException {
        while (true) {
            int type = parser.next();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("User")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("EMailAddress")) {
                    String addr = parser.nextText();
                    userLog("Autodiscover, email: " + addr);
                } else if (name.equals("DisplayName")) {
                    String dn = parser.nextText();
                    userLog("Autodiscover, user: " + dn);
                }
            }
        }
    }
    void parseResponse(XmlPullParser parser, HostAuth hostAuth)
            throws XmlPullParserException, IOException {
        while (true) {
            int type = parser.next();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("Response")) {
                break;
            } else if (type == XmlPullParser.START_TAG) {
                String name = parser.getName();
                if (name.equals("User")) {
                    parseUser(parser, hostAuth);
                } else if (name.equals("Action")) {
                    parseAction(parser, hostAuth);
                }
            }
        }
    }
    void parseAutodiscover(XmlPullParser parser, HostAuth hostAuth)
            throws XmlPullParserException, IOException {
        while (true) {
            int type = parser.nextTag();
            if (type == XmlPullParser.END_TAG && parser.getName().equals("Autodiscover")) {
                break;
            } else if (type == XmlPullParser.START_TAG && parser.getName().equals("Response")) {
                parseResponse(parser, hostAuth);
            }
        }
    }
    static public GalResult searchGal(Context context, long accountId, String filter) {
        Account acct = SyncManager.getAccountById(accountId);
        if (acct != null) {
            HostAuth ha = HostAuth.restoreHostAuthWithId(context, acct.mHostAuthKeyRecv);
            EasSyncService svc = new EasSyncService("%GalLookupk%");
            try {
                svc.mContext = context;
                svc.mHostAddress = ha.mAddress;
                svc.mUserName = ha.mLogin;
                svc.mPassword = ha.mPassword;
                svc.mSsl = (ha.mFlags & HostAuth.FLAG_SSL) != 0;
                svc.mTrustSsl = (ha.mFlags & HostAuth.FLAG_TRUST_ALL_CERTIFICATES) != 0;
                svc.mDeviceId = SyncManager.getDeviceId();
                svc.mAccount = acct;
                Serializer s = new Serializer();
                s.start(Tags.SEARCH_SEARCH).start(Tags.SEARCH_STORE);
                s.data(Tags.SEARCH_NAME, "GAL").data(Tags.SEARCH_QUERY, filter);
                s.start(Tags.SEARCH_OPTIONS);
                s.data(Tags.SEARCH_RANGE, "0-19");  
                s.end().end().end().done();
                if (DEBUG_GAL_SERVICE) svc.userLog("GAL lookup starting for " + ha.mAddress);
                HttpResponse resp = svc.sendHttpClientPost("Search", s.toByteArray());
                int code = resp.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_OK) {
                    InputStream is = resp.getEntity().getContent();
                    GalParser gp = new GalParser(is, svc);
                    if (gp.parse()) {
                        if (DEBUG_GAL_SERVICE) svc.userLog("GAL lookup OK for " + ha.mAddress);
                        return gp.getGalResult();
                    } else {
                        if (DEBUG_GAL_SERVICE) svc.userLog("GAL lookup returned no matches");
                    }
                } else {
                    svc.userLog("GAL lookup returned " + code);
                }
            } catch (IOException e) {
                svc.userLog("GAL lookup exception " + e);
            }
        }
        return null;
    }
    private void doStatusCallback(long messageId, long attachmentId, int status) {
        try {
            SyncManager.callback().loadAttachmentStatus(messageId, attachmentId, status, 0);
        } catch (RemoteException e) {
        }
    }
    private void doProgressCallback(long messageId, long attachmentId, int progress) {
        try {
            SyncManager.callback().loadAttachmentStatus(messageId, attachmentId,
                    EmailServiceStatus.IN_PROGRESS, progress);
        } catch (RemoteException e) {
        }
    }
    public File createUniqueFileInternal(String dir, String filename) {
        File directory;
        if (dir == null) {
            directory = mContext.getFilesDir();
        } else {
            directory = new File(dir);
        }
        if (!directory.exists()) {
            directory.mkdirs();
        }
        File file = new File(directory, filename);
        if (!file.exists()) {
            return file;
        }
        int index = filename.lastIndexOf('.');
        String name = filename;
        String extension = "";
        if (index != -1) {
            name = filename.substring(0, index);
            extension = filename.substring(index);
        }
        for (int i = 2; i < Integer.MAX_VALUE; i++) {
            file = new File(directory, name + '-' + i + extension);
            if (!file.exists()) {
                return file;
            }
        }
        return null;
    }
    protected void getAttachment(PartRequest req) throws IOException {
        Attachment att = req.mAttachment;
        Message msg = Message.restoreMessageWithId(mContext, att.mMessageKey);
        doProgressCallback(msg.mId, att.mId, 0);
        String cmd = "GetAttachment&AttachmentName=" + att.mLocation;
        HttpResponse res = sendHttpClientPost(cmd, null, COMMAND_TIMEOUT);
        int status = res.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK) {
            HttpEntity e = res.getEntity();
            int len = (int)e.getContentLength();
            InputStream is = res.getEntity().getContent();
            File f = (req.mDestination != null)
                    ? new File(req.mDestination)
                    : createUniqueFileInternal(req.mDestination, att.mFileName);
            if (f != null) {
                File destDir = f.getParentFile();
                if (!destDir.exists()) {
                    destDir.mkdirs();
                }
                FileOutputStream os = new FileOutputStream(f);
                if (len != 0) {
                    try {
                        mPendingRequest = req;
                        byte[] bytes = new byte[CHUNK_SIZE];
                        int length = len;
                        int totalRead = 0;
                        userLog("Attachment content-length: ", len);
                        while (true) {
                            int read = is.read(bytes, 0, CHUNK_SIZE);
                            if (read < 0) {
                                userLog("Attachment load reached EOF, totalRead: ", totalRead);
                                break;
                            }
                            totalRead += read;
                            os.write(bytes, 0, read);
                            if (length > 0) {
                                if (totalRead > length) {
                                    errorLog("totalRead is greater than attachment length?");
                                    break;
                                }
                                int pct = (totalRead * 100) / length;
                                doProgressCallback(msg.mId, att.mId, pct);
                            }
                       }
                    } finally {
                        mPendingRequest = null;
                    }
                }
                os.flush();
                os.close();
                if (att.isSaved()) {
                    String contentUriString = (req.mContentUriString != null)
                            ? req.mContentUriString
                            : "file:
                    ContentValues cv = new ContentValues();
                    cv.put(AttachmentColumns.CONTENT_URI, contentUriString);
                    att.update(mContext, cv);
                    doStatusCallback(msg.mId, att.mId, EmailServiceStatus.SUCCESS);
                }
            }
        } else {
            doStatusCallback(msg.mId, att.mId, EmailServiceStatus.MESSAGE_NOT_FOUND);
        }
    }
    private void sendMeetingResponseMail(Message msg, int response) {
        PackedString meetingInfo = new PackedString(msg.mMeetingInfo);
        if (meetingInfo == null) return;
        Address[] addrs = Address.parse(meetingInfo.get(MeetingInfo.MEETING_ORGANIZER_EMAIL));
        if (addrs.length != 1) return;
        String organizerEmail = addrs[0].getAddress();
        String dtStamp = meetingInfo.get(MeetingInfo.MEETING_DTSTAMP);
        String dtStart = meetingInfo.get(MeetingInfo.MEETING_DTSTART);
        String dtEnd = meetingInfo.get(MeetingInfo.MEETING_DTEND);
        ContentValues entityValues = new ContentValues();
        Entity entity = new Entity(entityValues);
        entityValues.put("DTSTAMP",
                CalendarUtilities.convertEmailDateTimeToCalendarDateTime(dtStamp));
        entityValues.put(Events.DTSTART, Utility.parseEmailDateTimeToMillis(dtStart));
        entityValues.put(Events.DTEND, Utility.parseEmailDateTimeToMillis(dtEnd));
        entityValues.put(Events.EVENT_LOCATION, meetingInfo.get(MeetingInfo.MEETING_LOCATION));
        entityValues.put(Events.TITLE, meetingInfo.get(MeetingInfo.MEETING_TITLE));
        entityValues.put(Events.ORGANIZER, organizerEmail);
        ContentValues attendeeValues = new ContentValues();
        attendeeValues.put(Attendees.ATTENDEE_RELATIONSHIP,
                Attendees.RELATIONSHIP_ATTENDEE);
        attendeeValues.put(Attendees.ATTENDEE_EMAIL, mAccount.mEmailAddress);
        entity.addSubValue(Attendees.CONTENT_URI, attendeeValues);
        ContentValues organizerValues = new ContentValues();
        organizerValues.put(Attendees.ATTENDEE_RELATIONSHIP,
                Attendees.RELATIONSHIP_ORGANIZER);
        organizerValues.put(Attendees.ATTENDEE_EMAIL, organizerEmail);
        entity.addSubValue(Attendees.CONTENT_URI, organizerValues);
        int flag;
        switch(response) {
            case EmailServiceConstants.MEETING_REQUEST_ACCEPTED:
                flag = Message.FLAG_OUTGOING_MEETING_ACCEPT;
                break;
            case EmailServiceConstants.MEETING_REQUEST_DECLINED:
                flag = Message.FLAG_OUTGOING_MEETING_DECLINE;
                break;
            case EmailServiceConstants.MEETING_REQUEST_TENTATIVE:
            default:
                flag = Message.FLAG_OUTGOING_MEETING_TENTATIVE;
                break;
        }
        Message outgoingMsg =
            CalendarUtilities.createMessageForEntity(mContext, entity, flag,
                    meetingInfo.get(MeetingInfo.MEETING_UID), mAccount);
        if (outgoingMsg != null) {
            EasOutboxService.sendMessage(mContext, mAccount.mId, outgoingMsg);
        }
    }
    protected void sendMeetingResponse(MeetingResponseRequest req) throws IOException {
        Message msg = Message.restoreMessageWithId(mContext, req.mMessageId);
        if (msg == null) return;
        Mailbox mailbox = Mailbox.restoreMailboxWithId(mContext, msg.mMailboxKey);
        if (mailbox == null) return;
        Serializer s = new Serializer();
        s.start(Tags.MREQ_MEETING_RESPONSE).start(Tags.MREQ_REQUEST);
        s.data(Tags.MREQ_USER_RESPONSE, Integer.toString(req.mResponse));
        s.data(Tags.MREQ_COLLECTION_ID, mailbox.mServerId);
        s.data(Tags.MREQ_REQ_ID, msg.mServerId);
        s.end().end().done();
        HttpResponse res = sendHttpClientPost("MeetingResponse", s.toByteArray());
        int status = res.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_OK) {
            HttpEntity e = res.getEntity();
            int len = (int)e.getContentLength();
            InputStream is = res.getEntity().getContent();
            if (len != 0) {
                new MeetingResponseParser(is, this).parse();
                sendMeetingResponseMail(msg, req.mResponse);
            }
        } else if (isAuthError(status)) {
            throw new EasAuthenticationException();
        } else {
            userLog("Meeting response request failed, code: " + status);
            throw new IOException();
        }
    }
    @SuppressWarnings("deprecation")
    private void cacheAuthAndCmdString() {
        String safeUserName = URLEncoder.encode(mUserName);
        String cs = mUserName + ':' + mPassword;
        mAuthString = "Basic " + Base64.encodeToString(cs.getBytes(), Base64.NO_WRAP);
        mCmdString = "&User=" + safeUserName + "&DeviceId=" + mDeviceId +
            "&DeviceType=" + mDeviceType;
    }
    private String makeUriString(String cmd, String extra) throws IOException {
        if (mAuthString == null || mCmdString == null) {
            cacheAuthAndCmdString();
        }
        String us = (mSsl ? (mTrustSsl ? "httpts" : "https") : "http") + ":
            "/Microsoft-Server-ActiveSync";
        if (cmd != null) {
            us += "?Cmd=" + cmd + mCmdString;
        }
        if (extra != null) {
            us += extra;
        }
        return us;
    }
     void setHeaders(HttpRequestBase method, boolean usePolicyKey) {
        method.setHeader("Authorization", mAuthString);
        method.setHeader("MS-ASProtocolVersion", mProtocolVersion);
        method.setHeader("Connection", "keep-alive");
        method.setHeader("User-Agent", mDeviceType + '/' + Eas.VERSION);
        if (usePolicyKey) {
            String key = "0";
            if (mAccount != null) {
                String accountKey = mAccount.mSecuritySyncKey;
                if (!TextUtils.isEmpty(accountKey)) {
                    key = accountKey;
                }
            }
            method.setHeader("X-MS-PolicyKey", key);
        }
    }
    private ClientConnectionManager getClientConnectionManager() {
        return SyncManager.getClientConnectionManager();
    }
    private HttpClient getHttpClient(int timeout) {
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
        HttpConnectionParams.setSoTimeout(params, timeout);
        HttpConnectionParams.setSocketBufferSize(params, 8192);
        HttpClient client = new DefaultHttpClient(getClientConnectionManager(), params);
        return client;
    }
    protected HttpResponse sendHttpClientPost(String cmd, byte[] bytes) throws IOException {
        return sendHttpClientPost(cmd, new ByteArrayEntity(bytes), COMMAND_TIMEOUT);
    }
    protected HttpResponse sendHttpClientPost(String cmd, HttpEntity entity) throws IOException {
        return sendHttpClientPost(cmd, entity, COMMAND_TIMEOUT);
    }
    protected HttpResponse sendPing(byte[] bytes, int heartbeat) throws IOException {
       Thread.currentThread().setName(mAccount.mDisplayName + ": Ping");
       if (Eas.USER_LOG) {
           userLog("Send ping, timeout: " + heartbeat + "s, high: " + mPingHighWaterMark + 's');
       }
       return sendHttpClientPost(PING_COMMAND, new ByteArrayEntity(bytes), (heartbeat+5)*SECONDS);
    }
    protected HttpResponse executePostWithTimeout(HttpClient client, HttpPost method, int timeout)
            throws IOException {
        return executePostWithTimeout(client, method, timeout, false);
    }
    protected HttpResponse executePostWithTimeout(HttpClient client, HttpPost method, int timeout,
            boolean isPingCommand) throws IOException {
        synchronized(getSynchronizer()) {
            mPendingPost = method;
            long alarmTime = timeout + WATCHDOG_TIMEOUT_ALLOWANCE;
            if (isPingCommand) {
                SyncManager.runAsleep(mMailboxId, alarmTime);
            } else {
                SyncManager.setWatchdogAlarm(mMailboxId, alarmTime);
            }
        }
        try {
            return client.execute(method);
        } finally {
            synchronized(getSynchronizer()) {
                if (isPingCommand) {
                    SyncManager.runAwake(mMailboxId);
                } else {
                    SyncManager.clearWatchdogAlarm(mMailboxId);
                }
                mPendingPost = null;
            }
        }
    }
    protected HttpResponse sendHttpClientPost(String cmd, HttpEntity entity, int timeout)
            throws IOException {
        HttpClient client = getHttpClient(timeout);
        boolean isPingCommand = cmd.equals(PING_COMMAND);
        String extra = null;
        boolean msg = false;
        if (cmd.startsWith("SmartForward&") || cmd.startsWith("SmartReply&")) {
            int cmdLength = cmd.indexOf('&');
            extra = cmd.substring(cmdLength);
            cmd = cmd.substring(0, cmdLength);
            msg = true;
        } else if (cmd.startsWith("SendMail&")) {
            msg = true;
        }
        String us = makeUriString(cmd, extra);
        HttpPost method = new HttpPost(URI.create(us));
        if (msg) {
            method.setHeader("Content-Type", "message/rfc822");
        } else if (entity != null) {
            method.setHeader("Content-Type", "application/vnd.ms-sync.wbxml");
        }
        setHeaders(method, !cmd.equals(PING_COMMAND));
        method.setEntity(entity);
        return executePostWithTimeout(client, method, timeout, isPingCommand);
    }
    protected HttpResponse sendHttpClientOptions() throws IOException {
        HttpClient client = getHttpClient(COMMAND_TIMEOUT);
        String us = makeUriString("OPTIONS", null);
        HttpOptions method = new HttpOptions(URI.create(us));
        setHeaders(method, false);
        return client.execute(method);
    }
    String getTargetCollectionClassFromCursor(Cursor c) {
        int type = c.getInt(Mailbox.CONTENT_TYPE_COLUMN);
        if (type == Mailbox.TYPE_CONTACTS) {
            return "Contacts";
        } else if (type == Mailbox.TYPE_CALENDAR) {
            return "Calendar";
        } else {
            return "Email";
        }
    }
    private boolean tryProvision() throws IOException {
        ProvisionParser pp = canProvision();
        if (pp != null) {
            SecurityPolicy sp = SecurityPolicy.getInstance(mContext);
            PolicySet ps = pp.getPolicySet();
            if (ps.writeAccount(mAccount, null, true, mContext)) {
                sp.updatePolicies(mAccount.mId);
            }
            if (pp.getRemoteWipe()) {
                SyncManager.alwaysLog("!!! Remote wipe request received");
                sp.setAccountHoldFlag(mAccount, true);
                SyncManager.stopNonAccountMailboxSyncsForAccount(mAccount.mId);
                if (!sp.isActiveAdmin()) {
                    SyncManager.alwaysLog("!!! Not device admin; can't wipe");
                    return false;
                }
                try {
                    SyncManager.alwaysLog("!!! Acknowledging remote wipe to server");
                    acknowledgeRemoteWipe(pp.getPolicyKey());
                } catch (Exception e) {
                }
                SyncManager.alwaysLog("!!! Executing remote wipe");
                sp.remoteWipe();
                return false;
            } else if (sp.isActive(ps)) {
                String policyKey = acknowledgeProvision(pp.getPolicyKey(), PROVISION_STATUS_OK);
                if (policyKey != null) {
                    ps.writeAccount(mAccount, policyKey, true, mContext);
                    SyncManager.releaseSecurityHold(mAccount);
                    return true;
                }
            } else {
                sp.policiesRequired(mAccount.mId);
            }
        }
        return false;
    }
    private String getPolicyType() {
        return (mProtocolVersionDouble >=
            Eas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) ? EAS_12_POLICY_TYPE : EAS_2_POLICY_TYPE;
    }
    private ProvisionParser canProvision() throws IOException {
        Serializer s = new Serializer();
        s.start(Tags.PROVISION_PROVISION).start(Tags.PROVISION_POLICIES);
        s.start(Tags.PROVISION_POLICY).data(Tags.PROVISION_POLICY_TYPE, getPolicyType())
            .end().end().end().done();
        HttpResponse resp = sendHttpClientPost("Provision", s.toByteArray());
        int code = resp.getStatusLine().getStatusCode();
        if (code == HttpStatus.SC_OK) {
            InputStream is = resp.getEntity().getContent();
            ProvisionParser pp = new ProvisionParser(is, this);
            if (pp.parse()) {
                if (pp.hasSupportablePolicySet()) {
                    return pp;
                } else {
                    String policyKey = acknowledgeProvision(pp.getPolicyKey(),
                            PROVISION_STATUS_PARTIAL);
                    return (policyKey != null) ? pp : null;
                }
            }
        }
        return null;
    }
    private void acknowledgeRemoteWipe(String tempKey) throws IOException {
        acknowledgeProvisionImpl(tempKey, PROVISION_STATUS_OK, true);
    }
    private String acknowledgeProvision(String tempKey, String result) throws IOException {
        return acknowledgeProvisionImpl(tempKey, result, false);
    }
    private String acknowledgeProvisionImpl(String tempKey, String status,
            boolean remoteWipe) throws IOException {
        Serializer s = new Serializer();
        s.start(Tags.PROVISION_PROVISION).start(Tags.PROVISION_POLICIES);
        s.start(Tags.PROVISION_POLICY);
        s.data(Tags.PROVISION_POLICY_TYPE, getPolicyType());
        s.data(Tags.PROVISION_POLICY_KEY, tempKey);
        s.data(Tags.PROVISION_STATUS, status);
        s.end().end(); 
        if (remoteWipe) {
            s.start(Tags.PROVISION_REMOTE_WIPE);
            s.data(Tags.PROVISION_STATUS, PROVISION_STATUS_OK);
            s.end();
        }
        s.end().done(); 
        HttpResponse resp = sendHttpClientPost("Provision", s.toByteArray());
        int code = resp.getStatusLine().getStatusCode();
        if (code == HttpStatus.SC_OK) {
            InputStream is = resp.getEntity().getContent();
            ProvisionParser pp = new ProvisionParser(is, this);
            if (pp.parse()) {
                return pp.getPolicyKey();
            }
        }
        return null;
    }
    public void runAccountMailbox() throws IOException, EasParserException {
        mExitStatus = EmailServiceStatus.SUCCESS;
        try {
            try {
                SyncManager.callback()
                    .syncMailboxListStatus(mAccount.mId, EmailServiceStatus.IN_PROGRESS, 0);
            } catch (RemoteException e1) {
            }
            if (mAccount.mSyncKey == null) {
                mAccount.mSyncKey = "0";
                userLog("Account syncKey INIT to 0");
                ContentValues cv = new ContentValues();
                cv.put(AccountColumns.SYNC_KEY, mAccount.mSyncKey);
                mAccount.update(mContext, cv);
            }
            boolean firstSync = mAccount.mSyncKey.equals("0");
            if (firstSync) {
                userLog("Initial FolderSync");
            }
            ContentValues cv = new ContentValues();
            cv.put(Mailbox.SYNC_INTERVAL, Mailbox.CHECK_INTERVAL_PUSH);
            if (mContentResolver.update(Mailbox.CONTENT_URI, cv,
                    WHERE_ACCOUNT_AND_SYNC_INTERVAL_PING,
                    new String[] {Long.toString(mAccount.mId)}) > 0) {
                SyncManager.kick("change ping boxes to push");
            }
            if (mAccount.mProtocolVersion == null ||
                    ((System.currentTimeMillis() - mMailbox.mSyncTime) > DAYS)) {
                userLog("Determine EAS protocol version");
                HttpResponse resp = sendHttpClientOptions();
                int code = resp.getStatusLine().getStatusCode();
                userLog("OPTIONS response: ", code);
                if (code == HttpStatus.SC_OK) {
                    Header header = resp.getFirstHeader("MS-ASProtocolCommands");
                    userLog(header.getValue());
                    header = resp.getFirstHeader("ms-asprotocolversions");
                    try {
                        setupProtocolVersion(this, header);
                    } catch (MessagingException e) {
                        throw new IOException();
                    }
                    cv.clear();
                    cv.put(Account.PROTOCOL_VERSION, mProtocolVersion);
                    mAccount.update(mContext, cv);
                    cv.clear();
                    cv.put(Mailbox.SYNC_TIME, System.currentTimeMillis());
                    mMailbox.update(mContext, cv);
                 } else {
                    errorLog("OPTIONS command failed; throwing IOException");
                    throw new IOException();
                }
            }
            if (mAccount.mSyncInterval == Account.CHECK_INTERVAL_PUSH) {
                cv.clear();
                cv.put(Mailbox.SYNC_INTERVAL, Mailbox.CHECK_INTERVAL_PUSH);
                if (mContentResolver.update(Mailbox.CONTENT_URI, cv,
                        SyncManager.WHERE_IN_ACCOUNT_AND_PUSHABLE,
                        new String[] {Long.toString(mAccount.mId)}) > 0) {
                    userLog("Push account; set pushable boxes to push...");
                }
            }
            while (!mStop) {
                userLog("Sending Account syncKey: ", mAccount.mSyncKey);
                Serializer s = new Serializer();
                s.start(Tags.FOLDER_FOLDER_SYNC).start(Tags.FOLDER_SYNC_KEY)
                    .text(mAccount.mSyncKey).end().end().done();
                HttpResponse resp = sendHttpClientPost("FolderSync", s.toByteArray());
                if (mStop) break;
                int code = resp.getStatusLine().getStatusCode();
                if (code == HttpStatus.SC_OK) {
                    HttpEntity entity = resp.getEntity();
                    int len = (int)entity.getContentLength();
                    if (len != 0) {
                        InputStream is = entity.getContent();
                        if (new FolderSyncParser(is, new AccountSyncAdapter(mMailbox, this))
                                .parse()) {
                            continue;
                        }
                    }
                } else if (isProvisionError(code)) {
                    if (!tryProvision()) {
                        mExitStatus = EXIT_SECURITY_FAILURE;
                        return;
                    } else {
                        continue;
                    }
                } else if (isAuthError(code)) {
                    mExitStatus = EXIT_LOGIN_FAILURE;
                    return;
                } else {
                    userLog("FolderSync response error: ", code);
                }
                cv.clear();
                cv.put(Mailbox.SYNC_INTERVAL, Account.CHECK_INTERVAL_PUSH);
                if (mContentResolver.update(Mailbox.CONTENT_URI, cv,
                        WHERE_PUSH_HOLD_NOT_ACCOUNT_MAILBOX,
                        new String[] {Long.toString(mAccount.mId)}) > 0) {
                    userLog("Set push/hold boxes to push...");
                }
                try {
                    SyncManager.callback()
                        .syncMailboxListStatus(mAccount.mId, mExitStatus, 0);
                } catch (RemoteException e1) {
                }
                String key = mAccount.mSecuritySyncKey;
                if (!TextUtils.isEmpty(key)) {
                    PolicySet ps = new PolicySet(mAccount);
                    SecurityPolicy sp = SecurityPolicy.getInstance(mContext);
                    if (!sp.isActive(ps)) {
                        cv.clear();
                        cv.put(AccountColumns.SECURITY_FLAGS, 0);
                        cv.putNull(AccountColumns.SECURITY_SYNC_KEY);
                        long accountId = mAccount.mId;
                        mContentResolver.update(ContentUris.withAppendedId(
                                Account.CONTENT_URI, accountId), cv, null, null);
                        sp.policiesRequired(accountId);
                    }
                }
                String threadName = Thread.currentThread().getName();
                try {
                    runPingLoop();
                } catch (StaleFolderListException e) {
                    userLog("Ping interrupted; folder list requires sync...");
                } catch (IllegalHeartbeatException e) {
                    resetHeartbeats(e.mLegalHeartbeat);
                } finally {
                    Thread.currentThread().setName(threadName);
                }
            }
         } catch (IOException e) {
            try {
                if (!mStop) {
                    SyncManager.callback()
                        .syncMailboxListStatus(mAccount.mId,
                                EmailServiceStatus.CONNECTION_ERROR, 0);
                }
            } catch (RemoteException e1) {
            }
            throw e;
        }
    }
     void resetHeartbeats(int legalHeartbeat) {
        userLog("Resetting min/max heartbeat, legal = " + legalHeartbeat);
        if (legalHeartbeat > mPingHeartbeat) {
            if (mPingMinHeartbeat < legalHeartbeat) {
                mPingMinHeartbeat = legalHeartbeat;
            }
            if (mPingForceHeartbeat < legalHeartbeat) {
                mPingForceHeartbeat = legalHeartbeat;
            }
            if (mPingMinHeartbeat > mPingMaxHeartbeat) {
                mPingMaxHeartbeat = legalHeartbeat;
            }
        } else if (legalHeartbeat < mPingHeartbeat) {
            mPingMaxHeartbeat = legalHeartbeat;
            if (mPingMaxHeartbeat < mPingMinHeartbeat) {
                mPingMinHeartbeat = legalHeartbeat;
            }
        }
        mPingHeartbeat = legalHeartbeat;
        mPingHeartbeatDropped = false;
    }
    private void pushFallback(long mailboxId) {
        Mailbox mailbox = Mailbox.restoreMailboxWithId(mContext, mailboxId);
        if (mailbox == null) {
            return;
        }
        ContentValues cv = new ContentValues();
        int mins = PING_FALLBACK_PIM;
        if (mailbox.mType == Mailbox.TYPE_INBOX) {
            mins = PING_FALLBACK_INBOX;
        }
        cv.put(Mailbox.SYNC_INTERVAL, mins);
        mContentResolver.update(ContentUris.withAppendedId(Mailbox.CONTENT_URI, mailboxId),
                cv, null, null);
        errorLog("*** PING ERROR LOOP: Set " + mailbox.mDisplayName + " to " + mins + " min sync");
        SyncManager.kick("push fallback");
    }
    private boolean isLikelyNatFailure(String message) {
        if (message == null) return false;
        if (message.contains("reset by peer")) {
            return true;
        }
        return false;
    }
    private void runPingLoop() throws IOException, StaleFolderListException,
            IllegalHeartbeatException {
        int pingHeartbeat = mPingHeartbeat;
        userLog("runPingLoop");
        long endTime = System.currentTimeMillis() + (30*MINUTES);
        HashMap<String, Integer> pingErrorMap = new HashMap<String, Integer>();
        ArrayList<String> readyMailboxes = new ArrayList<String>();
        ArrayList<String> notReadyMailboxes = new ArrayList<String>();
        int pingWaitCount = 0;
        while ((System.currentTimeMillis() < endTime) && !mStop) {
            int pushCount = 0;
            int canPushCount = 0;
            int uninitCount = 0;
            Serializer s = new Serializer();
            Cursor c = mContentResolver.query(Mailbox.CONTENT_URI, Mailbox.CONTENT_PROJECTION,
                    MailboxColumns.ACCOUNT_KEY + '=' + mAccount.mId +
                    AND_FREQUENCY_PING_PUSH_AND_NOT_ACCOUNT_MAILBOX, null, null);
            notReadyMailboxes.clear();
            readyMailboxes.clear();
            try {
                while (c.moveToNext()) {
                    pushCount++;
                    long mailboxId = c.getLong(Mailbox.CONTENT_ID_COLUMN);
                    int pingStatus = SyncManager.pingStatus(mailboxId);
                    String mailboxName = c.getString(Mailbox.CONTENT_DISPLAY_NAME_COLUMN);
                    if (pingStatus == SyncManager.PING_STATUS_OK) {
                        String syncKey = c.getString(Mailbox.CONTENT_SYNC_KEY_COLUMN);
                        if ((syncKey == null) || syncKey.equals("0")) {
                            pushCount--;
                            uninitCount++;
                            continue;
                        }
                        if (canPushCount++ == 0) {
                            s.start(Tags.PING_PING)
                                .data(Tags.PING_HEARTBEAT_INTERVAL,
                                        Integer.toString(pingHeartbeat))
                                .start(Tags.PING_FOLDERS);
                        }
                        String folderClass = getTargetCollectionClassFromCursor(c);
                        s.start(Tags.PING_FOLDER)
                            .data(Tags.PING_ID, c.getString(Mailbox.CONTENT_SERVER_ID_COLUMN))
                            .data(Tags.PING_CLASS, folderClass)
                            .end();
                        readyMailboxes.add(mailboxName);
                    } else if ((pingStatus == SyncManager.PING_STATUS_RUNNING) ||
                            (pingStatus == SyncManager.PING_STATUS_WAITING)) {
                        notReadyMailboxes.add(mailboxName);
                    } else if (pingStatus == SyncManager.PING_STATUS_UNABLE) {
                        pushCount--;
                        userLog(mailboxName, " in error state; ignore");
                        continue;
                    }
                }
            } finally {
                c.close();
            }
            if (Eas.USER_LOG) {
                if (!notReadyMailboxes.isEmpty()) {
                    userLog("Ping not ready for: " + notReadyMailboxes);
                }
                if (!readyMailboxes.isEmpty()) {
                    userLog("Ping ready for: " + readyMailboxes);
                }
            }
            boolean forcePing = !notReadyMailboxes.isEmpty() && (pingWaitCount > 5);
            if ((canPushCount > 0) && ((canPushCount == pushCount) || forcePing)) {
                s.end().end().done();
                pingWaitCount = 0;
                mPostReset = false;
                mPostAborted = false;
                if (mStop) return;
                long pingTime = SystemClock.elapsedRealtime();
                try {
                    if (forcePing) {
                        userLog("Forcing ping after waiting for all boxes to be ready");
                    }
                    HttpResponse res =
                        sendPing(s.toByteArray(), forcePing ? mPingForceHeartbeat : pingHeartbeat);
                    int code = res.getStatusLine().getStatusCode();
                    userLog("Ping response: ", code);
                    if (mStop) {
                        userLog("Stopping pingLoop");
                        return;
                    }
                    if (code == HttpStatus.SC_OK) {
                        SyncManager.removeFromSyncErrorMap(mMailboxId);
                        HttpEntity e = res.getEntity();
                        int len = (int)e.getContentLength();
                        InputStream is = res.getEntity().getContent();
                        if (len != 0) {
                            int pingResult = parsePingResult(is, mContentResolver, pingErrorMap);
                            if (pingResult == PROTOCOL_PING_STATUS_COMPLETED && !forcePing) {
                                if (pingHeartbeat > mPingHighWaterMark) {
                                    mPingHighWaterMark = pingHeartbeat;
                                    userLog("Setting high water mark at: ", mPingHighWaterMark);
                                }
                                if ((pingHeartbeat < mPingMaxHeartbeat) &&
                                        !mPingHeartbeatDropped) {
                                    pingHeartbeat += PING_HEARTBEAT_INCREMENT;
                                    if (pingHeartbeat > mPingMaxHeartbeat) {
                                        pingHeartbeat = mPingMaxHeartbeat;
                                    }
                                    userLog("Increasing ping heartbeat to ", pingHeartbeat, "s");
                                }
                            }
                        } else {
                            userLog("Ping returned empty result; throwing IOException");
                            throw new IOException();
                        }
                    } else if (isAuthError(code)) {
                        mExitStatus = EXIT_LOGIN_FAILURE;
                        userLog("Authorization error during Ping: ", code);
                        throw new IOException();
                    }
                } catch (IOException e) {
                    String message = e.getMessage();
                    boolean hasMessage = message != null;
                    userLog("IOException runPingLoop: " + (hasMessage ? message : "[no message]"));
                    if (mPostReset) {
                    } else if (mPostAborted || isLikelyNatFailure(message)) {
                        long pingLength = SystemClock.elapsedRealtime() - pingTime;
                        if ((pingHeartbeat > mPingMinHeartbeat) &&
                                (pingHeartbeat > mPingHighWaterMark)) {
                            pingHeartbeat -= PING_HEARTBEAT_INCREMENT;
                            mPingHeartbeatDropped = true;
                            if (pingHeartbeat < mPingMinHeartbeat) {
                                pingHeartbeat = mPingMinHeartbeat;
                            }
                            userLog("Decreased ping heartbeat to ", pingHeartbeat, "s");
                        } else if (mPostAborted) {
                            userLog("Ping aborted; retry");
                        } else if (pingLength < 2000) {
                            userLog("Abort or NAT type return < 2 seconds; throwing IOException");
                            throw e;
                        } else {
                            userLog("NAT type IOException");
                        }
                    } else if (hasMessage && message.contains("roken pipe")) {
                    } else {
                        throw e;
                    }
                }
            } else if (forcePing) {
                userLog("pingLoop waiting 60s for any pingable boxes");
                sleep(60*SECONDS, true);
            } else if (pushCount > 0) {
                sleep(2*SECONDS, false);
                pingWaitCount++;
            } else if (uninitCount > 0) {
                userLog("pingLoop waiting for initial sync of ", uninitCount, " box(es)");
                sleep(10*SECONDS, true);
            } else {
                userLog(ACCOUNT_MAILBOX_SLEEP_TEXT);
                sleep(ACCOUNT_MAILBOX_SLEEP_TIME, true);
            }
        }
        mPingHeartbeat = pingHeartbeat;
    }
    private void sleep(long ms, boolean runAsleep) {
        if (runAsleep) {
            SyncManager.runAsleep(mMailboxId, ms+(5*SECONDS));
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
        } finally {
            if (runAsleep) {
                SyncManager.runAwake(mMailboxId);
            }
        }
    }
    private int parsePingResult(InputStream is, ContentResolver cr,
            HashMap<String, Integer> errorMap)
            throws IOException, StaleFolderListException, IllegalHeartbeatException {
        PingParser pp = new PingParser(is, this);
        if (pp.parse()) {
            mBindArguments[0] = Long.toString(mAccount.mId);
            mPingChangeList = pp.getSyncList();
            for (String serverId: mPingChangeList) {
                mBindArguments[1] = serverId;
                Cursor c = cr.query(Mailbox.CONTENT_URI, Mailbox.CONTENT_PROJECTION,
                        WHERE_ACCOUNT_KEY_AND_SERVER_ID, mBindArguments, null);
                try {
                    if (c.moveToFirst()) {
                        String status = c.getString(Mailbox.CONTENT_SYNC_STATUS_COLUMN);
                        int type = SyncManager.getStatusType(status);
                        if (type == SyncManager.SYNC_PING) {
                            int changeCount = SyncManager.getStatusChangeCount(status);
                            if (changeCount > 0) {
                                errorMap.remove(serverId);
                            } else if (changeCount == 0) {
                                String name = c.getString(Mailbox.CONTENT_DISPLAY_NAME_COLUMN);
                                Integer failures = errorMap.get(serverId);
                                if (failures == null) {
                                    userLog("Last ping reported changes in error for: ", name);
                                    errorMap.put(serverId, 1);
                                } else if (failures > MAX_PING_FAILURES) {
                                    pushFallback(c.getLong(Mailbox.CONTENT_ID_COLUMN));
                                    continue;
                                } else {
                                    userLog("Last ping reported changes in error for: ", name);
                                    errorMap.put(serverId, failures + 1);
                                }
                            }
                        }
                        SyncManager.startManualSync(c.getLong(Mailbox.CONTENT_ID_COLUMN),
                                SyncManager.SYNC_PING, null);
                    }
                } finally {
                    c.close();
                }
            }
        }
        return pp.getSyncStatus();
    }
    private String getEmailFilter() {
        String filter = Eas.FILTER_1_WEEK;
        switch (mAccount.mSyncLookback) {
            case com.android.email.Account.SYNC_WINDOW_1_DAY: {
                filter = Eas.FILTER_1_DAY;
                break;
            }
            case com.android.email.Account.SYNC_WINDOW_3_DAYS: {
                filter = Eas.FILTER_3_DAYS;
                break;
            }
            case com.android.email.Account.SYNC_WINDOW_1_WEEK: {
                filter = Eas.FILTER_1_WEEK;
                break;
            }
            case com.android.email.Account.SYNC_WINDOW_2_WEEKS: {
                filter = Eas.FILTER_2_WEEKS;
                break;
            }
            case com.android.email.Account.SYNC_WINDOW_1_MONTH: {
                filter = Eas.FILTER_1_MONTH;
                break;
            }
            case com.android.email.Account.SYNC_WINDOW_ALL: {
                filter = Eas.FILTER_ALL;
                break;
            }
        }
        return filter;
    }
    public void sync(AbstractSyncAdapter target) throws IOException {
        Mailbox mailbox = target.mMailbox;
        boolean moreAvailable = true;
        int loopingCount = 0;
        while (!mStop && moreAvailable) {
            if (!hasConnectivity()) {
                userLog("No connectivity in sync; finishing sync");
                mExitStatus = EXIT_DONE;
                return;
            }
            if (!target.isSyncable()) {
                mExitStatus = EXIT_DONE;
                return;
            }
            while (true) {
                Request req = null;
                synchronized (mRequests) {
                    if (mRequests.isEmpty()) {
                        break;
                    } else {
                        req = mRequests.get(0);
                    }
                }
                if (req instanceof PartRequest) {
                    getAttachment((PartRequest)req);
                } else if (req instanceof MeetingResponseRequest) {
                    sendMeetingResponse((MeetingResponseRequest)req);
                }
                synchronized(mRequests) {
                    mRequests.remove(req);
                }
            }
            Serializer s = new Serializer();
            String className = target.getCollectionName();
            String syncKey = target.getSyncKey();
            userLog("sync, sending ", className, " syncKey: ", syncKey);
            s.start(Tags.SYNC_SYNC)
                .start(Tags.SYNC_COLLECTIONS)
                .start(Tags.SYNC_COLLECTION)
                .data(Tags.SYNC_CLASS, className)
                .data(Tags.SYNC_SYNC_KEY, syncKey)
                .data(Tags.SYNC_COLLECTION_ID, mailbox.mServerId);
            int timeout = COMMAND_TIMEOUT;
            if (!syncKey.equals("0")) {
                s.tag(Tags.SYNC_DELETES_AS_MOVES);
                s.tag(Tags.SYNC_GET_CHANGES);
                s.data(Tags.SYNC_WINDOW_SIZE,
                        className.equals("Email") ? EMAIL_WINDOW_SIZE : PIM_WINDOW_SIZE);
                s.start(Tags.SYNC_OPTIONS);
                if (className.equals("Email")) {
                    s.data(Tags.SYNC_FILTER_TYPE, getEmailFilter());
                } else if (className.equals("Calendar")) {
                    s.data(Tags.SYNC_FILTER_TYPE, Eas.FILTER_2_WEEKS);
                }
                if (mProtocolVersionDouble >= Eas.SUPPORTED_PROTOCOL_EX2007_DOUBLE) {
                    s.start(Tags.BASE_BODY_PREFERENCE)
                    .data(Tags.BASE_TYPE, (className.equals("Email") ? Eas.BODY_PREFERENCE_HTML
                            : Eas.BODY_PREFERENCE_TEXT))
                            .data(Tags.BASE_TRUNCATION_SIZE, Eas.EAS12_TRUNCATION_SIZE)
                            .end();
                } else {
                    s.data(Tags.SYNC_TRUNCATION, Eas.EAS2_5_TRUNCATION_SIZE);
                }
                s.end();
            } else {
                timeout = 120*SECONDS;
            }
            target.sendLocalChanges(s);
            s.end().end().end().done();
            HttpResponse resp = sendHttpClientPost("Sync", new ByteArrayEntity(s.toByteArray()),
                    timeout);
            int code = resp.getStatusLine().getStatusCode();
            if (code == HttpStatus.SC_OK) {
                InputStream is = resp.getEntity().getContent();
                if (is != null) {
                    moreAvailable = target.parse(is);
                    if (target.isLooping()) {
                        loopingCount++;
                        userLog("** Looping: " + loopingCount);
                        if (moreAvailable && (loopingCount > MAX_LOOPING_COUNT)) {
                            userLog("** Looping force stopped");
                            moreAvailable = false;
                        }
                    } else {
                        loopingCount = 0;
                    }
                    target.cleanup();
                } else {
                    userLog("Empty input stream in sync command response");
                }
            } else {
                userLog("Sync response error: ", code);
                if (isProvisionError(code)) {
                    mExitStatus = EXIT_SECURITY_FAILURE;
                } else if (isAuthError(code)) {
                    mExitStatus = EXIT_LOGIN_FAILURE;
                } else {
                    mExitStatus = EXIT_IO_ERROR;
                }
                return;
            }
        }
        mExitStatus = EXIT_DONE;
    }
    protected boolean setupService() {
        mAccount = Account.restoreAccountWithId(mContext, mAccount.mId);
        if (mAccount == null) return false;
        mMailbox = Mailbox.restoreMailboxWithId(mContext, mMailbox.mId);
        if (mMailbox == null) return false;
        mThread = Thread.currentThread();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
        TAG = mThread.getName();
        HostAuth ha = HostAuth.restoreHostAuthWithId(mContext, mAccount.mHostAuthKeyRecv);
        if (ha == null) return false;
        mHostAddress = ha.mAddress;
        mUserName = ha.mLogin;
        mPassword = ha.mPassword;
        mProtocolVersion = mAccount.mProtocolVersion;
        if (mProtocolVersion == null) {
            mProtocolVersion = Eas.DEFAULT_PROTOCOL_VERSION;
        }
        mProtocolVersionDouble = Double.parseDouble(mProtocolVersion);
        return true;
    }
    public void run() {
        if (!setupService()) return;
        try {
            SyncManager.callback().syncMailboxStatus(mMailboxId, EmailServiceStatus.IN_PROGRESS, 0);
        } catch (RemoteException e1) {
        }
        try {
            mDeviceId = SyncManager.getDeviceId();
            if ((mMailbox == null) || (mAccount == null)) {
                return;
            } else if (mMailbox.mType == Mailbox.TYPE_EAS_ACCOUNT_MAILBOX) {
                runAccountMailbox();
            } else {
                AbstractSyncAdapter target;
                if (mMailbox.mType == Mailbox.TYPE_CONTACTS) {
                    target = new ContactsSyncAdapter(mMailbox, this);
                } else if (mMailbox.mType == Mailbox.TYPE_CALENDAR) {
                    target = new CalendarSyncAdapter(mMailbox, this);
                } else {
                    target = new EmailSyncAdapter(mMailbox, this);
                }
                do {
                    if (mRequestTime != 0) {
                        userLog("Looping for user request...");
                        mRequestTime = 0;
                    }
                    sync(target);
                } while (mRequestTime != 0);
            }
        } catch (EasAuthenticationException e) {
            userLog("Caught authentication error");
            mExitStatus = EXIT_LOGIN_FAILURE;
        } catch (IOException e) {
            String message = e.getMessage();
            userLog("Caught IOException: ", (message == null) ? "No message" : message);
            mExitStatus = EXIT_IO_ERROR;
        } catch (Exception e) {
            userLog("Uncaught exception in EasSyncService", e);
        } finally {
            int status;
            if (!mStop) {
                userLog("Sync finished");
                SyncManager.done(this);
                switch (mExitStatus) {
                    case EXIT_IO_ERROR:
                        status = EmailServiceStatus.CONNECTION_ERROR;
                        break;
                    case EXIT_DONE:
                        status = EmailServiceStatus.SUCCESS;
                        ContentValues cv = new ContentValues();
                        cv.put(Mailbox.SYNC_TIME, System.currentTimeMillis());
                        String s = "S" + mSyncReason + ':' + status + ':' + mChangeCount;
                        cv.put(Mailbox.SYNC_STATUS, s);
                        mContentResolver.update(ContentUris.withAppendedId(Mailbox.CONTENT_URI,
                                mMailboxId), cv, null, null);
                        break;
                    case EXIT_LOGIN_FAILURE:
                        status = EmailServiceStatus.LOGIN_FAILED;
                        break;
                    case EXIT_SECURITY_FAILURE:
                        status = EmailServiceStatus.SECURITY_FAILURE;
                        SyncManager.reloadFolderList(mContext, mAccount.mId, true);
                        break;
                    default:
                        status = EmailServiceStatus.REMOTE_EXCEPTION;
                        errorLog("Sync ended due to an exception.");
                        break;
                }
            } else {
                userLog("Stopped sync finished.");
                status = EmailServiceStatus.SUCCESS;
            }
            try {
                SyncManager.callback().syncMailboxStatus(mMailboxId, status, 0);
            } catch (RemoteException e1) {
            }
            SyncManager.kick("sync finished");
       }
    }
}
