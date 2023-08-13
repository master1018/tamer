public class NTCredentials implements Credentials {
    private final NTUserPrincipal principal;
    private final String password;
    private final String workstation;
    public NTCredentials(String usernamePassword) {
        super();
        if (usernamePassword == null) {
            throw new IllegalArgumentException("Username:password string may not be null");            
        }
        String username;
        int atColon = usernamePassword.indexOf(':');
        if (atColon >= 0) {
            username = usernamePassword.substring(0, atColon);
            this.password = usernamePassword.substring(atColon + 1);
        } else {
            username = usernamePassword;
            this.password = null;
        }
        int atSlash = username.indexOf('/');
        if (atSlash >= 0) {
            this.principal = new NTUserPrincipal(
                    username.substring(0, atSlash).toUpperCase(Locale.ENGLISH),
                    username.substring(atSlash + 1));
        } else {
            this.principal = new NTUserPrincipal(
                    null,
                    username.substring(atSlash + 1));
        }
        this.workstation = null;
    }
    public NTCredentials(
            final String userName, 
            final String password, 
            final String workstation,
            final String domain) {
        super();
        if (userName == null) {
            throw new IllegalArgumentException("User name may not be null");
        }
        this.principal = new NTUserPrincipal(domain, userName);
        this.password = password;
        if (workstation != null) {
            this.workstation = workstation.toUpperCase(Locale.ENGLISH);
        } else {
            this.workstation = null;
        }
    }
    public Principal getUserPrincipal() {
        return this.principal;
    }
    public String getUserName() {
        return this.principal.getUsername();
    }
    public String getPassword() {
        return this.password;
    }
    public String getDomain() {
        return this.principal.getDomain();
    }
    public String getWorkstation() {
        return this.workstation;
    }
    @Override
    public int hashCode() {
        int hash = LangUtils.HASH_SEED;
        hash = LangUtils.hashCode(hash, this.principal);
        hash = LangUtils.hashCode(hash, this.workstation);
        return hash;
    }
    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (this == o) return true;
        if (o instanceof NTCredentials) {
            NTCredentials that = (NTCredentials) o;
            if (LangUtils.equals(this.principal, that.principal)
                    && LangUtils.equals(this.workstation, that.workstation)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("[principal: ");
        buffer.append(this.principal);
        buffer.append("][workstation: ");
        buffer.append(this.workstation);
        buffer.append("]");
        return buffer.toString();
    }
}
