 class CanvasAlternateSelection {
    private final CanvasViewInfo mOriginatingView;
    private final List<CanvasViewInfo> mAltViews;
    private int mIndex;
    public CanvasAlternateSelection(CanvasViewInfo originatingView, List<CanvasViewInfo> altViews) {
        assert originatingView != null;
        assert altViews != null;
        mOriginatingView = originatingView;
        mAltViews = altViews;
        mIndex = altViews.size() - 1;
    }
    public List<CanvasViewInfo> getAltViews() {
        return mAltViews;
    }
    public CanvasViewInfo getOriginatingView() {
        return mOriginatingView;
    }
    public CanvasViewInfo getCurrent() {
        return mIndex >= 0 ? mAltViews.get(mIndex) : null;
    }
    public CanvasViewInfo getNext() {
        if (mIndex == 0) {
            mIndex = mAltViews.size() - 1;
        } else if (mIndex > 0) {
            mIndex--;
        }
        return getCurrent();
    }
}
