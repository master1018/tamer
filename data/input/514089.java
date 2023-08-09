public class ProviderTests extends ProviderTestCase2<EmailProvider> {
    EmailProvider mProvider;
    Context mMockContext;
    public ProviderTests() {
        super(EmailProvider.class, EmailProvider.EMAIL_AUTHORITY);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockContext = getMockContext();
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
    public void testAccountSave() {
        Account account1 = ProviderTestUtils.setupAccount("account-save", true, mMockContext);
        long account1Id = account1.mId;
        Account account2 = EmailContent.Account.restoreAccountWithId(mMockContext, account1Id);
        ProviderTestUtils.assertAccountEqual("testAccountSave", account1, account2);
    }
    public void testAccountSaveHostAuth() {
        Account account1 = ProviderTestUtils.setupAccount("account-hostauth", false, mMockContext);
        account1.mHostAuthRecv = ProviderTestUtils.setupHostAuth("account-hostauth-recv", -1, false,
                mMockContext);
        account1.mHostAuthSend = ProviderTestUtils.setupHostAuth("account-hostauth-send", -1, false,
                mMockContext);
        account1.save(mMockContext);
        long account1Id = account1.mId;
        Account account1get = EmailContent.Account.restoreAccountWithId(mMockContext, account1Id);
        ProviderTestUtils.assertAccountEqual("testAccountSave", account1, account1get);
        HostAuth hostAuth1get = EmailContent.HostAuth.restoreHostAuthWithId(mMockContext,
                account1get.mHostAuthKeyRecv);
        ProviderTestUtils.assertHostAuthEqual("testAccountSaveHostAuth-recv",
                account1.mHostAuthRecv, hostAuth1get);
        HostAuth hostAuth2get = EmailContent.HostAuth.restoreHostAuthWithId(mMockContext,
                account1get.mHostAuthKeySend);
        ProviderTestUtils.assertHostAuthEqual("testAccountSaveHostAuth-send",
                account1.mHostAuthSend, hostAuth2get);
    }
    public void testAccountParcel() {
        Account account1 = ProviderTestUtils.setupAccount("parcel", false, mMockContext);
        Bundle b = new Bundle();
        b.putParcelable("account", account1);
        Parcel p = Parcel.obtain();
        b.writeToParcel(p, 0);
        p.setDataPosition(0);       
        Bundle b2 = new Bundle(Account.class.getClassLoader());
        b2.readFromParcel(p);
        Account account2 = (Account) b2.getParcelable("account");
        p.recycle();
        ProviderTestUtils.assertAccountEqual("testAccountParcel", account1, account2);
    }
    public void testAccountShortcutSafeUri() {
        final Account account1 = ProviderTestUtils.setupAccount("account-1", true, mMockContext);
        final Account account2 = ProviderTestUtils.setupAccount("account-2", true, mMockContext);
        final long account1Id = account1.mId;
        final long account2Id = account2.mId;
        final Uri uri1 = account1.getShortcutSafeUri();
        final Uri uri2 = account2.getShortcutSafeUri();
        MoreAsserts.assertEquals(new String[] {"account", account1.mCompatibilityUuid},
                uri1.getPathSegments().toArray());
        MoreAsserts.assertEquals(new String[] {"account", account2.mCompatibilityUuid},
                uri2.getPathSegments().toArray());
        assertEquals(account1Id, Account.getAccountIdFromShortcutSafeUri(mMockContext, uri1));
        assertEquals(account2Id, Account.getAccountIdFromShortcutSafeUri(mMockContext, uri2));
        assertEquals(account1Id, Account.getAccountIdFromShortcutSafeUri(mMockContext,
                getEclairStyleShortcutUri(account1)));
        assertEquals(account2Id, Account.getAccountIdFromShortcutSafeUri(mMockContext,
                getEclairStyleShortcutUri(account2)));
    }
    private static Uri getEclairStyleShortcutUri(Account account) {
        return Account.CONTENT_URI.buildUpon().appendEncodedPath("" + account.mId).build();
    }
    public void testAccountIsValidId() {
        final Account account1 = ProviderTestUtils.setupAccount("account-1", true, mMockContext);
        final Account account2 = ProviderTestUtils.setupAccount("account-2", true, mMockContext);
        assertTrue(Account.isValidId(mMockContext, account1.mId));
        assertTrue(Account.isValidId(mMockContext, account2.mId));
        assertFalse(Account.isValidId(mMockContext, 1234567)); 
        assertFalse(Account.isValidId(mMockContext, -1));
        assertFalse(Account.isValidId(mMockContext, -500));
    }
    private final static String[] MAILBOX_UNREAD_COUNT_PROJECTION = new String [] {
        MailboxColumns.UNREAD_COUNT
    };
    private final static int MAILBOX_UNREAD_COUNT_COLMUN = 0;
    private int getUnreadCount(long mailboxId) {
        String text = null;
        Cursor c = null;
        try {
            c = mMockContext.getContentResolver().query(
                    Mailbox.CONTENT_URI,
                    MAILBOX_UNREAD_COUNT_PROJECTION,
                    EmailContent.RECORD_ID + "=?",
                    new String[] { String.valueOf(mailboxId) },
                    null);
            c.moveToFirst();
            text = c.getString(MAILBOX_UNREAD_COUNT_COLMUN);
        } finally {
            c.close();
        }
        return Integer.valueOf(text);
    }
    @SuppressWarnings("deprecation")
    public void testHostAuthSecurityUri() {
        HostAuth ha = ProviderTestUtils.setupHostAuth("uri-security", 1, false, mMockContext);
        final int MASK =
            HostAuth.FLAG_SSL | HostAuth.FLAG_TLS | HostAuth.FLAG_TRUST_ALL_CERTIFICATES;
        ha.setStoreUri("protocol:
        assertEquals(0, ha.mFlags & MASK);
        ha.setStoreUri("protocol+ssl+:
        assertEquals(HostAuth.FLAG_SSL, ha.mFlags & MASK);
        ha.setStoreUri("protocol+ssl+trustallcerts:
        assertEquals(HostAuth.FLAG_SSL | HostAuth.FLAG_TRUST_ALL_CERTIFICATES, ha.mFlags & MASK);
        ha.setStoreUri("protocol+tls+:
        assertEquals(HostAuth.FLAG_TLS, ha.mFlags & MASK);
        ha.setStoreUri("protocol+tls+trustallcerts:
        assertEquals(HostAuth.FLAG_TLS | HostAuth.FLAG_TRUST_ALL_CERTIFICATES, ha.mFlags & MASK);
        ha.mFlags &= ~MASK;
        String uriString = ha.getStoreUri();
        assertTrue(uriString.startsWith("protocol:
        ha.mFlags |= HostAuth.FLAG_SSL;
        uriString = ha.getStoreUri();
        assertTrue(uriString.startsWith("protocol+ssl+:
        ha.mFlags |= HostAuth.FLAG_TRUST_ALL_CERTIFICATES;
        uriString = ha.getStoreUri();
        assertTrue(uriString.startsWith("protocol+ssl+trustallcerts:
        ha.mFlags &= ~MASK;
        ha.mFlags |= HostAuth.FLAG_TLS;
        uriString = ha.getStoreUri();
        assertTrue(uriString.startsWith("protocol+tls+:
        ha.mFlags |= HostAuth.FLAG_TRUST_ALL_CERTIFICATES;
        uriString = ha.getStoreUri();
        assertTrue(uriString.startsWith("protocol+tls+trustallcerts:
    }
    @SuppressWarnings("deprecation")
    public void testHostAuthPortAssignments() {
        HostAuth ha = ProviderTestUtils.setupHostAuth("uri-port", 1, false, mMockContext);
        ha.setStoreUri("imap:
        assertEquals(123, ha.mPort);
        ha.setStoreUri("imap:
        assertEquals(143, ha.mPort);
        ha.setStoreUri("imap+ssl:
        assertEquals(993, ha.mPort);
        ha.setStoreUri("imap+ssl+trustallcerts:
        assertEquals(993, ha.mPort);
        ha.setStoreUri("imap+tls:
        assertEquals(143, ha.mPort);
        ha.setStoreUri("imap+tls+trustallcerts:
        assertEquals(143, ha.mPort);
        ha.setStoreUri("pop3:
        assertEquals(123, ha.mPort);
        ha.setStoreUri("pop3:
        assertEquals(110, ha.mPort);
        ha.setStoreUri("pop3+ssl:
        assertEquals(995, ha.mPort);
        ha.setStoreUri("pop3+ssl+trustallcerts:
        assertEquals(995, ha.mPort);
        ha.setStoreUri("pop3+tls:
        assertEquals(110, ha.mPort);
        ha.setStoreUri("pop3+tls+trustallcerts:
        assertEquals(110, ha.mPort);
        ha.setStoreUri("eas:
        assertEquals(123, ha.mPort);
        ha.setStoreUri("eas:
        assertEquals(80, ha.mPort);
        ha.setStoreUri("eas+ssl:
        assertEquals(443, ha.mPort);
        ha.setStoreUri("eas+ssl+trustallcerts:
        assertEquals(443, ha.mPort);
        ha.setStoreUri("smtp:
        assertEquals(123, ha.mPort);
        ha.setStoreUri("smtp:
        assertEquals(587, ha.mPort);
        ha.setStoreUri("smtp+ssl:
        assertEquals(465, ha.mPort);
        ha.setStoreUri("smtp+ssl+trustallcerts:
        assertEquals(465, ha.mPort);
        ha.setStoreUri("smtp+tls:
        assertEquals(587, ha.mPort);
        ha.setStoreUri("smtp+tls+trustallcerts:
        assertEquals(587, ha.mPort);
    }
    public void testMailboxSave() {
        Account account1 = ProviderTestUtils.setupAccount("mailbox-save", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true,
                mMockContext);
        long box1Id = box1.mId;
        Mailbox box2 = EmailContent.Mailbox.restoreMailboxWithId(mMockContext, box1Id);
        ProviderTestUtils.assertMailboxEqual("testMailboxSave", box1, box2);
    }
    private static String[] expectedAttachmentNames =
        new String[] {"attachment1.doc", "attachment2.xls", "attachment3"};
    private static long[] expectedAttachmentSizes = new long[] {31415L, 97701L, 151213L};
    private Body loadBodyForMessageId(long messageId) {
        Cursor c = null;
        try {
            c = mMockContext.getContentResolver().query(
                    EmailContent.Body.CONTENT_URI,
                    EmailContent.Body.CONTENT_PROJECTION,
                    EmailContent.Body.MESSAGE_KEY + "=?",
                    new String[] {String.valueOf(messageId)},
                    null);
            int numBodies = c.getCount();
            assertTrue("at most one body", numBodies < 2);
            return c.moveToFirst() ? EmailContent.getContent(c, Body.class) : null;
        } finally {
            c.close();
        }
    }
    public void testMessageSave() {
        Account account1 = ProviderTestUtils.setupAccount("message-save", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        Message message1get = EmailContent.Message.restoreMessageWithId(mMockContext, message1Id);
        ProviderTestUtils.assertMessageEqual("testMessageSave", message1, message1get);
        Message message2 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, true,
                true, mMockContext);
        long message2Id = message2.mId;
        String text2 = message2.mText;
        String html2 = message2.mHtml;
        String textReply2 = message2.mTextReply;
        String htmlReply2 = message2.mHtmlReply;
        long sourceKey2 = message2.mSourceKey;
        String introText2 = message2.mIntroText;
        message2.mText = null;
        message2.mHtml = null;
        message2.mTextReply = null;
        message2.mHtmlReply = null;
        message2.mSourceKey = 0;
        message2.mIntroText = null;
        Message message2get = EmailContent.Message.restoreMessageWithId(mMockContext, message2Id);
        ProviderTestUtils.assertMessageEqual("testMessageSave", message2, message2get);
        Body body2 = loadBodyForMessageId(message2Id);
        assertEquals("body text", text2, body2.mTextContent);
        assertEquals("body html", html2, body2.mHtmlContent);
        assertEquals("reply text", textReply2, body2.mTextReply);
        assertEquals("reply html", htmlReply2, body2.mHtmlReply);
        assertEquals("source key", sourceKey2, body2.mSourceKey);
        assertEquals("intro text", introText2, body2.mIntroText);
        Message message3 = ProviderTestUtils.setupMessage("message3", account1Id, box1Id, true,
                false, mMockContext);
        ArrayList<Attachment> atts = new ArrayList<Attachment>();
        for (int i = 0; i < 3; i++) {
            atts.add(ProviderTestUtils.setupAttachment(
                    -1, expectedAttachmentNames[i], expectedAttachmentSizes[i],
                    false, mMockContext));
        }
        message3.mAttachments = atts;
        message3.save(mMockContext);
        long message3Id = message3.mId;
        Cursor c = null;
        try {
            c = mMockContext.getContentResolver().query(
                    Attachment.CONTENT_URI,
                    Attachment.CONTENT_PROJECTION,
                    Attachment.MESSAGE_KEY + "=?",
                    new String[] {
                            String.valueOf(message3Id)
                    },
                    Attachment.SIZE);
            int numAtts = c.getCount();
            assertEquals(3, numAtts);
            int i = 0;
            while (c.moveToNext()) {
                Attachment actual = EmailContent.getContent(c, Attachment.class);
                ProviderTestUtils.assertAttachmentEqual("save-message3", atts.get(i), actual);
                i++;
            }
        } finally {
            c.close();
        }
        Message message4 = ProviderTestUtils.setupMessage("message4", account1Id, box1Id, false,
                false, mMockContext);
        atts = new ArrayList<Attachment>();
        for (int i = 0; i < 3; i++) {
            atts.add(ProviderTestUtils.setupAttachment(
                    -1, expectedAttachmentNames[i], expectedAttachmentSizes[i],
                    false, mMockContext));
        }
        message4.mAttachments = atts;
        message4.save(mMockContext);
        long message4Id = message4.mId;
        c = null;
        try {
            c = mMockContext.getContentResolver().query(
                    Attachment.CONTENT_URI,
                    Attachment.CONTENT_PROJECTION,
                    Attachment.MESSAGE_KEY + "=?",
                    new String[] {
                            String.valueOf(message4Id)
                    },
                    Attachment.SIZE);
            int numAtts = c.getCount();
            assertEquals(3, numAtts);
            int i = 0;
            while (c.moveToNext()) {
                Attachment actual = EmailContent.getContent(c, Attachment.class);
                ProviderTestUtils.assertAttachmentEqual("save-message4", atts.get(i), actual);
                i++;
            }
        } finally {
            c.close();
        }
        Attachment[] attachments =
            Attachment.restoreAttachmentsWithMessageId(mMockContext, message4Id);
        int size = attachments.length;
        assertEquals(3, size);
        for (int i = 0; i < size; ++i) {
            ProviderTestUtils.assertAttachmentEqual("save-message4", atts.get(i), attachments[i]);
        }
    }
    public void testAccountDelete() {
        Account account1 = ProviderTestUtils.setupAccount("account-delete-1", true, mMockContext);
        long account1Id = account1.mId;
        Account account2 = ProviderTestUtils.setupAccount("account-delete-2", true, mMockContext);
        long account2Id = account2.mId;
        int numBoxes = EmailContent.count(mMockContext, Account.CONTENT_URI, null, null);
        assertEquals(2, numBoxes);
        Uri uri = ContentUris.withAppendedId(Account.CONTENT_URI, account1Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numBoxes = EmailContent.count(mMockContext, Account.CONTENT_URI, null, null);
        assertEquals(1, numBoxes);
        uri = ContentUris.withAppendedId(Account.CONTENT_URI, account2Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numBoxes = EmailContent.count(mMockContext, Account.CONTENT_URI, null, null);
        assertEquals(0, numBoxes);
    }
    public void testLookupBodyIdWithMessageId() {
        final ContentResolver resolver = mMockContext.getContentResolver();
        Account account1 = ProviderTestUtils.setupAccount("orphaned body", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        long bodyId1 = Body.lookupBodyIdWithMessageId(resolver, message1Id);
        assertEquals(bodyId1, -1);
        Message message2 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, true,
                true, mMockContext);
        long message2Id = message2.mId;
        long bodyId2 = Body.lookupBodyIdWithMessageId(resolver, message2Id);
        Body body = loadBodyForMessageId(message2Id);
        assertNotNull(body);
        assertEquals(body.mId, bodyId2);
    }
    public void testUpdateBodyWithMessageId() {
        Account account1 = ProviderTestUtils.setupAccount("orphaned body", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        final String textContent = "foobar some odd text";
        final String htmlContent = "and some html";
        final String textReply = "plain text reply";
        final String htmlReply = "or the html reply";
        final String introText = "fred wrote:";
        ContentValues values = new ContentValues();
        values.put(BodyColumns.TEXT_CONTENT, textContent);
        values.put(BodyColumns.HTML_CONTENT, htmlContent);
        values.put(BodyColumns.TEXT_REPLY, textReply);
        values.put(BodyColumns.HTML_REPLY, htmlReply);
        values.put(BodyColumns.SOURCE_MESSAGE_KEY, 17);
        values.put(BodyColumns.INTRO_TEXT, introText);
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        Body body1 = loadBodyForMessageId(message1Id);
        assertNull(body1);
        Body.updateBodyWithMessageId(mMockContext, message1Id, values);
        body1 = loadBodyForMessageId(message1Id);
        assertNotNull(body1);
        assertEquals(body1.mTextContent, textContent);
        assertEquals(body1.mHtmlContent, htmlContent);
        assertEquals(body1.mTextReply, textReply);
        assertEquals(body1.mHtmlReply, htmlReply);
        assertEquals(body1.mSourceKey, 17);
        assertEquals(body1.mIntroText, introText);
        Message message2 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, true,
                true, mMockContext);
        long message2Id = message2.mId;
        Body body2 = loadBodyForMessageId(message2Id);
        assertNotNull(body2);
        assertTrue(!body2.mTextContent.equals(textContent));
        Body.updateBodyWithMessageId(mMockContext, message2Id, values);
        body2 = loadBodyForMessageId(message1Id);
        assertNotNull(body2);
        assertEquals(body2.mTextContent, textContent);
        assertEquals(body2.mHtmlContent, htmlContent);
        assertEquals(body2.mTextReply, textReply);
        assertEquals(body2.mHtmlReply, htmlReply);
        assertEquals(body2.mSourceKey, 17);
        assertEquals(body2.mIntroText, introText);
    }
    public void testBodyRetrieve() {
        Message message1 = ProviderTestUtils.setupMessage("bodyretrieve", 1, 1, true,
                true, mMockContext);
        long messageId = message1.mId;
        assertEquals(message1.mText,
                Body.restoreBodyTextWithMessageId(mMockContext, messageId));
        assertEquals(message1.mHtml,
                Body.restoreBodyHtmlWithMessageId(mMockContext, messageId));
        assertEquals(message1.mTextReply,
                Body.restoreReplyTextWithMessageId(mMockContext, messageId));
        assertEquals(message1.mHtmlReply,
                Body.restoreReplyHtmlWithMessageId(mMockContext, messageId));
        assertEquals(message1.mIntroText,
                Body.restoreIntroTextWithMessageId(mMockContext, messageId));
        assertEquals(message1.mSourceKey,
                Body.restoreBodySourceKey(mMockContext, messageId));
    }
    public void testDeleteBody() {
        final ContentResolver resolver = mMockContext.getContentResolver();
        Account account1 = ProviderTestUtils.setupAccount("orphaned body", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        Message message2 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, true,
                true, mMockContext);
        long message2Id = message2.mId;
        assertNotNull(loadBodyForMessageId(message2Id));
        resolver.delete(ContentUris.withAppendedId(Message.CONTENT_URI, message1Id), null, null);
        assertNotNull(loadBodyForMessageId(message2Id));
        resolver.delete(ContentUris.withAppendedId(Message.CONTENT_URI, message2Id), null, null);
        assertNull(loadBodyForMessageId(message2Id));
    }
    public void testDeleteOrphanBodies() {
        final ContentResolver resolver = mMockContext.getContentResolver();
        Account account1 = ProviderTestUtils.setupAccount("orphaned body", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Mailbox box2 = ProviderTestUtils.setupMailbox("box2", account1Id, true, mMockContext);
        long box2Id = box2.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        Message message2 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, true,
                true, mMockContext);
        long message2Id = message2.mId;
        assertNotNull(loadBodyForMessageId(message2Id));
        resolver.delete(ContentUris.withAppendedId(Message.CONTENT_URI, message1Id), null, null);
        resolver.delete(ContentUris.withAppendedId(Mailbox.CONTENT_URI, box2Id), null, null);
        assertNotNull(loadBodyForMessageId(message2Id));
    }
     public void testDeleteOrphanMessages() {
        final ContentResolver resolver = mMockContext.getContentResolver();
        final Context context = mMockContext;
        Account acct = ProviderTestUtils.setupAccount("orphaned body", true, context);
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", acct.mId, true, context);
        Mailbox box2 = ProviderTestUtils.setupMailbox("box2", acct.mId, true, context);
        Message msg1_1 =
            ProviderTestUtils.setupMessage("message1", acct.mId, box1.mId, false, true, context);
        Message msg1_2 =
            ProviderTestUtils.setupMessage("message2", acct.mId, box1.mId, false, true, context);
        Message msg1_3 =
            ProviderTestUtils.setupMessage("message3", acct.mId, box1.mId, false, true, context);
        Message msg1_4 =
            ProviderTestUtils.setupMessage("message4", acct.mId, box1.mId, false, true, context);
        Message msg2_1 =
            ProviderTestUtils.setupMessage("message1", acct.mId, box2.mId, false, true, context);
        Message msg2_2 =
            ProviderTestUtils.setupMessage("message2", acct.mId, box2.mId, false, true, context);
        Message msg2_3 =
            ProviderTestUtils.setupMessage("message3", acct.mId, box2.mId, false, true, context);
        Message msg2_4 =
            ProviderTestUtils.setupMessage("message4", acct.mId, box2.mId, false, true, context);
        resolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg1_1.mId),
                null, null);
        resolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg1_2.mId),
                null, null);
        resolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg2_1.mId),
                null, null);
        resolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg2_2.mId),
                null, null);
        assertEquals(4, EmailContent.count(context, Message.DELETED_CONTENT_URI, null, null));
        ContentValues v = new ContentValues();
        v.put(MessageColumns.DISPLAY_NAME, "--updated--");
        resolver.update(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg1_3.mId),
                v, null, null);
        resolver.update(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg1_4.mId),
                v, null, null);
        resolver.update(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg2_3.mId),
                v, null, null);
        resolver.update(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, msg2_4.mId),
                v, null, null);
        assertEquals(4, EmailContent.count(context, Message.UPDATED_CONTENT_URI, null, null));
        long delBoxId = 10;
        Message msgX_A =
            ProviderTestUtils.setupMessage("messageA", acct.mId, delBoxId, false, false, context);
        Message msgX_B =
            ProviderTestUtils.setupMessage("messageB", acct.mId, delBoxId, false, false, context);
        Message msgX_C =
            ProviderTestUtils.setupMessage("messageC", acct.mId, delBoxId, false, false, context);
        Message msgX_D =
            ProviderTestUtils.setupMessage("messageD", acct.mId, delBoxId, false, false, context);
        ContentValues cv;
        long msgId = 10;
        try {
            cv = msgX_A.toContentValues();
            cv.put(EmailContent.RECORD_ID, msgId++);
            resolver.insert(Message.DELETED_CONTENT_URI, cv);
        } catch (IllegalArgumentException e) {
        }
        try {
            cv = msgX_B.toContentValues();
            cv.put(EmailContent.RECORD_ID, msgId++);
            resolver.insert(Message.DELETED_CONTENT_URI, cv);
        } catch (IllegalArgumentException e) {
        }
        try {
            cv = msgX_C.toContentValues();
            cv.put(EmailContent.RECORD_ID, msgId++);
            resolver.insert(Message.UPDATED_CONTENT_URI, cv);
        } catch (IllegalArgumentException e) {
        }
        try {
            cv = msgX_D.toContentValues();
            cv.put(EmailContent.RECORD_ID, msgId++);
            resolver.insert(Message.UPDATED_CONTENT_URI, cv);
        } catch (IllegalArgumentException e) {
        }
        assertEquals(6, EmailContent.count(context, Message.UPDATED_CONTENT_URI, null, null));
        assertEquals(6, EmailContent.count(context, Message.DELETED_CONTENT_URI, null, null));
        EmailProvider.deleteOrphans(EmailProvider.getReadableDatabase(context),
                Message.DELETED_TABLE_NAME);
        EmailProvider.deleteOrphans(EmailProvider.getReadableDatabase(context),
                Message.UPDATED_TABLE_NAME);
        assertEquals(4, EmailContent.count(context, Message.UPDATED_CONTENT_URI, null, null));
        assertEquals(4, EmailContent.count(context, Message.DELETED_CONTENT_URI, null, null));
    }
    public void testMailboxDelete() {
        Account account1 = ProviderTestUtils.setupAccount("mailbox-delete", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Mailbox box2 = ProviderTestUtils.setupMailbox("box2", account1Id, true, mMockContext);
        long box2Id = box2.mId;
        String selection = EmailContent.MailboxColumns.ACCOUNT_KEY + "=?";
        String[] selArgs = new String[] { String.valueOf(account1Id) };
        int numBoxes = EmailContent.count(mMockContext, Mailbox.CONTENT_URI, selection, selArgs);
        assertEquals(2, numBoxes);
        Uri uri = ContentUris.withAppendedId(Mailbox.CONTENT_URI, box1Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numBoxes = EmailContent.count(mMockContext, Mailbox.CONTENT_URI, selection, selArgs);
        assertEquals(1, numBoxes);
        uri = ContentUris.withAppendedId(Mailbox.CONTENT_URI, box2Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numBoxes = EmailContent.count(mMockContext, Mailbox.CONTENT_URI, selection, selArgs);
        assertEquals(0, numBoxes);
    }
    public void testMessageDelete() {
        Account account1 = ProviderTestUtils.setupAccount("message-delete", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        Message message2 = ProviderTestUtils.setupMessage("message2", account1Id, box1Id, false,
                true, mMockContext);
        long message2Id = message2.mId;
        String selection = EmailContent.MessageColumns.ACCOUNT_KEY + "=? AND " +
                EmailContent.MessageColumns.MAILBOX_KEY + "=?";
        String[] selArgs = new String[] { String.valueOf(account1Id), String.valueOf(box1Id) };
        int numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(2, numMessages);
        Uri uri = ContentUris.withAppendedId(Message.CONTENT_URI, message1Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(1, numMessages);
        uri = ContentUris.withAppendedId(Message.CONTENT_URI, message2Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(0, numMessages);
    }
    public void testSyncedMessageDelete() {
        Account account1 = ProviderTestUtils.setupAccount("synced-message-delete", true,
                mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        Message message2 = ProviderTestUtils.setupMessage("message2", account1Id, box1Id, false,
                true, mMockContext);
        long message2Id = message2.mId;
        String selection = EmailContent.MessageColumns.ACCOUNT_KEY + "=? AND "
                + EmailContent.MessageColumns.MAILBOX_KEY + "=?";
        String[] selArgs = new String[] {
            String.valueOf(account1Id), String.valueOf(box1Id)
        };
        int numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(2, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, selection,
                selArgs);
        assertEquals(0, numMessages);
        Uri uri = ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, message1Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(1, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, selection,
                selArgs);
        assertEquals(1, numMessages);
        uri = ContentUris.withAppendedId(Message.CONTENT_URI, message2Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(0, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, selection,
                selArgs);
        assertEquals(1, numMessages);
    }
    public void testMessageUpdate() {
        Account account1 = ProviderTestUtils.setupAccount("message-update", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mMockContext);
        long message1Id = message1.mId;
        Message message2 = ProviderTestUtils.setupMessage("message2", account1Id, box1Id, false,
                true, mMockContext);
        long message2Id = message2.mId;
        ContentResolver cr = mMockContext.getContentResolver();
        String selection = EmailContent.MessageColumns.ACCOUNT_KEY + "=? AND "
                + EmailContent.MessageColumns.MAILBOX_KEY + "=?";
        String[] selArgs = new String[] {
            String.valueOf(account1Id), String.valueOf(box1Id)
        };
        int numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(2, numMessages);
        Uri uri = ContentUris.withAppendedId(Message.CONTENT_URI, message1Id);
        ContentValues cv = new ContentValues();
        cv.put(MessageColumns.FROM_LIST, "from-list");
        cr.update(uri, cv, null, null);
        numMessages = EmailContent.count(mMockContext, Message.UPDATED_CONTENT_URI, selection,
                selArgs);
        assertEquals(0, numMessages);
        Message restoredMessage = Message.restoreMessageWithId(mMockContext, message1Id);
        assertEquals("from-list", restoredMessage.mFrom);
        uri = ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, message2Id);
        cv = new ContentValues();
        cv.put(MessageColumns.FROM_LIST, "from-list");
        cr.update(uri, cv, null, null);
        numMessages = EmailContent.count(mMockContext, Message.UPDATED_CONTENT_URI, selection,
                selArgs);
        assertEquals(1, numMessages);
        restoredMessage = Message.restoreMessageWithId(mMockContext, message2Id);
        assertEquals("from-list", restoredMessage.mFrom);
        Cursor c = cr.query(Message.UPDATED_CONTENT_URI, Message.CONTENT_PROJECTION, null, null,
                null);
        try {
            assertTrue(c.moveToFirst());
            Message originalMessage = EmailContent.getContent(c, Message.class);
            assertEquals("from message2", originalMessage.mFrom);
            assertFalse(c.moveToNext());
        } finally {
            c.close();
        }
        cr.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, message2Id), null, null);
        numMessages = EmailContent.count(mMockContext, Message.UPDATED_CONTENT_URI, selection,
                selArgs);
        assertEquals(0, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, selection,
                selArgs);
        assertEquals(1, numMessages);
    }
    public void testCascadeDeleteAccount() {
        Account account1 = ProviderTestUtils.setupAccount("account-delete-cascade", true,
                mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
         ProviderTestUtils.setupMessage("message1", account1Id, box1Id,
                false, true, mMockContext);
         ProviderTestUtils.setupMessage("message2", account1Id, box1Id,
                false, true, mMockContext);
        int numAccounts = EmailContent.count(mMockContext, Account.CONTENT_URI, null, null);
        assertEquals(1, numAccounts);
        int numBoxes = EmailContent.count(mMockContext, Mailbox.CONTENT_URI, null, null);
        assertEquals(1, numBoxes);
        int numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, null, null);
        assertEquals(2, numMessages);
        Uri uri = ContentUris.withAppendedId(Account.CONTENT_URI, account1Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numAccounts = EmailContent.count(mMockContext, Account.CONTENT_URI, null, null);
        assertEquals(0, numAccounts);
        numBoxes = EmailContent.count(mMockContext, Mailbox.CONTENT_URI, null, null);
        assertEquals(0, numBoxes);
        numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, null, null);
        assertEquals(0, numMessages);
    }
    public void testCascadeDeleteMailbox() {
        Account account1 = ProviderTestUtils.setupAccount("mailbox-delete-cascade", true,
                mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id,
                false, true, mMockContext);
        Message message2 = ProviderTestUtils.setupMessage("message2", account1Id, box1Id,
                false, true, mMockContext);
        Message message3 = ProviderTestUtils.setupMessage("message3", account1Id, box1Id,
                false, true, mMockContext);
        Message message4 = ProviderTestUtils.setupMessage("message4", account1Id, box1Id,
                false, true, mMockContext);
        ProviderTestUtils.setupMessage("message5", account1Id, box1Id, false, true, mMockContext);
        ProviderTestUtils.setupMessage("message6", account1Id, box1Id, false, true, mMockContext);
        String selection = EmailContent.MessageColumns.ACCOUNT_KEY + "=? AND " +
                EmailContent.MessageColumns.MAILBOX_KEY + "=?";
        String[] selArgs = new String[] { String.valueOf(account1Id), String.valueOf(box1Id) };
        int numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(6, numMessages);
        ContentValues cv = new ContentValues();
        cv.put(Message.SERVER_ID, "SERVER_ID");
        ContentResolver resolver = mMockContext.getContentResolver();
        resolver.update(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, message1.mId),
                cv, null, null);
        resolver.update(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, message2.mId),
                cv, null, null);
        resolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, message3.mId),
                null, null);
        resolver.delete(ContentUris.withAppendedId(Message.SYNCED_CONTENT_URI, message4.mId),
                null, null);
        numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(4, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, selection,
                selArgs);
        assertEquals(2, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.UPDATED_CONTENT_URI, selection,
                selArgs);
        assertEquals(2, numMessages);
        Uri uri = ContentUris.withAppendedId(Mailbox.CONTENT_URI, box1Id);
        resolver.delete(uri, null, null);
        numMessages = EmailContent.count(mMockContext, Message.CONTENT_URI, selection, selArgs);
        assertEquals(0, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.DELETED_CONTENT_URI, selection,
                selArgs);
        assertEquals(0, numMessages);
        numMessages = EmailContent.count(mMockContext, Message.UPDATED_CONTENT_URI, selection,
                selArgs);
        assertEquals(0, numMessages);
    }
    public void testCascadeMessageDelete() {
        Account account1 = ProviderTestUtils.setupAccount("message-cascade", true, mMockContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mMockContext);
        long box1Id = box1.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, true,
                false, mMockContext);
        ArrayList<Attachment> atts = new ArrayList<Attachment>();
        for (int i = 0; i < 2; i++) {
            atts.add(ProviderTestUtils.setupAttachment(
                    -1, expectedAttachmentNames[i], expectedAttachmentSizes[i],
                    false, mMockContext));
        }
        message1.mAttachments = atts;
        message1.save(mMockContext);
        long message1Id = message1.mId;
        Message message2 = ProviderTestUtils.setupMessage("message2", account1Id, box1Id, true,
                false, mMockContext);
        atts = new ArrayList<Attachment>();
        for (int i = 0; i < 2; i++) {
            atts.add(ProviderTestUtils.setupAttachment(
                    -1, expectedAttachmentNames[i], expectedAttachmentSizes[i],
                    false, mMockContext));
        }
        message2.mAttachments = atts;
        message2.save(mMockContext);
        long message2Id = message2.mId;
        String bodySelection = BodyColumns.MESSAGE_KEY + " IN (?,?)";
        String attachmentSelection = AttachmentColumns.MESSAGE_KEY + " IN (?,?)";
        String[] selArgs = new String[] { String.valueOf(message1Id), String.valueOf(message2Id) };
        int numBodies = EmailContent.count(mMockContext, Body.CONTENT_URI, bodySelection, selArgs);
        assertEquals(2, numBodies);
        int numAttachments = EmailContent.count(mMockContext, Attachment.CONTENT_URI,
                attachmentSelection, selArgs);
        assertEquals(4, numAttachments);
        Uri uri = ContentUris.withAppendedId(Message.CONTENT_URI, message1Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numBodies = EmailContent.count(mMockContext, Body.CONTENT_URI, bodySelection, selArgs);
        assertEquals(1, numBodies);
        numAttachments = EmailContent.count(mMockContext, Attachment.CONTENT_URI,
                attachmentSelection, selArgs);
        assertEquals(2, numAttachments);
        uri = ContentUris.withAppendedId(Message.CONTENT_URI, message2Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        numBodies = EmailContent.count(mMockContext, Body.CONTENT_URI, bodySelection, selArgs);
        assertEquals(0, numBodies);
        numAttachments = EmailContent.count(mMockContext, Attachment.CONTENT_URI,
                attachmentSelection, selArgs);
        assertEquals(0, numAttachments);
    }
    public void testCreateUniqueFile() throws IOException {
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return;
        }
        try {
            String fileName = "A11achm3n1.doc";
            File uniqueFile = Attachment.createUniqueFile(fileName);
            assertEquals(fileName, uniqueFile.getName());
            if (uniqueFile.createNewFile()) {
                uniqueFile = Attachment.createUniqueFile(fileName);
                assertEquals("A11achm3n1-2.doc", uniqueFile.getName());
                if (uniqueFile.createNewFile()) {
                    uniqueFile = Attachment.createUniqueFile(fileName);
                    assertEquals("A11achm3n1-3.doc", uniqueFile.getName());
                }
           }
            fileName = "A11achm3n1";
            uniqueFile = Attachment.createUniqueFile(fileName);
            assertEquals(fileName, uniqueFile.getName());
            if (uniqueFile.createNewFile()) {
                uniqueFile = Attachment.createUniqueFile(fileName);
                assertEquals("A11achm3n1-2", uniqueFile.getName());
            }
        } finally {
            File directory = Environment.getExternalStorageDirectory();
            String[] fileNames = new String[] {"A11achm3n1.doc", "A11achm3n1-2.doc", "A11achm3n1"};
            int length = fileNames.length;
            for (int i = 0; i < length; i++) {
                File file = new File(directory, fileNames[i]);
                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }
    public void testGetAttachmentByMessageIdUri() {
        Attachment a1 = ProviderTestUtils.setupAttachment(1, "a1", 100, true, mMockContext);
        Attachment a2 = ProviderTestUtils.setupAttachment(1, "a2", 200, true, mMockContext);
        ProviderTestUtils.setupAttachment(2, "a3", 300, true, mMockContext);
        ProviderTestUtils.setupAttachment(2, "a4", 400, true, mMockContext);
        Uri uri = ContentUris.withAppendedId(Attachment.MESSAGE_ID_URI, 1);
        Cursor c = mMockContext.getContentResolver().query(uri, Attachment.CONTENT_PROJECTION,
                null, null, Attachment.SIZE);
        assertEquals(2, c.getCount());
        try {
            c.moveToFirst();
            Attachment a1Get = EmailContent.getContent(c, Attachment.class);
            ProviderTestUtils.assertAttachmentEqual("getAttachByUri-1", a1, a1Get);
            c.moveToNext();
            Attachment a2Get = EmailContent.getContent(c, Attachment.class);
            ProviderTestUtils.assertAttachmentEqual("getAttachByUri-2", a2, a2Get);
        } finally {
            c.close();
        }
    }
    public void testDeleteAttachmentByMessageIdUri() {
        ContentResolver mockResolver = mMockContext.getContentResolver();
        ProviderTestUtils.setupAttachment(1, "a1", 100, true, mMockContext);
        ProviderTestUtils.setupAttachment(1, "a2", 200, true, mMockContext);
        Attachment a3 = ProviderTestUtils.setupAttachment(2, "a3", 300, true, mMockContext);
        Attachment a4 = ProviderTestUtils.setupAttachment(2, "a4", 400, true, mMockContext);
        Uri uri = ContentUris.withAppendedId(Attachment.MESSAGE_ID_URI, 1);
        mockResolver.delete(uri, null, null);
        Cursor c = mockResolver.query(Attachment.CONTENT_URI, Attachment.CONTENT_PROJECTION,
                null, null, Attachment.SIZE);
        assertEquals(2, c.getCount());
        try {
            c.moveToFirst();
            Attachment a3Get = EmailContent.getContent(c, Attachment.class);
            ProviderTestUtils.assertAttachmentEqual("getAttachByUri-3", a3, a3Get);
            c.moveToNext();
            Attachment a4Get = EmailContent.getContent(c, Attachment.class);
            ProviderTestUtils.assertAttachmentEqual("getAttachByUri-4", a4, a4Get);
        } finally {
            c.close();
        }
    }
    public void testSetGetDefaultAccount() {
        long defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertEquals(-1, defaultAccountId);
        Account account1 = ProviderTestUtils.setupAccount("account-default-1", true, mMockContext);
        long account1Id = account1.mId;
        Account account2 = ProviderTestUtils.setupAccount("account-default-2", true, mMockContext);
        long account2Id = account2.mId;
        Account account3 = ProviderTestUtils.setupAccount("account-default-3", true, mMockContext);
        long account3Id = account3.mId;
        defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertTrue(defaultAccountId == account1Id
                    || defaultAccountId == account2Id
                    || defaultAccountId == account3Id);
        updateIsDefault(account1, true);
        defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertEquals(account1Id, defaultAccountId);
        updateIsDefault(account2, true);
        defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertEquals(account2Id, defaultAccountId);
        updateIsDefault(account3, true);
        defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertEquals(account3Id, defaultAccountId);
        Uri uri = ContentUris.withAppendedId(Account.CONTENT_URI, account1Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertEquals(account3Id, defaultAccountId);
        uri = ContentUris.withAppendedId(Account.CONTENT_URI, account3Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertEquals(account2Id, defaultAccountId);
        uri = ContentUris.withAppendedId(Account.CONTENT_URI, account2Id);
        mMockContext.getContentResolver().delete(uri, null, null);
        defaultAccountId = Account.getDefaultAccountId(mMockContext);
        assertEquals(-1, defaultAccountId);
    }
    private void updateIsDefault(Account account, boolean newState) {
        account.setDefaultAccount(newState);
        ContentValues cv = new ContentValues();
        cv.put(AccountColumns.IS_DEFAULT, account.mIsDefault);
        account.update(mMockContext, cv);
    }
    public static Message setupUnreadMessage(String name, long accountId, long mailboxId,
            boolean addBody, boolean saveIt, Context context) {
        Message msg =
            ProviderTestUtils.setupMessage(name, accountId, mailboxId, addBody, false, context);
        msg.mFlagRead = false;
        if (saveIt) {
            msg.save(context);
        }
        return msg;
    }
    public void testUnreadCountTriggers() {
        Account account = ProviderTestUtils.setupAccount("triggers", true, mMockContext);
        Mailbox boxA = ProviderTestUtils.setupMailbox("boxA", account.mId, true, mMockContext);
        Mailbox boxB = ProviderTestUtils.setupMailbox("boxB", account.mId, true, mMockContext);
        Mailbox boxC = ProviderTestUtils.setupMailbox("boxC", account.mId, true, mMockContext);
        assertEquals(0, getUnreadCount(boxA.mId));
        assertEquals(0, getUnreadCount(boxB.mId));
        assertEquals(0, getUnreadCount(boxC.mId));
        Message message1 = setupUnreadMessage("message1", account.mId, boxA.mId,
                false, true, mMockContext);
        Message message2= setupUnreadMessage("message2", account.mId, boxA.mId,
                false, true, mMockContext);
        Message message3 =  setupUnreadMessage("message3", account.mId, boxA.mId,
                false, true, mMockContext);
        setupUnreadMessage("message4", account.mId, boxC.mId, false, true, mMockContext);
        assertEquals(3, getUnreadCount(boxA.mId));
        assertEquals(0, getUnreadCount(boxB.mId));
        assertEquals(1, getUnreadCount(boxC.mId));
        ContentResolver cr = mMockContext.getContentResolver();
        Uri uri = ContentUris.withAppendedId(Message.CONTENT_URI, message1.mId);
        cr.delete(uri, null, null);
        assertEquals(2, getUnreadCount(boxA.mId));
        assertEquals(0, getUnreadCount(boxB.mId));
        assertEquals(1, getUnreadCount(boxC.mId));
        message2.mMailboxKey = boxB.mId;
        ContentValues cv = new ContentValues();
        cv.put(MessageColumns.MAILBOX_KEY, boxB.mId);
        cr.update(ContentUris.withAppendedId(Message.CONTENT_URI, message2.mId), cv, null, null);
        assertEquals(1, getUnreadCount(boxA.mId));
        assertEquals(1, getUnreadCount(boxB.mId));
        assertEquals(1, getUnreadCount(boxC.mId));
        cv.clear();
        cv.put(MessageColumns.FLAG_READ, 1);
        cr.update(ContentUris.withAppendedId(Message.CONTENT_URI, message3.mId), cv, null, null);
        assertEquals(0, getUnreadCount(boxA.mId));
        assertEquals(1, getUnreadCount(boxB.mId));
        assertEquals(1, getUnreadCount(boxC.mId));
        message3.mMailboxKey = boxC.mId;
        cv.clear();
        cv.put(MessageColumns.MAILBOX_KEY, boxC.mId);
        cr.update(ContentUris.withAppendedId(Message.CONTENT_URI, message3.mId), cv, null, null);
        assertEquals(0, getUnreadCount(boxA.mId));
        assertEquals(1, getUnreadCount(boxB.mId));
        assertEquals(1, getUnreadCount(boxC.mId));
        cv.clear();
        cv.put(MessageColumns.FLAG_READ, 0);
        cr.update(ContentUris.withAppendedId(Message.CONTENT_URI, message3.mId), cv, null, null);
        assertEquals(0, getUnreadCount(boxA.mId));
        assertEquals(1, getUnreadCount(boxB.mId));
        assertEquals(2, getUnreadCount(boxC.mId));
    }
    public void testCreateIndex() {
        String oldStr = "create index message_" + MessageColumns.TIMESTAMP
            + " on " + Message.TABLE_NAME + " (" + MessageColumns.TIMESTAMP + ");";
        String newStr = EmailProvider.createIndex(Message.TABLE_NAME, MessageColumns.TIMESTAMP);
        assertEquals(newStr, oldStr);
    }
    public void testIdAddToField() {
        ContentResolver cr = mMockContext.getContentResolver();
        ContentValues cv = new ContentValues();
        Account account = ProviderTestUtils.setupAccount("field-add", true, mMockContext);
        int startCount = account.mNewMessageCount;
        cv.put(EmailContent.FIELD_COLUMN_NAME, AccountColumns.NEW_MESSAGE_COUNT);
        cv.put(EmailContent.ADD_COLUMN_NAME, 17);
        cr.update(ContentUris.withAppendedId(Account.ADD_TO_FIELD_URI, account.mId),
                cv, null, null);
        Account restoredAccount = Account.restoreAccountWithId(mMockContext, account.mId);
        assertEquals(17 + startCount, restoredAccount.mNewMessageCount);
        cv.put(EmailContent.ADD_COLUMN_NAME, -11);
        cr.update(ContentUris.withAppendedId(Account.ADD_TO_FIELD_URI, account.mId),
                cv, null, null);
        restoredAccount = Account.restoreAccountWithId(mMockContext, account.mId);
        assertEquals(17 - 11 + startCount, restoredAccount.mNewMessageCount);
        Mailbox boxA = ProviderTestUtils.setupMailbox("boxA", account.mId, true, mMockContext);
        assertEquals(0, boxA.mUnreadCount);
        cv.put(EmailContent.FIELD_COLUMN_NAME, MailboxColumns.UNREAD_COUNT);
        cv.put(EmailContent.ADD_COLUMN_NAME, 11);
        cr.update(ContentUris.withAppendedId(Mailbox.ADD_TO_FIELD_URI, boxA.mId), cv, null, null);
        Mailbox restoredBoxA = Mailbox.restoreMailboxWithId(mMockContext, boxA.mId);
        assertEquals(11, restoredBoxA.mUnreadCount);
    }
    public void testDatabaseCorruptionRecovery() {
        final ContentResolver resolver = mMockContext.getContentResolver();
        final Context context = mMockContext;
        Account acct = ProviderTestUtils.setupAccount("acct1", true, context);
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", acct.mId, true, context);
        ProviderTestUtils.setupMessage("message1", acct.mId, box1.mId, true, true, context);
        ProviderTestUtils.setupMessage("message2", acct.mId, box1.mId, true, true, context);
        ProviderTestUtils.setupMessage("message3", acct.mId, box1.mId, true, true, context);
        ProviderTestUtils.setupMessage("message4", acct.mId, box1.mId, true, true, context);
        int count = EmailContent.count(mMockContext, Message.CONTENT_URI, null, null);
        assertEquals(4, count);
        count = EmailContent.count(mMockContext, Body.CONTENT_URI, null, null);
        assertEquals(4, count);
        File dbFile = mMockContext.getDatabasePath(EmailProvider.DATABASE_NAME);
        assertTrue(dbFile != null);
        assertTrue(dbFile.exists());
        assertTrue(dbFile.delete());
        assertFalse(dbFile.exists());
        dbFile = mMockContext.getDatabasePath(EmailProvider.BODY_DATABASE_NAME);
        assertTrue(dbFile != null);
        assertTrue(dbFile.exists());
        resolver.update(EmailProvider.INTEGRITY_CHECK_URI, null, null, null);
        count = EmailContent.count(mMockContext, Body.CONTENT_URI, null, null);
        assertEquals(0, count);
        count = EmailContent.count(mMockContext, Message.CONTENT_URI, null, null);
        assertEquals(0, count);
    }
    public void testBodyDatabaseCorruptionRecovery() {
        final ContentResolver resolver = mMockContext.getContentResolver();
        final Context context = mMockContext;
        Account acct = ProviderTestUtils.setupAccount("acct1", true, context);
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", acct.mId, true, context);
        ProviderTestUtils.setupMessage("message1", acct.mId, box1.mId, true, true, context);
        ProviderTestUtils.setupMessage("message2", acct.mId, box1.mId, true, true, context);
        ProviderTestUtils.setupMessage("message3", acct.mId, box1.mId, true, true, context);
        ProviderTestUtils.setupMessage("message4", acct.mId, box1.mId, true, true, context);
        int count = EmailContent.count(mMockContext, Message.CONTENT_URI, null, null);
        assertEquals(4, count);
        count = EmailContent.count(mMockContext, Body.CONTENT_URI, null, null);
        assertEquals(4, count);
        File dbFile = mMockContext.getDatabasePath(EmailProvider.BODY_DATABASE_NAME);
        assertTrue(dbFile != null);
        assertTrue(dbFile.exists());
        assertTrue(dbFile.delete());
        assertFalse(dbFile.exists());
        dbFile = mMockContext.getDatabasePath(EmailProvider.DATABASE_NAME);
        assertTrue(dbFile != null);
        assertTrue(dbFile.exists());
        resolver.update(EmailProvider.INTEGRITY_CHECK_URI, null, null, null);
        count = EmailContent.count(mMockContext, Message.CONTENT_URI, null, null);
        assertEquals(0, count);
        count = EmailContent.count(mMockContext, Body.CONTENT_URI, null, null);
        assertEquals(0, count);
    }
    public void testFindMailboxOfType() {
        final Context context = mMockContext;
        Account acct1 = ProviderTestUtils.setupAccount("acct1", true, context);
        Mailbox acct1Inbox =
            ProviderTestUtils.setupMailbox("Inbox1", acct1.mId, true, context, Mailbox.TYPE_INBOX);
        Mailbox acct1Calendar
        = ProviderTestUtils.setupMailbox("Cal1", acct1.mId, true, context, Mailbox.TYPE_CALENDAR);
        Mailbox acct1Contacts =
            ProviderTestUtils.setupMailbox("Con1", acct1.mId, true, context, Mailbox.TYPE_CONTACTS);
        Account acct2 = ProviderTestUtils.setupAccount("acct1", true, context);
        Mailbox acct2Inbox =
            ProviderTestUtils.setupMailbox("Inbox2", acct2.mId, true, context, Mailbox.TYPE_INBOX);
        Mailbox acct2Calendar =
            ProviderTestUtils.setupMailbox("Cal2", acct2.mId, true, context, Mailbox.TYPE_CALENDAR);
        Mailbox acct2Contacts =
            ProviderTestUtils.setupMailbox("Con2", acct2.mId, true, context, Mailbox.TYPE_CONTACTS);
        assertEquals(acct1Inbox.mId,
                Mailbox.findMailboxOfType(context, acct1.mId, Mailbox.TYPE_INBOX));
        assertEquals(acct2Inbox.mId,
                Mailbox.findMailboxOfType(context, acct2.mId, Mailbox.TYPE_INBOX));
        assertEquals(acct1Calendar.mId,
                Mailbox.findMailboxOfType(context, acct1.mId, Mailbox.TYPE_CALENDAR));
        assertEquals(acct2Calendar.mId,
                Mailbox.findMailboxOfType(context, acct2.mId, Mailbox.TYPE_CALENDAR));
        assertEquals(acct1Contacts.mId,
                Mailbox.findMailboxOfType(context, acct1.mId, Mailbox.TYPE_CONTACTS));
        assertEquals(acct2Contacts.mId,
                Mailbox.findMailboxOfType(context, acct2.mId, Mailbox.TYPE_CONTACTS));
    }
    public void testRestoreMailboxOfType() {
        final Context context = mMockContext;
        Account acct1 = ProviderTestUtils.setupAccount("acct1", true, context);
        Mailbox acct1Inbox =
            ProviderTestUtils.setupMailbox("Inbox1", acct1.mId, true, context, Mailbox.TYPE_INBOX);
        Mailbox acct1Calendar
        = ProviderTestUtils.setupMailbox("Cal1", acct1.mId, true, context, Mailbox.TYPE_CALENDAR);
        Mailbox acct1Contacts =
            ProviderTestUtils.setupMailbox("Con1", acct1.mId, true, context, Mailbox.TYPE_CONTACTS);
        Account acct2 = ProviderTestUtils.setupAccount("acct1", true, context);
        Mailbox acct2Inbox =
            ProviderTestUtils.setupMailbox("Inbox2", acct2.mId, true, context, Mailbox.TYPE_INBOX);
        Mailbox acct2Calendar =
            ProviderTestUtils.setupMailbox("Cal2", acct2.mId, true, context, Mailbox.TYPE_CALENDAR);
        Mailbox acct2Contacts =
            ProviderTestUtils.setupMailbox("Con2", acct2.mId, true, context, Mailbox.TYPE_CONTACTS);
        ProviderTestUtils.assertMailboxEqual("testRestoreMailboxOfType", acct1Inbox,
                Mailbox.restoreMailboxOfType(context, acct1.mId, Mailbox.TYPE_INBOX));
        ProviderTestUtils.assertMailboxEqual("testRestoreMailboxOfType", acct2Inbox,
                Mailbox.restoreMailboxOfType(context, acct2.mId, Mailbox.TYPE_INBOX));
        ProviderTestUtils.assertMailboxEqual("testRestoreMailboxOfType", acct1Calendar,
                Mailbox.restoreMailboxOfType(context, acct1.mId, Mailbox.TYPE_CALENDAR));
        ProviderTestUtils.assertMailboxEqual("testRestoreMailboxOfType", acct2Calendar,
                Mailbox.restoreMailboxOfType(context, acct2.mId, Mailbox.TYPE_CALENDAR));
        ProviderTestUtils.assertMailboxEqual("testRestoreMailboxOfType", acct1Contacts,
                Mailbox.restoreMailboxOfType(context, acct1.mId, Mailbox.TYPE_CONTACTS));
        ProviderTestUtils.assertMailboxEqual("testRestoreMailboxOfType", acct2Contacts,
                Mailbox.restoreMailboxOfType(context, acct2.mId, Mailbox.TYPE_CONTACTS));
    }
}
