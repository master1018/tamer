public final class CacheManager {
    private static final String LOGTAG = "cache";
    static final String HEADER_KEY_IFMODIFIEDSINCE = "if-modified-since";
    static final String HEADER_KEY_IFNONEMATCH = "if-none-match";
    private static final String NO_STORE = "no-store";
    private static final String NO_CACHE = "no-cache";
    private static final String MAX_AGE = "max-age";
    private static final String MANIFEST_MIME = "text/cache-manifest";
    private static long CACHE_THRESHOLD = 6 * 1024 * 1024;
    private static long CACHE_TRIM_AMOUNT = 2 * 1024 * 1024;
    static long CACHE_MAX_SIZE = (CACHE_THRESHOLD - CACHE_TRIM_AMOUNT) / 2;
    private static boolean mDisabled;
    private static int mRefCount;
    private static int mTrimCacheCount = 0;
    private static final int TRIM_CACHE_INTERVAL = 5;
    private static WebViewDatabase mDataBase;
    private static File mBaseDir;
    private static boolean mClearCacheOnInit = false;
    public static class CacheResult {
        int httpStatusCode;
        long contentLength;
        long expires;
        String expiresString;
        String localPath;
        String lastModified;
        String etag;
        String mimeType;
        String location;
        String encoding;
        String contentdisposition;
        String crossDomain;
        InputStream inStream;
        OutputStream outStream;
        File outFile;
        public int getHttpStatusCode() {
            return httpStatusCode;
        }
        public long getContentLength() {
            return contentLength;
        }
        public String getLocalPath() {
            return localPath;
        }
        public long getExpires() {
            return expires;
        }
        public String getExpiresString() {
            return expiresString;
        }
        public String getLastModified() {
            return lastModified;
        }
        public String getETag() {
            return etag;
        }
        public String getMimeType() {
            return mimeType;
        }
        public String getLocation() {
            return location;
        }
        public String getEncoding() {
            return encoding;
        }
        public String getContentDisposition() {
            return contentdisposition;
        }
        public InputStream getInputStream() {
            return inStream;
        }
        public OutputStream getOutputStream() {
            return outStream;
        }
        public void setInputStream(InputStream stream) {
            this.inStream = stream;
        }
        public void setEncoding(String encoding) {
            this.encoding = encoding;
        }
    }
    static void init(Context context) {
        mDataBase = WebViewDatabase.getInstance(context.getApplicationContext());
        mBaseDir = new File(context.getCacheDir(), "webviewCache");
        if (createCacheDirectory() && mClearCacheOnInit) {
            removeAllCacheFiles();
            mClearCacheOnInit = false;
        }
    }
    static private boolean createCacheDirectory() {
        if (!mBaseDir.exists()) {
            if(!mBaseDir.mkdirs()) {
                Log.w(LOGTAG, "Unable to create webviewCache directory");
                return false;
            }
            FileUtils.setPermissions(
                    mBaseDir.toString(),
                    FileUtils.S_IRWXU|FileUtils.S_IRWXG|FileUtils.S_IXOTH,
                    -1, -1);
            WebViewWorker.getHandler().sendEmptyMessage(
                    WebViewWorker.MSG_CLEAR_CACHE);
            return true;
        }
        return false;
    }
    public static File getCacheFileBaseDir() {
        return mBaseDir;
    }
    static void setCacheDisabled(boolean disabled) {
        if (disabled == mDisabled) {
            return;
        }
        mDisabled = disabled;
        if (mDisabled) {
            removeAllCacheFiles();
        }
    }
    public static boolean cacheDisabled() {
        return mDisabled;
    }
    static boolean enableTransaction() {
        if (++mRefCount == 1) {
            mDataBase.startCacheTransaction();
            return true;
        }
        return false;
    }
    static boolean disableTransaction() {
        if (--mRefCount == 0) {
            mDataBase.endCacheTransaction();
            return true;
        }
        return false;
    }
    static boolean startTransaction() {
        return mDataBase.startCacheTransaction();
    }
    static boolean endTransaction() {
        boolean ret = mDataBase.endCacheTransaction();
        if (++mTrimCacheCount >= TRIM_CACHE_INTERVAL) {
            mTrimCacheCount = 0;
            trimCacheIfNeeded();
        }
        return ret;
    }
    @Deprecated
    public static boolean startCacheTransaction() {
        return false;
    }
    @Deprecated
    public static boolean endCacheTransaction() {
        return false;
    }
    public static CacheResult getCacheFile(String url,
            Map<String, String> headers) {
        return getCacheFile(url, 0, headers);
    }
    static CacheResult getCacheFile(String url, long postIdentifier,
            Map<String, String> headers) {
        if (mDisabled) {
            return null;
        }
        String databaseKey = getDatabaseKey(url, postIdentifier);
        CacheResult result = mDataBase.getCache(databaseKey);
        if (result != null) {
            if (result.contentLength == 0) {
                if (!checkCacheRedirect(result.httpStatusCode)) {
                    mDataBase.removeCache(databaseKey);
                    return null;
                }
            } else {
                File src = new File(mBaseDir, result.localPath);
                try {
                    result.inStream = new FileInputStream(src);
                } catch (FileNotFoundException e) {
                    mDataBase.removeCache(databaseKey);
                    return null;
                }
            }
        } else {
            return null;
        }
        if (headers != null && result.expires >= 0
                && result.expires <= System.currentTimeMillis()) {
            if (result.lastModified == null && result.etag == null) {
                return null;
            }
            if (result.etag != null) {
                headers.put(HEADER_KEY_IFNONEMATCH, result.etag);
            }
            if (result.lastModified != null) {
                headers.put(HEADER_KEY_IFMODIFIEDSINCE, result.lastModified);
            }
        }
        if (DebugFlags.CACHE_MANAGER) {
            Log.v(LOGTAG, "getCacheFile for url " + url);
        }
        return result;
    }
    public static CacheResult createCacheFile(String url, int statusCode,
            Headers headers, String mimeType, boolean forceCache) {
        return createCacheFile(url, statusCode, headers, mimeType, 0,
                forceCache);
    }
    static CacheResult createCacheFile(String url, int statusCode,
            Headers headers, String mimeType, long postIdentifier,
            boolean forceCache) {
        if (!forceCache && mDisabled) {
            return null;
        }
        String databaseKey = getDatabaseKey(url, postIdentifier);
        if (statusCode == 303) {
            mDataBase.removeCache(databaseKey);
            return null;
        }
        if (checkCacheRedirect(statusCode) && !headers.getSetCookie().isEmpty()) {
            mDataBase.removeCache(databaseKey);
            return null;
        }
        CacheResult ret = parseHeaders(statusCode, headers, mimeType);
        if (ret == null) {
            mDataBase.removeCache(databaseKey);
        } else {
            setupFiles(databaseKey, ret);
            try {
                ret.outStream = new FileOutputStream(ret.outFile);
            } catch (FileNotFoundException e) {
                if (createCacheDirectory()) {
                    try {
                        ret.outStream = new FileOutputStream(ret.outFile);
                    } catch  (FileNotFoundException e2) {
                        return null;
                    }
                } else {
                    return null;
                }
            }
            ret.mimeType = mimeType;
        }
        return ret;
    }
    public static void saveCacheFile(String url, CacheResult cacheRet) {
        saveCacheFile(url, 0, cacheRet);
    }
    static void saveCacheFile(String url, long postIdentifier,
            CacheResult cacheRet) {
        try {
            cacheRet.outStream.close();
        } catch (IOException e) {
            return;
        }
        if (!cacheRet.outFile.exists()) {
            return;
        }
        boolean redirect = checkCacheRedirect(cacheRet.httpStatusCode);
        if (redirect) {
            cacheRet.contentLength = 0;
            cacheRet.localPath = "";
        }
        if ((redirect || cacheRet.contentLength == 0)
                && !cacheRet.outFile.delete()) {
            Log.e(LOGTAG, cacheRet.outFile.getPath() + " delete failed.");
        }
        if (cacheRet.contentLength == 0) {
            return;
        }
        mDataBase.addCache(getDatabaseKey(url, postIdentifier), cacheRet);
        if (DebugFlags.CACHE_MANAGER) {
            Log.v(LOGTAG, "saveCacheFile for url " + url);
        }
    }
    static boolean cleanupCacheFile(CacheResult cacheRet) {
        try {
            cacheRet.outStream.close();
        } catch (IOException e) {
            return false;
        }
        return cacheRet.outFile.delete();
    }
    static boolean removeAllCacheFiles() {
        if (mBaseDir == null) {
            mClearCacheOnInit = true;
            return true;
        }
        WebViewWorker.getHandler().sendEmptyMessage(
                WebViewWorker.MSG_CLEAR_CACHE);
        final Runnable clearCache = new Runnable() {
            public void run() {
                try {
                    String[] files = mBaseDir.list();
                    if (files != null) {
                        for (int i = 0; i < files.length; i++) {
                            File f = new File(mBaseDir, files[i]);
                            if (!f.delete()) {
                                Log.e(LOGTAG, f.getPath() + " delete failed.");
                            }
                        }
                    }
                } catch (SecurityException e) {
                }
            }
        };
        new Thread(clearCache).start();
        return true;
    }
    static boolean cacheEmpty() {
        return mDataBase.hasCache();
    }
    static void trimCacheIfNeeded() {
        if (mDataBase.getCacheTotalSize() > CACHE_THRESHOLD) {
            List<String> pathList = mDataBase.trimCache(CACHE_TRIM_AMOUNT);
            int size = pathList.size();
            for (int i = 0; i < size; i++) {
                File f = new File(mBaseDir, pathList.get(i));
                if (!f.delete()) {
                    Log.e(LOGTAG, f.getPath() + " delete failed.");
                }
            }
            final List<String> fileList = mDataBase.getAllCacheFileNames();
            if (fileList == null) return;
            String[] toDelete = mBaseDir.list(new FilenameFilter() {
                public boolean accept(File dir, String filename) {
                    if (fileList.contains(filename)) {
                        return false;
                    } else {
                        return true;
                    }
                }
            });
            if (toDelete == null) return;
            size = toDelete.length;
            for (int i = 0; i < size; i++) {
                File f = new File(mBaseDir, toDelete[i]);
                if (!f.delete()) {
                    Log.e(LOGTAG, f.getPath() + " delete failed.");
                }
            }
        }
    }
    static void clearCache() {
        mDataBase.clearCache();
    }
    private static boolean checkCacheRedirect(int statusCode) {
        if (statusCode == 301 || statusCode == 302 || statusCode == 307) {
            return true;
        } else {
            return false;
        }
    }
    private static String getDatabaseKey(String url, long postIdentifier) {
        if (postIdentifier == 0) return url;
        return postIdentifier + url;
    }
    @SuppressWarnings("deprecation")
    private static void setupFiles(String url, CacheResult cacheRet) {
        if (true) {
            int hashCode = url.hashCode();
            StringBuffer ret = new StringBuffer(8);
            appendAsHex(hashCode, ret);
            String path = ret.toString();
            File file = new File(mBaseDir, path);
            if (true) {
                boolean checkOldPath = true;
                while (file.exists()) {
                    if (checkOldPath) {
                        CacheResult oldResult = mDataBase.getCache(url);
                        if (oldResult != null && oldResult.contentLength > 0) {
                            if (path.equals(oldResult.localPath)) {
                                path = oldResult.localPath;
                            } else {
                                path = oldResult.localPath;
                                file = new File(mBaseDir, path);
                            }
                            break;
                        }
                        checkOldPath = false;
                    }
                    ret = new StringBuffer(8);
                    appendAsHex(++hashCode, ret);
                    path = ret.toString();
                    file = new File(mBaseDir, path);
                }
            }
            cacheRet.localPath = path;
            cacheRet.outFile = file;
        } else {
            Digest digest = new SHA1Digest();
            int digestLen = digest.getDigestSize();
            byte[] hash = new byte[digestLen];
            int urlLen = url.length();
            byte[] data = new byte[urlLen];
            url.getBytes(0, urlLen, data, 0);
            digest.update(data, 0, urlLen);
            digest.doFinal(hash, 0);
            StringBuffer result = new StringBuffer(2 * digestLen);
            for (int i = 0; i < digestLen; i = i + 4) {
                int h = (0x00ff & hash[i]) << 24 | (0x00ff & hash[i + 1]) << 16
                        | (0x00ff & hash[i + 2]) << 8 | (0x00ff & hash[i + 3]);
                appendAsHex(h, result);
            }
            cacheRet.localPath = result.toString();
            cacheRet.outFile = new File(mBaseDir, cacheRet.localPath);
        }
    }
    private static void appendAsHex(int i, StringBuffer ret) {
        String hex = Integer.toHexString(i);
        switch (hex.length()) {
            case 1:
                ret.append("0000000");
                break;
            case 2:
                ret.append("000000");
                break;
            case 3:
                ret.append("00000");
                break;
            case 4:
                ret.append("0000");
                break;
            case 5:
                ret.append("000");
                break;
            case 6:
                ret.append("00");
                break;
            case 7:
                ret.append("0");
                break;
        }
        ret.append(hex);
    }
    private static CacheResult parseHeaders(int statusCode, Headers headers,
            String mimeType) {
        if (headers.getContentLength() > CACHE_MAX_SIZE) return null;
        if (MANIFEST_MIME.equals(mimeType)) return null;
        CacheResult ret = new CacheResult();
        ret.httpStatusCode = statusCode;
        String location = headers.getLocation();
        if (location != null) ret.location = location;
        ret.expires = -1;
        ret.expiresString = headers.getExpires();
        if (ret.expiresString != null) {
            try {
                ret.expires = AndroidHttpClient.parseDate(ret.expiresString);
            } catch (IllegalArgumentException ex) {
                if ("-1".equals(ret.expiresString)
                        || "0".equals(ret.expiresString)) {
                    ret.expires = 0;
                } else {
                    Log.e(LOGTAG, "illegal expires: " + ret.expiresString);
                }
            }
        }
        String contentDisposition = headers.getContentDisposition();
        if (contentDisposition != null) {
            ret.contentdisposition = contentDisposition;
        }
        String crossDomain = headers.getXPermittedCrossDomainPolicies();
        if (crossDomain != null) {
            ret.crossDomain = crossDomain;
        }
        String lastModified = headers.getLastModified();
        if (lastModified != null && lastModified.length() > 0) {
            ret.lastModified = lastModified;
        }
        String etag = headers.getEtag();
        if (etag != null && etag.length() > 0) ret.etag = etag;
        String cacheControl = headers.getCacheControl();
        if (cacheControl != null) {
            String[] controls = cacheControl.toLowerCase().split("[ ,;]");
            for (int i = 0; i < controls.length; i++) {
                if (NO_STORE.equals(controls[i])) {
                    return null;
                }
                if (NO_CACHE.equals(controls[i])) {
                    ret.expires = 0;
                } else if (controls[i].startsWith(MAX_AGE)) {
                    int separator = controls[i].indexOf('=');
                    if (separator < 0) {
                        separator = controls[i].indexOf(':');
                    }
                    if (separator > 0) {
                        String s = controls[i].substring(separator + 1);
                        try {
                            long sec = Long.parseLong(s);
                            if (sec >= 0) {
                                ret.expires = System.currentTimeMillis() + 1000
                                        * sec;
                            }
                        } catch (NumberFormatException ex) {
                            if ("1d".equals(s)) {
                                ret.expires = System.currentTimeMillis() + 86400000; 
                            } else {
                                Log.e(LOGTAG, "exception in parseHeaders for "
                                        + "max-age:"
                                        + controls[i].substring(separator + 1));
                                ret.expires = 0;
                            }
                        }
                    }
                }
            }
        }
        if (NO_CACHE.equals(headers.getPragma())) {
            ret.expires = 0;
        }
        if (ret.expires == -1) {
            if (ret.httpStatusCode == 301) {
                ret.expires = Long.MAX_VALUE;
            } else if (ret.httpStatusCode == 302 || ret.httpStatusCode == 307) {
                ret.expires = 0;
            } else if (ret.lastModified == null) {
                if (!mimeType.startsWith("text/html")) {
                    ret.expires = System.currentTimeMillis() + 86400000; 
                } else {
                    ret.expires = 0;
                }
            } else {
                long lastmod = System.currentTimeMillis() + 86400000;
                try {
                    lastmod = AndroidHttpClient.parseDate(ret.lastModified);
                } catch (IllegalArgumentException ex) {
                    Log.e(LOGTAG, "illegal lastModified: " + ret.lastModified);
                }
                long difference = System.currentTimeMillis() - lastmod;
                if (difference > 0) {
                    ret.expires = System.currentTimeMillis() + difference / 5;
                } else {
                    ret.expires = lastmod;
                }
            }
        }
        return ret;
    }
}
