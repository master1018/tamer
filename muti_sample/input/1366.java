public class AuthorizeCallback implements Callback, java.io.Serializable {
    private String authenticationID;
    private String authorizationID;
    private String authorizedID;
    private boolean authorized;
    public AuthorizeCallback(String authnID, String authzID) {
        authenticationID = authnID;
        authorizationID = authzID;
    }
    public String getAuthenticationID() {
        return authenticationID;
    }
    public String getAuthorizationID() {
        return authorizationID;
    }
    public boolean isAuthorized() {
        return authorized;
    }
    public void setAuthorized(boolean ok) {
        authorized = ok;
    }
    public String getAuthorizedID() {
        if (!authorized) {
            return null;
        }
        return (authorizedID == null) ? authorizationID : authorizedID;
    }
    public void setAuthorizedID(String id) {
        authorizedID = id;
    }
    private static final long serialVersionUID = -2353344186490470805L;
}
