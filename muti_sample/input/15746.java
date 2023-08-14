final class UnsolicitedResponseImpl implements UnsolicitedNotification {
    private String oid;
    private String[] referrals;
    private byte[] extensionValue;
    private NamingException exception;
    private Control[] controls;
    UnsolicitedResponseImpl(String oid, byte[] berVal, Vector ref,
        int status, String msg, String matchedDN, Control[] controls) {
        this.oid = oid;
        this.extensionValue = berVal;
        if (ref != null && ref.size() > 0) {
            int len = ref.size();
            referrals = new String[len];
            for (int i = 0; i < len; i++) {
                referrals[i] = (String)ref.elementAt(i);
            }
        }
        exception = LdapCtx.mapErrorCode(status, msg);
        this.controls = controls;
    }
    public String getID() {
        return oid;
    }
    public byte[] getEncodedValue() {
        return extensionValue;
    }
    public String[] getReferrals() {
        return referrals;
    }
    public NamingException getException() {
        return exception;
    }
    public Control[] getControls() throws NamingException {
        return controls;
    }
    private static final long serialVersionUID = 5913778898401784775L;
}
