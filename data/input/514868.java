public class SecurityPolicyTests extends ProviderTestCase2<EmailProvider> {
    private Context mMockContext;
    private static final PolicySet EMPTY_POLICY_SET =
        new PolicySet(0, PolicySet.PASSWORD_MODE_NONE, 0, 0, false);
    public SecurityPolicyTests() {
        super(EmailProvider.class, EmailProvider.EMAIL_AUTHORITY);
    }
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mMockContext = new MockContext2(getMockContext(), this.mContext);
    }
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    private static class MockContext2 extends ContextWrapper {
        private final Context mRealContext;
        public MockContext2(Context mockContext, Context realContext) {
            super(mockContext);
            mRealContext = realContext;
        }
        @Override
        public String getPackageName() {
            return mRealContext.getPackageName();
        }
    }
    private SecurityPolicy getSecurityPolicy() {
        SecurityPolicy sp = SecurityPolicy.getInstance(mMockContext);
        sp.setContext(mMockContext);
        return sp;
    }
    public void testPolicySetConstructor() {
        try {
            new PolicySet(100, PolicySet.PASSWORD_MODE_SIMPLE, 0, 0, false);
            fail("Too-long password allowed");
        } catch (IllegalArgumentException e) {
        }
        try {
            new PolicySet(0, PolicySet.PASSWORD_MODE_STRONG + 1, 0, 0, false);
            fail("Illegal password mode allowed");
        } catch (IllegalArgumentException e) {
        }
        PolicySet ps = new PolicySet(0, PolicySet.PASSWORD_MODE_SIMPLE, 0,
                PolicySet.SCREEN_LOCK_TIME_MAX + 1, false);
        assertEquals(PolicySet.SCREEN_LOCK_TIME_MAX, ps.getMaxScreenLockTime());
        ps = new PolicySet(0, PolicySet.PASSWORD_MODE_SIMPLE,
                PolicySet.PASSWORD_MAX_FAILS_MAX + 1, 0, false);
        assertEquals(PolicySet.PASSWORD_MAX_FAILS_MAX, ps.getMaxPasswordFails());
        ps = new PolicySet(999, PolicySet.PASSWORD_MODE_NONE,
                999, 9999, false);
        assertEquals(0, ps.mMinPasswordLength);
        assertEquals(0, ps.mMaxScreenLockTime);
        assertEquals(0, ps.mMaxPasswordFails);
    }
    public void testAggregator() {
        SecurityPolicy sp = getSecurityPolicy();
        assertTrue(EMPTY_POLICY_SET.equals(sp.computeAggregatePolicy()));
        Account a1 = ProviderTestUtils.setupAccount("no-sec-1", false, mMockContext);
        a1.mSecurityFlags = 0;
        a1.save(mMockContext);
        Account a2 = ProviderTestUtils.setupAccount("no-sec-2", false, mMockContext);
        a2.mSecurityFlags = 0;
        a2.save(mMockContext);
        assertTrue(EMPTY_POLICY_SET.equals(sp.computeAggregatePolicy()));
        Account a3 = ProviderTestUtils.setupAccount("sec-3", false, mMockContext);
        PolicySet p3ain = new PolicySet(10, PolicySet.PASSWORD_MODE_SIMPLE, 0, 0, false);
        p3ain.writeAccount(a3, null, true, mMockContext);
        PolicySet p3aout = sp.computeAggregatePolicy();
        assertNotNull(p3aout);
        assertEquals(p3ain, p3aout);
        PolicySet p3bin = new PolicySet(10, PolicySet.PASSWORD_MODE_SIMPLE, 15, 16, false);
        p3bin.writeAccount(a3, null, true, mMockContext);
        PolicySet p3bout = sp.computeAggregatePolicy();
        assertNotNull(p3bout);
        assertEquals(p3bin, p3bout);
        PolicySet p4in = new PolicySet(20, PolicySet.PASSWORD_MODE_STRONG, 25, 26, false);
        Account a4 = ProviderTestUtils.setupAccount("sec-4", false, mMockContext);
        p4in.writeAccount(a4, null, true, mMockContext);
        PolicySet p4out = sp.computeAggregatePolicy();
        assertNotNull(p4out);
        assertEquals(20, p4out.mMinPasswordLength);
        assertEquals(PolicySet.PASSWORD_MODE_STRONG, p4out.mPasswordMode);
        assertEquals(15, p4out.mMaxPasswordFails);
        assertEquals(16, p4out.mMaxScreenLockTime);
        assertFalse(p4out.mRequireRemoteWipe);
        PolicySet p5in = new PolicySet(4, PolicySet.PASSWORD_MODE_SIMPLE, 5, 6, true);
        Account a5 = ProviderTestUtils.setupAccount("sec-5", false, mMockContext);
        p5in.writeAccount(a5, null, true, mMockContext);
        PolicySet p5out = sp.computeAggregatePolicy();
        assertNotNull(p5out);
        assertEquals(20, p5out.mMinPasswordLength);
        assertEquals(PolicySet.PASSWORD_MODE_STRONG, p5out.mPasswordMode);
        assertEquals(5, p5out.mMaxPasswordFails);
        assertEquals(6, p5out.mMaxScreenLockTime);
        assertTrue(p5out.mRequireRemoteWipe);
    }
    public void testNullFlags() {
        SecurityPolicy sp = getSecurityPolicy();
        Account a1 = ProviderTestUtils.setupAccount("null-sec-1", true, mMockContext);
        ContentValues cv = new ContentValues();
        cv.putNull(AccountColumns.SECURITY_FLAGS);
        Uri uri = ContentUris.withAppendedId(Account.CONTENT_URI, a1.mId);
        mMockContext.getContentResolver().update(uri, cv, null, null);
        Account a2 = ProviderTestUtils.setupAccount("no-sec-2", false, mMockContext);
        a2.mSecurityFlags = 0;
        a2.save(mMockContext);
        assertTrue(EMPTY_POLICY_SET.equals(sp.computeAggregatePolicy()));
    }
    @SmallTest
    public void testFieldIsolation() {
        PolicySet p = new PolicySet(PolicySet.PASSWORD_LENGTH_MAX, PolicySet.PASSWORD_MODE_SIMPLE,
                0, 0, false);
        assertEquals(PolicySet.PASSWORD_LENGTH_MAX, p.mMinPasswordLength);
        assertEquals(0, p.mMaxPasswordFails);
        assertEquals(0, p.mMaxScreenLockTime);
        assertFalse(p.mRequireRemoteWipe);
        p = new PolicySet(0, PolicySet.PASSWORD_MODE_STRONG, 0, 0, false);
        assertEquals(0, p.mMinPasswordLength);
        assertEquals(PolicySet.PASSWORD_MODE_STRONG, p.mPasswordMode);
        assertEquals(0, p.mMinPasswordLength);
        assertEquals(0, p.mMaxPasswordFails);
        assertEquals(0, p.mMaxScreenLockTime);
        assertFalse(p.mRequireRemoteWipe);
        p = new PolicySet(0, PolicySet.PASSWORD_MODE_SIMPLE, PolicySet.PASSWORD_MAX_FAILS_MAX, 0,
                false);
        assertEquals(0, p.mMinPasswordLength);
        assertEquals(PolicySet.PASSWORD_MAX_FAILS_MAX, p.mMaxPasswordFails);
        assertEquals(0, p.mMaxScreenLockTime);
        assertFalse(p.mRequireRemoteWipe);
        p = new PolicySet(0, PolicySet.PASSWORD_MODE_SIMPLE, 0, PolicySet.SCREEN_LOCK_TIME_MAX,
                false);
        assertEquals(0, p.mMinPasswordLength);
        assertEquals(0, p.mMaxPasswordFails);
        assertEquals(PolicySet.SCREEN_LOCK_TIME_MAX, p.mMaxScreenLockTime);
        assertFalse(p.mRequireRemoteWipe);
        p = new PolicySet(0, PolicySet.PASSWORD_MODE_NONE, 0, 0, true);
        assertEquals(0, p.mMinPasswordLength);
        assertEquals(0, p.mMaxPasswordFails);
        assertEquals(0, p.mMaxScreenLockTime);
        assertTrue(p.mRequireRemoteWipe);
    }
    @SmallTest
    public void testAccountEncoding() {
        PolicySet p1 = new PolicySet(1, PolicySet.PASSWORD_MODE_STRONG, 3, 4, true);
        Account a = new Account();
        final String SYNC_KEY = "test_sync_key";
        p1.writeAccount(a, SYNC_KEY, false, null);
        PolicySet p2 = new PolicySet(a);
        assertEquals(p1, p2);
    }
    @SmallTest
    public void testEqualsAndHash() {
        PolicySet p1 = new PolicySet(1, PolicySet.PASSWORD_MODE_STRONG, 3, 4, true);
        PolicySet p2 = new PolicySet(1, PolicySet.PASSWORD_MODE_STRONG, 3, 4, true);
        PolicySet p3 = new PolicySet(2, PolicySet.PASSWORD_MODE_SIMPLE, 5, 6, true);
        assertTrue(p1.equals(p2));
        assertFalse(p2.equals(p3));
        assertTrue(p1.hashCode() == p2.hashCode());
        assertFalse(p2.hashCode() == p3.hashCode());
    }
    public void testSetClearHoldFlag() {
        SecurityPolicy sp = getSecurityPolicy();
        Account a1 = ProviderTestUtils.setupAccount("holdflag-1", false, mMockContext);
        a1.mFlags = Account.FLAGS_NOTIFY_NEW_MAIL;
        a1.save(mMockContext);
        Account a2 = ProviderTestUtils.setupAccount("holdflag-2", false, mMockContext);
        a2.mFlags = Account.FLAGS_VIBRATE_ALWAYS | Account.FLAGS_SECURITY_HOLD;
        a2.save(mMockContext);
        Account a1a = Account.restoreAccountWithId(mMockContext, a1.mId);
        assertEquals(Account.FLAGS_NOTIFY_NEW_MAIL, a1a.mFlags);
        sp.setAccountHoldFlag(a1, true);
        assertEquals(Account.FLAGS_NOTIFY_NEW_MAIL | Account.FLAGS_SECURITY_HOLD, a1.mFlags);
        Account a1b = Account.restoreAccountWithId(mMockContext, a1.mId);
        assertEquals(Account.FLAGS_NOTIFY_NEW_MAIL | Account.FLAGS_SECURITY_HOLD, a1b.mFlags);
        Account a2a = Account.restoreAccountWithId(mMockContext, a2.mId);
        assertEquals(Account.FLAGS_VIBRATE_ALWAYS | Account.FLAGS_SECURITY_HOLD, a2a.mFlags);
        sp.setAccountHoldFlag(a2, false);
        assertEquals(Account.FLAGS_VIBRATE_ALWAYS, a2.mFlags);
        Account a2b = Account.restoreAccountWithId(mMockContext, a2.mId);
        assertEquals(Account.FLAGS_VIBRATE_ALWAYS, a2b.mFlags);
    }
    public void testClearHoldFlags() {
        SecurityPolicy sp = getSecurityPolicy();
        Account a1 = ProviderTestUtils.setupAccount("holdflag-1", false, mMockContext);
        a1.mFlags = Account.FLAGS_NOTIFY_NEW_MAIL;
        a1.save(mMockContext);
        Account a2 = ProviderTestUtils.setupAccount("holdflag-2", false, mMockContext);
        a2.mFlags = Account.FLAGS_VIBRATE_ALWAYS | Account.FLAGS_SECURITY_HOLD;
        a2.save(mMockContext);
        sp.clearAccountHoldFlags();
        Account a1a = Account.restoreAccountWithId(mMockContext, a1.mId);
        assertEquals(Account.FLAGS_NOTIFY_NEW_MAIL, a1a.mFlags);
        Account a2a = Account.restoreAccountWithId(mMockContext, a2.mId);
        assertEquals(Account.FLAGS_VIBRATE_ALWAYS, a2a.mFlags);
    }
    public void testDisableAdmin() {
        Account a1 = ProviderTestUtils.setupAccount("disable-1", false, mMockContext);
        PolicySet p1 = new PolicySet(10, PolicySet.PASSWORD_MODE_SIMPLE, 0, 0, false);
        p1.writeAccount(a1, "sync-key-1", true, mMockContext);
        Account a2 = ProviderTestUtils.setupAccount("disable-2", false, mMockContext);
        PolicySet p2 = new PolicySet(20, PolicySet.PASSWORD_MODE_STRONG, 25, 26, false);
        p2.writeAccount(a2, "sync-key-2", true, mMockContext);
        Account a3 = ProviderTestUtils.setupAccount("disable-3", false, mMockContext);
        a3.mSecurityFlags = 0;
        a3.mSecuritySyncKey = null;
        a3.save(mMockContext);
        SecurityPolicy sp = getSecurityPolicy();
        PolicySet before = sp.getAggregatePolicy();
        sp.onAdminEnabled(true);        
        PolicySet after1 = sp.getAggregatePolicy();
        assertEquals(before, after1);
        Account a1a = Account.restoreAccountWithId(mMockContext, a1.mId);
        assertNotNull(a1a.mSecuritySyncKey);
        Account a2a = Account.restoreAccountWithId(mMockContext, a2.mId);
        assertNotNull(a2a.mSecuritySyncKey);
        Account a3a = Account.restoreAccountWithId(mMockContext, a3.mId);
        assertNull(a3a.mSecuritySyncKey);
        sp.onAdminEnabled(false);        
        PolicySet after2 = sp.getAggregatePolicy();
        assertEquals(SecurityPolicy.NO_POLICY_SET, after2);
        Account a1b = Account.restoreAccountWithId(mMockContext, a1.mId);
        assertNull(a1b.mSecuritySyncKey);
        Account a2b = Account.restoreAccountWithId(mMockContext, a2.mId);
        assertNull(a2b.mSecuritySyncKey);
        Account a3b = Account.restoreAccountWithId(mMockContext, a3.mId);
        assertNull(a3b.mSecuritySyncKey);
    }
}
