public class GraphicsUtilities {
  public static Font lookupFont(String fontName) {
    Font[] allFonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
    Font font = null;
    for (int i = 0; i < allFonts.length; i++) {
      if (allFonts[i].getFontName().indexOf(fontName) != -1) {
        font = allFonts[i];
        break;
      }
    }
    if (font == null) {
      return null;
    }
    return font.deriveFont(Font.PLAIN, 12);
  }
  public static Rectangle2D getStringBounds(String s, Graphics g) {
    FontMetrics fm = g.getFontMetrics();
    return fm.getStringBounds(s, 0, s.length(), g);
  }
  public static int getStringWidth(String s, FontMetrics fm) {
    return fm.stringWidth(s);
  }
  public static void reshapeToAspectRatio(Component component,
                                          float aspectRatio,
                                          float fillRatio,
                                          Dimension containerDimension) {
    int x = containerDimension.width;
    int y = containerDimension.height;
    int desiredX;
    int desiredY;
    if (((float) x / (float) y) > aspectRatio) {
      desiredY = (int) (fillRatio * y);
      desiredX = (int) (desiredY * aspectRatio);
    } else {
      desiredX = (int) (fillRatio * x);
      desiredY = (int) (desiredX / aspectRatio);
    }
    component.setSize(desiredX, desiredY);
  }
  public static void constrainToSize(Component component, Dimension containerDimension) {
    Dimension d = component.getSize();
    int x = d.width;
    int y = d.height;
    boolean changed = false;
    if (x > containerDimension.width) {
      x = containerDimension.width;
      changed = true;
    }
    if (y > containerDimension.height) {
      y = containerDimension.height;
      changed = true;
    }
    if (changed) {
      component.setSize(x, y);
    }
  }
  public static void centerInContainer(Component c) {
    centerInContainer(c, c.getParent().getSize());
  }
  public static void centerInContainer(Component component,
                                       Dimension containerDimension) {
    Dimension sz = component.getSize();
    int x = ((containerDimension.width - sz.width) / 2);
    int y = ((containerDimension.height - sz.height) / 2);
    component.setLocation(x, y);
  }
  public static void moveToInContainer(Component component,
                                       float relativeX,
                                       float relativeY,
                                       int minX,
                                       int minY) {
    Dimension d = component.getParent().getSize();
    Dimension sz = component.getSize();
    int xPos = Math.min(d.width - sz.width,
                        (int) ((d.width * relativeX) - (sz.width / 2)));
    int yPos = Math.min(d.height - sz.height,
                        (int) ((d.height * relativeY) - (sz.height / 2)));
    xPos = Math.max(xPos, minX);
    yPos = Math.max(yPos, minY);
    component.setLocation(xPos, yPos);
  }
  static Random random = new Random();
  public static void randomLocation(Component c) {
    randomLocation(c, c.getParent().getSize());
  }
  public static void randomLocation(Component component,
                                    Dimension containerDimension) {
    Dimension sz = component.getSize();
    int x = (int)((containerDimension.width - sz.width) * random.nextFloat());
    int y = (int)((containerDimension.height - sz.height) * random.nextFloat());
    component.setLocation(x, y);
  }
  public static Border newBorder(int size) {
    return BorderFactory.createEmptyBorder(size, size, size, size);
  }
}
