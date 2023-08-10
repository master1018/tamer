public class WuaUpdateSearcherBehaviors implements OvalObject {
    public static final Boolean DEFAULT_INCLUDE_SUPERSEDED_UPDATES = Boolean.TRUE;
    private Boolean include_superseded_updates;
    public WuaUpdateSearcherBehaviors() {
    }
    public void setIncludeSupersededUpdates(final Boolean include_superseded_updates) {
        this.include_superseded_updates = include_superseded_updates;
    }
    public Boolean getIncludeSupersededUpdates() {
        return this.include_superseded_updates;
    }
    protected final boolean _includeSupersededUpdates() {
        Boolean include_group = getIncludeSupersededUpdates();
        return (include_group == null ? DEFAULT_INCLUDE_SUPERSEDED_UPDATES.booleanValue() : include_group.booleanValue());
    }
    public static boolean includeSupersededUpdates(final WuaUpdateSearcherBehaviors behaviors) {
        if (behaviors == null) {
            return DEFAULT_INCLUDE_SUPERSEDED_UPDATES.booleanValue();
        }
        Boolean include_group = behaviors.getIncludeSupersededUpdates();
        return (include_group == null ? DEFAULT_INCLUDE_SUPERSEDED_UPDATES.booleanValue() : include_group.booleanValue());
    }
    @Override
    public int hashCode() {
        final int prime = 37;
        int result = 17;
        result = prime * result + (includeSupersededUpdates(this) ? 0 : 1);
        return result;
    }
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof WuaUpdateSearcherBehaviors)) {
            return false;
        }
        if (super.equals(obj)) {
            WuaUpdateSearcherBehaviors other = (WuaUpdateSearcherBehaviors) obj;
            if (includeSupersededUpdates(this) == includeSupersededUpdates(other)) {
                return true;
            }
        }
        return false;
    }
    @Override
    public String toString() {
        return super.toString() + ", include_superseded_updates=" + getIncludeSupersededUpdates();
    }
}
