public class Pop3Store extends Store {
    private static boolean DEBUG_FORCE_SINGLE_LINE_UIDL = false;
    private static boolean DEBUG_LOG_RAW_STREAM = false;
    private static final Flag[] PERMANENT_FLAGS = { Flag.DELETED };
    private Transport mTransport;
    private String mUsername;
    private String mPassword;
    private HashMap<String, Folder> mFolders = new HashMap<String, Folder>();
    public static Store newInstance(String uri, Context context, PersistentDataCallbacks callbacks)
            throws MessagingException {
        return new Pop3Store(uri);
    }
    private Pop3Store(String _uri) throws MessagingException {
        URI uri;
        try {
            uri = new URI(_uri);
        } catch (URISyntaxException use) {
            throw new MessagingException("Invalid Pop3Store URI", use);
        }
        String scheme = uri.getScheme();
        if (scheme == null || !scheme.startsWith(STORE_SCHEME_POP3)) {
            throw new MessagingException("Unsupported protocol");
        }
        int connectionSecurity = Transport.CONNECTION_SECURITY_NONE;
        int defaultPort = 110;
        if (scheme.contains("+ssl")) {
            connectionSecurity = Transport.CONNECTION_SECURITY_SSL;
            defaultPort = 995;
        } else if (scheme.contains("+tls")) {
            connectionSecurity = Transport.CONNECTION_SECURITY_TLS;
        }
        boolean trustCertificates = scheme.contains(STORE_SECURITY_TRUST_CERTIFICATES);
        mTransport = new MailTransport("POP3");
        mTransport.setUri(uri, defaultPort);
        mTransport.setSecurity(connectionSecurity, trustCertificates);
        String[] userInfoParts = mTransport.getUserInfoParts();
        if (userInfoParts != null) {
            mUsername = userInfoParts[0];
            if (userInfoParts.length > 1) {
                mPassword = userInfoParts[1];
            }
        }
    }
     void setTransport(Transport testTransport) {
        mTransport = testTransport;
    }
    @Override
    public Folder getFolder(String name) throws MessagingException {
        Folder folder = mFolders.get(name);
        if (folder == null) {
            folder = new Pop3Folder(name);
            mFolders.put(folder.getName(), folder);
        }
        return folder;
    }
    @Override
    public Folder[] getPersonalNamespaces() throws MessagingException {
        return new Folder[] {
            getFolder("INBOX"),
        };
    }
    @Override
    public void checkSettings() throws MessagingException {
        Pop3Folder folder = new Pop3Folder("INBOX");
        try {
            folder.open(OpenMode.READ_WRITE, null);
            folder.checkSettings();
        } finally {
            folder.close(false);    
        }
    }
    class Pop3Folder extends Folder {
        private HashMap<String, Pop3Message> mUidToMsgMap = new HashMap<String, Pop3Message>();
        private HashMap<Integer, Pop3Message> mMsgNumToMsgMap = new HashMap<Integer, Pop3Message>();
        private HashMap<String, Integer> mUidToMsgNumMap = new HashMap<String, Integer>();
        private String mName;
        private int mMessageCount;
        private Pop3Capabilities mCapabilities;
        public Pop3Folder(String name) {
            this.mName = name;
            if (mName.equalsIgnoreCase("INBOX")) {
                mName = "INBOX";
            }
        }
        public void checkSettings() throws MessagingException {
            if (!mCapabilities.uidl) {
                try {
                    UidlParser parser = new UidlParser();
                    executeSimpleCommand("UIDL");
                    String response;
                    while ((response = mTransport.readLine()) != null) {
                        parser.parseMultiLine(response);
                        if (parser.mEndOfMessage) {
                            break;
                        }
                    }
                } catch (IOException ioe) {
                    mTransport.close();
                    throw new MessagingException(null, ioe);
                }
            }
        }
        @Override
        public synchronized void open(OpenMode mode, PersistentDataCallbacks callbacks)
                throws MessagingException {
            if (mTransport.isOpen()) {
                return;
            }
            if (!mName.equalsIgnoreCase("INBOX")) {
                throw new MessagingException("Folder does not exist");
            }
            try {
                mTransport.open();
                executeSimpleCommand(null);
                mCapabilities = getCapabilities();
                if (mTransport.canTryTlsSecurity()) {
                    if (mCapabilities.stls) {
                        executeSimpleCommand("STLS");
                        mTransport.reopenTls();
                    } else {
                        if (Config.LOGD && Email.DEBUG) {
                            Log.d(Email.LOG_TAG, "TLS not supported but required");
                        }
                        throw new MessagingException(MessagingException.TLS_REQUIRED);
                    }
                }
                try {
                    executeSensitiveCommand("USER " + mUsername, "USER /redacted/");
                    executeSensitiveCommand("PASS " + mPassword, "PASS /redacted/");
                } catch (MessagingException me) {
                    if (Config.LOGD && Email.DEBUG) {
                        Log.d(Email.LOG_TAG, me.toString());
                    }
                    throw new AuthenticationFailedException(null, me);
                }
            } catch (IOException ioe) {
                mTransport.close();
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, ioe.toString());
                }
                throw new MessagingException(MessagingException.IOERROR, ioe.toString());
            }
            Exception statException = null;
            try {
                String response = executeSimpleCommand("STAT");
                String[] parts = response.split(" ");
                if (parts.length < 2) {
                    statException = new IOException();
                } else {
                    mMessageCount = Integer.parseInt(parts[1]);
                }
            } catch (IOException ioe) {
                statException = ioe;
            } catch (NumberFormatException nfe) {
                statException = nfe;
            }
            if (statException != null) {
                mTransport.close();
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, statException.toString());
                }
                throw new MessagingException("POP3 STAT", statException);
            }
            mUidToMsgMap.clear();
            mMsgNumToMsgMap.clear();
            mUidToMsgNumMap.clear();
        }
        @Override
        public OpenMode getMode() throws MessagingException {
            return OpenMode.READ_WRITE;
        }
        @Override
        public void close(boolean expunge) {
            try {
                executeSimpleCommand("QUIT");
            }
            catch (Exception e) {
            }
            mTransport.close();
        }
        @Override
        public String getName() {
            return mName;
        }
        public boolean canCreate(FolderType type) {
            return false;
        }
        @Override
        public boolean create(FolderType type) throws MessagingException {
            return false;
        }
        @Override
        public boolean exists() throws MessagingException {
            return mName.equalsIgnoreCase("INBOX");
        }
        @Override
        public int getMessageCount() {
            return mMessageCount;
        }
        @Override
        public int getUnreadMessageCount() throws MessagingException {
            return -1;
        }
        @Override
        public Message getMessage(String uid) throws MessagingException {
            if (mUidToMsgNumMap.size() == 0) {
                try {
                    indexMsgNums(1, mMessageCount);
                } catch (IOException ioe) {
                    mTransport.close();
                    if (Email.DEBUG) {
                        Log.d(Email.LOG_TAG, "Unable to index during getMessage " + ioe);
                    }
                    throw new MessagingException("getMessages", ioe);
                }
            }
            Pop3Message message = mUidToMsgMap.get(uid);
            return message;
        }
        @Override
        public Message[] getMessages(int start, int end, MessageRetrievalListener listener)
                throws MessagingException {
            if (start < 1 || end < 1 || end < start) {
                throw new MessagingException(String.format("Invalid message set %d %d",
                        start, end));
            }
            try {
                indexMsgNums(start, end);
            } catch (IOException ioe) {
                mTransport.close();
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, ioe.toString());
                }
                throw new MessagingException("getMessages", ioe);
            }
            ArrayList<Message> messages = new ArrayList<Message>();
            int i = 0;
            for (int msgNum = start; msgNum <= end; msgNum++) {
                Pop3Message message = mMsgNumToMsgMap.get(msgNum);
                if (listener != null) {
                    listener.messageStarted(message.getUid(), i++, (end - start) + 1);
                }
                messages.add(message);
                if (listener != null) {
                    listener.messageFinished(message, i++, (end - start) + 1);
                }
            }
            return messages.toArray(new Message[messages.size()]);
        }
        private void indexMsgNums(int start, int end)
                throws MessagingException, IOException {
            int unindexedMessageCount = 0;
            for (int msgNum = start; msgNum <= end; msgNum++) {
                if (mMsgNumToMsgMap.get(msgNum) == null) {
                    unindexedMessageCount++;
                }
            }
            if (unindexedMessageCount == 0) {
                return;
            }
            UidlParser parser = new UidlParser();
            if (DEBUG_FORCE_SINGLE_LINE_UIDL ||
                    (unindexedMessageCount < 50 && mMessageCount > 5000)) {
                for (int msgNum = start; msgNum <= end; msgNum++) {
                    Pop3Message message = mMsgNumToMsgMap.get(msgNum);
                    if (message == null) {
                        String response = executeSimpleCommand("UIDL " + msgNum);
                        if (!parser.parseSingleLine(response)) {
                            throw new IOException();
                        }
                        message = new Pop3Message(parser.mUniqueId, this);
                        indexMessage(msgNum, message);
                    }
                }
            } else {
                String response = executeSimpleCommand("UIDL");
                while ((response = mTransport.readLine()) != null) {
                    if (!parser.parseMultiLine(response)) {
                        throw new IOException();
                    }
                    if (parser.mEndOfMessage) {
                        break;
                    }
                    int msgNum = parser.mMessageNumber;
                    if (msgNum >= start && msgNum <= end) {
                        Pop3Message message = mMsgNumToMsgMap.get(msgNum);
                        if (message == null) {
                            message = new Pop3Message(parser.mUniqueId, this);
                            indexMessage(msgNum, message);
                        }
                    }
                }
            }
        }
        private void indexUids(ArrayList<String> uids)
                throws MessagingException, IOException {
            HashSet<String> unindexedUids = new HashSet<String>();
            for (String uid : uids) {
                if (mUidToMsgMap.get(uid) == null) {
                    unindexedUids.add(uid);
                }
            }
            if (unindexedUids.size() == 0) {
                return;
            }
            UidlParser parser = new UidlParser();
            String response = executeSimpleCommand("UIDL");
            while ((response = mTransport.readLine()) != null) {
                parser.parseMultiLine(response);
                if (parser.mEndOfMessage) {
                    break;
                }
                if (unindexedUids.contains(parser.mUniqueId)) {
                    Pop3Message message = mUidToMsgMap.get(parser.mUniqueId);
                    if (message == null) {
                        message = new Pop3Message(parser.mUniqueId, this);
                    }
                    indexMessage(parser.mMessageNumber, message);
                }
            }
        }
         class UidlParser {
            public int mMessageNumber;
            public String mUniqueId;
            public boolean mEndOfMessage;
            public boolean mErr;
            public UidlParser() {
                mErr = true;
            }
            public boolean parseSingleLine(String response) {
                mErr = false;
                if (response == null || response.length() == 0) {
                    return false;
                }
                char first = response.charAt(0);
                if (first == '+') {
                    String[] uidParts = response.split(" +");
                    if (uidParts.length >= 3) {
                        try {
                            mMessageNumber = Integer.parseInt(uidParts[1]);
                        } catch (NumberFormatException nfe) {
                            return false;
                        }
                        mUniqueId = uidParts[2];
                        mEndOfMessage = true;
                        return true;
                    }
                } else if (first == '-') {
                    mErr = true;
                    return true;
                }
                return false;
            }
            public boolean parseMultiLine(String response) {
                mErr = false;
                if (response == null || response.length() == 0) {
                    return false;
                }
                char first = response.charAt(0);
                if (first == '.') {
                    mEndOfMessage = true;
                    return true;
                } else {
                    String[] uidParts = response.split(" +");
                    if (uidParts.length >= 2) {
                        try {
                            mMessageNumber = Integer.parseInt(uidParts[0]);
                        } catch (NumberFormatException nfe) {
                            return false;
                        }
                        mUniqueId = uidParts[1];
                        mEndOfMessage = false;
                        return true;
                    }
                }
                return false;
            }
        }
        private void indexMessage(int msgNum, Pop3Message message) {
            mMsgNumToMsgMap.put(msgNum, message);
            mUidToMsgMap.put(message.getUid(), message);
            mUidToMsgNumMap.put(message.getUid(), msgNum);
        }
        @Override
        public Message[] getMessages(MessageRetrievalListener listener) throws MessagingException {
            throw new UnsupportedOperationException("Pop3Folder.getMessage(MessageRetrievalListener)");
        }
        @Override
        public Message[] getMessages(String[] uids, MessageRetrievalListener listener)
                throws MessagingException {
            throw new UnsupportedOperationException("Pop3Folder.getMessage(MessageRetrievalListener)");
        }
        public void fetch(Message[] messages, FetchProfile fp, MessageRetrievalListener listener)
                throws MessagingException {
            if (messages == null || messages.length == 0) {
                return;
            }
            ArrayList<String> uids = new ArrayList<String>();
            for (Message message : messages) {
                uids.add(message.getUid());
            }
            try {
                indexUids(uids);
                if (fp.contains(FetchProfile.Item.ENVELOPE)) {
                    fetchEnvelope(messages, null);
                }
            } catch (IOException ioe) {
                mTransport.close();
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, ioe.toString());
                }
                throw new MessagingException("fetch", ioe);
            }
            for (int i = 0, count = messages.length; i < count; i++) {
                Message message = messages[i];
                if (!(message instanceof Pop3Message)) {
                    throw new MessagingException("Pop3Store.fetch called with non-Pop3 Message");
                }
                Pop3Message pop3Message = (Pop3Message)message;
                try {
                    if (listener != null) {
                        listener.messageStarted(pop3Message.getUid(), i, count);
                    }
                    if (fp.contains(FetchProfile.Item.BODY)) {
                        fetchBody(pop3Message, -1);
                    }
                    else if (fp.contains(FetchProfile.Item.BODY_SANE)) {
                        fetchBody(pop3Message,
                                FETCH_BODY_SANE_SUGGESTED_SIZE / 76);
                    }
                    else if (fp.contains(FetchProfile.Item.STRUCTURE)) {
                        pop3Message.setBody(null);
                    }
                    if (listener != null) {
                        listener.messageFinished(message, i, count);
                    }
                } catch (IOException ioe) {
                    mTransport.close();
                    if (Config.LOGD && Email.DEBUG) {
                        Log.d(Email.LOG_TAG, ioe.toString());
                    }
                    throw new MessagingException("Unable to fetch message", ioe);
                }
            }
        }
        private void fetchEnvelope(Message[] messages,
                MessageRetrievalListener listener)  throws IOException, MessagingException {
            int unsizedMessages = 0;
            for (Message message : messages) {
                if (message.getSize() == -1) {
                    unsizedMessages++;
                }
            }
            if (unsizedMessages == 0) {
                return;
            }
            if (unsizedMessages < 50 && mMessageCount > 5000) {
                for (int i = 0, count = messages.length; i < count; i++) {
                    Message message = messages[i];
                    if (!(message instanceof Pop3Message)) {
                        throw new MessagingException("Pop3Store.fetch called with non-Pop3 Message");
                    }
                    Pop3Message pop3Message = (Pop3Message)message;
                    if (listener != null) {
                        listener.messageStarted(pop3Message.getUid(), i, count);
                    }
                    String response = executeSimpleCommand(String.format("LIST %d",
                            mUidToMsgNumMap.get(pop3Message.getUid())));
                    try {
                        String[] listParts = response.split(" ");
                        int msgNum = Integer.parseInt(listParts[1]);
                        int msgSize = Integer.parseInt(listParts[2]);
                        pop3Message.setSize(msgSize);
                    } catch (NumberFormatException nfe) {
                        throw new IOException();
                    }
                    if (listener != null) {
                        listener.messageFinished(pop3Message, i, count);
                    }
                }
            } else {
                HashSet<String> msgUidIndex = new HashSet<String>();
                for (Message message : messages) {
                    msgUidIndex.add(message.getUid());
                }
                int i = 0, count = messages.length;
                String response = executeSimpleCommand("LIST");
                while ((response = mTransport.readLine()) != null) {
                    if (response.equals(".")) {
                        break;
                    }
                    Pop3Message pop3Message = null;
                    int msgSize = 0;
                    try {
                        String[] listParts = response.split(" ");
                        int msgNum = Integer.parseInt(listParts[0]);
                        msgSize = Integer.parseInt(listParts[1]);
                        pop3Message = mMsgNumToMsgMap.get(msgNum);
                    } catch (NumberFormatException nfe) {
                        throw new IOException();
                    }
                    if (pop3Message != null && msgUidIndex.contains(pop3Message.getUid())) {
                        if (listener != null) {
                            listener.messageStarted(pop3Message.getUid(), i, count);
                        }
                        pop3Message.setSize(msgSize);
                        if (listener != null) {
                            listener.messageFinished(pop3Message, i, count);
                        }
                        i++;
                    }
                }
            }
        }
        private void fetchBody(Pop3Message message, int lines)
                throws IOException, MessagingException {
            String response = null;
            int messageId = mUidToMsgNumMap.get(message.getUid());
            if (lines == -1) {
                response = executeSimpleCommand(String.format("RETR %d", messageId));
            } else {
                try {
                    response = executeSimpleCommand(String.format("TOP %d %d", messageId,  lines));
                } catch (MessagingException me) {
                    response = executeSimpleCommand(String.format("RETR %d", messageId));
                }
            }
            if (response != null)  {
                try {
                    InputStream in = mTransport.getInputStream();
                    if (DEBUG_LOG_RAW_STREAM && Config.LOGD && Email.DEBUG) {
                        in = new LoggingInputStream(in);
                    }
                    message.parse(new Pop3ResponseInputStream(in));
                }
                catch (MessagingException me) {
                    if (lines == -1) {
                        throw me;
                    }
                }
            }
        }
        @Override
        public Flag[] getPermanentFlags() throws MessagingException {
            return PERMANENT_FLAGS;
        }
        public void appendMessages(Message[] messages) throws MessagingException {
        }
        public void delete(boolean recurse) throws MessagingException {
        }
        public Message[] expunge() throws MessagingException {
            return null;
        }
        public void setFlags(Message[] messages, Flag[] flags, boolean value)
                throws MessagingException {
            if (!value || !Utility.arrayContains(flags, Flag.DELETED)) {
                return;
            }
            try {
                for (Message message : messages) {
                    executeSimpleCommand(String.format("DELE %s",
                            mUidToMsgNumMap.get(message.getUid())));
                }
            }
            catch (IOException ioe) {
                mTransport.close();
                if (Config.LOGD && Email.DEBUG) {
                    Log.d(Email.LOG_TAG, ioe.toString());
                }
                throw new MessagingException("setFlags()", ioe);
            }
        }
        @Override
        public void copyMessages(Message[] msgs, Folder folder, MessageUpdateCallbacks callbacks)
                throws MessagingException {
            throw new UnsupportedOperationException("copyMessages is not supported in POP3");
        }
        private Pop3Capabilities getCapabilities() throws IOException, MessagingException {
            Pop3Capabilities capabilities = new Pop3Capabilities();
            try {
                String response = executeSimpleCommand("CAPA");
                while ((response = mTransport.readLine()) != null) {
                    if (response.equals(".")) {
                        break;
                    }
                    if (response.equalsIgnoreCase("STLS")){
                        capabilities.stls = true;
                    }
                    else if (response.equalsIgnoreCase("UIDL")) {
                        capabilities.uidl = true;
                    }
                    else if (response.equalsIgnoreCase("PIPELINING")) {
                        capabilities.pipelining = true;
                    }
                    else if (response.equalsIgnoreCase("USER")) {
                        capabilities.user = true;
                    }
                    else if (response.equalsIgnoreCase("TOP")) {
                        capabilities.top = true;
                    }
                }
            }
            catch (MessagingException me) {
            }
            return capabilities;
        }
        private String executeSimpleCommand(String command) throws IOException, MessagingException {
            return executeSensitiveCommand(command, null);
        }
        private String executeSensitiveCommand(String command, String sensitiveReplacement)
                throws IOException, MessagingException {
            open(OpenMode.READ_WRITE, null);
            if (command != null) {
                mTransport.writeLine(command, sensitiveReplacement);
            }
            String response = mTransport.readLine();
            if (response.length() > 1 && response.charAt(0) == '-') {
                throw new MessagingException(response);
            }
            return response;
        }
        @Override
        public boolean equals(Object o) {
            if (o instanceof Pop3Folder) {
                return ((Pop3Folder) o).mName.equals(mName);
            }
            return super.equals(o);
        }
        @Override
        public boolean isOpen() {
            return mTransport.isOpen();
        }
        @Override
        public Message createMessage(String uid) throws MessagingException {
            return new Pop3Message(uid, this);
        }
    }
    class Pop3Message extends MimeMessage {
        public Pop3Message(String uid, Pop3Folder folder) throws MessagingException {
            mUid = uid;
            mFolder = folder;
            mSize = -1;
        }
        public void setSize(int size) {
            mSize = size;
        }
        protected void parse(InputStream in) throws IOException, MessagingException {
            super.parse(in);
        }
        @Override
        public void setFlag(Flag flag, boolean set) throws MessagingException {
            super.setFlag(flag, set);
            mFolder.setFlags(new Message[] { this }, new Flag[] { flag }, set);
        }
    }
    class Pop3Capabilities {
        public boolean stls;
        public boolean top;
        public boolean user;
        public boolean uidl;
        public boolean pipelining;
        public String toString() {
            return String.format("STLS %b, TOP %b, USER %b, UIDL %b, PIPELINING %b",
                    stls,
                    top,
                    user,
                    uidl,
                    pipelining);
        }
    }
    class Pop3ResponseInputStream extends InputStream {
        InputStream mIn;
        boolean mStartOfLine = true;
        boolean mFinished;
        public Pop3ResponseInputStream(InputStream in) {
            mIn = in;
        }
        @Override
        public int read() throws IOException {
            if (mFinished) {
                return -1;
            }
            int d = mIn.read();
            if (mStartOfLine && d == '.') {
                d = mIn.read();
                if (d == '\r') {
                    mFinished = true;
                    mIn.read();
                    return -1;
                }
            }
            mStartOfLine = (d == '\n');
            return d;
        }
    }
}
