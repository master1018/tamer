public class HttpPrincipal implements Principal {
    private String username, realm;
    public HttpPrincipal (String username, String realm) {
        if (username == null || realm == null) {
            throw new NullPointerException();
        }
        this.username = username;
        this.realm = realm;
    }
    public boolean equals (Object another) {
        if (!(another instanceof HttpPrincipal)) {
            return false;
        }
        HttpPrincipal theother = (HttpPrincipal)another;
        return (username.equals(theother.username) &&
                realm.equals(theother.realm));
    }
    public String getName() {
        return username;
    }
    public String getUsername() {
        return username;
    }
    public String getRealm() {
        return realm;
    }
    public int hashCode() {
        return (username+realm).hashCode();
    }
    public String toString() {
        return getName();
    }
}
