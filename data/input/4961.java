public final class PagesPerMinute extends IntegerSyntax
        implements PrintServiceAttribute {
    private static final long serialVersionUID = -6366403993072862015L;
    public PagesPerMinute(int value) {
        super(value, 0, Integer.MAX_VALUE);
    }
    public boolean equals(Object object) {
        return (super.equals (object) &&
                object instanceof PagesPerMinute);
    }
    public final Class<? extends Attribute> getCategory() {
        return PagesPerMinute.class;
    }
    public final String getName() {
        return "pages-per-minute";
    }
}
