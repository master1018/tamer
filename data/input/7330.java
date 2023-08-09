public abstract class LayoutStyle {
    public static void setInstance(LayoutStyle style) {
        synchronized(LayoutStyle.class) {
            if (style == null) {
                AppContext.getAppContext().remove(LayoutStyle.class);
            }
            else {
                AppContext.getAppContext().put(LayoutStyle.class, style);
            }
        }
    }
    public static LayoutStyle getInstance() {
        LayoutStyle style;
        synchronized(LayoutStyle.class) {
            style = (LayoutStyle)AppContext.getAppContext().
                    get(LayoutStyle.class);
        }
        if (style == null) {
            return UIManager.getLookAndFeel().getLayoutStyle();
        }
        return style;
    }
    public enum ComponentPlacement {
        RELATED,
        UNRELATED,
        INDENT;
    }
    public LayoutStyle() {
    }
    public abstract int getPreferredGap(JComponent component1,
                                        JComponent component2,
                                        ComponentPlacement type, int position,
                                        Container parent);
    public abstract int getContainerGap(JComponent component, int position,
                                        Container parent);
}
