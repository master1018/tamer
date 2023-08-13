public class AccountExpiredException extends AccountException {
    private static final long serialVersionUID = -6064064890162661560L;
    public AccountExpiredException() {
        super();
    }
    public AccountExpiredException(String msg) {
        super(msg);
    }
}
