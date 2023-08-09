public class ImapStore extends Store {
    private static final boolean DEBUG_FORCE_SEND_ID = false;
    private static final Flag[] PERMANENT_FLAGS = { Flag.DELETED, Flag.SEEN, Flag.FLAGGED };
    private Context mContext;
    private Transport mRootTransport;
    private String mUsername;
    private String mPassword;
    private String mLoginPhrase;
    private String mPathPrefix;
    private String mIdPhrase = null;
    private static String sImapId = null;
    private LinkedList<ImapConnection> mConnections =
            new LinkedList<ImapConnection>();
    private Charset mModifiedUtf7Charset;
    private HashMap<String, ImapFolder> mFolderCache = new HashMap<String, ImapFolder>();
    public static Store newInstance(String uri, Context context, PersistentDataCallbacks callbacks)
            throws MessagingException {
        return new ImapStore(context, uri);
    }
    private ImapStore(Context context, String uriString) throws MessagingException {
        mContext = context;
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException use) {
            throw new MessagingException("Invalid ImapStore URI", use);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.startsWith(STORE_SCHEME_IMAP)) {
            throw new MessagingException("Unsupported protocol");
        }
        int connectionSecurity = Transport.CONNECTION_SECURITY_NONE;
        int defaultPort = 143;
        if (scheme.contains("+ssl")) {
            connectionSecurity = Transport.CONNECTION_SECURITY_SSL;
            defaultPort = 993;
        } else if (scheme.contains("+tls")) {
            connectionSecurity = Transport.CONNECTION_SECURITY_TLS;
        }
        boolean trustCertificates = scheme.contains(STORE_SECURITY_TRUST_CERTIFICATES);
        mRootTransport = new MailTransport("IMAP");
        mRootTransport.setUri(uri, defaultPort);
        mRootTransport.setSecurity(connectionSecurity, trustCertificates);
        String[] userInfoParts = mRootTransport.getUserInfoParts();
        if (userInfoParts != null) {
            mUsername = userInfoParts[0];
            if (userInfoParts.length > 1) {
                mPassword = userInfoParts[1];
                mLoginPhrase = "LOGIN " + mUsername + " " + Utility.imapQuoted(mPassword);
            }
        }
        if ((uri.getPath() != null) && (uri.getPath().length() > 0)) {
            mPathPrefix = uri.getPath().substring(1);
        }
        mModifiedUtf7Charset = new CharsetProvider().charsetForName("X-RFC-3501");
    }
     void setTransport(Transport testTransport) {
        mRootTransport = testTransport;
    }
    public String getImapId(Context context, String userName, String host, String capability) {
        synchronized (ImapStore.class) {
            if (sImapId == null) {
                TelephonyManager tm =
                        (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                String networkOperator = tm.getNetworkOperatorName();
                if (networkOperator == null) networkOperator = "";
                sImapId = makeCommonImapId(context.getPackageName(), Build.VERSION.RELEASE,
                        Build.VERSION.CODENAME, Build.MODEL, Build.ID, Build.MANUFACTURER,
                        networkOperator);
            }
        }
        StringBuilder id = new StringBuilder(sImapId);
        String vendorId =
            VendorPolicyLoader.getInstance(context).getImapIdValues(userName, host, capability);
        if (vendorId != null) {
            id.append(' ');
            id.append(vendorId);
        }
        try {
            String devUID = Preferences.getPreferences(context).getDeviceUID();
            MessageDigest messageDigest;
            messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(userName.getBytes());
            messageDigest.update(devUID.getBytes());
            byte[] uid = messageDigest.digest();
            String hexUid = Base64.encodeToString(uid, Base64.NO_WRAP);
            id.append(" \"AGUID\" \"");
            id.append(hexUid);
            id.append('\"');
        } catch (NoSuchAlgorithmException e) {
            Log.d(Email.LOG_TAG, "couldn't obtain SHA-1 hash for device UID");
        }
        return id.toString();
    }
     String makeCommonImapId(String packageName, String version,
            String codeName, String model, String id, String vendor, String networkOperator) {
        Pattern p = Pattern.compile("[^a-zA-Z0-9-_\\+=;:\\.,/ ]");
        packageName = p.matcher(packageName).replaceAll("");
        version = p.matcher(version).replaceAll("");
        codeName = p.matcher(codeName).replaceAll("");
        model = p.matcher(model).replaceAll("");
        id = p.matcher(id).replaceAll("");
        vendor = p.matcher(vendor).replaceAll("");
        networkOperator = p.matcher(networkOperator).replaceAll("");
        StringBuffer sb = new StringBuffer("\"name\" \"");
        sb.append(packageName);
        sb.append("\"");
        sb.append(" \"os\" \"android\"");
        sb.append(" \"os-version\" \"");
        if (version.length() > 0) {
            sb.append(version);
        } else {
            sb.append("1.0");
        }
        if (id.length() > 0) {
            sb.append("; ");
            sb.append(id);
        }
        sb.append("\"");
        if (vendor.length() > 0) {
            sb.append(" \"vendor\" \"");
            sb.append(vendor);
            sb.append("\"");
        }
        if ("REL".equals(codeName)) {
            if (model.length() > 0) {
                sb.append(" \"x-android-device-model\" \"");
                sb.append(model);
                sb.append("\"");
            }
        }
        if (networkOperator.length() > 0) {
            sb.append(" \"x-android-mobile-net-operator\" \"");
            sb.append(networkOperator);
            sb.append("\"");
        }
        return sb.toString();
    }
    @Override
    public Folder getFolder(String name) throws MessagingException {
        ImapFolder folder;
        synchronized (mFolderCache) {
            folder = mFolderCache.get(name);
            if (folder == null) {
                folder = new ImapFolder(name);
                mFolderCache.put(name, folder);
            }
        }
        return folder;
    }
    @Override
    public Folder[] getPersonalNamespaces() throws MessagingException {
        ImapConnection connection = getConnection();
        try {
            ArrayList<Folder> folders = new ArrayList<Folder>();
            List<ImapResponse> responses =
                    connection.executeSimpleCommand(String.format("LIST \"\" \"%s*\"",
                        mPathPrefix == null ? "" : mPathPrefix));
            for (ImapResponse response : responses) {
                if (response.get(0).equals("LIST")) {
                    boolean includeFolder = true;
                    String folder = decodeFolderName(response.getString(3));
                    if (folder.equalsIgnoreCase("INBOX")) {
                        continue;
                    }
                    ImapList attributes = response.getList(1);
                    for (int i = 0, count = attributes.size(); i < count; i++) {
                        String attribute = attributes.getString(i);
                        if (attribute.equalsIgnoreCase("\\NoSelect")) {
                            includeFolder = false;
                        }
                    }
                    if (includeFolder) {
                        folders.add(getFolder(folder));
                    }
                }
            }
            folders.add(getFolder("INBOX"));
            return folders.toArray(new Folder[] {});
        } catch (IOException ioe) {
            connection.close();
            throw new MessagingException("Unable to get folder list.", ioe);
        } finally {
            releaseConnection(connection);
        }
    }
    @Override
    public void checkSettings() throws MessagingException {
        try {
            ImapConnection connection = new ImapConnection();
            connection.open();
            connection.close();
        }
        catch (IOException ioe) {
            throw new MessagingException(MessagingException.IOERROR, ioe.toString());
        }
    }
    private ImapConnection getConnection() throws MessagingException {
        synchronized (mConnections) {
            ImapConnection connection = null;
            while ((connection = mConnections.poll()) != null) {
                try {
                    connection.executeSimpleCommand("NOOP");
                    break;
                }
                catch (IOException ioe) {
                    connection.close();
                }
            }
            if (connection == null) {
                connection = new ImapConnection();
            }
            return connection;
        }
    }
    private void releaseConnection(ImapConnection connection) {
        mConnections.offer(connection);
    }
    private String encodeFolderName(String name) {
        try {
            ByteBuffer bb = mModifiedUtf7Charset.encode(name);
            byte[] b = new byte[bb.limit()];
            bb.get(b);
            return new String(b, "US-ASCII");
        }
        catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Unabel to encode folder name: " + name, uee);
        }
    }
    private String decodeFolderName(String name) {
        try {
            byte[] encoded = name.getBytes("US-ASCII");
            CharBuffer cb = mModifiedUtf7Charset.decode(ByteBuffer.wrap(encoded));
            return cb.toString();
        }
        catch (UnsupportedEncodingException uee) {
            throw new RuntimeException("Unable to decode folder name: " + name, uee);
        }
    }
    class ImapFolder extends Folder {
        private String mName;
        private int mMessageCount = -1;
        private ImapConnection mConnection;
        private OpenMode mMode;
        private boolean mExists;
        public ImapFolder(String name) {
            this.mName = name;
        }
        public void open(OpenMode mode, PersistentDataCallbacks callbacks)
                throws MessagingException {
            if (isOpen() && mMode == mode) {
                try {
                    mConnection.executeSimpleCommand("NOOP");
                    return;
                }
                catch (IOException ioe) {
                    ioExceptionHandler(mConnection, ioe);
                }
            }
            synchronized (this) {
                mConnection = getConnection();
            }
            try {
                List<ImapResponse> responses = mConnection.executeSimpleCommand(
                        String.format("SELECT \"%s\"",
                                encodeFolderName(mName)));
                mMode = OpenMode.READ_WRITE;
                for (ImapResponse response : responses) {
                    if (response.mTag == null && response.get(1).equals("EXISTS")) {
                        mMessageCount = response.getNumber(0);
                    } else if (response.mTag != null) {
                        ImapList responseList = response.getListOrNull(1);
                        if (responseList != null) {
                            String atom = responseList.getStringOrNull(0);
                            if ("READ-ONLY".equalsIgnoreCase(atom)) {
                                mMode = OpenMode.READ_ONLY;
                            } else if ("READ-WRITE".equalsIgnoreCase(atom)) {
                                mMode = OpenMode.READ_WRITE;
                            }
                        }
                    }
                }
                if (mMessageCount == -1) {
                    throw new MessagingException(
                            "Did not find message count during select");
                }
                mExists = true;
            } catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
        }
        public boolean isOpen() {
            return mConnection != null;
        }
        @Override
        public OpenMode getMode() throws MessagingException {
            return mMode;
        }
        public void close(boolean expunge) {
            if (!isOpen()) {
                return;
            }
            mMessageCount = -1;
            synchronized (this) {
                releaseConnection(mConnection);
                mConnection = null;
            }
        }
        public String getName() {
            return mName;
        }
        public boolean exists() throws MessagingException {
            if (mExists) {
                return true;
            }
            ImapConnection connection = null;
            synchronized(this) {
                if (mConnection == null) {
                    connection = getConnection();
                }
                else {
                    connection = mConnection;
                }
            }
            try {
                connection.executeSimpleCommand(String.format("STATUS \"%s\" (UIDVALIDITY)",
                        encodeFolderName(mName)));
                mExists = true;
                return true;
            }
            catch (MessagingException me) {
                return false;
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(connection, ioe);
            }
            finally {
                if (mConnection == null) {
                    releaseConnection(connection);
                }
            }
        }
        public boolean canCreate(FolderType type) {
            return true;
        }
        public boolean create(FolderType type) throws MessagingException {
            ImapConnection connection = null;
            synchronized(this) {
                if (mConnection == null) {
                    connection = getConnection();
                }
                else {
                    connection = mConnection;
                }
            }
            try {
                connection.executeSimpleCommand(String.format("CREATE \"%s\"",
                        encodeFolderName(mName)));
                return true;
            }
            catch (MessagingException me) {
                return false;
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(connection, ioe);
            }
            finally {
                if (mConnection == null) {
                    releaseConnection(connection);
                }
            }
        }
        @Override
        public void copyMessages(Message[] messages, Folder folder,
                MessageUpdateCallbacks callbacks) throws MessagingException {
            checkOpen();
            String[] uids = new String[messages.length];
            for (int i = 0, count = messages.length; i < count; i++) {
                uids[i] = messages[i].getUid();
            }
            try {
                mConnection.executeSimpleCommand(String.format("UID COPY %s \"%s\"",
                        Utility.combine(uids, ','),
                        encodeFolderName(folder.getName())));
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
        }
        @Override
        public int getMessageCount() {
            return mMessageCount;
        }
        @Override
        public int getUnreadMessageCount() throws MessagingException {
            checkOpen();
            try {
                int unreadMessageCount = 0;
                List<ImapResponse> responses = mConnection.executeSimpleCommand(
                        String.format("STATUS \"%s\" (UNSEEN)",
                                encodeFolderName(mName)));
                for (ImapResponse response : responses) {
                    if (response.mTag == null && response.get(0).equals("STATUS")) {
                        ImapList status = response.getList(2);
                        unreadMessageCount = status.getKeyedNumber("UNSEEN");
                    }
                }
                return unreadMessageCount;
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
        }
        @Override
        public void delete(boolean recurse) throws MessagingException {
            throw new Error("ImapStore.delete() not yet implemented");
        }
        @Override
        public Message getMessage(String uid) throws MessagingException {
            checkOpen();
            try {
                try {
                    List<ImapResponse> responses =
                            mConnection.executeSimpleCommand(String.format("UID SEARCH UID %S", uid));
                    for (ImapResponse response : responses) {
                        if (response.mTag == null && response.get(0).equals("SEARCH")) {
                            for (int i = 1, count = response.size(); i < count; i++) {
                                if (uid.equals(response.get(i))) {
                                    return new ImapMessage(uid, this);
                                }
                            }
                        }
                    }
                }
                catch (MessagingException me) {
                    return null;
                }
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
            return null;
        }
        @Override
        public Message[] getMessages(int start, int end, MessageRetrievalListener listener)
                throws MessagingException {
            if (start < 1 || end < 1 || end < start) {
                throw new MessagingException(
                        String.format("Invalid message set %d %d",
                                start, end));
            }
            checkOpen();
            ArrayList<Message> messages = new ArrayList<Message>();
            try {
                ArrayList<String> uids = new ArrayList<String>();
                List<ImapResponse> responses = mConnection
                        .executeSimpleCommand(String.format("UID SEARCH %d:%d NOT DELETED", start, end));
                for (ImapResponse response : responses) {
                    if (response.get(0).equals("SEARCH")) {
                        for (int i = 1, count = response.size(); i < count; i++) {
                            uids.add(response.getString(i));
                        }
                    }
                }
                for (int i = 0, count = uids.size(); i < count; i++) {
                    if (listener != null) {
                        listener.messageStarted(uids.get(i), i, count);
                    }
                    ImapMessage message = new ImapMessage(uids.get(i), this);
                    messages.add(message);
                    if (listener != null) {
                        listener.messageFinished(message, i, count);
                    }
                }
            } catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
            return messages.toArray(new Message[] {});
        }
        public Message[] getMessages(MessageRetrievalListener listener) throws MessagingException {
            return getMessages(null, listener);
        }
        public Message[] getMessages(String[] uids, MessageRetrievalListener listener)
                throws MessagingException {
            checkOpen();
            ArrayList<Message> messages = new ArrayList<Message>();
            try {
                if (uids == null) {
                    List<ImapResponse> responses = mConnection
                            .executeSimpleCommand("UID SEARCH 1:* NOT DELETED");
                    ArrayList<String> tempUids = new ArrayList<String>();
                    for (ImapResponse response : responses) {
                        if (response.get(0).equals("SEARCH")) {
                            for (int i = 1, count = response.size(); i < count; i++) {
                                tempUids.add(response.getString(i));
                            }
                        }
                    }
                    uids = tempUids.toArray(new String[] {});
                }
                for (int i = 0, count = uids.length; i < count; i++) {
                    if (listener != null) {
                        listener.messageStarted(uids[i], i, count);
                    }
                    ImapMessage message = new ImapMessage(uids[i], this);
                    messages.add(message);
                    if (listener != null) {
                        listener.messageFinished(message, i, count);
                    }
                }
            } catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
            return messages.toArray(new Message[] {});
        }
        @Override
        public void fetch(Message[] messages, FetchProfile fp, MessageRetrievalListener listener)
                throws MessagingException {
            try {
                fetchInternal(messages, fp, listener);
            } catch (RuntimeException e) { 
                Log.w(Email.LOG_TAG, "Exception detected: " + e.getMessage());
                mConnection.logLastDiscourse();
                throw e;
            }
        }
        public void fetchInternal(Message[] messages, FetchProfile fp,
                MessageRetrievalListener listener) throws MessagingException {
            if (messages == null || messages.length == 0) {
                return;
            }
            checkOpen();
            String[] uids = new String[messages.length];
            HashMap<String, Message> messageMap = new HashMap<String, Message>();
            for (int i = 0, count = messages.length; i < count; i++) {
                uids[i] = messages[i].getUid();
                messageMap.put(uids[i], messages[i]);
            }
            LinkedHashSet<String> fetchFields = new LinkedHashSet<String>();
            fetchFields.add("UID");
            if (fp.contains(FetchProfile.Item.FLAGS)) {
                fetchFields.add("FLAGS");
            }
            if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                fetchFields.add("INTERNALDATE");
                fetchFields.add("RFC822.SIZE");
                fetchFields.add("BODY.PEEK[HEADER.FIELDS " +
                        "(date subject from content-type to cc message-id)]");
            }
            if (fp.contains(FetchProfile.Item.STRUCTURE)) {
                fetchFields.add("BODYSTRUCTURE");
            }
            if (fp.contains(FetchProfile.Item.BODY_SANE)) {
                fetchFields.add(String.format("BODY.PEEK[]<0.%d>", FETCH_BODY_SANE_SUGGESTED_SIZE));
            }
            if (fp.contains(FetchProfile.Item.BODY)) {
                fetchFields.add("BODY.PEEK[]");
            }
            for (Object o : fp) {
                if (o instanceof Part) {
                    Part part = (Part) o;
                    String[] partIds =
                        part.getHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA);
                    if (partIds != null) {
                        fetchFields.add("BODY.PEEK[" + partIds[0] + "]");
                    }
                }
            }
            try {
                String tag = mConnection.sendCommand(String.format("UID FETCH %s (%s)",
                        Utility.combine(uids, ','),
                        Utility.combine(fetchFields.toArray(new String[fetchFields.size()]), ' ')
                        ), false);
                ImapResponse response;
                int messageNumber = 0;
                do {
                    response = mConnection.readResponse();
                    if (response.mTag == null && response.get(1).equals("FETCH")) {
                        ImapList fetchList = (ImapList)response.getKeyedValue("FETCH");
                        String uid = fetchList.getKeyedString("UID");
                        if (uid == null) continue;
                        Message message = messageMap.get(uid);
                        if (message == null) continue;
                        if (listener != null) {
                            listener.messageStarted(uid, messageNumber++, messageMap.size());
                        }
                        if (fp.contains(FetchProfile.Item.FLAGS)) {
                            ImapList flags = fetchList.getKeyedList("FLAGS");
                            ImapMessage imapMessage = (ImapMessage) message;
                            if (flags != null) {
                                for (int i = 0, count = flags.size(); i < count; i++) {
                                    String flag = flags.getString(i);
                                    if (flag.equals("\\Deleted")) {
                                        imapMessage.setFlagInternal(Flag.DELETED, true);
                                    }
                                    else if (flag.equals("\\Answered")) {
                                        imapMessage.setFlagInternal(Flag.ANSWERED, true);
                                    }
                                    else if (flag.equals("\\Seen")) {
                                        imapMessage.setFlagInternal(Flag.SEEN, true);
                                    }
                                    else if (flag.equals("\\Flagged")) {
                                        imapMessage.setFlagInternal(Flag.FLAGGED, true);
                                    }
                                }
                            }
                        }
                        if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                            Date internalDate = fetchList.getKeyedDate("INTERNALDATE");
                            int size = fetchList.getKeyedNumber("RFC822.SIZE");
                            InputStream headerStream = fetchList.getLiteral(fetchList.size() - 1);
                            ImapMessage imapMessage = (ImapMessage) message;
                            message.setInternalDate(internalDate);
                            imapMessage.setSize(size);
                            imapMessage.parse(headerStream);
                        }
                        if (fp.contains(FetchProfile.Item.STRUCTURE)) {
                            ImapList bs = fetchList.getKeyedList("BODYSTRUCTURE");
                            if (bs != null) {
                                try {
                                    parseBodyStructure(bs, message, "TEXT");
                                }
                                catch (MessagingException e) {
                                    if (Email.LOGD) {
                                        Log.v(Email.LOG_TAG, "Error handling message", e);
                                    }
                                    message.setBody(null);
                                }
                            }
                        }
                        if (fp.contains(FetchProfile.Item.BODY)) {
                            InputStream bodyStream = fetchList.getLiteral(fetchList.size() - 1);
                            ImapMessage imapMessage = (ImapMessage) message;
                            imapMessage.parse(bodyStream);
                        }
                        if (fp.contains(FetchProfile.Item.BODY_SANE)) {
                            InputStream bodyStream = fetchList.getLiteral(fetchList.size() - 1);
                            ImapMessage imapMessage = (ImapMessage) message;
                            imapMessage.parse(bodyStream);
                        }
                        for (Object o : fp) {
                            if (o instanceof Part) {
                                Part part = (Part) o;
                                if (part.getSize() > 0) {
                                    InputStream bodyStream =
                                        fetchList.getLiteral(fetchList.size() - 1);
                                    String contentType = part.getContentType();
                                    String contentTransferEncoding = part.getHeader(
                                            MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING)[0];
                                    part.setBody(MimeUtility.decodeBody(
                                            bodyStream,
                                            contentTransferEncoding));
                                }
                            }
                        }
                        if (listener != null) {
                            listener.messageFinished(message, messageNumber, messageMap.size());
                        }
                    }
                    while (response.more());
                } while (response.mTag == null);
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
        }
        @Override
        public Flag[] getPermanentFlags() throws MessagingException {
            return PERMANENT_FLAGS;
        }
        private void handleUntaggedResponses(List<ImapResponse> responses) {
            for (ImapResponse response : responses) {
                handleUntaggedResponse(response);
            }
        }
        private void handleUntaggedResponse(ImapResponse response) {
            if (response.mTag == null && response.get(1).equals("EXISTS")) {
                mMessageCount = response.getNumber(0);
            }
        }
        private void parseBodyStructure(ImapList bs, Part part, String id)
                throws MessagingException {
            if (bs.get(0) instanceof ImapList) {
                MimeMultipart mp = new MimeMultipart();
                for (int i = 0, count = bs.size(); i < count; i++) {
                    if (bs.get(i) instanceof ImapList) {
                        ImapBodyPart bp = new ImapBodyPart();
                        if (id.equals("TEXT")) {
                            parseBodyStructure(bs.getList(i), bp, Integer.toString(i + 1));
                        }
                        else {
                            parseBodyStructure(bs.getList(i), bp, id + "." + (i + 1));
                        }
                        mp.addBodyPart(bp);
                    }
                    else {
                        String subType = bs.getString(i);
                        mp.setSubType(subType.toLowerCase());
                        break;
                    }
                }
                part.setBody(mp);
            }
            else{
                String type = bs.getString(0);
                String subType = bs.getString(1);
                String mimeType = (type + "/" + subType).toLowerCase();
                ImapList bodyParams = null;
                if (bs.get(2) instanceof ImapList) {
                    bodyParams = bs.getList(2);
                }
                String cid = bs.getString(3);
                String encoding = bs.getString(5);
                int size = bs.getNumber(6);
                if (MimeUtility.mimeTypeMatches(mimeType, "message/rfc822")) {
                    throw new MessagingException("BODYSTRUCTURE message/rfc822 not yet supported.");
                }
                String contentType = String.format("%s", mimeType);
                if (bodyParams != null) {
                    for (int i = 0, count = bodyParams.size(); i < count; i += 2) {
                        contentType += String.format(";\n %s=\"%s\"",
                                bodyParams.getString(i),
                                bodyParams.getString(i + 1));
                    }
                }
                part.setHeader(MimeHeader.HEADER_CONTENT_TYPE, contentType);
                ImapList bodyDisposition = null;
                if (("text".equalsIgnoreCase(type))
                        && (bs.size() > 8)
                        && (bs.get(9) instanceof ImapList)) {
                    bodyDisposition = bs.getList(9);
                }
                else if (!("text".equalsIgnoreCase(type))
                        && (bs.size() > 7)
                        && (bs.get(8) instanceof ImapList)) {
                    bodyDisposition = bs.getList(8);
                }
                String contentDisposition = "";
                if (bodyDisposition != null && bodyDisposition.size() > 0) {
                    if (!"NIL".equalsIgnoreCase(bodyDisposition.getString(0))) {
                        contentDisposition = bodyDisposition.getString(0).toLowerCase();
                    }
                    if ((bodyDisposition.size() > 1)
                            && (bodyDisposition.get(1) instanceof ImapList)) {
                        ImapList bodyDispositionParams = bodyDisposition.getList(1);
                        for (int i = 0, count = bodyDispositionParams.size(); i < count; i += 2) {
                            contentDisposition += String.format(";\n %s=\"%s\"",
                                    bodyDispositionParams.getString(i).toLowerCase(),
                                    bodyDispositionParams.getString(i + 1));
                        }
                    }
                }
                if (MimeUtility.getHeaderParameter(contentDisposition, "size") == null) {
                    contentDisposition += String.format(";\n size=%d", size);
                }
                part.setHeader(MimeHeader.HEADER_CONTENT_DISPOSITION, contentDisposition);
                part.setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, encoding);
                if (!"NIL".equalsIgnoreCase(cid)) {
                    part.setHeader(MimeHeader.HEADER_CONTENT_ID, cid);
                }
                if (part instanceof ImapMessage) {
                    ((ImapMessage) part).setSize(size);
                }
                else if (part instanceof ImapBodyPart) {
                    ((ImapBodyPart) part).setSize(size);
                }
                else {
                    throw new MessagingException("Unknown part type " + part.toString());
                }
                part.setHeader(MimeHeader.HEADER_ANDROID_ATTACHMENT_STORE_DATA, id);
            }
        }
        public void appendMessages(Message[] messages) throws MessagingException {
            checkOpen();
            try {
                for (Message message : messages) {
                    CountingOutputStream out = new CountingOutputStream();
                    EOLConvertingOutputStream eolOut = new EOLConvertingOutputStream(out);
                    message.writeTo(eolOut);
                    eolOut.flush();
                    String flagList = "";
                    Flag[] flags = message.getFlags();
                    if (flags.length > 0) {
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0, count = flags.length; i < count; i++) {
                            Flag flag = flags[i];
                            if (flag == Flag.SEEN) {
                                sb.append(" \\Seen");
                            } else if (flag == Flag.FLAGGED) {
                                sb.append(" \\Flagged");
                            }
                        }
                        if (sb.length() > 0) {
                            flagList = sb.substring(1);
                        }
                    }
                    mConnection.sendCommand(
                            String.format("APPEND \"%s\" (%s) {%d}",
                                    encodeFolderName(mName),
                                    flagList,
                                    out.getCount()), false);
                    ImapResponse response;
                    do {
                        response = mConnection.readResponse();
                        if (response.mCommandContinuationRequested) {
                            eolOut = new EOLConvertingOutputStream(mConnection.mTransport.getOutputStream());
                            message.writeTo(eolOut);
                            eolOut.write('\r');
                            eolOut.write('\n');
                            eolOut.flush();
                        }
                        else if (response.mTag == null) {
                            handleUntaggedResponse(response);
                        }
                        while (response.more());
                    } while (response.mTag == null);
                    ImapList appendList = response.getListOrNull(1);
                    if (appendList != null && appendList.size() == 3 &&
                            "APPENDUID".equalsIgnoreCase(appendList.getString(0))) {
                        String serverUid = appendList.getString(2);
                        message.setUid(serverUid);
                        continue;
                    }
                    String messageId = message.getMessageId();
                    if (messageId == null || messageId.length() == 0) {
                        continue;
                    }
                    List<ImapResponse> responses =
                        mConnection.executeSimpleCommand(
                                String.format("UID SEARCH (HEADER MESSAGE-ID %s)", messageId));
                    for (ImapResponse response1 : responses) {
                        if (response1.mTag == null && response1.get(0).equals("SEARCH")
                                && response1.size() > 1) {
                            message.setUid(response1.getString(response1.size()-1));
                        }
                    }
                }
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
        }
        public Message[] expunge() throws MessagingException {
            checkOpen();
            try {
                handleUntaggedResponses(mConnection.executeSimpleCommand("EXPUNGE"));
            } catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
            return null;
        }
        @Override
        public void setFlags(Message[] messages, Flag[] flags, boolean value)
                throws MessagingException {
            checkOpen();
            StringBuilder uidList = new StringBuilder();
            for (int i = 0, count = messages.length; i < count; i++) {
                if (i > 0) uidList.append(',');
                uidList.append(messages[i].getUid());
            }
            String allFlags = "";
            if (flags.length > 0) {
                StringBuilder flagList = new StringBuilder();
                for (int i = 0, count = flags.length; i < count; i++) {
                    Flag flag = flags[i];
                    if (flag == Flag.SEEN) {
                        flagList.append(" \\Seen"); 
                    } else if (flag == Flag.DELETED) {
                        flagList.append(" \\Deleted");
                    } else if (flag == Flag.FLAGGED) {
                        flagList.append(" \\Flagged");
                    }
                }
                allFlags = flagList.substring(1);
            }
            try {
                mConnection.executeSimpleCommand(String.format("UID STORE %s %sFLAGS.SILENT (%s)",
                        uidList,
                        value ? "+" : "-",
                        allFlags));
            }
            catch (IOException ioe) {
                throw ioExceptionHandler(mConnection, ioe);
            }
        }
        private void checkOpen() throws MessagingException {
            if (!isOpen()) {
                throw new MessagingException("Folder " + mName + " is not open.");
            }
        }
        private MessagingException ioExceptionHandler(ImapConnection connection, IOException ioe)
                throws MessagingException {
            connection.close();
            close(false);
            return new MessagingException("IO Error", ioe);
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof ImapFolder) {
                return ((ImapFolder)o).mName.equals(mName);
            }
            return super.equals(o);
        }
        @Override
        public Message createMessage(String uid) throws MessagingException {
            return new ImapMessage(uid, this);
        }
    }
    class ImapConnection {
        private static final String IMAP_DEDACTED_LOG = "[IMAP command redacted]";
        private Transport mTransport;
        private ImapResponseParser mParser;
        private int mNextCommandTag;
        private static final int DISCOURSE_LOGGER_SIZE = 64;
        private final DiscourseLogger mDiscourse = new DiscourseLogger(DISCOURSE_LOGGER_SIZE);
        public void open() throws IOException, MessagingException {
            if (mTransport != null && mTransport.isOpen()) {
                return;
            }
            mNextCommandTag = 1;
            try {
                if (mTransport == null) {
                    mTransport = mRootTransport.newInstanceWithConfiguration();
                }
                mTransport.open();
                mTransport.setSoTimeout(MailTransport.SOCKET_READ_TIMEOUT);
                mParser = new ImapResponseParser(mTransport.getInputStream(), mDiscourse);
                mParser.readResponse();
                List<ImapResponse> response = executeSimpleCommand("CAPABILITY");
                if (response.size() != 2) {
                    throw new MessagingException("Invalid CAPABILITY response received");
                }
                String capabilities = response.get(0).toString();
                if (mTransport.canTryTlsSecurity()) {
                    if (capabilities.contains("STARTTLS")) {
                        executeSimpleCommand("STARTTLS");
                        mTransport.reopenTls();
                        mTransport.setSoTimeout(MailTransport.SOCKET_READ_TIMEOUT);
                        mParser = new ImapResponseParser(mTransport.getInputStream(),
                                mDiscourse);
                    } else {
                        if (Config.LOGD && Email.DEBUG) {
                            Log.d(Email.LOG_TAG, "TLS not supported but required");
                        }
                        throw new MessagingException(MessagingException.TLS_REQUIRED);
                    }
                }
                String mUserAgent = getImapId(mContext, mUsername, mRootTransport.getHost(),
                        capabilities);
                if (mUserAgent != null) {
                    mIdPhrase = "ID (" + mUserAgent + ")";
                } else if (DEBUG_FORCE_SEND_ID) {
                    mIdPhrase = "ID NIL";
                }
                if (mIdPhrase != null) {
                    try {
                        executeSimpleCommand(mIdPhrase);
                    } catch (ImapException ie) {
                        if (Config.LOGD && Email.DEBUG) {
                            Log.d(Email.LOG_TAG, ie.toString());
                        }
                    } catch (IOException ioe) {
                    }
                }
                try {
                    executeSimpleCommand(mLoginPhrase, true);
                } catch (ImapException ie) {
                    if (Config.LOGD && Email.DEBUG) {
                        Log.d(Email.LOG_TAG, ie.toString());
                    }
                    throw new AuthenticationFailedException(ie.getAlertText(), ie);
                } catch (MessagingException me) {
                    throw new AuthenticationFailedException(null, me);
                }
            } catch (SSLException e) {
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, e.toString());
                }
                throw new CertificateValidationException(e.getMessage(), e);
            } catch (IOException ioe) {
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, ioe.toString());
                }
                throw ioe;
            }
        }
        public void close() {
            if (mTransport != null) {
                mTransport.close();
            }
        }
        public ImapResponse readResponse() throws IOException, MessagingException {
            return mParser.readResponse();
        }
        public String sendCommand(String command, boolean sensitive)
            throws MessagingException, IOException {
            open();
            String tag = Integer.toString(mNextCommandTag++);
            String commandToSend = tag + " " + command;
            mTransport.writeLine(commandToSend, sensitive ? IMAP_DEDACTED_LOG : null);
            mDiscourse.addSentCommand(sensitive ? IMAP_DEDACTED_LOG : commandToSend);
            return tag;
        }
        public List<ImapResponse> executeSimpleCommand(String command) throws IOException,
                ImapException, MessagingException {
            return executeSimpleCommand(command, false);
        }
        public List<ImapResponse> executeSimpleCommand(String command, boolean sensitive)
                throws IOException, ImapException, MessagingException {
            String tag = sendCommand(command, sensitive);
            ArrayList<ImapResponse> responses = new ArrayList<ImapResponse>();
            ImapResponse response;
            ImapResponse previous = null;
            do {
                if (previous != null && !previous.completed()) {
                    previous.nailDown();
                }
                response = mParser.readResponse();
                if (response.mTag != null && !response.mTag.equals(tag)
                        && previous != null && !previous.completed()) {
                    previous.appendAll(response);
                    response.mTag = null;
                    continue;
                }
                responses.add(response);
                previous = response;
            } while (response.mTag == null);
            if (response.size() < 1 || !response.get(0).equals("OK")) {
                throw new ImapException(response.toString(), response.getAlertText());
            }
            return responses;
        }
        public void logLastDiscourse() {
            mDiscourse.logLastDiscourse();
        }
    }
    class ImapMessage extends MimeMessage {
        ImapMessage(String uid, Folder folder) throws MessagingException {
            this.mUid = uid;
            this.mFolder = folder;
        }
        public void setSize(int size) {
            this.mSize = size;
        }
        @Override
        public void parse(InputStream in) throws IOException, MessagingException {
            super.parse(in);
        }
        public void setFlagInternal(Flag flag, boolean set) throws MessagingException {
            super.setFlag(flag, set);
        }
        @Override
        public void setFlag(Flag flag, boolean set) throws MessagingException {
            super.setFlag(flag, set);
            mFolder.setFlags(new Message[] { this }, new Flag[] { flag }, set);
        }
    }
    class ImapBodyPart extends MimeBodyPart {
        public ImapBodyPart() throws MessagingException {
            super();
        }
    }
    class ImapException extends MessagingException {
        String mAlertText;
        public ImapException(String message, String alertText, Throwable throwable) {
            super(message, throwable);
            this.mAlertText = alertText;
        }
        public ImapException(String message, String alertText) {
            super(message);
            this.mAlertText = alertText;
        }
        public String getAlertText() {
            return mAlertText;
        }
        public void setAlertText(String alertText) {
            mAlertText = alertText;
        }
    }
}
