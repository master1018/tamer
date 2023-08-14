public class SoftBevelBorder extends BevelBorder
{
    public SoftBevelBorder(int bevelType) {
        super(bevelType);
    }
    public SoftBevelBorder(int bevelType, Color highlight, Color shadow) {
        super(bevelType, highlight, shadow);
    }
    @ConstructorProperties({"bevelType", "highlightOuterColor", "highlightInnerColor", "shadowOuterColor", "shadowInnerColor"})
    public SoftBevelBorder(int bevelType, Color highlightOuterColor,
                        Color highlightInnerColor, Color shadowOuterColor,
                        Color shadowInnerColor) {
        super(bevelType, highlightOuterColor, highlightInnerColor,
              shadowOuterColor, shadowInnerColor);
    }
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        g.translate(x, y);
        if (bevelType == RAISED) {
            g.setColor(getHighlightOuterColor(c));
            g.drawLine(0, 0, width-2, 0);
            g.drawLine(0, 0, 0, height-2);
            g.drawLine(1, 1, 1, 1);
            g.setColor(getHighlightInnerColor(c));
            g.drawLine(2, 1, width-2, 1);
            g.drawLine(1, 2, 1, height-2);
            g.drawLine(2, 2, 2, 2);
            g.drawLine(0, height-1, 0, height-2);
            g.drawLine(width-1, 0, width-1, 0);
            g.setColor(getShadowOuterColor(c));
            g.drawLine(2, height-1, width-1, height-1);
            g.drawLine(width-1, 2, width-1, height-1);
            g.setColor(getShadowInnerColor(c));
            g.drawLine(width-2, height-2, width-2, height-2);
        } else if (bevelType == LOWERED) {
            g.setColor(getShadowOuterColor(c));
            g.drawLine(0, 0, width-2, 0);
            g.drawLine(0, 0, 0, height-2);
            g.drawLine(1, 1, 1, 1);
            g.setColor(getShadowInnerColor(c));
            g.drawLine(2, 1, width-2, 1);
            g.drawLine(1, 2, 1, height-2);
            g.drawLine(2, 2, 2, 2);
            g.drawLine(0, height-1, 0, height-2);
            g.drawLine(width-1, 0, width-1, 0);
            g.setColor(getHighlightOuterColor(c));
            g.drawLine(2, height-1, width-1, height-1);
            g.drawLine(width-1, 2, width-1, height-1);
            g.setColor(getHighlightInnerColor(c));
            g.drawLine(width-2, height-2, width-2, height-2);
        }
        g.translate(-x, -y);
        g.setColor(oldColor);
    }
    public Insets getBorderInsets(Component c, Insets insets)       {
        insets.set(3, 3, 3, 3);
        return insets;
    }
    public boolean isBorderOpaque() { return false; }
}
