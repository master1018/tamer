public final class PasswordAuthentication {
    private String userName;
    private char[] password;
    public PasswordAuthentication(String userName, char[] password) {
        this.userName = userName;
        this.password = password.clone();
    }
    public String getUserName() {
        return userName;
    }
    public char[] getPassword() {
        return password;
    }
}
