public class AuthState {
    private AuthScheme authScheme;
    private AuthScope authScope;
    private Credentials credentials;
    public AuthState() {
        super();
    }
    public void invalidate() {
        this.authScheme = null;
        this.authScope = null;
        this.credentials = null;
    }
    public boolean isValid() {
        return this.authScheme != null;
    }
    public void setAuthScheme(final AuthScheme authScheme) {
        if (authScheme == null) {
            invalidate();
            return;
        }
        this.authScheme = authScheme;
    }
    public AuthScheme getAuthScheme() {
        return this.authScheme;
    }
    public Credentials getCredentials() {
        return this.credentials;
    }
    public void setCredentials(final Credentials credentials) {
        this.credentials = credentials;
    }
     public AuthScope getAuthScope() {
        return this.authScope;
     }
     public void setAuthScope(final AuthScope authScope) {
        this.authScope = authScope;
     }
    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("auth scope [");
        buffer.append(this.authScope);
        buffer.append("]; credentials set [");
        buffer.append(this.credentials != null ? "true" : "false");
        buffer.append("]");
        return buffer.toString();
    }
}
