class FileLoader extends StreamLoader {
    private String mPath;  
    private int mType;  
    private boolean mAllowFileAccess; 
    static final int TYPE_ASSET = 1;
    static final int TYPE_RES = 2;
    static final int TYPE_FILE = 3;
    private static final String LOGTAG = "webkit";
    FileLoader(String url, LoadListener loadListener, int type,
            boolean allowFileAccess) {
        super(loadListener);
        mType = type;
        mAllowFileAccess = allowFileAccess;
        int index = url.indexOf('?');
        if (mType == TYPE_ASSET) {
            mPath = index > 0 ? URLUtil.stripAnchor(
                    url.substring(URLUtil.ASSET_BASE.length(), index)) :
                    URLUtil.stripAnchor(url.substring(
                            URLUtil.ASSET_BASE.length()));
        } else if (mType == TYPE_RES) {
            mPath = index > 0 ? URLUtil.stripAnchor(
                    url.substring(URLUtil.RESOURCE_BASE.length(), index)) :
                    URLUtil.stripAnchor(url.substring(
                            URLUtil.RESOURCE_BASE.length()));
        } else {
            mPath = index > 0 ? URLUtil.stripAnchor(
                    url.substring(URLUtil.FILE_BASE.length(), index)) :
                    URLUtil.stripAnchor(url.substring(
                            URLUtil.FILE_BASE.length()));
        }
    }
    private String errString(Exception ex) {
        String exMessage = ex.getMessage();
        String errString = mContext.getString(R.string.httpErrorFileNotFound);
        if (exMessage != null) {
            errString += " " + exMessage;
        }
        return errString;
    }
    @Override
    protected boolean setupStreamAndSendStatus() {
        try {
            if (mType == TYPE_ASSET) {
                try {
                    mDataStream = mContext.getAssets().open(mPath);
                } catch (java.io.FileNotFoundException ex) {
                    mDataStream = mContext.getAssets().openNonAsset(mPath);
                }
            } else if (mType == TYPE_RES) {
                if (mPath == null || mPath.length() == 0) {
                    Log.e(LOGTAG, "Need a path to resolve the res file");
                    mLoadListener.error(EventHandler.FILE_ERROR, mContext
                            .getString(R.string.httpErrorFileNotFound));
                    return false;
                }
                int slash = mPath.indexOf('/');
                int dot = mPath.indexOf('.', slash);
                if (slash == -1 || dot == -1) {
                    Log.e(LOGTAG, "Incorrect res path: " + mPath);
                    mLoadListener.error(EventHandler.FILE_ERROR, mContext
                            .getString(R.string.httpErrorFileNotFound));
                    return false;
                }
                String subClassName = mPath.substring(0, slash);
                String fieldName = mPath.substring(slash + 1, dot);
                String errorMsg = null;
                try {
                    final Class<?> d = mContext.getApplicationContext()
                            .getClassLoader().loadClass(
                                    mContext.getPackageName() + ".R$"
                                            + subClassName);
                    final Field field = d.getField(fieldName);
                    final int id = field.getInt(null);
                    TypedValue value = new TypedValue();
                    mContext.getResources().getValue(id, value, true);
                    if (value.type == TypedValue.TYPE_STRING) {
                        mDataStream = mContext.getAssets().openNonAsset(
                                value.assetCookie, value.string.toString(),
                                AssetManager.ACCESS_STREAMING);
                    } else {
                        errorMsg = "Only support TYPE_STRING for the res files";
                    }
                } catch (ClassNotFoundException e) {
                    errorMsg = "Can't find class:  "
                            + mContext.getPackageName() + ".R$" + subClassName;
                } catch (SecurityException e) {
                    errorMsg = "Caught SecurityException: " + e;
                } catch (NoSuchFieldException e) {
                    errorMsg = "Can't find field:  " + fieldName + " in "
                            + mContext.getPackageName() + ".R$" + subClassName;
                } catch (IllegalArgumentException e) {
                    errorMsg = "Caught IllegalArgumentException: " + e;
                } catch (IllegalAccessException e) {
                    errorMsg = "Caught IllegalAccessException: " + e;
                }
                if (errorMsg != null) {
                    mLoadListener.error(EventHandler.FILE_ERROR, mContext
                            .getString(R.string.httpErrorFileNotFound));
                    return false;
                }
            } else {
                if (!mAllowFileAccess) {
                    mLoadListener.error(EventHandler.FILE_ERROR,
                            mContext.getString(R.string.httpErrorFileNotFound));
                    return false;
                }
                mDataStream = new FileInputStream(mPath);
                mContentLength = (new File(mPath)).length();
            }
            mLoadListener.status(1, 1, 200, "OK");
        } catch (java.io.FileNotFoundException ex) {
            mLoadListener.error(EventHandler.FILE_NOT_FOUND_ERROR, errString(ex));
            return false;
        } catch (java.io.IOException ex) {
            mLoadListener.error(EventHandler.FILE_ERROR, errString(ex));
            return false;
        }
        return true;
    }
    @Override
    protected void buildHeaders(Headers headers) {
    }
}
