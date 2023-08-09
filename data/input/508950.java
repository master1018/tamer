public class EmailSyncAdapterTests extends SyncAdapterTestCase<EmailSyncAdapter> {
    public EmailSyncAdapterTests() {
        super();
    }
    public void testGetMimeTypeFromFileName() throws IOException {
        EasSyncService service = getTestService();
        EmailSyncAdapter adapter = new EmailSyncAdapter(service.mMailbox, service);
        EasEmailSyncParser p = adapter.new EasEmailSyncParser(getTestInputStream(), adapter);
        String mimeType = p.getMimeTypeFromFileName("foo.jpg");
        assertEquals("image/jpeg", mimeType);
        mimeType = p.getMimeTypeFromFileName("foo.JPG");
        assertEquals("image/jpeg", mimeType);
        mimeType = p.getMimeTypeFromFileName("this_is_a_weird_filename.gif");
        assertEquals("image/gif", mimeType);
        mimeType = p.getMimeTypeFromFileName("foo.");
        assertEquals("application/octet-stream", mimeType);
        mimeType = p.getMimeTypeFromFileName(".....");
        assertEquals("application/octet-stream", mimeType);
        mimeType = p.getMimeTypeFromFileName("foo");
        assertEquals("application/octet-stream", mimeType);
        mimeType = p.getMimeTypeFromFileName("");
        assertEquals("application/octet-stream", mimeType);
    }
    public void testFormatDateTime() throws IOException {
        EmailSyncAdapter adapter = getTestSyncAdapter(EmailSyncAdapter.class);
        GregorianCalendar calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        calendar.set(2008, 11, 11, 18, 19, 20);
        String date = adapter.formatDateTime(calendar);
        assertEquals("2008-12-11T18:19:20.000Z", date);
        calendar.clear();
        calendar.set(2012, 0, 2, 23, 0, 1);
        date = adapter.formatDateTime(calendar);
        assertEquals("2012-01-02T23:00:01.000Z", date);
    }
    public void testSendDeletedItems() throws IOException {
        EasSyncService service = getTestService();
        EmailSyncAdapter adapter = new EmailSyncAdapter(service.mMailbox, service);
        Serializer s = new Serializer();
        ArrayList<Long> ids = new ArrayList<Long>();
        ArrayList<Long> deletedIds = new ArrayList<Long>();
        adapter.mContext = mMockContext;
        Account acct = ProviderTestUtils.setupAccount("account", true, mMockContext);
        adapter.mAccount = acct;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", acct.mId, true, mMockContext);
        adapter.mMailbox = box1;
        Message msg1 = ProviderTestUtils.setupMessage("message1", acct.mId, box1.mId,
                true, true, mMockContext);
        ids.add(msg1.mId);
        Message msg2 = ProviderTestUtils.setupMessage("message2", acct.mId, box1.mId,
                true, true, mMockContext);
        ids.add(msg2.mId);
        Message msg3 = ProviderTestUtils.setupMessage("message3", acct.mId, box1.mId,
                true, true, mMockContext);
        ids.add(msg3.mId);
        assertEquals(3, EmailContent.count(mMockContext, Message.CONTENT_URI, null, null));
        for (long id: ids) {
            mMockResolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, id),
                    null, null);
        }
        assertEquals(0, EmailContent.count(mMockContext, Message.CONTENT_URI, null, null));
        assertEquals(3, EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, null, null));
        adapter.sendDeletedItems(s, deletedIds, true);
        assertEquals(3, deletedIds.size());
        deletedIds.clear();
        Message msg4 = ProviderTestUtils.setupMessage("message4", acct.mId, box1.mId,
                true, true, mMockContext);
        assertEquals(1, EmailContent.count(mMockContext, Message.CONTENT_URI, null, null));
        Body body = Body.restoreBodyWithMessageId(mMockContext, msg4.mId);
        ContentValues values = new ContentValues();
        values.put(Body.SOURCE_MESSAGE_KEY, msg2.mId);
        body.update(mMockContext, values);
        adapter.sendDeletedItems(s, deletedIds, true);
        assertEquals(2, deletedIds.size());
        assertFalse(deletedIds.contains(msg2.mId));
    }
    void setupSyncParserAndAdapter(Account account, Mailbox mailbox) throws IOException {
        EasSyncService service = getTestService(account, mailbox);
        mSyncAdapter = new EmailSyncAdapter(mailbox, service);
        mSyncParser = mSyncAdapter.new EasEmailSyncParser(getTestInputStream(), mSyncAdapter);
    }
    ArrayList<Long> setupAccountMailboxAndMessages(int numMessages) {
        ArrayList<Long> ids = new ArrayList<Long>();
        mAccount = ProviderTestUtils.setupAccount("account", true, mMockContext);
        mMailbox = ProviderTestUtils.setupMailbox("box1", mAccount.mId, true, mMockContext);
        for (int i = 0; i < numMessages; i++) {
            Message msg = ProviderTestUtils.setupMessage("message" + i, mAccount.mId, mMailbox.mId,
                    true, true, mMockContext);
            ids.add(msg.mId);
        }
        assertEquals(numMessages, EmailContent.count(mMockContext, Message.CONTENT_URI,
                null, null));
        return ids;
    }
    public void testDeleteParser() throws IOException {
        ArrayList<Long> messageIds = setupAccountMailboxAndMessages(3);
        ContentValues cv = new ContentValues();
        cv.put(SyncColumns.SERVER_ID, "1:22");
        long deleteMessageId = messageIds.get(1);
        mMockResolver.update(ContentUris.withAppendedId(Message.CONTENT_URI, deleteMessageId), cv,
                null, null);
        setupSyncParserAndAdapter(mAccount, mMailbox);
        Serializer s = new Serializer(false);
        s.start(Tags.SYNC_DELETE).data(Tags.SYNC_SERVER_ID, "1:22").end().done();
        byte[] bytes = s.toByteArray();
        mSyncParser.resetInput(new ByteArrayInputStream(bytes));
        mSyncParser.nextTag(0);
        ArrayList<Long> deleteList = new ArrayList<Long>();
        mSyncParser.deleteParser(deleteList, Tags.SYNC_DELETE);
        assertEquals(1, deleteList.size());
        long id = deleteList.get(0);
        assertEquals(deleteMessageId, id);
    }
    public void testChangeParser() throws IOException {
        ArrayList<Long> messageIds = setupAccountMailboxAndMessages(3);
        ContentValues cv = new ContentValues();
        cv.put(SyncColumns.SERVER_ID, "1:22");
        long changeMessageId = messageIds.get(1);
        mMockResolver.update(ContentUris.withAppendedId(Message.CONTENT_URI, changeMessageId), cv,
                null, null);
        setupSyncParserAndAdapter(mAccount, mMailbox);
        Serializer s = new Serializer(false);
        s.start(Tags.SYNC_CHANGE).data(Tags.SYNC_SERVER_ID, "1:22");
        s.start(Tags.SYNC_APPLICATION_DATA).data(Tags.EMAIL_READ, "0").end();
        s.end().done();
        byte[] bytes = s.toByteArray();
        mSyncParser.resetInput(new ByteArrayInputStream(bytes));
        mSyncParser.nextTag(0);
        ArrayList<ServerChange> changeList = new ArrayList<ServerChange>();
        mSyncParser.changeParser(changeList);
        assertEquals(1, changeList.size());
        ServerChange change = changeList.get(0);
        assertEquals(changeMessageId, change.id);
        assertNotNull(change.read);
        assertFalse(change.read);
    }
    public void testCleanup() throws IOException {
        ArrayList<Long> messageIds = setupAccountMailboxAndMessages(3);
        setupSyncParserAndAdapter(mAccount, mMailbox);
        long id = messageIds.get(0);
        mMockResolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, id),
                null, null);
        mSyncAdapter.mDeletedIdList.add(id);
        id = messageIds.get(1);
        mMockResolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI,
                id), null, null);
        mSyncAdapter.mDeletedIdList.add(id);
        id = messageIds.get(2);
        ContentValues cv = new ContentValues();
        cv.put(Message.FLAG_READ, 0);
        mMockResolver.update(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI,
                id), cv, null, null);
        mSyncAdapter.mUpdatedIdList.add(id);
        assertEquals(1, EmailContent.count(mMockContext, Message.CONTENT_URI, null, null));
        assertEquals(2, EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, null, null));
        assertEquals(1, EmailContent.count(mMockContext, Message.UPDATED_CONTENT_URI, null, null));
        mSyncAdapter.cleanup();
        assertEquals(0, EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, null, null));
        assertEquals(0, EmailContent.count(mMockContext, Message.UPDATED_CONTENT_URI, null, null));
    }
}
