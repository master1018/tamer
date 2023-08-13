public final class SunMinMaxPage implements PrintRequestAttribute {
    private int page_max, page_min;
    public SunMinMaxPage(int min, int max) {
       page_min = min;
       page_max = max;
    }
    public final Class getCategory() {
        return SunMinMaxPage.class;
    }
    public final int getMin() {
        return page_min;
    }
    public final int getMax() {
        return page_max;
    }
    public final String getName() {
        return "sun-page-minmax";
    }
}
