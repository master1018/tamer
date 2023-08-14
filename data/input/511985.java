public class RegionModel extends Model {
    private static final String DEFAULT_FIT = "meet";
    private final String mRegionId;
    private String mFit;
    private int mLeft;
    private int mTop;
    private int mWidth;
    private int mHeight;
    private String mBackgroundColor;
    public RegionModel(String regionId, int left, int top,
            int width, int height) {
        this(regionId, DEFAULT_FIT, left, top, width, height);
    }
    public RegionModel(String regionId, String fit, int left, int top,
            int width, int height) {
        this(regionId, fit, left, top, width, height, null);
    }
    public RegionModel(String regionId, String fit, int left, int top,
            int width, int height, String bgColor) {
        mRegionId = regionId;
        mFit = fit;
        mLeft = left;
        mTop = top;
        mWidth = width;
        mHeight = height;
        mBackgroundColor = bgColor;
    }
    public String getRegionId() {
        return mRegionId;
    }
    public String getFit() {
        return mFit;
    }
    public void setFit(String fit) {
        mFit = fit;
        notifyModelChanged(true);
    }
    public int getLeft() {
        return mLeft;
    }
    public void setLeft(int left) {
        mLeft = left;
        notifyModelChanged(true);
    }
    public int getTop() {
        return mTop;
    }
    public void setTop(int top) {
        mTop = top;
        notifyModelChanged(true);
    }
    public int getWidth() {
        return mWidth;
    }
    public void setWidth(int width) {
        mWidth = width;
        notifyModelChanged(true);
    }
    public int getHeight() {
        return mHeight;
    }
    public void setHeight(int height) {
        mHeight = height;
        notifyModelChanged(true);
    }
    public String getBackgroundColor() {
        return mBackgroundColor;
    }
    public void setBackgroundColor(String bgColor) {
        mBackgroundColor = bgColor;
        notifyModelChanged(true);
    }
}
