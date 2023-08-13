abstract class Underline {
    abstract void drawUnderline(Graphics2D g2d,
                                float thickness,
                                float x1,
                                float x2,
                                float y);
    abstract float getLowerDrawLimit(float thickness);
    abstract Shape getUnderlineShape(float thickness,
                                     float x1,
                                     float x2,
                                     float y);
    private static final float DEFAULT_THICKNESS = 1.0f;
    private static final boolean USE_THICKNESS = true;
    private static final boolean IGNORE_THICKNESS = false;
    private static final class StandardUnderline extends Underline {
        private float shift;
        private float thicknessMultiplier;
        private float[] dashPattern;
        private boolean useThickness;
        private BasicStroke cachedStroke;
        StandardUnderline(float shift,
                          float thicknessMultiplier,
                          float[] dashPattern,
                          boolean useThickness) {
            this.shift = shift;
            this.thicknessMultiplier = thicknessMultiplier;
            this.dashPattern = dashPattern;
            this.useThickness = useThickness;
            this.cachedStroke = null;
        }
        private BasicStroke createStroke(float lineThickness) {
            if (dashPattern == null) {
                return new BasicStroke(lineThickness,
                                       BasicStroke.CAP_BUTT,
                                       BasicStroke.JOIN_MITER);
            }
            else {
                return new BasicStroke(lineThickness,
                                       BasicStroke.CAP_BUTT,
                                       BasicStroke.JOIN_MITER,
                                       10.0f,
                                       dashPattern,
                                       0);
            }
        }
        private float getLineThickness(float thickness) {
            if (useThickness) {
                return thickness * thicknessMultiplier;
            }
            else {
                return DEFAULT_THICKNESS * thicknessMultiplier;
            }
        }
        private Stroke getStroke(float thickness) {
            float lineThickness = getLineThickness(thickness);
            BasicStroke stroke = cachedStroke;
            if (stroke == null ||
                    stroke.getLineWidth() != lineThickness) {
                stroke = createStroke(lineThickness);
                cachedStroke = stroke;
            }
            return stroke;
        }
        void drawUnderline(Graphics2D g2d,
                           float thickness,
                           float x1,
                           float x2,
                           float y) {
            Stroke saveStroke = g2d.getStroke();
            g2d.setStroke(getStroke(thickness));
            g2d.draw(new Line2D.Float(x1, y + shift, x2, y + shift));
            g2d.setStroke(saveStroke);
        }
        float getLowerDrawLimit(float thickness) {
            return shift + getLineThickness(thickness);
        }
        Shape getUnderlineShape(float thickness,
                                float x1,
                                float x2,
                                float y) {
            Stroke ulStroke = getStroke(thickness);
            Line2D line = new Line2D.Float(x1, y + shift, x2, y + shift);
            return ulStroke.createStrokedShape(line);
        }
    }
    private static class IMGrayUnderline extends Underline {
        private BasicStroke stroke;
        IMGrayUnderline() {
            stroke = new BasicStroke(DEFAULT_THICKNESS,
                                     BasicStroke.CAP_BUTT,
                                     BasicStroke.JOIN_MITER,
                                     10.0f,
                                     new float[] {1, 1},
                                     0);
        }
        void drawUnderline(Graphics2D g2d,
                           float thickness,
                           float x1,
                           float x2,
                           float y) {
            Stroke saveStroke = g2d.getStroke();
            g2d.setStroke(stroke);
            Line2D.Float drawLine = new Line2D.Float(x1, y, x2, y);
            g2d.draw(drawLine);
            drawLine.y1 += DEFAULT_THICKNESS;
            drawLine.y2 += DEFAULT_THICKNESS;
            drawLine.x1 += DEFAULT_THICKNESS;
            g2d.draw(drawLine);
            g2d.setStroke(saveStroke);
        }
        float getLowerDrawLimit(float thickness) {
            return DEFAULT_THICKNESS * 2;
        }
        Shape getUnderlineShape(float thickness,
                                float x1,
                                float x2,
                                float y) {
            GeneralPath gp = new GeneralPath();
            Line2D.Float line = new Line2D.Float(x1, y, x2, y);
            gp.append(stroke.createStrokedShape(line), false);
            line.y1 += DEFAULT_THICKNESS;
            line.y2 += DEFAULT_THICKNESS;
            line.x1 += DEFAULT_THICKNESS;
            gp.append(stroke.createStrokedShape(line), false);
            return gp;
        }
    }
    private static final ConcurrentHashMap<Object, Underline>
        UNDERLINES = new ConcurrentHashMap<Object, Underline>(6);
    private static final Underline[] UNDERLINE_LIST;
    static {
        Underline[] uls = new Underline[6];
        uls[0] = new StandardUnderline(0, 1, null, USE_THICKNESS);
        UNDERLINES.put(TextAttribute.UNDERLINE_ON, uls[0]);
        uls[1] = new StandardUnderline(1, 1, null, IGNORE_THICKNESS);
        UNDERLINES.put(TextAttribute.UNDERLINE_LOW_ONE_PIXEL, uls[1]);
        uls[2] = new StandardUnderline(1, 2, null, IGNORE_THICKNESS);
        UNDERLINES.put(TextAttribute.UNDERLINE_LOW_TWO_PIXEL, uls[2]);
        uls[3] = new StandardUnderline(1, 1, new float[] { 1, 1 }, IGNORE_THICKNESS);
        UNDERLINES.put(TextAttribute.UNDERLINE_LOW_DOTTED, uls[3]);
        uls[4] = new IMGrayUnderline();
        UNDERLINES.put(TextAttribute.UNDERLINE_LOW_GRAY, uls[4]);
        uls[5] = new StandardUnderline(1, 1, new float[] { 4, 4 }, IGNORE_THICKNESS);
        UNDERLINES.put(TextAttribute.UNDERLINE_LOW_DASHED, uls[5]);
        UNDERLINE_LIST = uls;
    }
    static Underline getUnderline(Object value) {
        if (value == null) {
            return null;
        }
        return (Underline) UNDERLINES.get(value);
    }
    static Underline getUnderline(int index) {
        return index < 0 ? null : UNDERLINE_LIST[index];
    }
}
