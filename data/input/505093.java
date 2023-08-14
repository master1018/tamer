public class BasicCommentHandler extends AbstractCookieAttributeHandler {
    public BasicCommentHandler() {
        super();
    }
    public void parse(final SetCookie cookie, final String value) 
            throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        cookie.setComment(value);
    }
}
