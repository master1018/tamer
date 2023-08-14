public class ResourceItem implements Comparable<ResourceItem> {
    private final String mName;
    public ResourceItem(String name) {
        mName = name;
    }
    public final String getName() {
        return mName;
    }
    public int compareTo(ResourceItem other) {
        return mName.compareTo(other.mName);
    }
}
