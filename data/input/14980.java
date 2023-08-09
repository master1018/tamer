class SearchResultWithControls extends SearchResult implements HasControls {
    private Control[] controls;
    public SearchResultWithControls(String name, Object obj, Attributes attrs,
        boolean isRelative, Control[] controls) {
        super(name, obj, attrs, isRelative);
        this.controls = controls;
    }
    public Control[] getControls() throws NamingException {
        return controls;
    }
    private static final long serialVersionUID = 8476983938747908202L;
}
