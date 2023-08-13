public class ScrollBarDrawable extends Drawable {
    private Drawable mVerticalTrack;
    private Drawable mHorizontalTrack;
    private Drawable mVerticalThumb;
    private Drawable mHorizontalThumb;
    private int mRange;
    private int mOffset;
    private int mExtent;
    private boolean mVertical;
    private boolean mChanged;
    private boolean mRangeChanged;
    private final Rect mTempBounds = new Rect();
    private boolean mAlwaysDrawHorizontalTrack;
    private boolean mAlwaysDrawVerticalTrack;
    public ScrollBarDrawable() {
    }
    public void setAlwaysDrawHorizontalTrack(boolean alwaysDrawTrack) {
        mAlwaysDrawHorizontalTrack = alwaysDrawTrack;
    }
    public void setAlwaysDrawVerticalTrack(boolean alwaysDrawTrack) {
        mAlwaysDrawVerticalTrack = alwaysDrawTrack;
    }
    public boolean getAlwaysDrawVerticalTrack() {
        return mAlwaysDrawVerticalTrack;
    }
    public boolean getAlwaysDrawHorizontalTrack() {
        return mAlwaysDrawHorizontalTrack;
    }
    public void setParameters(int range, int offset, int extent, boolean vertical) {
        if (mVertical != vertical) {
            mChanged = true;
        }
        if (mRange != range || mOffset != offset || mExtent != extent) {
            mRangeChanged = true;
        }
        mRange = range;
        mOffset = offset;
        mExtent = extent;
        mVertical = vertical;
    }
    @Override
    public void draw(Canvas canvas) {
        final boolean vertical = mVertical;
        final int extent = mExtent;
        final int range = mRange;
        boolean drawTrack = true;
        boolean drawThumb = true;
        if (extent <= 0 || range <= extent) {
            drawTrack = vertical ? mAlwaysDrawVerticalTrack : mAlwaysDrawHorizontalTrack;
            drawThumb = false;
        }
        Rect r = getBounds();
        if (canvas.quickReject(r.left, r.top, r.right, r.bottom, 
                Canvas.EdgeType.AA)) {
            return;
        }
        if (drawTrack) {
            drawTrack(canvas, r, vertical);
        }
        if (drawThumb) {
            int size = vertical ? r.height() : r.width();
            int thickness = vertical ? r.width() : r.height();
            int length = Math.round((float) size * extent / range);
            int offset = Math.round((float) (size - length) * mOffset / (range - extent));
            int minLength = thickness * 2;
            if (length < minLength) {
                length = minLength;
            }
            if (offset + length > size) {
                offset = size - length;
            }
            drawThumb(canvas, r, offset, length, vertical);
        }
    }
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mChanged = true;
    }
    protected void drawTrack(Canvas canvas, Rect bounds, boolean vertical) {
        Drawable track;
        if (vertical) {
            track = mVerticalTrack;
        } else {
            track = mHorizontalTrack;
        }
        if (track != null) {
            if (mChanged) {
                track.setBounds(bounds);
            }
            track.draw(canvas);
        }
    }
    protected void drawThumb(Canvas canvas, Rect bounds, int offset, int length, boolean vertical) {
        final Rect thumbRect = mTempBounds;
        final boolean changed = mRangeChanged || mChanged;
        if (changed) {
            if (vertical) {
                thumbRect.set(bounds.left,  bounds.top + offset,
                        bounds.right, bounds.top + offset + length);
            } else {
                thumbRect.set(bounds.left + offset, bounds.top,
                        bounds.left + offset + length, bounds.bottom);
            }
        }
        if (vertical) {
            final Drawable thumb = mVerticalThumb;
            if (changed) thumb.setBounds(thumbRect);
            thumb.draw(canvas);
        } else {
            final Drawable thumb = mHorizontalThumb;
            if (changed) thumb.setBounds(thumbRect);
            thumb.draw(canvas);
        }
    }
    public void setVerticalThumbDrawable(Drawable thumb) {
        if (thumb != null) {
            mVerticalThumb = thumb;
        }
    }
    public void setVerticalTrackDrawable(Drawable track) {
        mVerticalTrack = track;
    }
    public void setHorizontalThumbDrawable(Drawable thumb) {
        if (thumb != null) {
            mHorizontalThumb = thumb;
        }
    }
    public void setHorizontalTrackDrawable(Drawable track) {
        mHorizontalTrack = track;
    }
    public int getSize(boolean vertical) {
        if (vertical) {
            return (mVerticalTrack != null ?
                    mVerticalTrack : mVerticalThumb).getIntrinsicWidth();
        } else {
            return (mHorizontalTrack != null ?
                    mHorizontalTrack : mHorizontalThumb).getIntrinsicHeight();
        }
    }
    @Override
    public void setAlpha(int alpha) {
        if (mVerticalTrack != null) {
            mVerticalTrack.setAlpha(alpha);
        }
        mVerticalThumb.setAlpha(alpha);
        if (mHorizontalTrack != null) {
            mHorizontalTrack.setAlpha(alpha);
        }
        mHorizontalThumb.setAlpha(alpha);
    }
    @Override
    public void setColorFilter(ColorFilter cf) {
        if (mVerticalTrack != null) {
            mVerticalTrack.setColorFilter(cf);
        }
        mVerticalThumb.setColorFilter(cf);
        if (mHorizontalTrack != null) {
            mHorizontalTrack.setColorFilter(cf);
        }
        mHorizontalThumb.setColorFilter(cf);
    }
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
    @Override
    public String toString() {
        return "ScrollBarDrawable: range=" + mRange + " offset=" + mOffset +
               " extent=" + mExtent + (mVertical ? " V" : " H");
    }
}
