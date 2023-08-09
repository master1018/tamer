public class TextLabelFactory {
  private FontRenderContext frc;
  private char[] text;
  private Bidi bidi;
  private Bidi lineBidi;
  private int flags;
  private int lineStart;
  private int lineLimit;
  public TextLabelFactory(FontRenderContext frc,
                          char[] text,
                          Bidi bidi,
                          int flags) {
    this.frc = frc;
    this.text = text;
    this.bidi = bidi;
    this.flags = flags;
    this.lineBidi = bidi;
    this.lineStart = 0;
    this.lineLimit = text.length;
  }
  public FontRenderContext getFontRenderContext() {
    return frc;
  }
  public char[] getText() {
    return text;
  }
  public Bidi getParagraphBidi() {
    return bidi;
  }
  public Bidi getLineBidi() {
    return lineBidi;
  }
  public int getLayoutFlags() {
    return flags;
  }
  public int getLineStart() {
    return lineStart;
  }
  public int getLineLimit() {
    return lineLimit;
  }
  public void setLineContext(int lineStart, int lineLimit) {
    this.lineStart = lineStart;
    this.lineLimit = lineLimit;
    if (bidi != null) {
      lineBidi = bidi.createLineBidi(lineStart, lineLimit);
    }
  }
  public ExtendedTextLabel createExtended(Font font,
                                          CoreMetrics lm,
                                          Decoration decorator,
                                          int start,
                                          int limit) {
    if (start >= limit || start < lineStart || limit > lineLimit) {
      throw new IllegalArgumentException("bad start: " + start + " or limit: " + limit);
    }
    int level = lineBidi == null ? 0 : lineBidi.getLevelAt(start - lineStart);
    int linedir = (lineBidi == null || lineBidi.baseIsLeftToRight()) ? 0 : 1;
    int layoutFlags = flags & ~0x9; 
    if ((level & 0x1) != 0) layoutFlags |= 1; 
    if ((linedir & 0x1) != 0) layoutFlags |= 8; 
    TextSource source = new StandardTextSource(text, start, limit - start, lineStart, lineLimit - lineStart, level, layoutFlags, font, frc, lm);
    return new ExtendedTextSourceLabel(source, decorator);
  }
  public TextLabel createSimple(Font font,
                                CoreMetrics lm,
                                int start,
                                int limit) {
    if (start >= limit || start < lineStart || limit > lineLimit) {
      throw new IllegalArgumentException("bad start: " + start + " or limit: " + limit);
    }
    int level = lineBidi == null ? 0 : lineBidi.getLevelAt(start - lineStart);
    int linedir = (lineBidi == null || lineBidi.baseIsLeftToRight()) ? 0 : 1;
    int layoutFlags = flags & ~0x9; 
    if ((level & 0x1) != 0) layoutFlags |= 1; 
    if ((linedir & 0x1) != 0) layoutFlags |= 8; 
    TextSource source = new StandardTextSource(text, start, limit - start, lineStart, lineLimit - lineStart, level, layoutFlags, font, frc, lm);
    return new TextSourceLabel(source);
  }
}
