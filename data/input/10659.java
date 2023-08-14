public final class SunPageSelection implements PrintRequestAttribute {
    public static final SunPageSelection ALL = new SunPageSelection(0);
    public static final SunPageSelection RANGE = new SunPageSelection(1);
    public static final SunPageSelection SELECTION = new SunPageSelection(2);
    private int pages;
    public SunPageSelection(int value) {
        pages = value;
    }
    public final Class getCategory() {
        return SunPageSelection.class;
    }
    public final String getName() {
        return "sun-page-selection";
    }
    public String toString() {
       return "page-selection: " + pages;
    }
}
