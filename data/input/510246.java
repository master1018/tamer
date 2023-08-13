public class ImapStoreUnitTests extends AndroidTestCase {
    private final static String[] NO_REPLY = new String[0];
    private ImapStore mStore = null;
    private ImapStore.ImapFolder mFolder = null;
    private int mNextTag;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStore = (ImapStore) ImapStore.newInstance("imap:
                getContext(), null);
        mFolder = (ImapStore.ImapFolder) mStore.getFolder("INBOX");
        BinaryTempFileBody.setTempDirectory(this.getContext().getCacheDir());
    }
    public void testSimpleLogin() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport);
        mFolder.open(OpenMode.READ_WRITE, null);
    }
    public void testImapIdBasic() {
        String id = mStore.getImapId(getContext(), "user-name", "host-name", "IMAP4rev1 STARTTLS");
        HashMap<String, String> map = tokenizeImapId(id);
        assertEquals(getContext().getPackageName(), map.get("name"));
        assertEquals("android", map.get("os"));
        assertNotNull(map.get("os-version"));
        assertNotNull(map.get("vendor"));
        assertNotNull(map.get("AGUID"));
        id = mStore.makeCommonImapId("packageName", "version", "codeName",
                "model", "id", "vendor", "network-operator");
        map = tokenizeImapId(id);
        assertEquals("packageName", map.get("name"));
        assertEquals("android", map.get("os"));
        assertEquals("version; id", map.get("os-version"));
        assertEquals("vendor", map.get("vendor"));
        assertEquals(null, map.get("x-android-device-model"));
        assertEquals("network-operator", map.get("x-android-mobile-net-operator"));
        assertEquals(null, map.get("AGUID"));
        id = mStore.makeCommonImapId("packageName", "", "REL",
                "model", "id", "vendor", "");
        map = tokenizeImapId(id);
        assertEquals("packageName", map.get("name"));
        assertEquals("android", map.get("os"));
        assertEquals("1.0; id", map.get("os-version"));
        assertEquals("vendor", map.get("vendor"));
        assertEquals("model", map.get("x-android-device-model"));
        assertEquals(null, map.get("x-android-mobile-net-operator"));
        assertEquals(null, map.get("AGUID"));
    }
    public void testImapIdFiltering() {
        String id = mStore.makeCommonImapId("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ",
                "0123456789", "codeName",
                "model", "-_+=;:.,
                "v(e)n\"d\ro\nr",           
                "()\"");                    
        HashMap<String, String> map = tokenizeImapId(id);
        assertEquals("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ", map.get("name"));
        assertEquals("0123456789; -_+=;:.,
        assertEquals("vendor", map.get("vendor"));
        assertNull(map.get("x-android-mobile-net-operator"));
    }
    public void testImapIdDeviceId() throws MessagingException {
        ImapStore store1a = (ImapStore) ImapStore.newInstance("imap:
                getContext(), null);
        ImapStore store1b = (ImapStore) ImapStore.newInstance("imap:
                getContext(), null);
        ImapStore store2 = (ImapStore) ImapStore.newInstance("imap:
                getContext(), null);
        String id1a = mStore.getImapId(getContext(), "user1", "host-name", "IMAP4rev1");
        String id1b = mStore.getImapId(getContext(), "user1", "host-name", "IMAP4rev1");
        String id2 = mStore.getImapId(getContext(), "user2", "host-name", "IMAP4rev1");
        String uid1a = tokenizeImapId(id1a).get("AGUID");
        String uid1b = tokenizeImapId(id1b).get("AGUID");
        String uid2 = tokenizeImapId(id2).get("AGUID");
        assertEquals(uid1a, uid1b);
        MoreAsserts.assertNotEqual(uid1a, uid2);
    }
    private HashMap<String, String> tokenizeImapId(String id) {
        String[] elements = id.split("\"");
        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < elements.length; ) {
            map.put(elements[i+1], elements[i+3]);
            i += 4;
        }
        return map;
    }
    public void testServerId() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, new String[] {
                "* ID (\"name\" \"Cyrus\" \"version\" \"1.5\"" +
                " \"os\" \"sunos\" \"os-version\" \"5.5\"" +
                " \"support-url\" \"mailto:cyrus-bugs+@andrew.cmu.edu\")",
                "OK"}, "READ-WRITE");
        mFolder.open(OpenMode.READ_WRITE, null);
    }
    public void testImapIdOkParsing() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, new String[] {
                "* ID NIL",
                "OK [ID] bad-char-%"}, "READ-WRITE");
        mFolder.open(OpenMode.READ_WRITE, null);
    }
    public void testImapIdBad() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, new String[] {
                "BAD unknown command bad-char-%"}, "READ-WRITE");
        mFolder.open(OpenMode.READ_WRITE, null);
    }
    public void testImapListWithUsLocale() throws MessagingException {
        Locale savedLocale = Locale.getDefault();
        Locale.setDefault(Locale.US);
        doTestImapList();
        Locale.setDefault(Locale.JAPAN);
        doTestImapList();
        Locale.setDefault(savedLocale);
    }
    private void doTestImapList() throws MessagingException {
        ImapResponseParser parser = new ImapResponseParser(null, new DiscourseLogger(4));
        ImapResponseParser.ImapList list = parser.new ImapList();
        String key = "key";
        String date = "01-Jan-2009 01:00:00 -0800";
        list.add(key);
        list.add(date);
        Date result = list.getKeyedDate(key);
        assertEquals(1230800400000L, result.getTime());
    }
    public void testSmallFolderFunctions() throws MessagingException {
        Flag[] flags = mFolder.getPermanentFlags();
        assertEquals(3, flags.length);
        assertEquals(Flag.DELETED, flags[0]);
        assertEquals(Flag.SEEN, flags[1]);
        assertEquals(Flag.FLAGGED, flags[2]);
        assertTrue(mFolder.canCreate(FolderType.HOLDS_FOLDERS));
        assertTrue(mFolder.canCreate(FolderType.HOLDS_MESSAGES));
    }
    public void testNoFolderRolesYet() {
        assertEquals(Folder.FolderRole.UNKNOWN, mFolder.getRole()); 
    }
    public void testNoStructurePrefetch() {
        assertFalse(mStore.requireStructurePrefetch()); 
    }
    public void testSentUploadRequested() {
        assertTrue(mStore.requireCopyMessageToSentFolder()); 
    }
    private MockTransport openAndInjectMockTransport() {
        MockTransport mockTransport = new MockTransport();
        mockTransport.setSecurity(Transport.CONNECTION_SECURITY_NONE, false);
        mockTransport.setMockHost("mock.server.com");
        mStore.setTransport(mockTransport);
        return mockTransport;
    }
    private void setupOpenFolder(MockTransport mockTransport) {
        setupOpenFolder(mockTransport, "READ-WRITE");
    }
    private void setupOpenFolder(MockTransport mockTransport, String readWriteMode) {
        setupOpenFolder(mockTransport, new String[] {
                "* ID NIL", "OK"}, readWriteMode);
    }
    private void setupOpenFolder(MockTransport mockTransport, String[] imapIdResponse,
            String readWriteMode) {
        String last = imapIdResponse[imapIdResponse.length-1];
        last = "2 " + last;
        imapIdResponse[imapIdResponse.length-1] = last;
        mockTransport.expect(null, "* OK Imap 2000 Ready To Assist You");
        mockTransport.expect("1 CAPABILITY", new String[] {
                "* CAPABILITY IMAP4rev1 STARTTLS AUTH=GSSAPI LOGINDISABLED",
                "1 OK CAPABILITY completed"});
        mockTransport.expect("2 ID \\(.*\\)", imapIdResponse);
        mockTransport.expect("3 LOGIN user \"password\"", 
                "3 OK user authenticated (Success)");
        mockTransport.expect("4 SELECT \"INBOX\"", new String[] {
                "* FLAGS (\\Answered \\Flagged \\Draft \\Deleted \\Seen)",
                "* OK [PERMANENTFLAGS (\\Answered \\Flagged \\Draft \\Deleted \\Seen \\*)]",
                "* 0 EXISTS",
                "* 0 RECENT",
                "* OK [UNSEEN 0]",
                "* OK [UIDNEXT 1]",
                "4 OK [" + readWriteMode + "] INBOX selected. (Success)"});
        mNextTag = 5;
    }
    public String getNextTag(boolean advance)  {
        if (advance) ++mNextTag;
        return Integer.toString(mNextTag);
    }
    public void testReadWrite() throws MessagingException {
        MockTransport mock = openAndInjectMockTransport();
        setupOpenFolder(mock, "READ-WRITE");
        mFolder.open(OpenMode.READ_WRITE, null);
        assertEquals(OpenMode.READ_WRITE, mFolder.getMode());
    }
    public void testReadOnly() throws MessagingException {
        MockTransport mock = openAndInjectMockTransport();
        setupOpenFolder(mock, "READ-ONLY");
        mFolder.open(OpenMode.READ_ONLY, null);
        assertEquals(OpenMode.READ_ONLY, mFolder.getMode());
    }
    public void testGetUnreadMessageCountWithQuotedString() throws Exception {
        MockTransport mock = openAndInjectMockTransport();
        setupOpenFolder(mock);
        mock.expect(getNextTag(false) + " STATUS \"INBOX\" \\(UNSEEN\\)", new String[] {
                "* STATUS \"INBOX\" (UNSEEN 2)",
                getNextTag(true) + " OK STATUS completed"});
        mFolder.open(OpenMode.READ_WRITE, null);
        int unreadCount = mFolder.getUnreadMessageCount();
        assertEquals("getUnreadMessageCount with quoted string", 2, unreadCount);
    }
    public void testGetUnreadMessageCountWithLiteralString() throws Exception {
        MockTransport mock = openAndInjectMockTransport();
        setupOpenFolder(mock);
        mock.expect(getNextTag(false) + " STATUS \"INBOX\" \\(UNSEEN\\)", new String[] {
                "* STATUS {5}",
                "INBOX (UNSEEN 10)",
                getNextTag(true) + " OK STATUS completed"});
        mFolder.open(OpenMode.READ_WRITE, null);
        int unreadCount = mFolder.getUnreadMessageCount();
        assertEquals("getUnreadMessageCount with literal string", 10, unreadCount);
    }
    public void testNilMessage() throws MessagingException {
        MockTransport mock = openAndInjectMockTransport();
        setupOpenFolder(mock);
        mFolder.open(OpenMode.READ_WRITE, null);
        FetchProfile fp = new FetchProfile();fp.clear();
        fp.add(FetchProfile.Item.STRUCTURE);
        Message message1 = mFolder.createMessage("1");
        mock.expect(getNextTag(false) + " UID FETCH 1 \\(UID BODYSTRUCTURE\\)", new String[] {
                "* 1 FETCH (UID 1 BODYSTRUCTURE (TEXT PLAIN NIL NIL NIL 7BIT 0 0 NIL NIL NIL))",
                getNextTag(true) + " OK SUCCESS"
        });
        mFolder.fetch(new Message[] { message1 }, fp, null);
        mock.expect(getNextTag(false) + " UID FETCH 1 \\(UID BODY.PEEK\\[TEXT\\]\\)", new String[] {
                "* 1 FETCH (UID 1 BODY[TEXT] NIL)",
                getNextTag(true) + " OK SUCCESS"
        });
        ArrayList<Part> viewables = new ArrayList<Part>();
        ArrayList<Part> attachments = new ArrayList<Part>();
        MimeUtility.collectParts(message1, viewables, attachments);
        assertTrue(viewables.size() == 1);
        Part emptyBodyPart = viewables.get(0);
        fp.clear();
        fp.add(emptyBodyPart);
        mFolder.fetch(new Message[] { message1 }, fp, null);
        viewables = new ArrayList<Part>();
        attachments = new ArrayList<Part>();
        MimeUtility.collectParts(message1, viewables, attachments);
        assertTrue(viewables.size() == 1);
        emptyBodyPart = viewables.get(0);
        String text = MimeUtility.getTextFromPart(emptyBodyPart);
        assertNull(text);
    }
    public void testExcessFetchResult() throws MessagingException {
        MockTransport mock = openAndInjectMockTransport();
        setupOpenFolder(mock);
        mFolder.open(OpenMode.READ_WRITE, null);
        Message message1 = mFolder.createMessage("1");
        assertFalse(message1.isSet(Flag.SEEN));
        FetchProfile fp = new FetchProfile();
        fp.clear();
        fp.add(FetchProfile.Item.FLAGS);
        mock.expect(getNextTag(false) + " UID FETCH 1 \\(UID FLAGS\\)",
                new String[] {
                "* 1 FETCH (UID 1 FLAGS (\\Seen))",
                "* 2 FETCH (FLAGS (\\Seen))",
                getNextTag(true) + " OK SUCCESS"
        });
        mFolder.fetch(new Message[] { message1 }, fp, null);
        assertTrue(message1.isSet(Flag.SEEN));
    }
    public void testAppendMessages() throws Exception {
        MockTransport mock = openAndInjectMockTransport();
        setupOpenFolder(mock);
        mFolder.open(OpenMode.READ_WRITE, null);
        ImapMessage message = (ImapMessage) mFolder.createMessage("1");
        message.setFrom(new Address("me@test.com"));
        message.setRecipient(RecipientType.TO, new Address("you@test.com"));
        message.setMessageId("<message.id@test.com>");
        message.setFlagDirectlyForTest(Flag.SEEN, true);
        message.setBody(new TextBody("Test Body"));
        mock.expect(getNextTag(false) + " APPEND \\\"INBOX\\\" \\(\\\\Seen\\) \\{166\\}",
                new String[] {"+ go ahead"});
        mock.expectLiterally("From: me@test.com", NO_REPLY);
        mock.expectLiterally("To: you@test.com", NO_REPLY);
        mock.expectLiterally("Message-ID: <message.id@test.com>", NO_REPLY);
        mock.expectLiterally("Content-Type: text/plain;", NO_REPLY);
        mock.expectLiterally(" charset=utf-8", NO_REPLY);
        mock.expectLiterally("Content-Transfer-Encoding: base64", NO_REPLY);
        mock.expectLiterally("", NO_REPLY);
        mock.expectLiterally("VGVzdCBCb2R5", NO_REPLY);
        mock.expectLiterally("", new String[] {
                "* 7 EXISTS",
                getNextTag(true) + " OK [APPENDUID 1234567 13] (Success)"
                });
        mFolder.appendMessages(new Message[] {message});
        mock.close();
        assertEquals("13", message.getUid());
        assertEquals(7, mFolder.getMessageCount());
    }
}
