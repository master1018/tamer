public class BasicMaxAgeHandler extends AbstractCookieAttributeHandler {
    public BasicMaxAgeHandler() {
        super();
    }
    public void parse(final SetCookie cookie, final String value) 
            throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        if (value == null) {
            throw new MalformedCookieException("Missing value for max-age attribute");
        }
        int age;
        try {
            age = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new MalformedCookieException ("Invalid max-age attribute: " 
                    + value);
        }
        if (age < 0) {
            throw new MalformedCookieException ("Negative max-age attribute: " 
                    + value);
        }
        cookie.setExpiryDate(new Date(System.currentTimeMillis() + age * 1000L));
    }
}
