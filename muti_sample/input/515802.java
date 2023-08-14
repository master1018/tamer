public class LoginInfo {
    private String mUserName;
    private String mPassword;
    public LoginInfo(String userName, String password) {
        mUserName = userName;
        mPassword = password;
    }
    public String getUserName() {
        return mUserName;
    }
    public String getPassword() {
        return mPassword;
    }
}
