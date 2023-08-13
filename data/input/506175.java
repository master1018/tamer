public class NetscapeDomainHandler extends BasicDomainHandler {
    public NetscapeDomainHandler() {
        super();
    }
    @Override
    public void validate(final Cookie cookie, final CookieOrigin origin) 
            throws MalformedCookieException {
        super.validate(cookie, origin);
        String host = origin.getHost();
        String domain = cookie.getDomain();
        if (host.contains(".")) {
            int domainParts = new StringTokenizer(domain, ".").countTokens();
            if (isSpecialDomain(domain)) {
                if (domainParts < 2) {
                    throw new MalformedCookieException("Domain attribute \""
                        + domain 
                        + "\" violates the Netscape cookie specification for "
                        + "special domains");
                }
            } else {
                if (domainParts < 3) {
                    throw new MalformedCookieException("Domain attribute \""
                        + domain 
                        + "\" violates the Netscape cookie specification");
                }            
            }
        }
    }
   private static boolean isSpecialDomain(final String domain) {
       final String ucDomain = domain.toUpperCase(Locale.ENGLISH);
       return ucDomain.endsWith(".COM")
               || ucDomain.endsWith(".EDU")
               || ucDomain.endsWith(".NET")
               || ucDomain.endsWith(".GOV")
               || ucDomain.endsWith(".MIL")
               || ucDomain.endsWith(".ORG")
               || ucDomain.endsWith(".INT");
   }
   @Override
public boolean match(Cookie cookie, CookieOrigin origin) {
       if (cookie == null) {
           throw new IllegalArgumentException("Cookie may not be null");
       }
       if (origin == null) {
           throw new IllegalArgumentException("Cookie origin may not be null");
       }
       String host = origin.getHost();
       String domain = cookie.getDomain();
       if (domain == null) {
           return false;
       }
       return host.endsWith(domain);
   }
}
