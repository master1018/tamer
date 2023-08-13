public class SyncAdapterTestCase<T extends AbstractSyncAdapter>
        extends ProviderTestCase2<EmailProvider> {
    EmailProvider mProvider;
    Context mMockContext;
    ContentResolver mMockResolver;
    Mailbox mMailbox;
    Account mAccount;
    EmailSyncAdapter mSyncAdapter;
    EasEmailSyncParser mSyncParser;
    public SyncAdapterTestCase() {
        super(EmailProvider.class, EmailProvider.EMAIL_AUTHORITY);
    }
    @Override
    public void setUp() throws Exception {
        super.setUp();
        mMockContext = getMockContext();
        mMockResolver = mMockContext.getContentResolver();
    }
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
    public InputStream getTestInputStream() {
        return new ByteArrayInputStream(new byte[] {0, 0, 0, 0, 0});
    }
    EasSyncService getTestService() {
        Account account = new Account();
        account.mEmailAddress = "__test__@android.com";
        account.mId = -1;
        Mailbox mailbox = new Mailbox();
        mailbox.mId = -1;
        return getTestService(account, mailbox);
    }
    EasSyncService getTestService(Account account, Mailbox mailbox) {
        EasSyncService service = new EasSyncService();
        service.mContext = mMockContext;
        service.mMailbox = mailbox;
        service.mAccount = account;
        service.mContentResolver = mMockResolver;
        return service;
    }
    T getTestSyncAdapter(Class<T> klass) {
        EasSyncService service = getTestService();
        Constructor<T> c;
        try {
            c = klass.getDeclaredConstructor(new Class[] {Mailbox.class, EasSyncService.class});
            return c.newInstance(service.mMailbox, service);
        } catch (SecurityException e) {
        } catch (NoSuchMethodException e) {
        } catch (IllegalArgumentException e) {
        } catch (InstantiationException e) {
        } catch (IllegalAccessException e) {
        } catch (InvocationTargetException e) {
        }
        return null;
    }
}
