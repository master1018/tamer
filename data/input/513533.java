class PercentageBar extends Drawable {
    Drawable bar;
    double percent;
    int lastWidth = -1;
    @Override
    public void draw(Canvas canvas) {
        if (lastWidth == -1) {
            lastWidth = getBarWidth();
            bar.setBounds(0, 0, lastWidth, bar.getIntrinsicHeight());
        }
        bar.draw(canvas);
    }
    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
    @Override
    public void setAlpha(int alpha) {
    }
    @Override
    public void setColorFilter(ColorFilter cf) {
    }
    private int getBarWidth() {
        int width = (int) ((this.getBounds().width() * percent) / 100);
        int intrinsicWidth = bar.getIntrinsicWidth();
        return Math.max(width, intrinsicWidth);
    }
    @Override
    public int getIntrinsicHeight() {
        return bar.getIntrinsicHeight();
    }
}
