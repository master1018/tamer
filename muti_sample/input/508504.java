public class ClientContextConfigurer implements ClientContext {
    private final HttpContext context;
    public ClientContextConfigurer (final HttpContext context) {
        if (context == null)
            throw new IllegalArgumentException("HTTP context may not be null");
        this.context = context;
    }
    public void setCookieSpecRegistry(final CookieSpecRegistry registry) {
        this.context.setAttribute(COOKIESPEC_REGISTRY, registry);
    }
    public void setAuthSchemeRegistry(final AuthSchemeRegistry registry) {
        this.context.setAttribute(AUTHSCHEME_REGISTRY, registry);
    }
    public void setCookieStore(final CookieStore store) {
        this.context.setAttribute(COOKIE_STORE, store);
    }
    public void setCredentialsProvider(final CredentialsProvider provider) {
        this.context.setAttribute(CREDS_PROVIDER, provider);
    }
    public void setAuthSchemePref(final List<String> list) {
        this.context.setAttribute(AUTH_SCHEME_PREF, list);
    }
}
