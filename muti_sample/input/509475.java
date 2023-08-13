public class MessagingControllerUnitTests extends AndroidTestCase {
    private long mAccountId;
    private EmailContent.Account mAccount;
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (mAccount != null) {
            Uri uri = ContentUris.withAppendedId(
                    EmailContent.Account.CONTENT_URI, mAccountId);
            getContext().getContentResolver().delete(uri, null, null);
        }
    }
    private static class MyMockFolder extends MockFolder {
        private FolderRole mRole;
        private String mName;
        public MyMockFolder(FolderRole role, String name) {
            mRole = role;
            mName = name;
        }
        public String getName() {
            return mName;
        }
        @Override
        public FolderRole getRole() {
            return mRole;
        }
    }
    private void createTestAccount() {
        mAccount = new EmailContent.Account();
        mAccount.save(getContext());
        mAccountId = mAccount.mId;
    }
}
