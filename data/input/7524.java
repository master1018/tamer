class CSSBorder extends AbstractBorder {
    final static int COLOR = 0, STYLE = 1, WIDTH = 2;
    final static int TOP = 0, RIGHT = 1, BOTTOM = 2, LEFT = 3;
    final static Attribute[][] ATTRIBUTES = {
        { Attribute.BORDER_TOP_COLOR, Attribute.BORDER_RIGHT_COLOR,
          Attribute.BORDER_BOTTOM_COLOR, Attribute.BORDER_LEFT_COLOR, },
        { Attribute.BORDER_TOP_STYLE, Attribute.BORDER_RIGHT_STYLE,
          Attribute.BORDER_BOTTOM_STYLE, Attribute.BORDER_LEFT_STYLE, },
        { Attribute.BORDER_TOP_WIDTH, Attribute.BORDER_RIGHT_WIDTH,
          Attribute.BORDER_BOTTOM_WIDTH, Attribute.BORDER_LEFT_WIDTH, },
    };
    final static CssValue PARSERS[] = {
        new ColorValue(), new BorderStyle(), new BorderWidthValue(null, 0),
    };
    final static Object[] DEFAULTS = {
        Attribute.BORDER_COLOR, 
        PARSERS[1].parseCssValue(Attribute.BORDER_STYLE.getDefaultValue()),
        PARSERS[2].parseCssValue(Attribute.BORDER_WIDTH.getDefaultValue()),
    };
    final AttributeSet attrs;
    CSSBorder(AttributeSet attrs) {
        this.attrs = attrs;
    }
    private Color getBorderColor(int side) {
        Object o = attrs.getAttribute(ATTRIBUTES[COLOR][side]);
        ColorValue cv;
        if (o instanceof ColorValue) {
            cv = (ColorValue) o;
        } else {
            cv = (ColorValue) attrs.getAttribute(Attribute.COLOR);
            if (cv == null) {
                cv = (ColorValue) PARSERS[COLOR].parseCssValue(
                                            Attribute.COLOR.getDefaultValue());
            }
        }
        return cv.getValue();
    }
    private int getBorderWidth(int side) {
        int width = 0;
        BorderStyle bs = (BorderStyle) attrs.getAttribute(
                                                    ATTRIBUTES[STYLE][side]);
        if ((bs != null) && (bs.getValue() != Value.NONE)) {
            LengthValue bw = (LengthValue) attrs.getAttribute(
                                                    ATTRIBUTES[WIDTH][side]);
            if (bw == null) {
                bw = (LengthValue) DEFAULTS[WIDTH];
            }
            width = (int) bw.getValue(true);
        }
        return width;
    }
    private int[] getWidths() {
        int[] widths = new int[4];
        for (int i = 0; i < widths.length; i++) {
            widths[i] = getBorderWidth(i);
        }
        return widths;
    }
    private Value getBorderStyle(int side) {
        BorderStyle style =
                    (BorderStyle) attrs.getAttribute(ATTRIBUTES[STYLE][side]);
        if (style == null) {
            style = (BorderStyle) DEFAULTS[STYLE];
        }
        return style.getValue();
    }
    private Polygon getBorderShape(int side) {
        Polygon shape = null;
        int[] widths = getWidths();
        if (widths[side] != 0) {
            shape = new Polygon(new int[4], new int[4], 0);
            shape.addPoint(0, 0);
            shape.addPoint(-widths[(side + 3) % 4], -widths[side]);
            shape.addPoint(widths[(side + 1) % 4], -widths[side]);
            shape.addPoint(0, 0);
        }
        return shape;
    }
    private BorderPainter getBorderPainter(int side) {
        Value style = getBorderStyle(side);
        return borderPainters.get(style);
    }
    static Color getAdjustedColor(Color c, double factor) {
        double f = 1 - Math.min(Math.abs(factor), 1);
        double inc = (factor > 0 ? 255 * (1 - f) : 0);
        return new Color((int) (c.getRed() * f + inc),
                         (int) (c.getGreen() * f + inc),
                         (int) (c.getBlue() * f + inc));
    }
    public Insets getBorderInsets(Component c, Insets insets) {
        int[] widths = getWidths();
        insets.set(widths[TOP], widths[LEFT], widths[BOTTOM], widths[RIGHT]);
        return insets;
    }
    public void paintBorder(Component c, Graphics g,
                                        int x, int y, int width, int height) {
        if (!(g instanceof Graphics2D)) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        int[] widths = getWidths();
        int intX = x + widths[LEFT];
        int intY = y + widths[TOP];
        int intWidth = width - (widths[RIGHT] + widths[LEFT]);
        int intHeight = height - (widths[TOP] + widths[BOTTOM]);
        int[][] intCorners = {
            { intX, intY },
            { intX + intWidth, intY },
            { intX + intWidth, intY + intHeight },
            { intX, intY + intHeight, },
        };
        for (int i = 0; i < 4; i++) {
            Value style = getBorderStyle(i);
            Polygon shape = getBorderShape(i);
            if ((style != Value.NONE) && (shape != null)) {
                int sideLength = (i % 2 == 0 ? intWidth : intHeight);
                shape.xpoints[2] += sideLength;
                shape.xpoints[3] += sideLength;
                Color color = getBorderColor(i);
                BorderPainter painter = getBorderPainter(i);
                double angle = i * Math.PI / 2;
                g2.setClip(g.getClip()); 
                g2.translate(intCorners[i][0], intCorners[i][1]);
                g2.rotate(angle);
                g2.clip(shape);
                painter.paint(shape, g2, color, i);
                g2.rotate(-angle);
                g2.translate(-intCorners[i][0], -intCorners[i][1]);
            }
        }
        g2.dispose();
    }
    interface BorderPainter {
        void paint(Polygon shape, Graphics g, Color color, int side);
    }
    static class NullPainter implements BorderPainter {
        public void paint(Polygon shape, Graphics g, Color color, int side) {
        }
    }
    static class SolidPainter implements BorderPainter {
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            g.setColor(color);
            g.fillPolygon(shape);
        }
    }
    abstract static class StrokePainter implements BorderPainter {
        void paintStrokes(Rectangle r, Graphics g, int axis,
                                int[] lengthPattern, Color[] colorPattern) {
            boolean xAxis = (axis == View.X_AXIS);
            int start = 0;
            int end = (xAxis ? r.width : r.height);
            while (start < end) {
                for (int i = 0; i < lengthPattern.length; i++) {
                    if (start >= end) {
                        break;
                    }
                    int length = lengthPattern[i];
                    Color c = colorPattern[i];
                    if (c != null) {
                        int x = r.x + (xAxis ? start : 0);
                        int y = r.y + (xAxis ? 0 : start);
                        int width = xAxis ? length : r.width;
                        int height = xAxis ? r.height : length;
                        g.setColor(c);
                        g.fillRect(x, y, width, height);
                    }
                    start += length;
                }
            }
        }
    }
    static class DoublePainter extends StrokePainter {
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            Rectangle r = shape.getBounds();
            int length = Math.max(r.height / 3, 1);
            int[] lengthPattern = { length, length };
            Color[] colorPattern = { color, null };
            paintStrokes(r, g, View.Y_AXIS, lengthPattern, colorPattern);
        }
    }
    static class DottedDashedPainter extends StrokePainter {
        final int factor;
        DottedDashedPainter(int factor) {
            this.factor = factor;
        }
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            Rectangle r = shape.getBounds();
            int length = r.height * factor;
            int[] lengthPattern = { length, length };
            Color[] colorPattern = { color, null };
            paintStrokes(r, g, View.X_AXIS, lengthPattern, colorPattern);
        }
    }
    abstract static class ShadowLightPainter extends StrokePainter {
        static Color getShadowColor(Color c) {
            return CSSBorder.getAdjustedColor(c, -0.3);
        }
        static Color getLightColor(Color c) {
            return CSSBorder.getAdjustedColor(c, 0.7);
        }
    }
    static class GrooveRidgePainter extends ShadowLightPainter {
        final Value type;
        GrooveRidgePainter(Value type) {
            this.type = type;
        }
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            Rectangle r = shape.getBounds();
            int length = Math.max(r.height / 2, 1);
            int[] lengthPattern = { length, length };
            Color[] colorPattern =
                             ((side + 1) % 4 < 2) == (type == Value.GROOVE) ?
                new Color[] { getShadowColor(color), getLightColor(color) } :
                new Color[] { getLightColor(color), getShadowColor(color) };
            paintStrokes(r, g, View.Y_AXIS, lengthPattern, colorPattern);
        }
    }
    static class InsetOutsetPainter extends ShadowLightPainter {
        Value type;
        InsetOutsetPainter(Value type) {
            this.type = type;
        }
        public void paint(Polygon shape, Graphics g, Color color, int side) {
            g.setColor(((side + 1) % 4 < 2) == (type == Value.INSET) ?
                                getShadowColor(color) : getLightColor(color));
            g.fillPolygon(shape);
        }
    }
    static void registerBorderPainter(Value style, BorderPainter painter) {
        borderPainters.put(style, painter);
    }
    static Map<Value, BorderPainter> borderPainters =
                                        new HashMap<Value, BorderPainter>();
    static {
        registerBorderPainter(Value.NONE, new NullPainter());
        registerBorderPainter(Value.HIDDEN, new NullPainter());
        registerBorderPainter(Value.SOLID, new SolidPainter());
        registerBorderPainter(Value.DOUBLE, new DoublePainter());
        registerBorderPainter(Value.DOTTED, new DottedDashedPainter(1));
        registerBorderPainter(Value.DASHED, new DottedDashedPainter(3));
        registerBorderPainter(Value.GROOVE, new GrooveRidgePainter(Value.GROOVE));
        registerBorderPainter(Value.RIDGE, new GrooveRidgePainter(Value.RIDGE));
        registerBorderPainter(Value.INSET, new InsetOutsetPainter(Value.INSET));
        registerBorderPainter(Value.OUTSET, new InsetOutsetPainter(Value.OUTSET));
    }
}
