public abstract class ReusableEmailAccountDaoTest extends ReusableTestCase<EmailAccount> {
    private EmailAccountDao dao;
    private final Log log = LogFactory.getLog(getClass());
    public void setDao(EmailAccountDao dao) {
        assertNull(this.dao);
        this.dao = dao;
        assertEquals("There are already email accounts in the data source!  It should be fresh and empty.", 0, this.dao.getAllEmailAccounts().size());
    }
    @Override
    public void tearDown() throws Exception {
        for (EmailAccount account : this.dao.getAllEmailAccounts()) {
            this.dao.deleteEmailAccount(account);
        }
        this.dao = null;
    }
    public void test() throws DuplicateKeyException {
        assertEquals("Checking there are no unexpected entries in the email DAO.", 0, dao.getAllEmailAccounts().size());
        boolean useSsl = false;
        String accountName = "test@frontlinesms.com";
        String accountServer = "FrontlineSMS Test";
        int accountServerPort = 123;
        String accountPassword = "secretpassword";
        EmailAccount account = new EmailAccount(accountName, accountServer, accountServerPort, accountPassword, useSsl);
        dao.saveEmailAccount(account);
        assertEquals(1, dao.getAllEmailAccounts().size());
        EmailAccount retrievedAccount = dao.getAllEmailAccounts().toArray(new EmailAccount[0])[0];
        assertEquals(accountName, retrievedAccount.getAccountName());
        assertEquals(accountServer, retrievedAccount.getAccountServer());
        assertEquals(accountServerPort, retrievedAccount.getAccountServerPort());
        assertEquals(accountPassword, retrievedAccount.getAccountPassword());
        assertEquals(useSsl, retrievedAccount.useSsl());
        assertEquals(1, dao.getAllEmailAccounts().size());
        dao.deleteEmailAccount(account);
        assertEquals(0, dao.getAllEmailAccounts().size());
    }
    public void testDuplicates() throws DuplicateKeyException {
        boolean useSsl = false;
        String accountName = "test@frontlinesms.com";
        String accountServer = "FrontlineSMS Test";
        int accountServerPort = 123;
        String accountPassword = "secretpassword";
        EmailAccount account = new EmailAccount(accountName, accountServer, accountServerPort, accountPassword, useSsl);
        dao.saveEmailAccount(account);
        EmailAccount duplicateAccount = new EmailAccount(accountName, accountServer, accountServerPort, accountPassword, useSsl);
        try {
            System.out.println("Preparing to save...");
            dao.saveEmailAccount(duplicateAccount);
            System.out.println("Save passed!");
            fail("Should have thrown DKE");
        } catch (DuplicateKeyException ex) {
        }
    }
}
