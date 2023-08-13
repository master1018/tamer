class CertificateValidatorCache {
    public static long mSave = 0;
    public static long mCost = 0;
    private static final long CACHE_ENTRY_LIFETIME = 10 * 60 * 1000;
    private static CertificateFactory sCertificateFactory;
    private HashMap<Integer, CacheEntry> mCacheMap;
    private int mBigScrew;
    public static byte[] secureHash(Certificate[] certificates) {
        byte[] secureHash = null;
        long beg = SystemClock.uptimeMillis();
        if (certificates != null && certificates.length != 0) {
            byte[] encodedCertPath = null;
            try {
                synchronized (CertificateValidatorCache.class) {
                    if (sCertificateFactory == null) {
                        try {
                            sCertificateFactory =
                                CertificateFactory.getInstance("X.509");
                        } catch(GeneralSecurityException e) {
                            if (HttpLog.LOGV) {
                                HttpLog.v("CertificateValidatorCache:" +
                                          " failed to create the certificate factory");
                            }
                        }
                    }
                }
                CertPath certPath =
                    sCertificateFactory.generateCertPath(Arrays.asList(certificates));
                if (certPath != null) {
                    encodedCertPath = certPath.getEncoded();
                    if (encodedCertPath != null) {
                      Sha1MessageDigest messageDigest =
                          new Sha1MessageDigest();
                      secureHash = messageDigest.digest(encodedCertPath);
                    }
                }
            } catch (GeneralSecurityException e) {}
        }
        long end = SystemClock.uptimeMillis();
        mCost += (end - beg);
        return secureHash;
    }
    public CertificateValidatorCache() {
        Random random = new Random();
        mBigScrew = random.nextInt();
        mCacheMap = new HashMap<Integer, CacheEntry>();
    }
    public boolean has(String domain, byte[] secureHash) {
        boolean rval = false;
        if (domain != null && domain.length() != 0) {
            if (secureHash != null && secureHash.length != 0) {
                CacheEntry cacheEntry = (CacheEntry)mCacheMap.get(
                    new Integer(mBigScrew ^ domain.hashCode()));
                if (cacheEntry != null) {
                    if (!cacheEntry.expired()) {
                        rval = cacheEntry.has(domain, secureHash);
                        if (rval) {
                            mSave += cacheEntry.mSave;
                        }
                    } else {
                        mCacheMap.remove(cacheEntry);
                    }
                }
            }
        }
        return rval;
    }
    public boolean put(String domain, byte[] secureHash, long save) {
        if (domain != null && domain.length() != 0) {
            if (secureHash != null && secureHash.length != 0) {
                mCacheMap.put(
                    new Integer(mBigScrew ^ domain.hashCode()),
                    new CacheEntry(domain, secureHash, save));
                return true;
            }
        }
        return false;
    }
    private class CacheEntry {
        private byte[] mHash;
        private long mTime;
        public long mSave;
        private String mDomain;
        public CacheEntry(String domain, byte[] secureHash, long save) {
            mDomain = domain;
            mHash = secureHash;
            mSave = save;
            mTime = SystemClock.uptimeMillis();
        }
        public boolean expired() {
            return CACHE_ENTRY_LIFETIME < SystemClock.uptimeMillis() - mTime;
        }
        public boolean has(String domain, byte[] secureHash) {
            if (domain != null && 0 < domain.length()) {
                if (!mDomain.equals(domain)) {
                    return false;
                }
            }
            if (secureHash != null) {
                int hashLength = secureHash.length;
                if (0 < hashLength) {
                    if (hashLength == mHash.length) {
                        for (int i = 0; i < hashLength; ++i) {
                            if (secureHash[i] != mHash[i]) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }
};
