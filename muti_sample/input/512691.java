public final class StyleResourceValue extends ResourceValue implements IStyleResourceValue {
    private String mParentStyle = null;
    private HashMap<String, IResourceValue> mItems = new HashMap<String, IResourceValue>();
    public StyleResourceValue(String type, String name, boolean isFramework) {
        super(type, name, isFramework);
    }
    public StyleResourceValue(String type, String name, String parentStyle, boolean isFramework) {
        super(type, name, isFramework);
        mParentStyle = parentStyle;
    }
    public String getParentStyle() {
        return mParentStyle;
    }
    public IResourceValue findItem(String name) {
        return mItems.get(name);
    }
    public void addItem(IResourceValue value) {
        mItems.put(value.getName(), value);
    }
    @Override
    public void replaceWith(ResourceValue value) {
        super.replaceWith(value);
        if (value instanceof StyleResourceValue) {
            mItems.clear();
            mItems.putAll(((StyleResourceValue)value).mItems);
        }
    }
}
