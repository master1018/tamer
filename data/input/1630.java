public class URIParameter implements
        Policy.Parameters, javax.security.auth.login.Configuration.Parameters {
    private java.net.URI uri;
    public URIParameter(java.net.URI uri) {
        if (uri == null) {
            throw new NullPointerException("invalid null URI");
        }
        this.uri = uri;
    }
    public java.net.URI getURI() {
        return uri;
    }
}
