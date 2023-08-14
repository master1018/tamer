public class B6791927 {
    public static final void main( String[] aaParamters ) throws Exception{
        Locale.setDefault(Locale.FRANCE);
        List<HttpCookie> cookies = HttpCookie.parse("set-cookie: CUSTOMER=WILE_E_COYOTE; expires=Wednesday, 09-Nov-2019 23:12:40 GMT");
        if (cookies == null || cookies.isEmpty()) {
            throw new RuntimeException("No cookie found");
        }
        for (HttpCookie c : cookies) {
            if (c.getMaxAge() == 0) {
                throw new RuntimeException("Expiration date shouldn't be 0");
            }
        }
    }
}
