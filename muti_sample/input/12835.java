public final class PagesPerMinuteColor extends IntegerSyntax
        implements PrintServiceAttribute {
    static final long serialVersionUID = 1684993151687470944L;
    public PagesPerMinuteColor(int value) {
        super(value, 0, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return (super.equals(object) &&
                object instanceof PagesPerMinuteColor);
    }
    public final Class<? extends Attribute> getCategory() {
        return PagesPerMinuteColor.class;
    }
    public final String getName() {
        return "pages-per-minute-color";
    }
}
