public abstract class RegionMediaModel extends MediaModel {
    protected RegionModel mRegion;
    protected boolean mVisible = true;
    public RegionMediaModel(Context context, String tag, Uri uri,
            RegionModel region) throws MmsException {
        this(context, tag, null, null, uri, region);
    }
    public RegionMediaModel(Context context, String tag, String contentType,
            String src, Uri uri, RegionModel region) throws MmsException {
        super(context, tag, contentType, src, uri);
        mRegion = region;
    }
    public RegionMediaModel(Context context, String tag, String contentType,
            String src, byte[] data, RegionModel region) {
        super(context, tag, contentType, src, data);
        mRegion = region;
    }
    public RegionMediaModel(Context context, String tag, String contentType,
            String src, DrmWrapper wrapper, RegionModel region)
            throws IOException {
        super(context, tag, contentType, src, wrapper);
        mRegion = region;
    }
    public RegionModel getRegion() {
        return mRegion;
    }
    public void setRegion(RegionModel region) {
        mRegion = region;
        notifyModelChanged(true);
    }
    public boolean isVisible() {
        return mVisible;
    }
    public void setVisible(boolean visible) {
        mVisible = visible;
    }
}
