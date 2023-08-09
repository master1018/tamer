public final class TextLayout implements Cloneable {
    public static class CaretPolicy {
        public CaretPolicy() {
        }
        public TextHitInfo getStrongCaret(TextHitInfo hit1, TextHitInfo hit2, TextLayout layout) {
            int level1 = layout.getCharacterLevel(hit1.getCharIndex());
            int level2 = layout.getCharacterLevel(hit2.getCharIndex());
            if (level1 == level2) {
                return (hit2.isLeadingEdge() && (!hit1.isLeadingEdge())) ? hit2 : hit1;
            }
            return level1 > level2 ? hit1 : hit2;
        }
    }
    public static final TextLayout.CaretPolicy DEFAULT_CARET_POLICY = new CaretPolicy();
    private TextRunBreaker breaker;
    private boolean metricsValid = false;
    private TextMetricsCalculator tmc;
    private BasicMetrics metrics;
    private CaretManager caretManager;
    float justificationWidth = -1;
    public TextLayout(String string, Font font, FontRenderContext frc) {
        if (string == null) {
            throw new IllegalArgumentException(Messages.getString("awt.01", "string")); 
        }
        if (font == null) {
            throw new IllegalArgumentException(Messages.getString("awt.01", "font")); 
        }
        if (string.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("awt.02", "string")); 
        }
        AttributedString as = new AttributedString(string);
        as.addAttribute(TextAttribute.FONT, font);
        this.breaker = new TextRunBreaker(as.getIterator(), frc);
        caretManager = new CaretManager(breaker);
    }
    public TextLayout(String string,
            Map<? extends java.text.AttributedCharacterIterator.Attribute, ?> attributes,
            FontRenderContext frc) {
        if (string == null) {
            throw new IllegalArgumentException(Messages.getString("awt.01", "string")); 
        }
        if (attributes == null) {
            throw new IllegalArgumentException(Messages.getString("awt.01", "attributes")); 
        }
        if (string.length() == 0) {
            throw new IllegalArgumentException(Messages.getString("awt.02", "string")); 
        }
        AttributedString as = new AttributedString(string);
        as.addAttributes(attributes, 0, string.length());
        this.breaker = new TextRunBreaker(as.getIterator(), frc);
        caretManager = new CaretManager(breaker);
    }
    public TextLayout(AttributedCharacterIterator text, FontRenderContext frc) {
        if (text == null) {
            throw new IllegalArgumentException(Messages.getString("awt.03", "text")); 
        }
        if (text.getBeginIndex() == text.getEndIndex()) {
            throw new IllegalArgumentException(Messages.getString("awt.04", "text")); 
        }
        this.breaker = new TextRunBreaker(text, frc);
        caretManager = new CaretManager(breaker);
    }
    TextLayout(TextRunBreaker breaker) {
        this.breaker = breaker;
        caretManager = new CaretManager(this.breaker);
    }
    @Override
    public int hashCode() {
        return breaker.hashCode();
    }
    @Override
    protected Object clone() {
        TextLayout res = new TextLayout((TextRunBreaker)breaker.clone());
        if (justificationWidth >= 0) {
            res.handleJustify(justificationWidth);
        }
        return res;
    }
    public boolean equals(TextLayout layout) {
        if (layout == null) {
            return false;
        }
        return this.breaker.equals(layout.breaker);
    }
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TextLayout ? equals((TextLayout)obj) : false;
    }
    @Override
    public String toString() { 
        return super.toString();
    }
    public void draw(Graphics2D g2d, float x, float y) {
        updateMetrics();
        breaker.drawSegments(g2d, x, y);
    }
    private void updateMetrics() {
        if (!metricsValid) {
            breaker.createAllSegments();
            tmc = new TextMetricsCalculator(breaker);
            metrics = tmc.createMetrics();
            metricsValid = true;
        }
    }
    public float getAdvance() {
        updateMetrics();
        return metrics.getAdvance();
    }
    public float getAscent() {
        updateMetrics();
        return metrics.getAscent();
    }
    public byte getBaseline() {
        updateMetrics();
        return (byte)metrics.getBaseLineIndex();
    }
    public float[] getBaselineOffsets() {
        updateMetrics();
        return tmc.getBaselineOffsets();
    }
    public Shape getBlackBoxBounds(int firstEndpoint, int secondEndpoint) {
        updateMetrics();
        if (firstEndpoint < secondEndpoint) {
            return breaker.getBlackBoxBounds(firstEndpoint, secondEndpoint);
        }
        return breaker.getBlackBoxBounds(secondEndpoint, firstEndpoint);
    }
    public Rectangle2D getBounds() {
        updateMetrics();
        return breaker.getVisualBounds();
    }
    public float[] getCaretInfo(TextHitInfo hitInfo) {
        updateMetrics();
        return caretManager.getCaretInfo(hitInfo);
    }
    public float[] getCaretInfo(TextHitInfo hitInfo, Rectangle2D bounds) {
        updateMetrics();
        return caretManager.getCaretInfo(hitInfo);
    }
    public Shape getCaretShape(TextHitInfo hitInfo, Rectangle2D bounds) {
        updateMetrics();
        return caretManager.getCaretShape(hitInfo, this);
    }
    public Shape getCaretShape(TextHitInfo hitInfo) {
        updateMetrics();
        return caretManager.getCaretShape(hitInfo, this);
    }
    public Shape[] getCaretShapes(int offset) {
        return getCaretShapes(offset, null, TextLayout.DEFAULT_CARET_POLICY);
    }
    public Shape[] getCaretShapes(int offset, Rectangle2D bounds) {
        return getCaretShapes(offset, bounds, TextLayout.DEFAULT_CARET_POLICY);
    }
    public Shape[] getCaretShapes(int offset, Rectangle2D bounds, TextLayout.CaretPolicy policy) {
        if (offset < 0 || offset > breaker.getCharCount()) {
            throw new IllegalArgumentException(Messages.getString("awt.195")); 
        }
        updateMetrics();
        return caretManager.getCaretShapes(offset, bounds, policy, this);
    }
    public int getCharacterCount() {
        return breaker.getCharCount();
    }
    public byte getCharacterLevel(int index) {
        if (index == -1 || index == getCharacterCount()) {
            return (byte)breaker.getBaseLevel();
        }
        return breaker.getLevel(index);
    }
    public float getDescent() {
        updateMetrics();
        return metrics.getDescent();
    }
    public TextLayout getJustifiedLayout(float justificationWidth) throws Error {
        float justification = breaker.getJustification();
        if (justification < 0) {
            throw new Error(Messages.getString("awt.196")); 
        } else if (justification == 0) {
            return this;
        }
        TextLayout justifiedLayout = new TextLayout((TextRunBreaker)breaker.clone());
        justifiedLayout.handleJustify(justificationWidth);
        return justifiedLayout;
    }
    public float getLeading() {
        updateMetrics();
        return metrics.getLeading();
    }
    public Shape getLogicalHighlightShape(int firstEndpoint, int secondEndpoint) {
        updateMetrics();
        return getLogicalHighlightShape(firstEndpoint, secondEndpoint, breaker.getLogicalBounds());
    }
    public Shape getLogicalHighlightShape(int firstEndpoint, int secondEndpoint, Rectangle2D bounds) {
        updateMetrics();
        if (firstEndpoint > secondEndpoint) {
            if (secondEndpoint < 0 || firstEndpoint > breaker.getCharCount()) {
                throw new IllegalArgumentException(Messages.getString("awt.197")); 
            }
            return caretManager.getLogicalHighlightShape(secondEndpoint, firstEndpoint, bounds,
                    this);
        }
        if (firstEndpoint < 0 || secondEndpoint > breaker.getCharCount()) {
            throw new IllegalArgumentException(Messages.getString("awt.197")); 
        }
        return caretManager.getLogicalHighlightShape(firstEndpoint, secondEndpoint, bounds, this);
    }
    public int[] getLogicalRangesForVisualSelection(TextHitInfo hit1, TextHitInfo hit2) {
        return caretManager.getLogicalRangesForVisualSelection(hit1, hit2);
    }
    public TextHitInfo getNextLeftHit(int offset) {
        return getNextLeftHit(offset, DEFAULT_CARET_POLICY);
    }
    public TextHitInfo getNextLeftHit(TextHitInfo hitInfo) {
        breaker.createAllSegments();
        return caretManager.getNextLeftHit(hitInfo);
    }
    public TextHitInfo getNextLeftHit(int offset, TextLayout.CaretPolicy policy) {
        if (offset < 0 || offset > breaker.getCharCount()) {
            throw new IllegalArgumentException(Messages.getString("awt.195")); 
        }
        TextHitInfo hit = TextHitInfo.afterOffset(offset);
        TextHitInfo strongHit = policy.getStrongCaret(hit, hit.getOtherHit(), this);
        TextHitInfo nextLeftHit = getNextLeftHit(strongHit);
        if (nextLeftHit != null) {
            return policy.getStrongCaret(getVisualOtherHit(nextLeftHit), nextLeftHit, this);
        }
        return null;
    }
    public TextHitInfo getNextRightHit(TextHitInfo hitInfo) {
        breaker.createAllSegments();
        return caretManager.getNextRightHit(hitInfo);
    }
    public TextHitInfo getNextRightHit(int offset) {
        return getNextRightHit(offset, DEFAULT_CARET_POLICY);
    }
    public TextHitInfo getNextRightHit(int offset, TextLayout.CaretPolicy policy) {
        if (offset < 0 || offset > breaker.getCharCount()) {
            throw new IllegalArgumentException(Messages.getString("awt.195")); 
        }
        TextHitInfo hit = TextHitInfo.afterOffset(offset);
        TextHitInfo strongHit = policy.getStrongCaret(hit, hit.getOtherHit(), this);
        TextHitInfo nextRightHit = getNextRightHit(strongHit);
        if (nextRightHit != null) {
            return policy.getStrongCaret(getVisualOtherHit(nextRightHit), nextRightHit, this);
        }
        return null;
    }
    public Shape getOutline(AffineTransform xform) {
        breaker.createAllSegments();
        GeneralPath outline = breaker.getOutline();
        if (outline != null && xform != null) {
            outline.transform(xform);
        }
        return outline;
    }
    public float getVisibleAdvance() {
        updateMetrics();
        int lastNonWhitespace = breaker.getLastNonWhitespace();
        if (lastNonWhitespace < 0) {
            return 0;
        } else if (lastNonWhitespace == getCharacterCount() - 1) {
            return getAdvance();
        } else if (justificationWidth >= 0) { 
            return justificationWidth;
        } else {
            breaker.pushSegments(breaker.getACI().getBeginIndex(), lastNonWhitespace
                    + breaker.getACI().getBeginIndex() + 1);
            breaker.createAllSegments();
            float visAdvance = tmc.createMetrics().getAdvance();
            breaker.popSegments();
            return visAdvance;
        }
    }
    public Shape getVisualHighlightShape(TextHitInfo hit1, TextHitInfo hit2, Rectangle2D bounds) {
        return caretManager.getVisualHighlightShape(hit1, hit2, bounds, this);
    }
    public Shape getVisualHighlightShape(TextHitInfo hit1, TextHitInfo hit2) {
        breaker.createAllSegments();
        return caretManager.getVisualHighlightShape(hit1, hit2, breaker.getLogicalBounds(), this);
    }
    public TextHitInfo getVisualOtherHit(TextHitInfo hitInfo) {
        return caretManager.getVisualOtherHit(hitInfo);
    }
    protected void handleJustify(float justificationWidth) {
        float justification = breaker.getJustification();
        if (justification < 0) {
            throw new IllegalStateException(Messages.getString("awt.196")); 
        } else if (justification == 0) {
            return;
        }
        float gap = (justificationWidth - getVisibleAdvance()) * justification;
        breaker.justify(gap);
        this.justificationWidth = justificationWidth;
        tmc = new TextMetricsCalculator(breaker);
        tmc.correctAdvance(metrics);
    }
    public TextHitInfo hitTestChar(float x, float y) {
        return hitTestChar(x, y, getBounds());
    }
    public TextHitInfo hitTestChar(float x, float y, Rectangle2D bounds) {
        if (x > bounds.getMaxX()) {
            return breaker.isLTR() ? TextHitInfo.trailing(breaker.getCharCount() - 1) : TextHitInfo
                    .leading(0);
        }
        if (x < bounds.getMinX()) {
            return breaker.isLTR() ? TextHitInfo.leading(0) : TextHitInfo.trailing(breaker
                    .getCharCount() - 1);
        }
        return breaker.hitTest(x, y);
    }
    public boolean isLeftToRight() {
        return breaker.isLTR();
    }
    public boolean isVertical() {
        return false;
    }
}
