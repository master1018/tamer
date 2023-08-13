class CacheLoader extends StreamLoader {
    CacheManager.CacheResult mCacheResult;  
    CacheLoader(LoadListener loadListener, CacheManager.CacheResult result) {
        super(loadListener);
        mCacheResult = result;
    }
    @Override
    protected boolean setupStreamAndSendStatus() {
        mDataStream = mCacheResult.inStream;
        mContentLength = mCacheResult.contentLength;
        mLoadListener.status(1, 1, mCacheResult.httpStatusCode, "OK");
        return true;
    }
    @Override
    protected void buildHeaders(Headers headers) {
        StringBuilder sb = new StringBuilder(mCacheResult.mimeType);
        if (!TextUtils.isEmpty(mCacheResult.encoding)) {
            sb.append(';');
            sb.append(mCacheResult.encoding);
        }
        headers.setContentType(sb.toString());
        if (!TextUtils.isEmpty(mCacheResult.location)) {
            headers.setLocation(mCacheResult.location);
        }
        if (!TextUtils.isEmpty(mCacheResult.expiresString)) {
            headers.setExpires(mCacheResult.expiresString);
        }
        if (!TextUtils.isEmpty(mCacheResult.contentdisposition)) {
            headers.setContentDisposition(mCacheResult.contentdisposition);
        }
        if (!TextUtils.isEmpty(mCacheResult.crossDomain)) {
            headers.setXPermittedCrossDomainPolicies(mCacheResult.crossDomain);
        }
    }
}
