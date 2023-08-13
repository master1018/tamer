public abstract class TextLabel {
  public abstract Rectangle2D getVisualBounds(float x, float y);
  public abstract Rectangle2D getLogicalBounds(float x, float y);
  public abstract Rectangle2D getAlignBounds(float x, float y);
  public abstract Rectangle2D getItalicBounds(float x, float y);
  public abstract Shape getOutline(float x, float y);
  public abstract void draw(Graphics2D g, float x, float y);
  public Rectangle2D getVisualBounds() {
    return getVisualBounds(0f, 0f);
  }
  public Rectangle2D getLogicalBounds() {
    return getLogicalBounds(0f, 0f);
  }
  public Rectangle2D getAlignBounds() {
    return getAlignBounds(0f, 0f);
  }
  public Rectangle2D getItalicBounds() {
    return getItalicBounds(0f, 0f);
  }
  public Shape getOutline() {
    return getOutline(0f, 0f);
  }
  public void draw(Graphics2D g) {
    draw(g, 0f, 0f);
  }
}
