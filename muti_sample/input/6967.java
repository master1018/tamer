public class SearchResult extends Binding {
    private Attributes attrs;
    public SearchResult(String name, Object obj, Attributes attrs) {
        super(name, obj);
        this.attrs = attrs;
    }
    public SearchResult(String name, Object obj, Attributes attrs,
        boolean isRelative) {
        super(name, obj, isRelative);
        this.attrs = attrs;
    }
    public SearchResult(String name, String className,
        Object obj, Attributes attrs) {
        super(name, className, obj);
        this.attrs = attrs;
    }
    public SearchResult(String name, String className, Object obj,
        Attributes attrs, boolean isRelative) {
        super(name, className, obj, isRelative);
        this.attrs = attrs;
    }
    public Attributes getAttributes() {
        return attrs;
    }
    public void setAttributes(Attributes attrs) {
        this.attrs = attrs;
    }
    public String toString() {
        return super.toString() + ":" + getAttributes();
    }
    private static final long serialVersionUID = -9158063327699723172L;
}
