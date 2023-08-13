public class ProgressEvent extends EventObject  {
    private URL url;
    private String contentType;
    private String method;
    private long progress;
    private long expected;
    private ProgressSource.State state;
    public ProgressEvent(ProgressSource source, URL url, String method, String contentType, ProgressSource.State state, long progress, long expected) {
        super(source);
        this.url = url;
        this.method = method;
        this.contentType = contentType;
        this.progress = progress;
        this.expected = expected;
        this.state = state;
    }
    public URL getURL()
    {
        return url;
    }
    public String getMethod()
    {
        return method;
    }
    public String getContentType()
    {
        return contentType;
    }
    public long getProgress()
    {
        return progress;
    }
    public long getExpected() {
        return expected;
    }
    public ProgressSource.State getState() {
        return state;
    }
    public String toString()    {
        return getClass().getName() + "[url=" + url + ", method=" + method + ", state=" + state
             + ", content-type=" + contentType + ", progress=" + progress + ", expected=" + expected + "]";
    }
}
