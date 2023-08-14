public abstract class ExtendedTextLabel extends TextLabel
                            implements TextLineComponent{
  public abstract int getNumCharacters();
  public abstract CoreMetrics getCoreMetrics();
  public abstract float getCharX(int logicalIndex);
  public abstract float getCharY(int logicalIndex);
  public abstract float getCharAdvance(int logicalIndex);
  public abstract Rectangle2D getCharVisualBounds(int logicalIndex, float x, float y);
  public abstract int logicalToVisual(int logicalIndex);
  public abstract int visualToLogical(int visualIndex);
  public abstract int getLineBreakIndex(int logicalStart, float width);
  public abstract float getAdvanceBetween(int logicalStart, int logicalLimit);
  public abstract boolean caretAtOffsetIsValid(int offset);
  public Rectangle2D getCharVisualBounds(int logicalIndex) {
    return getCharVisualBounds(logicalIndex, 0, 0);
  }
  public abstract TextLineComponent getSubset(int start, int limit, int dir);
  public abstract int getNumJustificationInfos();
  public abstract void getJustificationInfos(GlyphJustificationInfo[] infos, int infoStart, int charStart, int charLimit);
  public abstract TextLineComponent applyJustificationDeltas(float[] deltas, int deltaStart, boolean[] flags);
}
