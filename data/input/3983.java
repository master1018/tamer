public class ShellFolderColumnInfo {
    private String title;
    private Integer width;
    private boolean visible;
    private Integer alignment;
    private SortOrder sortOrder;
    private Comparator comparator;
    private boolean compareByColumn;
    public ShellFolderColumnInfo(String title, Integer width,
                                 Integer alignment, boolean visible,
                                 SortOrder sortOrder, Comparator comparator,
                                 boolean compareByColumn) {
        this.title = title;
        this.width = width;
        this.alignment = alignment;
        this.visible = visible;
        this.sortOrder = sortOrder;
        this.comparator = comparator;
        this.compareByColumn = compareByColumn;
    }
    public ShellFolderColumnInfo(String title, Integer width,
                                 Integer alignment, boolean visible,
                                 SortOrder sortOrder, Comparator comparator) {
        this(title, width, alignment, visible, sortOrder, comparator, false);
    }
    public ShellFolderColumnInfo(String title, int width, int alignment,
                                 boolean visible) {
        this(title, width, alignment, visible, null, null);
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public Integer getWidth() {
        return width;
    }
    public void setWidth(Integer width) {
        this.width = width;
    }
    public Integer getAlignment() {
        return alignment;
    }
    public void setAlignment(Integer alignment) {
        this.alignment = alignment;
    }
    public boolean isVisible() {
        return visible;
    }
    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    public SortOrder getSortOrder() {
        return sortOrder;
    }
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
    public Comparator getComparator() {
        return comparator;
    }
    public void setComparator(Comparator comparator) {
        this.comparator = comparator;
    }
    public boolean isCompareByColumn() {
        return compareByColumn;
    }
    public void setCompareByColumn(boolean compareByColumn) {
        this.compareByColumn = compareByColumn;
    }
}
