public class AccountLockedException extends AccountException {
    private static final long serialVersionUID = 8280345554014066334L;
    public AccountLockedException() {
        super();
    }
    public AccountLockedException(String msg) {
        super(msg);
    }
}
