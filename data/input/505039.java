public class DefaultTargetAuthenticationHandler extends AbstractAuthenticationHandler {
    public DefaultTargetAuthenticationHandler() {
        super();
    }
    public boolean isAuthenticationRequested(
            final HttpResponse response, 
            final HttpContext context) {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        int status = response.getStatusLine().getStatusCode();
        return status == HttpStatus.SC_UNAUTHORIZED;
    }
    public Map<String, Header> getChallenges(
            final HttpResponse response, 
            final HttpContext context) throws MalformedChallengeException {
        if (response == null) {
            throw new IllegalArgumentException("HTTP response may not be null");
        }
        Header[] headers = response.getHeaders(AUTH.WWW_AUTH);
        return parseChallenges(headers);
    }
}
