public class ControllerProviderOpsTests extends ProviderTestCase2<EmailProvider> {
    EmailProvider mProvider;
    Context mProviderContext;
    Context mContext;
    public ControllerProviderOpsTests() {
        super(EmailProvider.class, EmailProvider.EMAIL_AUTHORITY);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mProviderContext = getMockContext();
        mContext = getContext();
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
    public static class TestController extends Controller {
        protected TestController(Context providerContext, Context systemContext) {
            super(systemContext);
            setProviderContext(providerContext);
        }
    }
    public void testGetMailboxServerName() {
        Controller ct = new TestController(mProviderContext, mContext);
        assertEquals("", ct.getMailboxServerName(-1));
        assertEquals("Inbox", ct.getMailboxServerName(Mailbox.TYPE_INBOX));
        assertEquals("Outbox", ct.getMailboxServerName(Mailbox.TYPE_OUTBOX));
        assertEquals("Trash", ct.getMailboxServerName(Mailbox.TYPE_TRASH));
        assertEquals("Sent", ct.getMailboxServerName(Mailbox.TYPE_SENT));
        assertEquals("Junk", ct.getMailboxServerName(Mailbox.TYPE_JUNK));
        Locale savedLocale = Locale.getDefault();
        Locale.setDefault(Locale.FRANCE);
        assertEquals("Inbox", ct.getMailboxServerName(Mailbox.TYPE_INBOX));
        assertEquals("Outbox", ct.getMailboxServerName(Mailbox.TYPE_OUTBOX));
        assertEquals("Trash", ct.getMailboxServerName(Mailbox.TYPE_TRASH));
        assertEquals("Sent", ct.getMailboxServerName(Mailbox.TYPE_SENT));
        assertEquals("Junk", ct.getMailboxServerName(Mailbox.TYPE_JUNK));
        Locale.setDefault(savedLocale);
    }
    public void testCreateMailbox() {
        Account account = ProviderTestUtils.setupAccount("mailboxid", true, mProviderContext);
        long accountId = account.mId;
        long oldBoxId = Mailbox.findMailboxOfType(mProviderContext, accountId, Mailbox.TYPE_DRAFTS);
        assertEquals(Mailbox.NO_MAILBOX, oldBoxId);
        Controller ct = new TestController(mProviderContext, mContext);
        ct.createMailbox(accountId, Mailbox.TYPE_DRAFTS);
        long boxId = Mailbox.findMailboxOfType(mProviderContext, accountId, Mailbox.TYPE_DRAFTS);
        assertTrue("mailbox exists", boxId != Mailbox.NO_MAILBOX);
    }
    public void testFindOrCreateMailboxOfType() {
        Account account = ProviderTestUtils.setupAccount("mailboxid", true, mProviderContext);
        long accountId = account.mId;
        Mailbox box = ProviderTestUtils.setupMailbox("box", accountId, false, mProviderContext);
        final int boxType = Mailbox.TYPE_TRASH;
        box.mType = boxType;
        box.save(mProviderContext);
        long boxId = box.mId;
        Controller ct = new TestController(mProviderContext, mContext);
        long testBoxId = ct.findOrCreateMailboxOfType(accountId, boxType);
        assertEquals(boxId, testBoxId);
        long boxId2 = ct.findOrCreateMailboxOfType(accountId, Mailbox.TYPE_DRAFTS);
        assertTrue("mailbox created", boxId2 != Mailbox.NO_MAILBOX);
        assertTrue("with different id", testBoxId != boxId2);
        long boxId3 = ct.findOrCreateMailboxOfType(accountId, Mailbox.TYPE_DRAFTS);
        assertEquals("don't create if exists", boxId3, boxId2);
        assertEquals(Mailbox.NO_MAILBOX, ct.findOrCreateMailboxOfType(-1, Mailbox.TYPE_DRAFTS));
        assertEquals(Mailbox.NO_MAILBOX, ct.findOrCreateMailboxOfType(accountId, -1));
    }
    public void testDeleteMessage() {
        Account account1 = ProviderTestUtils.setupAccount("message-delete", true, mProviderContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mProviderContext);
        long box1Id = box1.mId;
        Mailbox box2 = ProviderTestUtils.setupMailbox("box2", account1Id, false, mProviderContext);
        box2.mType = EmailContent.Mailbox.TYPE_TRASH;
        box2.save(mProviderContext);
        long box2Id = box2.mId;
        Message message1 = ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false,
                true, mProviderContext);
        long message1Id = message1.mId;
        Controller ct = new TestController(mProviderContext, mContext);
        ct.deleteMessage(message1Id, account1Id);
        Message message1get = EmailContent.Message.restoreMessageWithId(mProviderContext,
                message1Id);
        assertEquals(box2Id, message1get.mMailboxKey);
        Message message2 = ProviderTestUtils.setupMessage("message2", account1Id, box1Id, false,
                true, mProviderContext);
        long message2Id = message2.mId;
        ct.deleteMessage(message2Id, -1);
        Message message2get = EmailContent.Message.restoreMessageWithId(mProviderContext,
                message2Id);
        assertEquals(box2Id, message2get.mMailboxKey);
    }
    public void testDeleteMessageNoTrash() {
        Account account1 =
                ProviderTestUtils.setupAccount("message-delete-notrash", true, mProviderContext);
        long account1Id = account1.mId;
        Mailbox box1 = ProviderTestUtils.setupMailbox("box1", account1Id, true, mProviderContext);
        long box1Id = box1.mId;
        Message message1 =
                ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false, true,
                        mProviderContext);
        long message1Id = message1.mId;
        Controller ct = new TestController(mProviderContext, mContext);
        ct.deleteMessage(message1Id, account1Id);
        Message message1get =
                EmailContent.Message.restoreMessageWithId(mProviderContext, message1Id);
        assertFalse(-1 == message1get.mMailboxKey);
        assertFalse(box1Id == message1get.mMailboxKey);
        Mailbox mailbox2get = EmailContent.Mailbox.restoreMailboxWithId(mProviderContext,
                message1get.mMailboxKey);
        assertEquals(EmailContent.Mailbox.TYPE_TRASH, mailbox2get.mType);
    }
    public void testReadUnread() {
        Account account1 = ProviderTestUtils.setupAccount("read-unread", false, mProviderContext);
        account1.mHostAuthRecv
                = ProviderTestUtils.setupHostAuth("read-unread", 0, false, mProviderContext);
        account1.save(mProviderContext);
        long account1Id = account1.mId;
        long box1Id = 2;
        Message message1 =
                ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false, true,
                        mProviderContext);
        long message1Id = message1.mId;
        Controller ct = new TestController(mProviderContext, mContext);
        ct.setMessageRead(message1Id, true);
        Message message1get = Message.restoreMessageWithId(mProviderContext, message1Id);
        assertTrue(message1get.mFlagRead);
        ct.setMessageRead(message1Id, false);
        message1get = Message.restoreMessageWithId(mProviderContext, message1Id);
        assertFalse(message1get.mFlagRead);
    }
    public void testFavorites() {
        Account account1 = ProviderTestUtils.setupAccount("favorites", false, mProviderContext);
        account1.mHostAuthRecv
                = ProviderTestUtils.setupHostAuth("favorites", 0, false, mProviderContext);
        account1.save(mProviderContext);
        long account1Id = account1.mId;
        long box1Id = 2;
        Message message1 =
                ProviderTestUtils.setupMessage("message1", account1Id, box1Id, false, true,
                        mProviderContext);
        long message1Id = message1.mId;
        Controller ct = new TestController(mProviderContext, mContext);
        ct.setMessageFavorite(message1Id, true);
        Message message1get = Message.restoreMessageWithId(mProviderContext, message1Id);
        assertTrue(message1get.mFlagFavorite);
        ct.setMessageFavorite(message1Id, false);
        message1get = Message.restoreMessageWithId(mProviderContext, message1Id);
        assertFalse(message1get.mFlagFavorite);
    }
}
