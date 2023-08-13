public class TextSourceLabel extends TextLabel {
  TextSource source;
  Rectangle2D lb;
  Rectangle2D ab;
  Rectangle2D vb;
  Rectangle2D ib;
  GlyphVector gv;
  public TextSourceLabel(TextSource source) {
    this(source, null, null, null);
  }
  public TextSourceLabel(TextSource source, Rectangle2D lb, Rectangle2D ab, GlyphVector gv) {
    this.source = source;
    this.lb = lb;
    this.ab = ab;
    this.gv = gv;
  }
  public TextSource getSource() {
    return source;
  }
  public final Rectangle2D getLogicalBounds(float x, float y) {
    if (lb == null) {
      lb = createLogicalBounds();
    }
    return new Rectangle2D.Float((float)(lb.getX() + x),
                                 (float)(lb.getY() + y),
                                 (float)lb.getWidth(),
                                 (float)lb.getHeight());
  }
  public final Rectangle2D getVisualBounds(float x, float y) {
    if (vb == null) {
      vb = createVisualBounds();
    }
    return new Rectangle2D.Float((float)(vb.getX() + x),
                                 (float)(vb.getY() + y),
                                 (float)vb.getWidth(),
                                 (float)vb.getHeight());
  }
  public final Rectangle2D getAlignBounds(float x, float y) {
    if (ab == null) {
      ab = createAlignBounds();
    }
    return new Rectangle2D.Float((float)(ab.getX() + x),
                                 (float)(ab.getY() + y),
                                 (float)ab.getWidth(),
                                 (float)ab.getHeight());
  }
  public Rectangle2D getItalicBounds(float x, float y) {
    if (ib == null) {
      ib = createItalicBounds();
    }
    return new Rectangle2D.Float((float)(ib.getX() + x),
                                 (float)(ib.getY() + y),
                                 (float)ib.getWidth(),
                                 (float)ib.getHeight());
  }
  public Rectangle getPixelBounds(FontRenderContext frc, float x, float y) {
      return getGV().getPixelBounds(frc, x, y); 
  }
  public AffineTransform getBaselineTransform() {
      Font font = source.getFont();
      if (font.hasLayoutAttributes()) {
          return AttributeValues.getBaselineTransform(font.getAttributes());
      }
      return null;
  }
  public Shape getOutline(float x, float y) {
    return getGV().getOutline(x, y);
  }
  public void draw(Graphics2D g, float x, float y) {
    g.drawGlyphVector(getGV(), x, y);
  }
  protected Rectangle2D createLogicalBounds() {
    return getGV().getLogicalBounds();
  }
  protected Rectangle2D createVisualBounds() {
    return getGV().getVisualBounds();
  }
  protected Rectangle2D createItalicBounds() {
    return getGV().getLogicalBounds();
  }
  protected Rectangle2D createAlignBounds() {
    return createLogicalBounds();
  }
  private final GlyphVector getGV() {
    if (gv == null) {
      gv = createGV();
    }
    return gv;
  }
  protected GlyphVector createGV() {
    Font font = source.getFont();
    FontRenderContext frc = source.getFRC();
    int flags = source.getLayoutFlags();
    char[] context = source.getChars();
    int start = source.getStart();
    int length = source.getLength();
    GlyphLayout gl = GlyphLayout.get(null); 
    StandardGlyphVector gv = gl.layout(font, frc, context, start, length,
                                       flags, null); 
    GlyphLayout.done(gl);
    return gv;
  }
}
