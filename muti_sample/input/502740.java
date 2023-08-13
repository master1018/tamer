public class Pop3StoreUnitTests extends AndroidTestCase {
    final String UNIQUE_ID_1 = "20080909002219r1800rrjo9e00";
    final static int PER_MESSAGE_SIZE = 100;
    private Pop3Store mStore = null;
    private Pop3Store.Pop3Folder mFolder = null;
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mStore = (Pop3Store) Pop3Store.newInstance("pop3:
                getContext(), null);
        mFolder = (Pop3Store.Pop3Folder) mStore.getFolder("INBOX");
        BinaryTempFileBody.setTempDirectory(this.getContext().getCacheDir());
    }
    public void testUIDLParserMulti() {
        Pop3Store.Pop3Folder.UidlParser parser = mFolder.new UidlParser();
        parser.parseMultiLine("101 " + UNIQUE_ID_1);
        assertEquals(101, parser.mMessageNumber);
        assertEquals(UNIQUE_ID_1, parser.mUniqueId);
        assertFalse(parser.mEndOfMessage);
        assertFalse(parser.mErr);
        parser.parseMultiLine(".");
        assertTrue(parser.mEndOfMessage);
        assertFalse(parser.mErr);
    }
    public void testUIDLParserSingle() {      
        Pop3Store.Pop3Folder.UidlParser parser = mFolder.new UidlParser();
        parser.parseSingleLine("+OK 101 " + UNIQUE_ID_1);
        assertEquals(101, parser.mMessageNumber);
        assertEquals(UNIQUE_ID_1, parser.mUniqueId);
        assertTrue(parser.mEndOfMessage);
        parser.parseSingleLine("-ERR what???");
        assertTrue(parser.mErr);
    }
    public void testUIDLParserMultiFail() {
        Pop3Store.Pop3Folder.UidlParser parser = mFolder.new UidlParser();
        boolean result;
        result = parser.parseMultiLine(null);
        assertFalse(result);
        result = parser.parseMultiLine("");
        assertFalse(result);
    }
    public void testUIDLParserSingleFail() {
        Pop3Store.Pop3Folder.UidlParser parser = mFolder.new UidlParser();
        boolean result;
        result = parser.parseSingleLine(null);
        assertFalse(result);
        result = parser.parseSingleLine("");
        assertFalse(result);
    }
    public void testUIDLComcastVariant() {
        Pop3Store.Pop3Folder.UidlParser parser = mFolder.new UidlParser();
        parser.parseMultiLine("101   " + UNIQUE_ID_1);
        assertEquals(101, parser.mMessageNumber);
        assertEquals(UNIQUE_ID_1, parser.mUniqueId);
        assertFalse(parser.mEndOfMessage);
        assertFalse(parser.mErr);
    }
    public void testSimpleLogin() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, 0, null);
        mFolder.open(OpenMode.READ_ONLY, null);
    }
    public void testCheckSettings() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, 0, null);
        setupUidlSequence(mockTransport, 1);
        mockTransport.expect("QUIT", "");
        mStore.checkSettings();
        setupOpenFolder(mockTransport, 0, "UIDL");
        mockTransport.expect("QUIT", "");
        mStore.checkSettings();
        try {
            setupOpenFolder(mockTransport, 0, null);
            mockTransport.expect("UIDL", "-ERR unsupported");
            mockTransport.expect("QUIT", "");
            mStore.checkSettings();
            fail("MessagingException was expected due to UIDL unsupported.");
        } catch (MessagingException me) {
        }
    }
    public void testStoreFoldersFunctions() throws MessagingException {
        Folder[] folders = mStore.getPersonalNamespaces();
        assertEquals(1, folders.length);
        assertSame(mFolder, folders[0]);
        assertEquals("INBOX", mFolder.getName());
        Pop3Store.Pop3Folder folderMixedCaseInbox = mStore.new Pop3Folder("iNbOx");
        assertEquals("INBOX", folderMixedCaseInbox.getName());
        Pop3Store.Pop3Folder folderNotInbox = mStore.new Pop3Folder("NOT-INBOX");
        assertEquals("NOT-INBOX", folderNotInbox.getName());
        assertTrue(mFolder.exists());
        assertTrue(folderMixedCaseInbox.exists());
        assertFalse(folderNotInbox.exists());
    }
    public void testSmallFolderFunctions() throws MessagingException {
        assertEquals(OpenMode.READ_WRITE, mFolder.getMode());
        assertFalse(mFolder.canCreate(FolderType.HOLDS_FOLDERS));
        assertFalse(mFolder.canCreate(FolderType.HOLDS_MESSAGES));
        assertFalse(mFolder.create(FolderType.HOLDS_FOLDERS));
        assertFalse(mFolder.create(FolderType.HOLDS_MESSAGES));
        assertEquals(-1, mFolder.getUnreadMessageCount());
        try {
            mFolder.getMessages(null);
            fail("Exception not thrown by getMessages()");
        } catch (UnsupportedOperationException e) {
        }
        try {
            mFolder.getMessages(null, null);
            fail("Exception not thrown by getMessages()");
        } catch (UnsupportedOperationException e) {
        }
        Flag[] flags = mFolder.getPermanentFlags();
        assertEquals(1, flags.length);
        assertEquals(Flag.DELETED, flags[0]);
        mFolder.appendMessages(null);
        mFolder.delete(false);
        assertNull(mFolder.expunge());
        try {
            mFolder.copyMessages(null, null, null);
            fail("Exception not thrown by copyMessages()");
        } catch (UnsupportedOperationException e) {
        }
    }
    public void testNoFolderRolesYet() throws MessagingException {
        Folder[] remoteFolders = mStore.getPersonalNamespaces();
        for (Folder folder : remoteFolders) {
            assertEquals(Folder.FolderRole.UNKNOWN, folder.getRole()); 
        }
    }
    public void testNoStructurePrefetch() {
        assertFalse(mStore.requireStructurePrefetch()); 
    }
    public void testSentUploadRequested() {
        assertTrue(mStore.requireCopyMessageToSentFolder()); 
    }
    public void testOneUnread() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        checkOneUnread(mockTransport);
    }
    public void testGetMessageByUid() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, 2, null);
        mFolder.open(OpenMode.READ_WRITE, null);
        assertEquals(2, mFolder.getMessageCount());
        setupUidlSequence(mockTransport, 2);
        String uid1 = getSingleMessageUID(1);
        String uid2 = getSingleMessageUID(2);
        String uid3 = getSingleMessageUID(3);
        Message msg1 = mFolder.getMessage(uid1);
        assertTrue("message with uid1", msg1 != null);
        Message msg3 = mFolder.getMessage(uid3);
        assertTrue("message with uid3", msg3 == null);
        Message msg2 = mFolder.getMessage(uid2);
        assertTrue("message with uid2", msg2 != null);
    }
    public void testCatchClosed1a() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        openFolderWithMessage(mockTransport);
        mockTransport.closeInputStream();
        try {
            setupUidlSequence(mockTransport, 1);
            Message[] messages = mFolder.getMessages(1, 1, null);
            assertEquals(1, messages.length);
            assertEquals(getSingleMessageUID(1), messages[0].getUid());
            fail("Broken stream should cause getMessages() to throw.");
        } catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed1b() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        openFolderWithMessage(mockTransport);
        try {
            mockTransport.expect("UIDL", "+OK sending UIDL list");
            mockTransport.expect(null, "bad-data" + " " + "THE-UIDL");
            mockTransport.expect(null, ".");
            Message[] messages = mFolder.getMessages(1, 1, null);
            fail("Bad UIDL should cause getMessages() to throw.");
        } catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed1c() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, 6000, null);
        mFolder.open(OpenMode.READ_ONLY, null);
        assertEquals(6000, mFolder.getMessageCount());
        try {
            mockTransport.expect("UIDL 1", "+OK " + "bad-data" + " " + "THE-UIDL");
            Message[] messages = mFolder.getMessages(1, 1, null);
            fail("Bad UIDL should cause getMessages() to throw.");
        } catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed2() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        openFolderWithMessage(mockTransport);
        setupUidlSequence(mockTransport, 1);
        Message[] messages = mFolder.getMessages(1, 1, null);
        assertEquals(1, messages.length);
        assertEquals(getSingleMessageUID(1), messages[0].getUid());         
        mockTransport.closeInputStream();
        try {
            setupListSequence(mockTransport, 1);
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.ENVELOPE);
            mFolder.fetch(messages, fp, null);
            assertEquals(PER_MESSAGE_SIZE, messages[0].getSize());
            fail("Broken stream should cause fetch() to throw.");
        }
        catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed2a() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        openFolderWithMessage(mockTransport);
        setupUidlSequence(mockTransport, 1);
        Message[] messages = mFolder.getMessages(1, 1, null);
        assertEquals(1, messages.length);
        assertEquals(getSingleMessageUID(1), messages[0].getUid());         
        setupBrokenListSequence(mockTransport, 1);
        try {
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfile.Item.ENVELOPE);
            mFolder.fetch(messages, fp, null);
            assertEquals(PER_MESSAGE_SIZE, messages[0].getSize());
            fail("Broken stream should cause fetch() to throw.");
        } catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed3() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        openFolderWithMessage(mockTransport);
        setupUidlSequence(mockTransport, 1);
        Message[] messages = mFolder.getMessages(1, 1, null);
        assertEquals(1, messages.length);
        assertEquals(getSingleMessageUID(1), messages[0].getUid());         
        setupListSequence(mockTransport, 1);
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(FetchProfile.Item.ENVELOPE);
        mFolder.fetch(messages, fp, null);
        assertEquals(PER_MESSAGE_SIZE, messages[0].getSize());
        mockTransport.closeInputStream();
        try {
            setupSingleMessage(mockTransport, 1, false);
            fp = new FetchProfile();
            fp.add(FetchProfile.Item.BODY);
            mFolder.fetch(messages, fp, null);
            checkFetchedMessage(messages[0], 1, false);
            fail("Broken stream should cause fetch() to throw.");
        }
        catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed4() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        openFolderWithMessage(mockTransport);
        setupUidlSequence(mockTransport, 1);
        Message[] messages = mFolder.getMessages(1, 1, null);
        assertEquals(1, messages.length);
        assertEquals(getSingleMessageUID(1), messages[0].getUid());
        mockTransport.closeInputStream();
        try {
            mockTransport.expect("DELE 1", "+OK message deleted");
            mFolder.setFlags(messages, new Flag[] { Flag.DELETED }, true);
            fail("Broken stream should cause fetch() to throw.");
        }
        catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed5() {
    }
    public void testCatchClosed6a() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        setupOpenFolder(mockTransport, -1, null);
        try {
            mFolder.open(OpenMode.READ_ONLY, null);
            fail("Broken STAT should cause open() to throw.");
        } catch(MessagingException me) {
        }
        assertFalse("folder should be 'closed' after an IOError", mFolder.isOpen());
        checkOneUnread(mockTransport);
    }
    public void testCatchClosed6b() throws MessagingException {
    }
    private void checkOneUnread(MockTransport mockTransport) throws MessagingException {
        openFolderWithMessage(mockTransport);
        setupUidlSequence(mockTransport, 1);
        Message[] messages = mFolder.getMessages(1, 1, null);
        assertEquals(1, messages.length);
        assertEquals(getSingleMessageUID(1), messages[0].getUid());
        setupListSequence(mockTransport, 1);
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(FetchProfile.Item.ENVELOPE);
        mFolder.fetch(messages, fp, null);
        assertEquals(PER_MESSAGE_SIZE, messages[0].getSize());
        MimeMessage message = (MimeMessage) messages[0];
        message.getRecipients(RecipientType.TO);
        message.getRecipients(RecipientType.CC);
        message.getRecipients(RecipientType.BCC);
        setupSingleMessage(mockTransport, 1, false);
        fp = new FetchProfile();
        fp.add(FetchProfile.Item.BODY);
        mFolder.fetch(messages, fp, null);
        checkFetchedMessage(messages[0], 1, false);
    }
    public void testRetrVariants() throws MessagingException {
        MockTransport mockTransport = openAndInjectMockTransport();
        openFolderWithMessage(mockTransport);
        setupUidlSequence(mockTransport, 2);
        Message[] messages = mFolder.getMessages(1, 2, null);
        assertEquals(2, messages.length);
        setupListSequence(mockTransport, 2);
        FetchProfile fp = new FetchProfile();
        fp.add(FetchProfile.Item.FLAGS);
        fp.add(FetchProfile.Item.ENVELOPE);
        mFolder.fetch(messages, fp, null);
        for (Message message : messages) {
            message.getRecipients(RecipientType.TO);
            message.getRecipients(RecipientType.CC);
            message.getRecipients(RecipientType.BCC);
        }
        Message[] singleMessage = new Message[] { messages[0] };
        setupSingleMessageTop(mockTransport, 1, true, true);        
        fp = new FetchProfile();
        fp.add(FetchProfile.Item.BODY_SANE);
        mFolder.fetch(singleMessage, fp, null);
        checkFetchedMessage(singleMessage[0], 1, false);
        singleMessage[0] = messages[1];
        setupSingleMessageTop(mockTransport, 2, true, false);        
        fp = new FetchProfile();
        fp.add(FetchProfile.Item.BODY_SANE);
        mFolder.fetch(singleMessage, fp, null);
        checkFetchedMessage(singleMessage[0], 2, false);
    }
    private MockTransport openAndInjectMockTransport() {
        MockTransport mockTransport = new MockTransport();
        mockTransport.setSecurity(Transport.CONNECTION_SECURITY_NONE, false);
        mStore.setTransport(mockTransport);
        return mockTransport;
    }
    private void openFolderWithMessage(MockTransport mockTransport) throws MessagingException {
        setupOpenFolder(mockTransport, 1, null);
        mFolder.open(OpenMode.READ_ONLY, null);
        assertEquals(1, mFolder.getMessageCount());
    }
    private void checkFetchedMessage(Message message, int msgNum, boolean body) 
            throws MessagingException {
        Address[] to = message.getRecipients(RecipientType.TO);
        assertNotNull(to);
        assertEquals(1, to.length);
        assertEquals("Smith@Registry.Org", to[0].getAddress());
        assertNull(to[0].getPersonal());
        Address[] from = message.getFrom();
        assertNotNull(from);
        assertEquals(1, from.length);
        assertEquals("Jones@Registry.Org", from[0].getAddress());
        assertNull(from[0].getPersonal());
        Address[] cc = message.getRecipients(RecipientType.CC);
        assertNotNull(cc);
        assertEquals(1, cc.length);
        assertEquals("Chris@Registry.Org", cc[0].getAddress());
        assertNull(cc[0].getPersonal());
        Address[] replyto = message.getReplyTo();
        assertNotNull(replyto);
        assertEquals(1, replyto.length);
        assertEquals("Roger@Registry.Org", replyto[0].getAddress());
        assertNull(replyto[0].getPersonal());
    }
    private void setupOpenFolder(MockTransport mockTransport, int statCount, String capabilities) {
        mockTransport.expect(null, "+OK Hello there from the Mock Transport.");
        if (capabilities == null) {
            mockTransport.expect("CAPA", "-ERR unimplemented");
        } else {
            mockTransport.expect("CAPA", "+OK capabilities follow");
            mockTransport.expect(null, capabilities.split(","));        
            mockTransport.expect(null, ".");                            
        }
        mockTransport.expect("USER user", "+OK User name accepted");
        mockTransport.expect("PASS password", "+OK Logged in");
        if (statCount == -1) {
            mockTransport.expect("STAT", "");
        } else {
            String stat = "+OK " + Integer.toString(statCount) + " "
                    + Integer.toString(PER_MESSAGE_SIZE * statCount);
            mockTransport.expect("STAT", stat);
        }
    }
    private static void setupUidlSequence(MockTransport transport, int numMessages) {
        transport.expect("UIDL", "+OK sending UIDL list");          
        for (int msgNum = 1; msgNum <= numMessages; ++msgNum) {
            transport.expect(null, Integer.toString(msgNum) + " " + getSingleMessageUID(msgNum));
        }
        transport.expect(null, ".");
    }
    private static void setupListSequence(MockTransport transport, int numMessages) {
        transport.expect("LIST", "+OK sending scan listing");          
        for (int msgNum = 1; msgNum <= numMessages; ++msgNum) {
            transport.expect(null, Integer.toString(msgNum) + " " + 
                    Integer.toString(PER_MESSAGE_SIZE * msgNum));
        }
        transport.expect(null, ".");
    }
    private static void setupBrokenListSequence(MockTransport transport, int numMessages) {
        transport.expect("LIST", "");
        for (int msgNum = 1; msgNum <= numMessages; ++msgNum) {
            transport.expect(null, "");
        }
        transport.expect(null, "");
    }
    private static void setupSingleMessage(MockTransport transport, int msgNum, boolean body) {
        setupSingleMessageTop(transport, msgNum, false, false);
    }
    private static void setupSingleMessageTop(MockTransport transport, int msgNum,
            boolean topTry, boolean topSupported) {
        String msgNumString = Integer.toString(msgNum);
        String topCommand = "TOP " + msgNumString + " 673";
        String retrCommand = "RETR " + msgNumString;
        if (topTry) {
            if (topSupported) {
                transport.expect(topCommand, "+OK message follows");
            } else {
                transport.expect(topCommand, "-ERR unsupported command");
                transport.expect(retrCommand, "+OK message follows");
            }
        } else {
            transport.expect(retrCommand, "+OK message follows");
        }
        transport.expect(null, "Date: 26 Aug 76 1429 EDT");
        transport.expect(null, "From: Jones@Registry.Org");
        transport.expect(null, "To:   Smith@Registry.Org");
        transport.expect(null, "CC:   Chris@Registry.Org");
        transport.expect(null, "Reply-To: Roger@Registry.Org");
        transport.expect(null, "");
        transport.expect(null, ".");
    }
    private static String getSingleMessageUID(int msgNum) {
        final String UID_HEAD = "ABCDEF-";
        final String UID_TAIL = "";
        return UID_HEAD + Integer.toString(msgNum) + UID_TAIL;
    }
}
