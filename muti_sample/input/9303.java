public class Annotation {
  private Interval interval;
  private java.util.List strings;
  private java.util.List heights;
  private Color baseColor;
  private int width;
  private int height;
  private int x;
  private int y;
  public Annotation(Address lowAddress,
                    Address highAddress,
                    String s) {
    strings = new ArrayList();
    heights = new ArrayList();
    for (StringTokenizer tok = new StringTokenizer(s, "\n"); tok.hasMoreTokens(); ) {
      strings.add(tok.nextToken());
    }
    if (AddressOps.lessThan(highAddress, lowAddress)) {
      Address temp = lowAddress;
      lowAddress = highAddress;
      highAddress = temp;
    }
    interval = new Interval(lowAddress, highAddress);
  }
  public Interval getInterval() {
    return interval;
  }
  public Address getLowAddress() {
    return (Address) getInterval().getLowEndpoint();
  }
  public Address getHighAddress() {
    return (Address) getInterval().getHighEndpoint();
  }
  public void draw(Graphics g) {
    g.setColor(baseColor);
    int tmpY = y;
    for (int i = 0; i < strings.size(); i++) {
      String s = (String) strings.get(i);
      Integer h = (Integer) heights.get(i);
      g.drawString(s, x, tmpY);
      tmpY += h.intValue();
    }
  }
  public void setColor(Color c) {
    this.baseColor = c;
  }
  public Color getColor() {
    return baseColor;
  }
  public void computeWidthAndHeight(Graphics g) {
    width = 0;
    height = 0;
    heights.clear();
    for (Iterator iter = strings.iterator(); iter.hasNext(); ) {
      String s = (String) iter.next();
      Rectangle2D bounds = GraphicsUtilities.getStringBounds(s, g);
      width  =  Math.max(width, (int) bounds.getWidth());
      height += (int) bounds.getHeight();
      heights.add(new Integer((int) bounds.getHeight()));
    }
  }
  public int getWidth() {
    return width;
  }
  public int getHeight() {
    return height;
  }
  public void setXAndY(int x, int y) {
    this.x = x; this.y = y;
  }
  public void setX(int x) {
    this.x = x;
  }
  public int getX() {
    return x;
  }
  public void setY(int y) {
    this.y = y;
  }
  public int getY() {
    return y;
  }
  public Rectangle getBounds() {
    return new Rectangle(x, y, width, height);
  }
  public String toString() {
    String result = "Annotation: lowAddr: " + getLowAddress() + " highAddr: " + getHighAddress() + " strings: "  + strings.size();
    for (int i = 0; i < strings.size(); i++) {
      result += "\n" + (String) strings.get(i);
    }
    return result;
  }
}
