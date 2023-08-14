public class WebHistoryItem implements Cloneable {
    private static int sNextId = 0;
    private final int mId;
    private String mTitle;
    private String mUrl;
    private String mOriginalUrl;
    private Bitmap mFavicon;
    private byte[] mFlattenedData;
    private String mTouchIconUrl;
    private Object mCustomData;
    private WebHistoryItem() {
        synchronized (WebHistoryItem.class) {
            mId = sNextId++;
        }
    }
     WebHistoryItem(byte[] data) {
        mUrl = null; 
        mFlattenedData = data;
        synchronized (WebHistoryItem.class) {
            mId = sNextId++;
        }
    }
    private WebHistoryItem(WebHistoryItem item) {
        mUrl = item.mUrl;
        mTitle = item.mTitle;
        mFlattenedData = item.mFlattenedData;
        mFavicon = item.mFavicon;
        mId = item.mId;
}
    public int getId() {
        return mId;
    }
    public String getUrl() {
        return mUrl;
    }
    public String getOriginalUrl() {
        return mOriginalUrl;
    }
    public String getTitle() {
        return mTitle;
    }
    public Bitmap getFavicon() {
        return mFavicon;
    }
    public String getTouchIconUrl() {
        return mTouchIconUrl;
    }
    public Object getCustomData() {
        return mCustomData;
    }
    public void setCustomData(Object data) {
        mCustomData = data;
    }
     void setFavicon(Bitmap icon) {
        mFavicon = icon;
    }
     void setTouchIconUrl(String url) {
        mTouchIconUrl = url;
    }
     byte[] getFlattenedData() {
        return mFlattenedData;
    }
     void inflate(int nativeFrame) {
        inflate(nativeFrame, mFlattenedData);
    }
    protected synchronized WebHistoryItem clone() {
        return new WebHistoryItem(this);
    }
    private native void inflate(int nativeFrame, byte[] data);
    private void update(String url, String originalUrl, String title, 
            Bitmap favicon, byte[] data) {
        mUrl = url;
        mOriginalUrl = originalUrl;
        mTitle = title;
        mFavicon = favicon;
        mFlattenedData = data;
    }
}
