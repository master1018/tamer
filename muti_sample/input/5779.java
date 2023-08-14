public class PeekMetrics {
    private boolean mHasNonSolidColors;
    private boolean mHasCompositing;
    private boolean mHasText;
    private boolean mHasImages;
    public boolean hasNonSolidColors() {
        return mHasNonSolidColors;
    }
    public boolean hasCompositing() {
        return mHasCompositing;
    }
    public boolean hasText() {
        return mHasText;
    }
    public boolean hasImages() {
        return mHasImages;
    }
    public void fill(Graphics2D g) {
        checkDrawingMode(g);
    }
    public void draw(Graphics2D g) {
        checkDrawingMode(g);
    }
    public void clear(Graphics2D g) {
        checkPaint(g.getBackground());
    }
    public void drawText(Graphics2D g) {
        mHasText = true;
        checkDrawingMode(g);
    }
    public void drawText(Graphics2D g, TextLayout textLayout) {
        mHasText = true;
        checkDrawingMode(g);
    }
    public void drawImage(Graphics2D g, Image image) {
        mHasImages = true;
    }
    public void drawImage(Graphics2D g, RenderedImage image) {
        mHasImages = true;
    }
    public void drawImage(Graphics2D g, RenderableImage image) {
        mHasImages = true;
    }
    private void checkDrawingMode(Graphics2D g) {
        checkPaint(g.getPaint());
        checkAlpha(g.getComposite());
    }
    private void checkPaint(Paint paint) {
        if (paint instanceof Color) {
            if (((Color)paint).getAlpha() < 255) {
                mHasNonSolidColors = true;
            }
        } else {
            mHasNonSolidColors = true;
        }
    }
    private void checkAlpha(Composite composite) {
        if (composite instanceof AlphaComposite) {
            AlphaComposite alphaComposite = (AlphaComposite) composite;
            float alpha = alphaComposite.getAlpha();
            int rule = alphaComposite.getRule();
            if (alpha != 1.0
                    || (rule != AlphaComposite.SRC
                        && rule != AlphaComposite.SRC_OVER)) {
                mHasCompositing = true;
            }
        } else {
            mHasCompositing = true;
        }
    }
}
