public class EmailHtmlUtilTest extends AndroidTestCase {
    private EmailContent.Account mAccount;
    private long mCreatedAccountId = -1;
    private static final String textTags = "<b>Plain</b> &";
    private static final String textSpaces = "3 spaces   end.";
    private static final String textNewlines = "ab \r\n  \n   \n\r\n";
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Context context = getContext();
        long accountId = Account.getDefaultAccountId(context);
        if (accountId == -1) {
            Account account = new Account();
            account.mSenderName = "Bob Sender";
            account.mEmailAddress = "bob@sender.com";
            account.save(context);
            accountId = account.mId;
            mCreatedAccountId = accountId;
        }
        Account.restoreAccountWithId(context, accountId);
        BinaryTempFileBody.setTempDirectory(getContext().getCacheDir());
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        Context context = getContext();
        if (mCreatedAccountId > -1) {
            context.getContentResolver().delete(
                    ContentUris.withAppendedId(Account.CONTENT_URI, mCreatedAccountId), null, null);
        }
    }
    public void disable_testResolveInlineImage() throws MessagingException, IOException {
        final LocalStore store = (LocalStore) LocalStore.newInstance(
                mAccount.getLocalStoreUri(getContext()), mContext, null);
        final String cid1 = "cid.1@android.com";
        final long aid1 = 10;
        final Uri uri1 = MessageTestUtils.contentUri(aid1, mAccount);
        final String text1     = new TextBuilder("text1 > ").addCidImg(cid1).build(" <.");
        final String expected1 = new TextBuilder("text1 > ").addUidImg(uri1).build(" <.");
        final Message msg1 = new MessageBuilder()
            .setBody(new MultipartBuilder("multipart/related")
                .addBodyPart(MessageTestUtils.textPart("text/html", text1))
                .addBodyPart(MessageTestUtils.imagePart("image/jpeg", "<"+cid1+">", aid1, store))
                .build())
            .build();
        final String actual1 = EmailHtmlUtil.resolveInlineImage(
                getContext().getContentResolver(), mAccount.mId, text1, msg1, 0);
        assertEquals("one content id reference is not resolved",
                    expected1, actual1);
        final String actual0 = EmailHtmlUtil.resolveInlineImage(
                getContext().getContentResolver(), mAccount.mId, text1, msg1, 10);
        assertEquals("recursive call limit may exceeded",
                    text1, actual0);
        final String cid2 = "cid.2@android.com";
        final long aid2 = 20;
        final Uri uri2 = MessageTestUtils.contentUri(aid2, mAccount);
        final String text2     = new TextBuilder("text2 ").addCidImg(cid2).build(".");
        final String expected2 = new TextBuilder("text2 ").addUidImg(uri2).build(".");
        final Message msg2 = new MessageBuilder()
            .setBody(new MultipartBuilder("multipart/related")
                .addBodyPart(MessageTestUtils.textPart("text/html", text1 + text2))
                .addBodyPart(MessageTestUtils.imagePart("image/gif", cid2, aid2, store))
                .build())
            .build();
        final String actual2 = EmailHtmlUtil.resolveInlineImage(
                getContext().getContentResolver(), mAccount.mId, text1 + text2, msg2, 0);
        assertEquals("only one of two content id is resolved",
                text1 + expected2, actual2);
        final Message msg3 = new MessageBuilder()
            .setBody(new MultipartBuilder("multipart/related")
                .addBodyPart(MessageTestUtils.textPart("text/html", text2 + text1))
                .addBodyPart(MessageTestUtils.imagePart("image/jpeg", cid1, aid1, store))
                .addBodyPart(MessageTestUtils.imagePart("image/gif", cid2, aid2, store))
                .build())
            .build();
        final String actual3 = EmailHtmlUtil.resolveInlineImage(
                getContext().getContentResolver(), mAccount.mId, text2 + text1, msg3, 0);
        assertEquals("two content ids are resolved correctly",
                expected2 + expected1, actual3);
        final Message msg4 = new MessageBuilder()
            .setBody(new MultipartBuilder("multipart/mixed")
                .addBodyPart(MessageTestUtils.imagePart("image/jpeg", null, 30, store))
                .addBodyPart(MessageTestUtils.imagePart("application/pdf", cid1, aid1, store))
                .addBodyPart(new MultipartBuilder("multipart/related")
                    .addBodyPart(MessageTestUtils.textPart("text/html", text2 + text1))
                    .addBodyPart(MessageTestUtils.imagePart("image/jpg", cid1, aid1, store))
                    .addBodyPart(MessageTestUtils.imagePart("image/gif", cid2, aid2, store))
                    .buildBodyPart())
                .addBodyPart(MessageTestUtils.imagePart("application/pdf", cid2, aid2, store))
                .build())
            .build();
        final String actual4 = EmailHtmlUtil.resolveInlineImage(
                getContext().getContentResolver(), mAccount.mId, text2 + text1, msg4, 0);
        assertEquals("two content ids in deep multipart level are resolved",
                expected2 + expected1, actual4);
        final String actual5 = EmailHtmlUtil.resolveInlineImage(getContext().getContentResolver(),
                                                                mAccount.mId, null, msg4, 0);
        assertNull(actual5);
    }
    public void testEscapeCharacterToDisplayPlainText() {
        String plainTags = EmailHtmlUtil.escapeCharacterToDisplay(textTags);
        assertEquals("plain tag", "&lt;b&gt;Plain&lt;/b&gt; &amp;", plainTags);
        String plainSpaces = EmailHtmlUtil.escapeCharacterToDisplay(textSpaces);
        assertEquals("plain spaces", "3 spaces&nbsp;&nbsp; end.", plainSpaces);
        String plainNewlines = EmailHtmlUtil.escapeCharacterToDisplay(textNewlines);
        assertEquals("plain spaces", "ab <br>&nbsp; <br>&nbsp;&nbsp; <br><br>", plainNewlines);
        String textAll = textTags + "\n" + textSpaces + "\n" + textNewlines;
        String plainAll = EmailHtmlUtil.escapeCharacterToDisplay(textAll);
        assertEquals("plain all",      
                "&lt;b&gt;Plain&lt;/b&gt; &amp;<br>" +
                "3 spaces&nbsp;&nbsp; end.<br>" +
                "ab <br>&nbsp; <br>&nbsp;&nbsp; <br><br>",
                plainAll);
     }
}
