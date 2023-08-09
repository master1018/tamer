public abstract class ResourceQualifier implements Comparable<ResourceQualifier> {
    public abstract String getName();
    public abstract String getShortName();
    public abstract Image getIcon();
    public abstract boolean isValid();
    public abstract boolean checkAndSet(String value, FolderConfiguration config);
    public abstract String getFolderSegment(IAndroidTarget target);
    public boolean isMatchFor(ResourceQualifier qualifier) {
        return equals(qualifier);
    }
    public boolean isBetterMatchThan(ResourceQualifier compareTo, ResourceQualifier reference) {
        return false;
    }
    @Override
    public String toString() {
        return getFolderSegment(null);
    }
    public abstract String getStringValue();
    @Override
    public abstract boolean equals(Object object);
    @Override
    public abstract int hashCode();
    public final int compareTo(ResourceQualifier o) {
        return toString().compareTo(o.toString());
    }
}
