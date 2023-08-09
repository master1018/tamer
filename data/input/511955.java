public abstract class TextRunSegment implements Cloneable {
    float x; 
    float y; 
    BasicMetrics metrics; 
    TextDecorator.Decoration decoration; 
    Rectangle2D logicalBounds = null; 
    Rectangle2D visualBounds = null; 
    abstract int getStart();
    abstract int getEnd();
    abstract int getLength();
    abstract void draw(Graphics2D g2d, float xOffset, float yOffset);
    abstract Shape getCharsBlackBoxBounds(int start, int limit);
    abstract Shape getOutline();
    abstract Rectangle2D getVisualBounds();
    abstract Rectangle2D getLogicalBounds();
    abstract float getAdvance();
    abstract float getAdvanceDelta(int start, int end);
    abstract int getCharIndexFromAdvance(float advance, int start);
    abstract boolean charHasZeroAdvance(int index);
    abstract float getCharPosition(int index);
    abstract float getCharAdvance(int index);
    abstract TextHitInfo hitTest(float x, float y);
    abstract void updateJustificationInfo(TextRunBreaker.JustificationInfo jInfo);
    abstract float doJustification(TextRunBreaker.JustificationInfo jInfos[]);
    @Override
    public abstract Object clone();
}
