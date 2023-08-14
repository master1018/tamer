public abstract class LineMetrics {
    public abstract float[] getBaselineOffsets();
    public abstract int getNumChars();
    public abstract int getBaselineIndex();
    public abstract float getUnderlineThickness();
    public abstract float getUnderlineOffset();
    public abstract float getStrikethroughThickness();
    public abstract float getStrikethroughOffset();
    public abstract float getLeading();
    public abstract float getHeight();
    public abstract float getDescent();
    public abstract float getAscent();
}
