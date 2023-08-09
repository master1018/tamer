public class RFC2965VersionAttributeHandler implements CookieAttributeHandler {
    public RFC2965VersionAttributeHandler() {
        super();
    }
    public void parse(final SetCookie cookie, final String value)
            throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        if (value == null) {
            throw new MalformedCookieException(
                    "Missing value for version attribute");
        }
        int version = -1;
        try {
            version = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            version = -1;
        }
        if (version < 0) {
            throw new MalformedCookieException("Invalid cookie version.");
        }
        cookie.setVersion(version);
    }
    public void validate(final Cookie cookie, final CookieOrigin origin)
            throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        if (cookie instanceof SetCookie2) {
            if (cookie instanceof ClientCookie 
                    && !((ClientCookie) cookie).containsAttribute(ClientCookie.VERSION_ATTR)) {
                throw new MalformedCookieException(
                        "Violates RFC 2965. Version attribute is required.");
            }
        }
    }
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        return true;
    }
}