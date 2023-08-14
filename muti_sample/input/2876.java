public abstract class ReferralException extends NamingException {
    protected ReferralException(String explanation) {
        super(explanation);
    }
    protected ReferralException() {
        super();
    }
    public abstract Object getReferralInfo();
    public abstract Context getReferralContext() throws NamingException;
    public abstract Context
        getReferralContext(Hashtable<?,?> env)
        throws NamingException;
    public abstract boolean skipReferral();
    public abstract void retryReferral();
    private static final long serialVersionUID = -2881363844695698876L;
}
