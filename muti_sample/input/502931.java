public final class PasswordAuthentication {
    private byte[] mUserName;
    private final byte[] mPassword;
    public PasswordAuthentication(final byte[] userName, final byte[] password) {
        if (userName != null) {
            mUserName = new byte[userName.length];
            System.arraycopy(userName, 0, mUserName, 0, userName.length);
        }
        mPassword = new byte[password.length];
        System.arraycopy(password, 0, mPassword, 0, password.length);
    }
    public byte[] getUserName() {
        return mUserName;
    }
    public byte[] getPassword() {
        return mPassword;
    }
}
