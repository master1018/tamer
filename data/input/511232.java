public class AttachmentProviderTests extends ProviderTestCase2<AttachmentProvider> {
    private final boolean USE_LOCALSTORE = false;
    LocalStore mLocalStore = null;
    EmailProvider mEmailProvider;
    Context mMockContext;
    ContentResolver mMockResolver;
    public AttachmentProviderTests() {
        super(AttachmentProvider.class, AttachmentProvider.AUTHORITY);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockContext = getMockContext();
        mMockResolver = mMockContext.getContentResolver();
        mEmailProvider = new EmailProvider();
        mEmailProvider.attachInfo(mMockContext, null);
        assertNotNull(mEmailProvider);
        ((MockContentResolver) mMockResolver)
                .addProvider(EmailProvider.EMAIL_AUTHORITY, mEmailProvider);
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        if (mLocalStore != null) {
            mLocalStore.delete();
        }
    }
    public void testUnimplemented() {
        assertEquals(0, mMockResolver.delete(AttachmentProvider.CONTENT_URI, null, null));
        assertEquals(0, mMockResolver.update(AttachmentProvider.CONTENT_URI, null, null, null));
        assertEquals(null, mMockResolver.insert(AttachmentProvider.CONTENT_URI, null));
    }
    public void testQuery() throws MessagingException {
        Account account1 = ProviderTestUtils.setupAccount("attachment-query", false, mMockContext);
        account1.mCompatibilityUuid = "test-UUID";
        account1.save(mMockContext);
        final long message1Id = 1;
        long attachment1Id = 1;
        long attachment2Id = 2;
        long attachment3Id = 3;
        Uri attachment1Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment1Id);
        Uri attachment2Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment2Id);
        Uri attachment3Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment3Id);
        Cursor c = mMockResolver.query(attachment1Uri, (String[])null, null, (String[])null, null);
        assertNull(c);
        setupAttachmentDatabase(account1);
        c = mMockResolver.query(attachment1Uri, (String[])null, null, (String[])null, null);
        assertNull(c);
        Attachment newAttachment1 = ProviderTestUtils.setupAttachment(message1Id, "file1", 100,
                false, mMockContext);
        newAttachment1.mContentUri =
            AttachmentProvider.getAttachmentUri(account1.mId, attachment1Id).toString();
        attachment1Id = addAttachmentToDb(account1, newAttachment1);
        assertEquals("Broken test:  Unexpected id assignment", 1, attachment1Id);
        Attachment newAttachment2 = ProviderTestUtils.setupAttachment(message1Id, "file2", 200,
                false, mMockContext);
        newAttachment2.mContentUri =
            AttachmentProvider.getAttachmentUri(account1.mId, attachment2Id).toString();
        attachment2Id = addAttachmentToDb(account1, newAttachment2);
        assertEquals("Broken test:  Unexpected id assignment", 2, attachment2Id);
        Attachment newAttachment3 = ProviderTestUtils.setupAttachment(message1Id, "file3", 300,
                false, mMockContext);
        newAttachment3.mContentUri =
            AttachmentProvider.getAttachmentUri(account1.mId, attachment3Id).toString();
        attachment3Id = addAttachmentToDb(account1, newAttachment3);
        assertEquals("Broken test:  Unexpected id assignment", 3, attachment3Id);
        attachment2Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment2Id);
        c = mMockResolver.query(
                attachment2Uri,
                new String[] { AttachmentProviderColumns._ID, AttachmentProviderColumns.DATA,
                               AttachmentProviderColumns.DISPLAY_NAME,
                               AttachmentProviderColumns.SIZE },
                null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(attachment2Id, c.getLong(0));                  
        assertEquals(attachment2Uri.toString(), c.getString(1));    
        assertEquals("file2", c.getString(2));                      
        assertEquals(200, c.getInt(3));                             
        attachment3Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment3Id);
        c = mMockResolver.query(
                attachment3Uri,
                new String[] { AttachmentProviderColumns.SIZE,
                               AttachmentProviderColumns.DISPLAY_NAME,
                               AttachmentProviderColumns.DATA, AttachmentProviderColumns._ID },
                null, null, null);
        assertEquals(1, c.getCount());
        assertTrue(c.moveToFirst());
        assertEquals(attachment3Id, c.getLong(3));                  
        assertEquals(attachment3Uri.toString(), c.getString(2));    
        assertEquals("file3", c.getString(1));                      
        assertEquals(300, c.getInt(0));                             
    }
    public void testGetType() throws MessagingException {
        Account account1 = ProviderTestUtils.setupAccount("get-type", false, mMockContext);
        account1.mCompatibilityUuid = "test-UUID";
        account1.save(mMockContext);
        final long message1Id = 1;
        long attachment1Id = 1;
        long attachment2Id = 2;
        long attachment3Id = 3;
        long attachment4Id = 4;
        long attachment5Id = 5;
        long attachment6Id = 6;
        Uri attachment1Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment1Id);
        String type = mMockResolver.getType(attachment1Uri);
        assertNull(type);
        setupAttachmentDatabase(account1);
        type = mMockResolver.getType(attachment1Uri);
        assertNull(type);
        Attachment newAttachment2 = ProviderTestUtils.setupAttachment(message1Id, "file2", 100,
                false, mMockContext);
        newAttachment2.mMimeType = "image/jpg";
        attachment2Id = addAttachmentToDb(account1, newAttachment2);
        Attachment newAttachment3 = ProviderTestUtils.setupAttachment(message1Id, "file3", 100,
                false, mMockContext);
        newAttachment3.mMimeType = "text/plain";
        attachment3Id = addAttachmentToDb(account1, newAttachment3);
        Attachment newAttachment4 = ProviderTestUtils.setupAttachment(message1Id, "file4.doc", 100,
                false, mMockContext);
        newAttachment4.mMimeType = "application/octet-stream";
        attachment4Id = addAttachmentToDb(account1, newAttachment4);
        Attachment newAttachment5 = ProviderTestUtils.setupAttachment(message1Id, "file5.xyz", 100,
                false, mMockContext);
        newAttachment5.mMimeType = "application/octet-stream";
        attachment5Id = addAttachmentToDb(account1, newAttachment5);
        Attachment newAttachment6 = ProviderTestUtils.setupAttachment(message1Id, "file6", 100,
                false, mMockContext);
        newAttachment6.mMimeType = "";
        attachment6Id = addAttachmentToDb(account1, newAttachment6);
        Uri uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment2Id);
        type = mMockResolver.getType(uri);
        assertEquals("image/jpg", type);
        uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment3Id);
        type = mMockResolver.getType(uri);
        assertEquals("text/plain", type);
        uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment4Id);
        type = mMockResolver.getType(uri);
        assertEquals("application/msword", type);
        uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment5Id);
        type = mMockResolver.getType(uri);
        assertEquals("application/xyz", type);
        uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment6Id);
        type = mMockResolver.getType(uri);
        assertEquals("application/octet-stream", type);
        uri = AttachmentProvider.getAttachmentThumbnailUri(account1.mId, attachment2Id, 62, 62);
        type = mMockResolver.getType(uri);
        assertEquals("image/png", type);
        uri = AttachmentProvider.getAttachmentThumbnailUri(account1.mId, attachment3Id, 62, 62);
        type = mMockResolver.getType(uri);
        assertEquals("image/png", type);
    }
    public void testInferMimeType() {
        final String DEFAULT = "application/octet-stream";
        final String FILE_PDF = "myfile.false.pdf";
        final String FILE_ABC = "myfile.false.abc";
        final String FILE_NO_EXT = "myfile";
        assertEquals("mime/type", AttachmentProvider.inferMimeType(null, "mime/type"));
        assertEquals("mime/type", AttachmentProvider.inferMimeType("", "mime/type"));
        assertEquals("mime/type", AttachmentProvider.inferMimeType(FILE_PDF, "mime/type"));
        assertEquals("application/pdf", AttachmentProvider.inferMimeType(FILE_PDF, null));
        assertEquals("application/pdf", AttachmentProvider.inferMimeType(FILE_PDF, ""));
        assertEquals("application/pdf", AttachmentProvider.inferMimeType(FILE_PDF, DEFAULT));
        assertEquals("application/abc", AttachmentProvider.inferMimeType(FILE_ABC, null));
        assertEquals("application/abc", AttachmentProvider.inferMimeType(FILE_ABC, ""));
        assertEquals("application/abc", AttachmentProvider.inferMimeType(FILE_ABC, DEFAULT));
        assertEquals(DEFAULT, AttachmentProvider.inferMimeType(FILE_NO_EXT, null));
        assertEquals(DEFAULT, AttachmentProvider.inferMimeType(FILE_NO_EXT, ""));
        assertEquals(DEFAULT, AttachmentProvider.inferMimeType(FILE_NO_EXT, DEFAULT));
        assertEquals(DEFAULT, AttachmentProvider.inferMimeType(null, null));
        assertEquals(DEFAULT, AttachmentProvider.inferMimeType("", ""));
    }
    public void testOpenFile() throws MessagingException, IOException {
        Account account1 = ProviderTestUtils.setupAccount("open-file", false, mMockContext);
        account1.mCompatibilityUuid = "test-UUID";
        account1.save(mMockContext);
        final long message1Id = 1;
        long attachment1Id = 1;
        long attachment2Id = 2;
        Uri file1Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment1Id);
        Uri file2Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment2Id);
        AssetFileDescriptor afd;
        try {
            afd = mMockResolver.openAssetFileDescriptor(file1Uri, "r");
            fail("Should throw an exception on a bad URI");
        } catch (FileNotFoundException fnf) {
        }
        setupAttachmentDatabase(account1);
        try {
            afd = mMockResolver.openAssetFileDescriptor(file1Uri, "r");
            fail("Should throw an exception on a missing attachment entry");
        } catch (FileNotFoundException fnf) {
        }
        Attachment newAttachment = ProviderTestUtils.setupAttachment(message1Id, "file", 100,
                false, mMockContext);
        attachment1Id = addAttachmentToDb(account1, newAttachment);
        assertEquals("Broken test:  Unexpected id assignment", 1, attachment1Id);
        try {
            afd = mMockResolver.openAssetFileDescriptor(file1Uri, "r");
            fail("Should throw an exception on a missing attachment file");
        } catch (FileNotFoundException fnf) {
        }
         createAttachmentFile(account1, attachment2Id);
        Attachment newAttachment2 = ProviderTestUtils.setupAttachment(message1Id, "file", 100,
                false, mMockContext);
        newAttachment2.mContentId = null;
        newAttachment2.mContentUri =
                AttachmentProvider.getAttachmentUri(account1.mId, attachment2Id).toString();
        newAttachment2.mMimeType = "image/png";
        attachment2Id = addAttachmentToDb(account1, newAttachment2);
        assertEquals("Broken test:  Unexpected id assignment", 2, attachment2Id);
        afd = mMockResolver.openAssetFileDescriptor(file2Uri, "r");
        assertNotNull(afd);
        afd.close();
    }
    public void testOpenThumbnail() throws MessagingException, IOException {
        Account account1 = ProviderTestUtils.setupAccount("open-thumbnail", false, mMockContext);
        account1.mCompatibilityUuid = "test-UUID";
        account1.save(mMockContext);
        final long message1Id = 1;
        long attachment1Id = 1;
        long attachment2Id = 2;
        Uri thumb1Uri = AttachmentProvider.getAttachmentThumbnailUri(account1.mId, attachment1Id,
                62, 62);
        Uri thumb2Uri = AttachmentProvider.getAttachmentThumbnailUri(account1.mId, attachment2Id,
                62, 62);
        AssetFileDescriptor afd = mMockResolver.openAssetFileDescriptor(thumb1Uri, "r");
        assertNull(afd);
        setupAttachmentDatabase(account1);
        afd = mMockResolver.openAssetFileDescriptor(thumb1Uri, "r");
        assertNull(afd);
        Attachment newAttachment = ProviderTestUtils.setupAttachment(message1Id, "file", 100,
                false, mMockContext);
        attachment1Id = addAttachmentToDb(account1, newAttachment);
        assertEquals("Broken test:  Unexpected id assignment", 1, attachment1Id);
        afd = mMockResolver.openAssetFileDescriptor(thumb1Uri, "r");
        assertNull(afd);
         createAttachmentFile(account1, attachment2Id);
        Attachment newAttachment2 = ProviderTestUtils.setupAttachment(message1Id, "file", 100,
                false, mMockContext);
        newAttachment2.mContentId = null;
        newAttachment2.mContentUri =
                AttachmentProvider.getAttachmentUri(account1.mId, attachment2Id).toString();
        newAttachment2.mMimeType = "image/png";
        attachment2Id = addAttachmentToDb(account1, newAttachment2);
        assertEquals("Broken test:  Unexpected id assignment", 2, attachment2Id);
        afd = mMockResolver.openAssetFileDescriptor(thumb2Uri, "r");
        assertNotNull(afd);
        afd.close();
    }
    private Uri createAttachment(Account account, long messageId, String contentUriStr) {
        Attachment newAttachment = ProviderTestUtils.setupAttachment(messageId, "file", 100,
                false, mMockContext);
        newAttachment.mContentUri = contentUriStr;
        long attachmentId = addAttachmentToDb(account, newAttachment);
        Uri attachmentUri = AttachmentProvider.getAttachmentUri(account.mId, attachmentId);
        return attachmentUri;
    }
    public void testResolveAttachmentIdToContentUri() throws MessagingException {
        Account account1 = ProviderTestUtils.setupAccount("attachment-query", false, mMockContext);
        account1.mCompatibilityUuid = "test-UUID";
        account1.save(mMockContext);
        final long message1Id = 1;
        final long attachment1Id = 1;
        final Uri attachment1Uri = AttachmentProvider.getAttachmentUri(account1.mId, attachment1Id);
        Uri result = AttachmentProvider.resolveAttachmentIdToContentUri(
                mMockResolver, attachment1Uri);
        assertEquals(attachment1Uri, result);
        setupAttachmentDatabase(account1);
        result = AttachmentProvider.resolveAttachmentIdToContentUri(
                mMockResolver, attachment1Uri);
        assertEquals(attachment1Uri, result);
        {
            Uri attachmentUri = createAttachment(account1, message1Id, "file:
            Uri contentUri = AttachmentProvider.resolveAttachmentIdToContentUri(mMockResolver, 
                    attachmentUri);
            assertEquals("file:
        }
        {
            Uri attachmentUri = createAttachment(account1, message1Id, null);
            Uri contentUri = AttachmentProvider.resolveAttachmentIdToContentUri(mMockResolver, 
                    attachmentUri);
            assertEquals(attachmentUri, contentUri);
        }
    }
    public void testDeleteFiles() throws IOException {
        Account account1 = ProviderTestUtils.setupAccount("attachment-query", false, mMockContext);
        account1.mCompatibilityUuid = "test-UUID";
        account1.save(mMockContext);
        final long message1Id = 1;      
        final long message2Id = 2;      
        final long message3Id = 3;      
        final long message4Id = 4;      
        Attachment newAttachment1 = ProviderTestUtils.setupAttachment(message1Id, "file1", 100,
                true, mMockContext);
        Attachment newAttachment2 = ProviderTestUtils.setupAttachment(message2Id, "file2", 200,
                true, mMockContext);
        Attachment newAttachment3 = ProviderTestUtils.setupAttachment(message2Id, "file3", 100,
                true, mMockContext);
        Attachment newAttachment4 = ProviderTestUtils.setupAttachment(message3Id, "file4", 100,
                true, mMockContext);
        createAttachmentFile(account1, newAttachment1.mId);
        createAttachmentFile(account1, newAttachment2.mId);
        createAttachmentFile(account1, newAttachment3.mId);
        File attachmentsDir = AttachmentProvider.getAttachmentDirectory(mMockContext, account1.mId);
        assertEquals(3, attachmentsDir.listFiles().length);
        AttachmentProvider.deleteAllAttachmentFiles(mMockContext, account1.mId, message4Id);
        assertEquals(3, attachmentsDir.listFiles().length);
        AttachmentProvider.deleteAllAttachmentFiles(mMockContext, account1.mId, message3Id);
        assertEquals(3, attachmentsDir.listFiles().length);
        AttachmentProvider.deleteAllAttachmentFiles(mMockContext, account1.mId, message2Id);
        assertEquals(1, attachmentsDir.listFiles().length);
        AttachmentProvider.deleteAllAttachmentFiles(mMockContext, account1.mId, message1Id);
        assertEquals(0, attachmentsDir.listFiles().length);
    }
    public void testDeleteMailbox() throws IOException {
        Account account1 = ProviderTestUtils.setupAccount("attach-mbox-del", false, mMockContext);
        account1.mCompatibilityUuid = "test-UUID";
        account1.save(mMockContext);
        long account1Id = account1.mId;
        Mailbox mailbox1 = ProviderTestUtils.setupMailbox("mbox1", account1Id, true, mMockContext);
        long mailbox1Id = mailbox1.mId;
        Mailbox mailbox2 = ProviderTestUtils.setupMailbox("mbox2", account1Id, true, mMockContext);
        long mailbox2Id = mailbox2.mId;
        Message message1a = ProviderTestUtils.setupMessage("msg1a", account1Id, mailbox1Id, false,
                true, mMockContext);
        Message message1b = ProviderTestUtils.setupMessage("msg1b", account1Id, mailbox1Id, false,
                true, mMockContext);
        Message message2a = ProviderTestUtils.setupMessage("msg2a", account1Id, mailbox2Id, false,
                true, mMockContext);
        Message message2b = ProviderTestUtils.setupMessage("msg2b", account1Id, mailbox2Id, false,
                true, mMockContext);
        Attachment newAttachment1 = ProviderTestUtils.setupAttachment(message1a.mId, "file1", 100,
                true, mMockContext);
        Attachment newAttachment2 = ProviderTestUtils.setupAttachment(message1a.mId, "file2", 200,
                true, mMockContext);
        Attachment newAttachment3 = ProviderTestUtils.setupAttachment(message1a.mId, "file3", 100,
                true, mMockContext);
        Attachment newAttachment4 = ProviderTestUtils.setupAttachment(message2a.mId, "file4", 100,
                true, mMockContext);
        createAttachmentFile(account1, newAttachment1.mId);
        createAttachmentFile(account1, newAttachment2.mId);
        createAttachmentFile(account1, newAttachment3.mId);
        createAttachmentFile(account1, newAttachment4.mId);
        File attachmentsDir = AttachmentProvider.getAttachmentDirectory(mMockContext, account1.mId);
        assertEquals(4, attachmentsDir.listFiles().length);
        AttachmentProvider.deleteAllMailboxAttachmentFiles(mMockContext, account1Id, mailbox1Id);
        assertEquals(1, attachmentsDir.listFiles().length);
        AttachmentProvider.deleteAllMailboxAttachmentFiles(mMockContext, account1Id, mailbox2Id);
        assertEquals(0, attachmentsDir.listFiles().length);
    }
    private String createAttachmentFile(Account forAccount, long id) throws IOException {
        File outFile = getAttachmentFile(forAccount, id);
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(),
                R.drawable.ic_email_attachment);
        FileOutputStream out = new FileOutputStream(outFile);
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        out.close();
        return outFile.getAbsolutePath();
    }
    private void setupAttachmentDatabase(Account forAccount) throws MessagingException {
        if (USE_LOCALSTORE) {
            String localStoreUri = "local:
            mLocalStore = (LocalStore) LocalStore.newInstance(localStoreUri, mMockContext, null);
        } else {
        }
    }
    private long addAttachmentToDb(Account forAccount, Attachment newAttachment) {
        long attachmentId = -1;
        if (USE_LOCALSTORE) {
            ContentValues cv = new ContentValues();
            cv.put("message_id", newAttachment.mMessageKey);
            cv.put("content_uri", newAttachment.mContentUri);
            cv.put("store_data", (String)null);
            cv.put("size", newAttachment.mSize);
            cv.put("name", newAttachment.mFileName);
            cv.put("mime_type", newAttachment.mMimeType);
            cv.put("content_id", newAttachment.mContentId);
            SQLiteDatabase db = null;
            try {
                db = SQLiteDatabase.openDatabase(dbName(forAccount), null, 0);
                attachmentId = db.insertOrThrow("attachments", "message_id", cv);
            }
            finally {
                if (db != null) {
                    db.close();
                }
            }
        } else {
            newAttachment.save(mMockContext);
            attachmentId = newAttachment.mId;
        }
        return attachmentId;
    }
    private String dbName(Account forAccount) {
        if (USE_LOCALSTORE) {
            return mMockContext.getDatabasePath(forAccount.mCompatibilityUuid + ".db").toString();
        } else {
            throw new java.lang.UnsupportedOperationException();
        }
    }
    private File getAttachmentFile(Account forAccount, long id) {
        String idString = Long.toString(id);
        if (USE_LOCALSTORE) {
            return new File(mMockContext.getDatabasePath(forAccount.mCompatibilityUuid + ".db_att"),
                    idString);
        } else {
            File attachmentsDir = mMockContext.getDatabasePath(forAccount.mId + ".db_att");
            if (!attachmentsDir.exists()) {
                attachmentsDir.mkdirs();
            }
            return new File(attachmentsDir, idString);
        }
    }
}
