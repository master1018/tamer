public class NewExceptions {
    public static void main(String[] args) throws Exception {
        AccountException ac =
                new AccountException("AccountException");
        AccountExpiredException aee =
                new AccountExpiredException("AccountExpiredException");
        AccountLockedException ale =
                new AccountLockedException("AccountLockedException");
        AccountNotFoundException anfe =
                new AccountNotFoundException("AccountNotFoundException");
        CredentialException ce =
                new CredentialException("CredentialException");
        CredentialExpiredException cee =
                new CredentialExpiredException("CredentialExpiredException");
        CredentialNotFoundException cnfe =
                new CredentialNotFoundException("CredentialNotFoundException");
        if (! (ac instanceof LoginException) ||
            ! (ce instanceof LoginException) ||
            ! (aee instanceof AccountException) ||
            ! (ale instanceof AccountException) ||
            ! (anfe instanceof AccountException) ||
            ! (cee instanceof CredentialException) ||
            ! (cnfe instanceof CredentialNotFoundException)) {
            throw new SecurityException("Test 1 failed");
        }
        if (!ac.getMessage().equals("AccountException") ||
            !aee.getMessage().equals("AccountExpiredException") ||
            !ale.getMessage().equals("AccountLockedException") ||
            !anfe.getMessage().equals("AccountNotFoundException") ||
            !ce.getMessage().equals("CredentialException") ||
            !cee.getMessage().equals("CredentialExpiredException") ||
            !cnfe.getMessage().equals("CredentialNotFoundException")) {
            throw new SecurityException("Test 2 failed");
        }
    }
}
