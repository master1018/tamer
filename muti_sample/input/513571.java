public final class CookieSyncManager extends WebSyncManager {
    private static CookieSyncManager sRef;
    private long mLastUpdate;
    private CookieSyncManager(Context context) {
        super(context, "CookieSyncManager");
    }
    public static synchronized CookieSyncManager getInstance() {
        if (sRef == null) {
            throw new IllegalStateException(
                    "CookieSyncManager::createInstance() needs to be called "
                            + "before CookieSyncManager::getInstance()");
        }
        return sRef;
    }
    public static synchronized CookieSyncManager createInstance(
            Context context) {
        if (sRef == null) {
            sRef = new CookieSyncManager(context.getApplicationContext());
        }
        return sRef;
    }
    ArrayList<Cookie> getCookiesForDomain(String domain) {
        if (mDataBase == null) {
            return new ArrayList<Cookie>();
        }
        return mDataBase.getCookiesForDomain(domain);
    }
    void clearAllCookies() {
        if (mDataBase == null) {
            return;
        }
        mDataBase.clearCookies();
    }
    boolean hasCookies() {
        if (mDataBase == null) {
            return false;
        }
        return mDataBase.hasCookies();
    }
    void clearSessionCookies() {
        if (mDataBase == null) {
            return;
        }
        mDataBase.clearSessionCookies();
    }
    void clearExpiredCookies(long now) {
        if (mDataBase == null) {
            return;
        }
        mDataBase.clearExpiredCookies(now);
    }
    protected void syncFromRamToFlash() {
        if (DebugFlags.COOKIE_SYNC_MANAGER) {
            Log.v(LOGTAG, "CookieSyncManager::syncFromRamToFlash STARTS");
        }
        if (!CookieManager.getInstance().acceptCookie()) {
            return;
        }
        ArrayList<Cookie> cookieList = CookieManager.getInstance()
                .getUpdatedCookiesSince(mLastUpdate);
        mLastUpdate = System.currentTimeMillis();
        syncFromRamToFlash(cookieList);
        ArrayList<Cookie> lruList =
                CookieManager.getInstance().deleteLRUDomain();
        syncFromRamToFlash(lruList);
        if (DebugFlags.COOKIE_SYNC_MANAGER) {
            Log.v(LOGTAG, "CookieSyncManager::syncFromRamToFlash DONE");
        }
    }
    private void syncFromRamToFlash(ArrayList<Cookie> list) {
        Iterator<Cookie> iter = list.iterator();
        while (iter.hasNext()) {
            Cookie cookie = iter.next();
            if (cookie.mode != Cookie.MODE_NORMAL) {
                if (cookie.mode != Cookie.MODE_NEW) {
                    mDataBase.deleteCookies(cookie.domain, cookie.path,
                            cookie.name);
                }
                if (cookie.mode != Cookie.MODE_DELETED) {
                    mDataBase.addCookie(cookie);
                    CookieManager.getInstance().syncedACookie(cookie);
                } else {
                    CookieManager.getInstance().deleteACookie(cookie);
                }
            }
        }
    }
}
