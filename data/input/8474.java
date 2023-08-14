public class BorderFactory
{
    private BorderFactory() {
    }
    public static Border createLineBorder(Color color) {
        return new LineBorder(color, 1);
    }
    public static Border createLineBorder(Color color, int thickness)  {
        return new LineBorder(color, thickness);
    }
    public static Border createLineBorder(Color color, int thickness, boolean rounded) {
        return new LineBorder(color, thickness, rounded);
    }
    static final Border sharedRaisedBevel = new BevelBorder(BevelBorder.RAISED);
    static final Border sharedLoweredBevel = new BevelBorder(BevelBorder.LOWERED);
    public static Border createRaisedBevelBorder() {
        return createSharedBevel(BevelBorder.RAISED);
    }
    public static Border createLoweredBevelBorder() {
        return createSharedBevel(BevelBorder.LOWERED);
    }
    public static Border createBevelBorder(int type) {
        return createSharedBevel(type);
    }
    public static Border createBevelBorder(int type, Color highlight, Color shadow) {
        return new BevelBorder(type, highlight, shadow);
    }
    public static Border createBevelBorder(int type,
                        Color highlightOuter, Color highlightInner,
                        Color shadowOuter, Color shadowInner) {
        return new BevelBorder(type, highlightOuter, highlightInner,
                                        shadowOuter, shadowInner);
    }
    static Border createSharedBevel(int type)   {
        if(type == BevelBorder.RAISED) {
            return sharedRaisedBevel;
        } else if(type == BevelBorder.LOWERED) {
            return sharedLoweredBevel;
        }
        return null;
    }
    private static Border sharedSoftRaisedBevel;
    private static Border sharedSoftLoweredBevel;
    public static Border createRaisedSoftBevelBorder() {
        if (sharedSoftRaisedBevel == null) {
            sharedSoftRaisedBevel = new SoftBevelBorder(BevelBorder.RAISED);
        }
        return sharedSoftRaisedBevel;
    }
    public static Border createLoweredSoftBevelBorder() {
        if (sharedSoftLoweredBevel == null) {
            sharedSoftLoweredBevel = new SoftBevelBorder(BevelBorder.LOWERED);
        }
        return sharedSoftLoweredBevel;
    }
    public static Border createSoftBevelBorder(int type) {
        if (type == BevelBorder.RAISED) {
            return createRaisedSoftBevelBorder();
        }
        if (type == BevelBorder.LOWERED) {
            return createLoweredSoftBevelBorder();
        }
        return null;
    }
    public static Border createSoftBevelBorder(int type, Color highlight, Color shadow) {
        return new SoftBevelBorder(type, highlight, shadow);
    }
    public static Border createSoftBevelBorder(int type, Color highlightOuter, Color highlightInner, Color shadowOuter, Color shadowInner) {
        return new SoftBevelBorder(type, highlightOuter, highlightInner, shadowOuter, shadowInner);
    }
    static final Border sharedEtchedBorder = new EtchedBorder();
    private static Border sharedRaisedEtchedBorder;
    public static Border createEtchedBorder()    {
        return sharedEtchedBorder;
    }
    public static Border createEtchedBorder(Color highlight, Color shadow)    {
        return new EtchedBorder(highlight, shadow);
    }
    public static Border createEtchedBorder(int type)    {
        switch (type) {
        case EtchedBorder.RAISED:
            if (sharedRaisedEtchedBorder == null) {
                sharedRaisedEtchedBorder = new EtchedBorder
                                           (EtchedBorder.RAISED);
            }
            return sharedRaisedEtchedBorder;
        case EtchedBorder.LOWERED:
            return sharedEtchedBorder;
        default:
            throw new IllegalArgumentException("type must be one of EtchedBorder.RAISED or EtchedBorder.LOWERED");
        }
    }
    public static Border createEtchedBorder(int type, Color highlight,
                                            Color shadow)    {
        return new EtchedBorder(type, highlight, shadow);
    }
    public static TitledBorder createTitledBorder(String title)     {
        return new TitledBorder(title);
    }
    public static TitledBorder createTitledBorder(Border border)       {
        return new TitledBorder(border);
    }
    public static TitledBorder createTitledBorder(Border border,
                                                   String title) {
        return new TitledBorder(border, title);
    }
    public static TitledBorder createTitledBorder(Border border,
                        String title,
                        int titleJustification,
                        int titlePosition)      {
        return new TitledBorder(border, title, titleJustification,
                        titlePosition);
    }
    public static TitledBorder createTitledBorder(Border border,
                        String title,
                        int titleJustification,
                        int titlePosition,
                        Font titleFont) {
        return new TitledBorder(border, title, titleJustification,
                        titlePosition, titleFont);
    }
    public static TitledBorder createTitledBorder(Border border,
                        String title,
                        int titleJustification,
                        int titlePosition,
                        Font titleFont,
                        Color titleColor)       {
        return new TitledBorder(border, title, titleJustification,
                        titlePosition, titleFont, titleColor);
    }
    final static Border emptyBorder = new EmptyBorder(0, 0, 0, 0);
    public static Border createEmptyBorder() {
        return emptyBorder;
    }
    public static Border createEmptyBorder(int top, int left,
                                                int bottom, int right) {
        return new EmptyBorder(top, left, bottom, right);
    }
    public static CompoundBorder createCompoundBorder() {
        return new CompoundBorder();
    }
    public static CompoundBorder createCompoundBorder(Border outsideBorder,
                                                Border insideBorder) {
        return new CompoundBorder(outsideBorder, insideBorder);
    }
    public static MatteBorder createMatteBorder(int top, int left, int bottom, int right,
                                                Color color) {
        return new MatteBorder(top, left, bottom, right, color);
    }
    public static MatteBorder createMatteBorder(int top, int left, int bottom, int right,
                                                Icon tileIcon) {
        return new MatteBorder(top, left, bottom, right, tileIcon);
    }
    public static Border createStrokeBorder(BasicStroke stroke) {
        return new StrokeBorder(stroke);
    }
    public static Border createStrokeBorder(BasicStroke stroke, Paint paint) {
        return new StrokeBorder(stroke, paint);
    }
    private static Border sharedDashedBorder;
    public static Border createDashedBorder(Paint paint) {
        return createDashedBorder(paint, 1.0f, 1.0f, 1.0f, false);
    }
    public static Border createDashedBorder(Paint paint, float length, float spacing) {
        return createDashedBorder(paint, 1.0f, length, spacing, false);
    }
    public static Border createDashedBorder(Paint paint, float thickness, float length, float spacing, boolean rounded) {
        boolean shared = !rounded && (paint == null) && (thickness == 1.0f) && (length == 1.0f) && (spacing == 1.0f);
        if (shared && (sharedDashedBorder != null)) {
            return sharedDashedBorder;
        }
        if (thickness < 1.0f) {
            throw new IllegalArgumentException("thickness is less than 1");
        }
        if (length < 1.0f) {
            throw new IllegalArgumentException("length is less than 1");
        }
        if (spacing < 0.0f) {
            throw new IllegalArgumentException("spacing is less than 0");
        }
        int cap = rounded ? BasicStroke.CAP_ROUND : BasicStroke.CAP_SQUARE;
        int join = rounded ? BasicStroke.JOIN_ROUND : BasicStroke.JOIN_MITER;
        float[] array = { thickness * (length - 1.0f), thickness * (spacing + 1.0f) };
        Border border = createStrokeBorder(new BasicStroke(thickness, cap, join, thickness * 2.0f, array, 0.0f), paint);
        if (shared) {
            sharedDashedBorder = border;
        }
        return border;
    }
}
