public abstract class AccountTestCase extends ProviderTestCase2<EmailProvider> {
    protected static final String TEST_ACCOUNT_PREFIX = "__test";
    protected static final String TEST_ACCOUNT_SUFFIX = "@android.com";
    public AccountTestCase() {
        super(EmailProvider.class, EmailProvider.EMAIL_AUTHORITY);
    }
    protected android.accounts.Account[] getExchangeAccounts() {
        return AccountManager.get(getContext())
                .getAccountsByType(Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
    }
    protected android.accounts.Account makeAccountManagerAccount(String username) {
        return new android.accounts.Account(username, Email.EXCHANGE_ACCOUNT_MANAGER_TYPE);
    }
    protected void createAccountManagerAccount(String username) {
        final android.accounts.Account account = makeAccountManagerAccount(username);
        AccountManager.get(getContext()).addAccountExplicitly(account, "password", null);
    }
    protected Account setupProviderAndAccountManagerAccount(String username) {
        createAccountManagerAccount(username + TEST_ACCOUNT_SUFFIX);
        return ProviderTestUtils.setupAccount(username, true, getMockContext());
    }
    protected ArrayList<Account> makeSyncManagerAccountList() {
        ArrayList<Account> accountList = new ArrayList<Account>();
        Cursor c = getMockContext().getContentResolver().query(Account.CONTENT_URI,
                Account.CONTENT_PROJECTION, null, null, null);
        try {
            while (c.moveToNext()) {
                accountList.add(new Account().restore(c));
            }
        } finally {
            c.close();
        }
        return accountList;
    }
    protected void deleteAccountManagerAccount(android.accounts.Account account) {
        AccountManagerFuture<Boolean> future =
            AccountManager.get(getContext()).removeAccount(account, null, null);
        try {
            future.getResult();
        } catch (OperationCanceledException e) {
        } catch (AuthenticatorException e) {
        } catch (IOException e) {
        }
    }
    protected void deleteTemporaryAccountManagerAccounts() {
        for (android.accounts.Account accountManagerAccount: getExchangeAccounts()) {
            if (accountManagerAccount.name.startsWith(TEST_ACCOUNT_PREFIX) &&
                    accountManagerAccount.name.endsWith(TEST_ACCOUNT_SUFFIX)) {
                deleteAccountManagerAccount(accountManagerAccount);
            }
        }
    }
    protected String getTestAccountName(String name) {
        return TEST_ACCOUNT_PREFIX + name;
    }
    protected String getTestAccountEmailAddress(String name) {
        return TEST_ACCOUNT_PREFIX + name + TEST_ACCOUNT_SUFFIX;
    }
    protected android.accounts.Account[] getAccountManagerAccounts(
            android.accounts.Account[] baseline) {
        android.accounts.Account[] rawList = getExchangeAccounts();
        if (baseline.length == 0) {
            return rawList;
        }
        HashSet<android.accounts.Account> set = new HashSet<android.accounts.Account>();
        for (android.accounts.Account addAccount : rawList) {
            set.add(addAccount);
        }
        for (android.accounts.Account removeAccount : baseline) {
            set.remove(removeAccount);
        }
        return set.toArray(new android.accounts.Account[0]);
    }
}
