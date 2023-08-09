public class BasicCookieStore implements CookieStore {
    private final ArrayList<Cookie> cookies;
    private final Comparator<Cookie> cookieComparator;
    public BasicCookieStore() {
        super();
        this.cookies = new ArrayList<Cookie>();
        this.cookieComparator = new CookieIdentityComparator();
    }
    public synchronized void addCookie(Cookie cookie) {
        if (cookie != null) {
            for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
                if (cookieComparator.compare(cookie, it.next()) == 0) {
                    it.remove();
                    break;
                }
            }
            if (!cookie.isExpired(new Date())) {
                cookies.add(cookie);
            }
        }
    }
    public synchronized void addCookies(Cookie[] cookies) {
        if (cookies != null) {
            for (Cookie cooky : cookies) {
                this.addCookie(cooky);
            }
        }
    }
    public synchronized List<Cookie> getCookies() {
        return Collections.unmodifiableList(this.cookies);
    }
    public synchronized boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        boolean removed = false;
        for (Iterator<Cookie> it = cookies.iterator(); it.hasNext();) {
            if (it.next().isExpired(date)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }
    @Override
    public String toString() {
        return cookies.toString();
    }
    public synchronized void clear() {
        cookies.clear();
    }
}
