public class MockSources implements Sources {
    private final HashMap<String, Source> mSources = new HashMap<String, Source>();
    public void addSource(Source source) {
        mSources.put(source.getName(), source);
    }
    public Source getSource(String name) {
        return mSources.get(name);
    }
    public Collection<Source> getSources() {
        return mSources.values();
    }
    public Source getWebSearchSource() {
        return null;
    }
    public void update() {
    }
    public Source createSourceFor(ComponentName component) {
        return null;
    }
}
