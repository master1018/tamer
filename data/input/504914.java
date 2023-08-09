public class RequestTargetAuthentication implements HttpRequestInterceptor {
    private final Log log = LogFactory.getLog(getClass());
    public RequestTargetAuthentication() {
        super();
    }
    public void process(final HttpRequest request, final HttpContext context) 
            throws HttpException, IOException {
        if (request == null) {
            throw new IllegalArgumentException("HTTP request may not be null");
        }
        if (context == null) {
            throw new IllegalArgumentException("HTTP context may not be null");
        }
        if (request.containsHeader(AUTH.WWW_AUTH_RESP)) {
            return;
        }
        AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE);
        if (authState == null) {
            return;
        }
        AuthScheme authScheme = authState.getAuthScheme();
        if (authScheme == null) {
            return;
        }
        Credentials creds = authState.getCredentials();
        if (creds == null) {
            this.log.debug("User credentials not available");
            return;
        }
        if (authState.getAuthScope() != null || !authScheme.isConnectionBased()) {
            try {
                request.addHeader(authScheme.authenticate(creds, request));
            } catch (AuthenticationException ex) {
                if (this.log.isErrorEnabled()) {
                    this.log.error("Authentication error: " + ex.getMessage());
                }
            }
        }
    }
}
