public final class PluginData {
    private InputStream mStream;
    private long mContentLength;
    private Map<String, String[]> mHeaders;
    private int mStatusCode;
    @Deprecated
    public PluginData(
            InputStream stream,
            long length,
            Map<String, String[]> headers,
            int code) {
        mStream = stream;
        mContentLength = length;
        mHeaders = headers;
        mStatusCode = code;
    }
    @Deprecated
    public InputStream getInputStream() {
        return mStream;
    }
    @Deprecated
    public long getContentLength() {
        return mContentLength;
    }
    @Deprecated
    public Map<String, String[]> getHeaders() {
        return mHeaders;
    }
    @Deprecated
    public int getStatusCode() {
        return mStatusCode;
    }
}
