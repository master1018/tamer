public final class CookieManager {
    private static CookieManager sRef;
    private static final String LOGTAG = "webkit";
    private static final String DOMAIN = "domain";
    private static final String PATH = "path";
    private static final String EXPIRES = "expires";
    private static final String SECURE = "secure";
    private static final String MAX_AGE = "max-age";
    private static final String HTTP_ONLY = "httponly";
    private static final String HTTPS = "https";
    private static final char PERIOD = '.';
    private static final char COMMA = ',';
    private static final char SEMICOLON = ';';
    private static final char EQUAL = '=';
    private static final char PATH_DELIM = '/';
    private static final char QUESTION_MARK = '?';
    private static final char WHITE_SPACE = ' ';
    private static final char QUOTATION = '\"';
    private static final int SECURE_LENGTH = SECURE.length();
    private static final int HTTP_ONLY_LENGTH = HTTP_ONLY.length();
    private static final int MAX_COOKIE_LENGTH = 4 * 1024;
    private static final int MAX_COOKIE_COUNT_PER_BASE_DOMAIN = 50;
    private static final int MAX_DOMAIN_COUNT = 200;
    private static final int MAX_RAM_COOKIES_COUNT = 1000;
    private static final int MAX_RAM_DOMAIN_COUNT = 15;
    private Map<String, ArrayList<Cookie>> mCookieMap = new LinkedHashMap
            <String, ArrayList<Cookie>>(MAX_DOMAIN_COUNT, 0.75f, true);
    private boolean mAcceptCookie = true;
    private final static String[] BAD_COUNTRY_2LDS =
          { "ac", "co", "com", "ed", "edu", "go", "gouv", "gov", "info",
            "lg", "ne", "net", "or", "org" };
    static {
        Arrays.sort(BAD_COUNTRY_2LDS);
    }
    static class Cookie {
        static final byte MODE_NEW = 0;
        static final byte MODE_NORMAL = 1;
        static final byte MODE_DELETED = 2;
        static final byte MODE_REPLACED = 3;
        String domain;
        String path;
        String name;
        String value;
        long expires;
        long lastAcessTime;
        long lastUpdateTime;
        boolean secure;
        byte mode;
        Cookie() {
        }
        Cookie(String defaultDomain, String defaultPath) {
            domain = defaultDomain;
            path = defaultPath;
            expires = -1;
        }
        boolean exactMatch(Cookie in) {
            boolean valuesMatch = !((value == null) ^ (in.value == null));
            return domain.equals(in.domain) && path.equals(in.path) &&
                    name.equals(in.name) && valuesMatch;
        }
        boolean domainMatch(String urlHost) {
            if (domain.startsWith(".")) {
                if (urlHost.endsWith(domain.substring(1))) {
                    int len = domain.length();
                    int urlLen = urlHost.length();
                    if (urlLen > len - 1) {
                        return urlHost.charAt(urlLen - len) == PERIOD;
                    }
                    return true;
                }
                return false;
            } else {
                return urlHost.equals(domain);
            }
        }
        boolean pathMatch(String urlPath) {
            if (urlPath.startsWith(path)) {
                int len = path.length();
                if (len == 0) {
                    Log.w(LOGTAG, "Empty cookie path");
                    return false;
                }
                int urlLen = urlPath.length();
                if (path.charAt(len-1) != PATH_DELIM && urlLen > len) {
                    return urlPath.charAt(len) == PATH_DELIM;
                }
                return true;
            }
            return false;
        }
        public String toString() {
            return "domain: " + domain + "; path: " + path + "; name: " + name
                    + "; value: " + value;
        }
    }
    private static final CookieComparator COMPARATOR = new CookieComparator();
    private static final class CookieComparator implements Comparator<Cookie> {
        public int compare(Cookie cookie1, Cookie cookie2) {
            int diff = cookie2.path.length() - cookie1.path.length();
            if (diff != 0) return diff;
            diff = cookie2.domain.length() - cookie1.domain.length();
            if (diff != 0) return diff;
            if (cookie2.value == null) {
                if (cookie1.value != null) {
                    return -1;
                }
            } else if (cookie1.value == null) {
                return 1;
            }
            return cookie1.name.compareTo(cookie2.name);
        }
    }
    private CookieManager() {
    }
    protected Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException("doesn't implement Cloneable");
    }
    public static synchronized CookieManager getInstance() {
        if (sRef == null) {
            sRef = new CookieManager();
        }
        return sRef;
    }
    public synchronized void setAcceptCookie(boolean accept) {
        mAcceptCookie = accept;
    }
    public synchronized boolean acceptCookie() {
        return mAcceptCookie;
    }
    public void setCookie(String url, String value) {
        WebAddress uri;
        try {
            uri = new WebAddress(url);
        } catch (ParseException ex) {
            Log.e(LOGTAG, "Bad address: " + url);
            return;
        }
        setCookie(uri, value);
    }
    public synchronized void setCookie(WebAddress uri, String value) {
        if (value != null && value.length() > MAX_COOKIE_LENGTH) {
            return;
        }
        if (!mAcceptCookie || uri == null) {
            return;
        }
        if (DebugFlags.COOKIE_MANAGER) {
            Log.v(LOGTAG, "setCookie: uri: " + uri + " value: " + value);
        }
        String[] hostAndPath = getHostAndPath(uri);
        if (hostAndPath == null) {
            return;
        }
        if (hostAndPath[1].length() > 1) {
            int index = hostAndPath[1].lastIndexOf(PATH_DELIM);
            hostAndPath[1] = hostAndPath[1].substring(0, 
                    index > 0 ? index : index + 1);
        }
        ArrayList<Cookie> cookies = null;
        try {
            cookies = parseCookie(hostAndPath[0], hostAndPath[1], value);
        } catch (RuntimeException ex) {
            Log.e(LOGTAG, "parse cookie failed for: " + value);
        }
        if (cookies == null || cookies.size() == 0) {
            return;
        }
        String baseDomain = getBaseDomain(hostAndPath[0]);
        ArrayList<Cookie> cookieList = mCookieMap.get(baseDomain);
        if (cookieList == null) {
            cookieList = CookieSyncManager.getInstance()
                    .getCookiesForDomain(baseDomain);
            mCookieMap.put(baseDomain, cookieList);
        }
        long now = System.currentTimeMillis();
        int size = cookies.size();
        for (int i = 0; i < size; i++) {
            Cookie cookie = cookies.get(i);
            boolean done = false;
            Iterator<Cookie> iter = cookieList.iterator();
            while (iter.hasNext()) {
                Cookie cookieEntry = iter.next();
                if (cookie.exactMatch(cookieEntry)) {
                    if (cookie.expires < 0 || cookie.expires > now) {
                        if (!cookieEntry.secure || HTTPS.equals(uri.mScheme)) {
                            cookieEntry.value = cookie.value;
                            cookieEntry.expires = cookie.expires;
                            cookieEntry.secure = cookie.secure;
                            cookieEntry.lastAcessTime = now;
                            cookieEntry.lastUpdateTime = now;
                            cookieEntry.mode = Cookie.MODE_REPLACED;
                        }
                    } else {
                        cookieEntry.lastUpdateTime = now;
                        cookieEntry.mode = Cookie.MODE_DELETED;
                    }
                    done = true;
                    break;
                }
            }
            if (!done && (cookie.expires < 0 || cookie.expires > now)) {
                cookie.lastAcessTime = now;
                cookie.lastUpdateTime = now;
                cookie.mode = Cookie.MODE_NEW;
                if (cookieList.size() > MAX_COOKIE_COUNT_PER_BASE_DOMAIN) {
                    Cookie toDelete = new Cookie();
                    toDelete.lastAcessTime = now;
                    Iterator<Cookie> iter2 = cookieList.iterator();
                    while (iter2.hasNext()) {
                        Cookie cookieEntry2 = iter2.next();
                        if ((cookieEntry2.lastAcessTime < toDelete.lastAcessTime)
                                && cookieEntry2.mode != Cookie.MODE_DELETED) {
                            toDelete = cookieEntry2;
                        }
                    }
                    toDelete.mode = Cookie.MODE_DELETED;
                }
                cookieList.add(cookie);
            }
        }
    }
    public String getCookie(String url) {
        WebAddress uri;
        try {
            uri = new WebAddress(url);
        } catch (ParseException ex) {
            Log.e(LOGTAG, "Bad address: " + url);
            return null;
        }
        return getCookie(uri);
    }
    public synchronized String getCookie(WebAddress uri) {
        if (!mAcceptCookie || uri == null) {
            return null;
        }
        String[] hostAndPath = getHostAndPath(uri);
        if (hostAndPath == null) {
            return null;
        }
        String baseDomain = getBaseDomain(hostAndPath[0]);
        ArrayList<Cookie> cookieList = mCookieMap.get(baseDomain);
        if (cookieList == null) {
            cookieList = CookieSyncManager.getInstance()
                    .getCookiesForDomain(baseDomain);
            mCookieMap.put(baseDomain, cookieList);
        }
        long now = System.currentTimeMillis();
        boolean secure = HTTPS.equals(uri.mScheme);
        Iterator<Cookie> iter = cookieList.iterator();
        SortedSet<Cookie> cookieSet = new TreeSet<Cookie>(COMPARATOR);
        while (iter.hasNext()) {
            Cookie cookie = iter.next();
            if (cookie.domainMatch(hostAndPath[0]) &&
                    cookie.pathMatch(hostAndPath[1])
                    && (cookie.expires < 0 || cookie.expires > now)
                    && (!cookie.secure || secure)
                    && cookie.mode != Cookie.MODE_DELETED) {
                cookie.lastAcessTime = now;
                cookieSet.add(cookie);
            }
        }
        StringBuilder ret = new StringBuilder(256);
        Iterator<Cookie> setIter = cookieSet.iterator();
        while (setIter.hasNext()) {
            Cookie cookie = setIter.next();
            if (ret.length() > 0) {
                ret.append(SEMICOLON);
                ret.append(WHITE_SPACE);
            }
            ret.append(cookie.name);
            if (cookie.value != null) {
                ret.append(EQUAL);
                ret.append(cookie.value);
            }
        }
        if (ret.length() > 0) {
            if (DebugFlags.COOKIE_MANAGER) {
                Log.v(LOGTAG, "getCookie: uri: " + uri + " value: " + ret);
            }
            return ret.toString();
        } else {
            if (DebugFlags.COOKIE_MANAGER) {
                Log.v(LOGTAG, "getCookie: uri: " + uri
                        + " But can't find cookie.");
            }
            return null;
        }
    }
    public void removeSessionCookie() {
        final Runnable clearCache = new Runnable() {
            public void run() {
                synchronized(CookieManager.this) {
                    Collection<ArrayList<Cookie>> cookieList = mCookieMap.values();
                    Iterator<ArrayList<Cookie>> listIter = cookieList.iterator();
                    while (listIter.hasNext()) {
                        ArrayList<Cookie> list = listIter.next();
                        Iterator<Cookie> iter = list.iterator();
                        while (iter.hasNext()) {
                            Cookie cookie = iter.next();
                            if (cookie.expires == -1) {
                                iter.remove();
                            }
                        }
                    }
                    CookieSyncManager.getInstance().clearSessionCookies();
                }
            }
        };
        new Thread(clearCache).start();
    }
    public void removeAllCookie() {
        final Runnable clearCache = new Runnable() {
            public void run() {
                synchronized(CookieManager.this) {
                    mCookieMap = new LinkedHashMap<String, ArrayList<Cookie>>(
                            MAX_DOMAIN_COUNT, 0.75f, true);
                    CookieSyncManager.getInstance().clearAllCookies();
                }
            }
        };
        new Thread(clearCache).start();
    }
    public synchronized boolean hasCookies() {
        return CookieSyncManager.getInstance().hasCookies();
    }
    public void removeExpiredCookie() {
        final Runnable clearCache = new Runnable() {
            public void run() {
                synchronized(CookieManager.this) {
                    long now = System.currentTimeMillis();
                    Collection<ArrayList<Cookie>> cookieList = mCookieMap.values();
                    Iterator<ArrayList<Cookie>> listIter = cookieList.iterator();
                    while (listIter.hasNext()) {
                        ArrayList<Cookie> list = listIter.next();
                        Iterator<Cookie> iter = list.iterator();
                        while (iter.hasNext()) {
                            Cookie cookie = iter.next();
                            if (cookie.expires > 0 && cookie.expires < now) {
                                iter.remove();
                            }
                        }
                    }
                    CookieSyncManager.getInstance().clearExpiredCookies(now);
                }
            }
        };
        new Thread(clearCache).start();
    }
    synchronized ArrayList<Cookie> getUpdatedCookiesSince(long last) {
        ArrayList<Cookie> cookies = new ArrayList<Cookie>();
        Collection<ArrayList<Cookie>> cookieList = mCookieMap.values();
        Iterator<ArrayList<Cookie>> listIter = cookieList.iterator();
        while (listIter.hasNext()) {
            ArrayList<Cookie> list = listIter.next();
            Iterator<Cookie> iter = list.iterator();
            while (iter.hasNext()) {
                Cookie cookie = iter.next();
                if (cookie.lastUpdateTime > last) {
                    cookies.add(cookie);
                }
            }
        }
        return cookies;
    }
    synchronized void deleteACookie(Cookie cookie) {
        if (cookie.mode == Cookie.MODE_DELETED) {
            String baseDomain = getBaseDomain(cookie.domain);
            ArrayList<Cookie> cookieList = mCookieMap.get(baseDomain);
            if (cookieList != null) {
                cookieList.remove(cookie);
                if (cookieList.isEmpty()) {
                    mCookieMap.remove(baseDomain);
                }
            }
        }
    }
    synchronized void syncedACookie(Cookie cookie) {
        cookie.mode = Cookie.MODE_NORMAL;
    }
    synchronized ArrayList<Cookie> deleteLRUDomain() {
        int count = 0;
        int byteCount = 0;
        int mapSize = mCookieMap.size();
        if (mapSize < MAX_RAM_DOMAIN_COUNT) {
            Collection<ArrayList<Cookie>> cookieLists = mCookieMap.values();
            Iterator<ArrayList<Cookie>> listIter = cookieLists.iterator();
            while (listIter.hasNext() && count < MAX_RAM_COOKIES_COUNT) {
                ArrayList<Cookie> list = listIter.next();
                if (DebugFlags.COOKIE_MANAGER) {
                    Iterator<Cookie> iter = list.iterator();
                    while (iter.hasNext() && count < MAX_RAM_COOKIES_COUNT) {
                        Cookie cookie = iter.next();
                        byteCount += cookie.domain.length()
                                + cookie.path.length()
                                + cookie.name.length()
                                + (cookie.value != null
                                        ? cookie.value.length()
                                        : 0)
                                + 14;
                        count++;
                    }
                } else {
                    count += list.size();
                }
            }
        }
        ArrayList<Cookie> retlist = new ArrayList<Cookie>();
        if (mapSize >= MAX_RAM_DOMAIN_COUNT || count >= MAX_RAM_COOKIES_COUNT) {
            if (DebugFlags.COOKIE_MANAGER) {
                Log.v(LOGTAG, count + " cookies used " + byteCount
                        + " bytes with " + mapSize + " domains");
            }
            Object[] domains = mCookieMap.keySet().toArray();
            int toGo = mapSize / 10 + 1;
            while (toGo-- > 0){
                String domain = domains[toGo].toString();
                if (DebugFlags.COOKIE_MANAGER) {
                    Log.v(LOGTAG, "delete domain: " + domain
                            + " from RAM cache");
                }
                retlist.addAll(mCookieMap.get(domain));
                mCookieMap.remove(domain);
            }
        }
        return retlist;
    }
    private String[] getHostAndPath(WebAddress uri) {
        if (uri.mHost != null && uri.mPath != null) {
            String[] ret = new String[2];
            ret[0] = uri.mHost.toLowerCase();
            ret[1] = uri.mPath;
            int index = ret[0].indexOf(PERIOD);
            if (index == -1) {
                if (uri.mScheme.equalsIgnoreCase("file")) {
                    ret[0] = "localhost";
                }
            } else if (index == ret[0].lastIndexOf(PERIOD)) {
                ret[0] = PERIOD + ret[0];
            }
            if (ret[1].charAt(0) != PATH_DELIM) {
                return null;
            }
            index = ret[1].indexOf(QUESTION_MARK);
            if (index != -1) {
                ret[1] = ret[1].substring(0, index);
            }
            return ret;
        } else
            return null;
    }
    private String getBaseDomain(String host) {
        int startIndex = 0;
        int nextIndex = host.indexOf(PERIOD);
        int lastIndex = host.lastIndexOf(PERIOD);
        while (nextIndex < lastIndex) {
            startIndex = nextIndex + 1;
            nextIndex = host.indexOf(PERIOD, startIndex);
        }
        if (startIndex > 0) {
            return host.substring(startIndex);
        } else {
            return host;
        }
    }
    private ArrayList<Cookie> parseCookie(String host, String path,
            String cookieString) {
        ArrayList<Cookie> ret = new ArrayList<Cookie>();
        int index = 0;
        int length = cookieString.length();
        while (true) {
            Cookie cookie = null;
            if (index < 0 || index >= length) {
                break;
            }
            if (cookieString.charAt(index) == WHITE_SPACE) {
                index++;
                continue;
            }
            int semicolonIndex = cookieString.indexOf(SEMICOLON, index);
            int equalIndex = cookieString.indexOf(EQUAL, index);
            cookie = new Cookie(host, path);
            if ((semicolonIndex != -1 && (semicolonIndex < equalIndex)) ||
                    equalIndex == -1) {
                if (semicolonIndex == -1) {
                    semicolonIndex = length;
                }
                cookie.name = cookieString.substring(index, semicolonIndex);
                cookie.value = null;
            } else {
                cookie.name = cookieString.substring(index, equalIndex);
                if ((equalIndex < length - 1) &&
                        (cookieString.charAt(equalIndex + 1) == QUOTATION)) {
                    index = cookieString.indexOf(QUOTATION, equalIndex + 2);
                    if (index == -1) {
                        break;
                    }
                }
                semicolonIndex = cookieString.indexOf(SEMICOLON, index);
                if (semicolonIndex == -1) {
                    semicolonIndex = length;
                }
                if (semicolonIndex - equalIndex > MAX_COOKIE_LENGTH) {
                    cookie.value = cookieString.substring(equalIndex + 1,
                            equalIndex + 1 + MAX_COOKIE_LENGTH);
                } else if (equalIndex + 1 == semicolonIndex
                        || semicolonIndex < equalIndex) {
                    cookie.value = "";
                } else {
                    cookie.value = cookieString.substring(equalIndex + 1,
                            semicolonIndex);
                }
            }
            index = semicolonIndex;
            while (true) {
                if (index < 0 || index >= length) {
                    break;
                }
                if (cookieString.charAt(index) == WHITE_SPACE
                        || cookieString.charAt(index) == SEMICOLON) {
                    index++;
                    continue;
                }
                if (cookieString.charAt(index) == COMMA) {
                    index++;
                    break;
                }
                if (length - index >= SECURE_LENGTH
                        && cookieString.substring(index, index + SECURE_LENGTH).
                        equalsIgnoreCase(SECURE)) {
                    index += SECURE_LENGTH;
                    cookie.secure = true;
                    if (index == length) break;
                    if (cookieString.charAt(index) == EQUAL) index++;
                    continue;
                }
                if (length - index >= HTTP_ONLY_LENGTH
                        && cookieString.substring(index,
                            index + HTTP_ONLY_LENGTH).
                        equalsIgnoreCase(HTTP_ONLY)) {
                    index += HTTP_ONLY_LENGTH;
                    if (index == length) break;
                    if (cookieString.charAt(index) == EQUAL) index++;
                    continue;
                }
                equalIndex = cookieString.indexOf(EQUAL, index);
                if (equalIndex > 0) {
                    String name = cookieString.substring(index, equalIndex)
                            .toLowerCase();
                    if (name.equals(EXPIRES)) {
                        int comaIndex = cookieString.indexOf(COMMA, equalIndex);
                        if ((comaIndex != -1) &&
                                (comaIndex - equalIndex <= 10)) {
                            index = comaIndex + 1;
                        }
                    }
                    semicolonIndex = cookieString.indexOf(SEMICOLON, index);
                    int commaIndex = cookieString.indexOf(COMMA, index);
                    if (semicolonIndex == -1 && commaIndex == -1) {
                        index = length;
                    } else if (semicolonIndex == -1) {
                        index = commaIndex;
                    } else if (commaIndex == -1) {
                        index = semicolonIndex;
                    } else {
                        index = Math.min(semicolonIndex, commaIndex);
                    }
                    String value =
                            cookieString.substring(equalIndex + 1, index);
                    if (value.length() > 2 && value.charAt(0) == QUOTATION) {
                        int endQuote = value.indexOf(QUOTATION, 1);
                        if (endQuote > 0) {
                            value = value.substring(1, endQuote);
                        }
                    }
                    if (name.equals(EXPIRES)) {
                        try {
                            cookie.expires = AndroidHttpClient.parseDate(value);
                        } catch (IllegalArgumentException ex) {
                            Log.e(LOGTAG,
                                    "illegal format for expires: " + value);
                        }
                    } else if (name.equals(MAX_AGE)) {
                        try {
                            cookie.expires = System.currentTimeMillis() + 1000
                                    * Long.parseLong(value);
                        } catch (NumberFormatException ex) {
                            Log.e(LOGTAG,
                                    "illegal format for max-age: " + value);
                        }
                    } else if (name.equals(PATH)) {
                        if (value.length() > 0) {
                            cookie.path = value;
                        }
                    } else if (name.equals(DOMAIN)) {
                        int lastPeriod = value.lastIndexOf(PERIOD);
                        if (lastPeriod == 0) {
                            cookie.domain = null;
                            continue;
                        }
                        try {
                            Integer.parseInt(value.substring(lastPeriod + 1));
                            if (!value.equals(host)) {
                                cookie.domain = null;
                            }
                            continue;
                        } catch (NumberFormatException ex) {
                        }
                        value = value.toLowerCase();
                        if (value.charAt(0) != PERIOD) {
                            value = PERIOD + value;
                            lastPeriod++;
                        }
                        if (host.endsWith(value.substring(1))) {
                            int len = value.length();
                            int hostLen = host.length();
                            if (hostLen > (len - 1)
                                    && host.charAt(hostLen - len) != PERIOD) {
                                cookie.domain = null;
                                continue;
                            }
                            if ((len == lastPeriod + 3)
                                    && (len >= 6 && len <= 8)) {
                                String s = value.substring(1, lastPeriod);
                                if (Arrays.binarySearch(BAD_COUNTRY_2LDS, s) >= 0) {
                                    cookie.domain = null;
                                    continue;
                                }
                            }
                            cookie.domain = value;
                        } else {
                            cookie.domain = null;
                        }
                    }
                } else {
                    index = length;
                }
            }
            if (cookie != null && cookie.domain != null) {
                ret.add(cookie);
            }
        }
        return ret;
    }
}
