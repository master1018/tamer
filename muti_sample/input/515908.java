class ContentLoader extends StreamLoader {
    private String mUrl;
    private String mContentType;
    ContentLoader(String rawUrl, LoadListener loadListener) {
        super(loadListener);
        int mimeIndex = rawUrl.lastIndexOf('?');
        if (mimeIndex != -1) {
            mUrl = rawUrl.substring(0, mimeIndex);
            mContentType = rawUrl.substring(mimeIndex + 1);
        } else {
            mUrl = rawUrl;
        }
    }
    private String errString(Exception ex) {
        String exMessage = ex.getMessage();
        String errString = mContext.getString(
                com.android.internal.R.string.httpErrorFileNotFound);
        if (exMessage != null) {
            errString += " " + exMessage;
        }
        return errString;
    }
    @Override
    protected boolean setupStreamAndSendStatus() {
        Uri uri = Uri.parse(mUrl);
        if (uri == null) {
            mLoadListener.error(
                    EventHandler.FILE_NOT_FOUND_ERROR,
                    mContext.getString(
                            com.android.internal.R.string.httpErrorBadUrl) +
                    " " + mUrl);
            return false;
        }
        try {
            mDataStream = mContext.getContentResolver().openInputStream(uri);
            mLoadListener.status(1, 1, 200, "OK");
        } catch (java.io.FileNotFoundException ex) {
            mLoadListener.error(EventHandler.FILE_NOT_FOUND_ERROR, errString(ex));
            return false;
        } catch (RuntimeException ex) {
            mLoadListener.error(EventHandler.FILE_ERROR, errString(ex));
            return false;
        }
        return true;
    }
    @Override
    protected void buildHeaders(Headers headers) {
        if (mContentType != null) {
            headers.setContentType("text/html");
        }
        headers.setCacheControl("no-store, no-cache");
    }
}
