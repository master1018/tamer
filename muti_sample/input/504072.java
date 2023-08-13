public class UsernamePasswordCredentials implements Credentials {
    private final BasicUserPrincipal principal;
    private final String password;
    public UsernamePasswordCredentials(String usernamePassword) {
        super();
        if (usernamePassword == null) {
            throw new IllegalArgumentException("Username:password string may not be null");            
        }
        int atColon = usernamePassword.indexOf(':');
        if (atColon >= 0) {
            this.principal = new BasicUserPrincipal(usernamePassword.substring(0, atColon));
            this.password = usernamePassword.substring(atColon + 1);
        } else {
            this.principal = new BasicUserPrincipal(usernamePassword);
            this.password = null;
        }
    }
    public UsernamePasswordCredentials(String userName, String password) {
        super();
        if (userName == null) {
            throw new IllegalArgumentException("Username may not be null");            
        }
        this.principal = new BasicUserPrincipal(userName);
        this.password = password;
    }
    public Principal getUserPrincipal() {
        return this.principal;
    }
    public String getUserName() {
        return this.principal.getName();
    }
    public String getPassword() {
        return password;
    }
    @Override
    public int hashCode() {
        return this.principal.hashCode();
    }
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials that = (UsernamePasswordCredentials) o;
            if (LangUtils.equals(this.principal, that.principal)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString() {
        return this.principal.toString();
    }
}
