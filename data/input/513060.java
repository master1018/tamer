public class FeatureDescriptor {
    private Map<String, Object> values;
    boolean preferred, hidden, expert;
    String shortDescription;
    String name;
    String displayName;
    public FeatureDescriptor() {
        this.values = new HashMap<String, Object>();
    }
    public void setValue(String attributeName, Object value) {
        if (attributeName == null || value == null) {
            throw new NullPointerException();
        }
        values.put(attributeName, value);
    }
    public Object getValue(String attributeName) {
        Object result = null;
        if (attributeName != null) {
            result = values.get(attributeName);
        }
        return result;
    }
    public Enumeration<String> attributeNames() {
        return Collections.enumeration(new LinkedList<String>(values.keySet()));
    }
    public void setShortDescription(String text) {
        this.shortDescription = text;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
    public String getShortDescription() {
        return shortDescription == null ? getDisplayName() : shortDescription;
    }
    public String getName() {
        return name;
    }
    public String getDisplayName() {
        return displayName == null ? getName() : displayName;
    }
    public void setPreferred(boolean preferred) {
        this.preferred = preferred;
    }
    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
    public void setExpert(boolean expert) {
        this.expert = expert;
    }
    public boolean isPreferred() {
        return preferred;
    }
    public boolean isHidden() {
        return hidden;
    }
    public boolean isExpert() {
        return expert;
    }
}
