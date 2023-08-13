class GalleryPickerItem extends ImageView {
    private Drawable mFrame;
    private Rect mFrameBounds = new Rect();
    private Drawable mOverlay;
    public GalleryPickerItem(Context context) {
        this(context, null);
    }
    public GalleryPickerItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public GalleryPickerItem(Context context,
                             AttributeSet attrs,
                             int defStyle) {
        super(context, attrs, defStyle);
        mFrame = getResources().getDrawable(R.drawable.frame_gallery_preview);
        mFrame.setCallback(this);
    }
    @Override
    protected boolean verifyDrawable(Drawable who) {
        return super.verifyDrawable(who) || (who == mFrame)
                || (who == mOverlay);
    }
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mFrame != null) {
            int[] drawableState = getDrawableState();
            mFrame.setState(drawableState);
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        final Rect frameBounds = mFrameBounds;
        if (frameBounds.isEmpty()) {
            final int w = getWidth();
            final int h = getHeight();
            frameBounds.set(0, 0, w, h);
            mFrame.setBounds(frameBounds);
            if (mOverlay != null) {
                mOverlay.setBounds(w - mOverlay.getIntrinsicWidth(),
                        h - mOverlay.getIntrinsicHeight(), w, h);
            }
        }
        mFrame.draw(canvas);
        if (mOverlay != null) {
            mOverlay.draw(canvas);
        }
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mFrameBounds.setEmpty();
    }
    public void setOverlay(int overlayId) {
        if (overlayId >= 0) {
            mOverlay = getResources().getDrawable(overlayId);
            mFrameBounds.setEmpty();
        } else {
            mOverlay = null;
        }
    }
}
