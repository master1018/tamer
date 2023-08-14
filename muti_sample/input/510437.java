public class BestMatchSpec implements CookieSpec {
    private final String[] datepatterns;
    private final boolean oneHeader;
    private RFC2965Spec strict;
    private BrowserCompatSpec compat;
    private NetscapeDraftSpec netscape;
    public BestMatchSpec(final String[] datepatterns, boolean oneHeader) {
        super();
        this.datepatterns = datepatterns;
        this.oneHeader = oneHeader;
    }
    public BestMatchSpec() {
        this(null, false);
    }
    private RFC2965Spec getStrict() {
        if (this.strict == null) {
             this.strict = new RFC2965Spec(this.datepatterns, this.oneHeader);
        }
        return strict;
    }
    private BrowserCompatSpec getCompat() {
        if (this.compat == null) {
            this.compat = new BrowserCompatSpec(this.datepatterns);
        }
        return compat;
    }
    private NetscapeDraftSpec getNetscape() {
        if (this.netscape == null) {
            String[] patterns = this.datepatterns;
            if (patterns == null) {
                patterns = BrowserCompatSpec.DATE_PATTERNS;
            }
            this.netscape = new NetscapeDraftSpec(patterns);
        }
        return netscape;
    }
    public List<Cookie> parse(
            final Header header, 
            final CookieOrigin origin) throws MalformedCookieException {
        if (header == null) {
            throw new IllegalArgumentException("Header may not be null");
        }
        if (origin == null) {
           throw new IllegalArgumentException("Cookie origin may not be null");
        }
        HeaderElement[] helems = header.getElements();
        boolean versioned = false;
        boolean netscape = false;
        for (HeaderElement helem: helems) {
            if (helem.getParameterByName("version") != null) {
                versioned = true;
            }
            if (helem.getParameterByName("expires") != null) {
               netscape = true;
            }
        }
        if (netscape) {
        }
        if (versioned) {
            return getStrict().parse(helems, origin);
        } else if (netscape) {
            return getNetscape().parse(header, origin);
        } else {
            return getCompat().parse(helems, origin);
        }
    }
    public void validate(
            final Cookie cookie, 
            final CookieOrigin origin) throws MalformedCookieException {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
        if (cookie.getVersion() > 0) {
            getStrict().validate(cookie, origin);
        } else {
            getCompat().validate(cookie, origin);
        }
    }
    public boolean match(final Cookie cookie, final CookieOrigin origin) {
        if (cookie == null) {
            throw new IllegalArgumentException("Cookie may not be null");
        }
        if (origin == null) {
            throw new IllegalArgumentException("Cookie origin may not be null");
        }
        if (cookie.getVersion() > 0) {
            return getStrict().match(cookie, origin);
        } else {
            return getCompat().match(cookie, origin);
        }
    }
    public List<Header> formatCookies(final List<Cookie> cookies) {
        if (cookies == null) {
            throw new IllegalArgumentException("List of cookie may not be null");
        }
        int version = Integer.MAX_VALUE;
        for (Cookie cookie: cookies) {
            if (cookie.getVersion() < version) {
                version = cookie.getVersion();
            }
        }
        if (version > 0) {
            return getStrict().formatCookies(cookies);
        } else {
            return getCompat().formatCookies(cookies);
        }
    }
    public int getVersion() {
        return getStrict().getVersion();
    }
    public Header getVersionHeader() {
        return getStrict().getVersionHeader();
    }
}