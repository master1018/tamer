class DataLoader extends StreamLoader {
    DataLoader(String dataUrl, LoadListener loadListener) {
        super(loadListener);
        String url = dataUrl.substring("data:".length());
        byte[] data = null;
        int commaIndex = url.indexOf(',');
        if (commaIndex != -1) {
            String contentType = url.substring(0, commaIndex);
            data = url.substring(commaIndex + 1).getBytes();
            loadListener.parseContentTypeHeader(contentType);
            if ("base64".equals(loadListener.transferEncoding())) {
                data = Base64.decode(data);
            }
        } else {
            data = url.getBytes();
        }
        if (data != null) {
            mDataStream = new ByteArrayInputStream(data);
            mContentLength = data.length;
        }
    }
    @Override
    protected boolean setupStreamAndSendStatus() {
        if (mDataStream != null) {
            mLoadListener.status(1, 1, 200, "OK");
            return true;
        } else {
            mLoadListener.error(EventHandler.ERROR,
                    mContext.getString(R.string.httpError));
            return false;
        }
    }
    @Override
    protected void buildHeaders(android.net.http.Headers h) {
    }
}
