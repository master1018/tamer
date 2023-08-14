public class StrokeBorder extends AbstractBorder {
    private final BasicStroke stroke;
    private final Paint paint;
    public StrokeBorder(BasicStroke stroke) {
        this(stroke, null);
    }
    @ConstructorProperties({ "stroke", "paint" })
    public StrokeBorder(BasicStroke stroke, Paint paint) {
        if (stroke == null) {
            throw new NullPointerException("border's stroke");
        }
        this.stroke = stroke;
        this.paint = paint;
    }
    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        float size = this.stroke.getLineWidth();
        if (size > 0.0f) {
            g = g.create();
            if (g instanceof Graphics2D) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setStroke(this.stroke);
                g2d.setPaint(this.paint != null ? this.paint : c == null ? null : c.getForeground());
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                     RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.draw(new Rectangle2D.Float(x + size / 2, y + size / 2, width - size, height - size));
            }
            g.dispose();
        }
    }
    @Override
    public Insets getBorderInsets(Component c, Insets insets) {
        int size = (int) Math.ceil(this.stroke.getLineWidth());
        insets.set(size, size, size, size);
        return insets;
    }
    public BasicStroke getStroke() {
        return this.stroke;
    }
    public Paint getPaint() {
        return this.paint;
    }
}
