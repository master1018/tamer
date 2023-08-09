public abstract class AbstractCookieAttributeHandler implements CookieAttributeHandler {
    public void validate(final Cookie cookie, final CookieOrigin origin) 
            throws MalformedCookieException {
    }
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        return true;
    }
}
