public class PictureDrawable extends Drawable {
    private Picture mPicture;
    public PictureDrawable(Picture picture) {
        mPicture = picture;
    }
    public Picture getPicture() {
        return mPicture;
    }
    public void setPicture(Picture picture) {
        mPicture = picture;
    }
    @Override
    public void draw(Canvas canvas) {
        if (mPicture != null) {
            Rect bounds = getBounds();
            canvas.save();
            canvas.clipRect(bounds);
            canvas.translate(bounds.left, bounds.top);
            canvas.drawPicture(mPicture);
            canvas.restore();
        }
    }
    @Override
    public int getIntrinsicWidth() {
        return mPicture != null ? mPicture.getWidth() : -1;
    }
    @Override
    public int getIntrinsicHeight() {
        return mPicture != null ? mPicture.getHeight() : -1;
    }
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
    @Override
    public void setFilterBitmap(boolean filter) {}
    @Override
    public void setDither(boolean dither) {}
    @Override
    public void setColorFilter(ColorFilter colorFilter) {}
    @Override
    public void setAlpha(int alpha) {}
}
