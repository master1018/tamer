public class AttributeFilterConfig {
    public AttributeFilterConfig(String attribute, String newAttributeName, String filterMethod, String filterClass) {
        this.attribute = attribute;
        this.newAttributeName = newAttributeName;
        this.filterMethod = filterMethod;
        this.filterClass = filterClass;
    }
    public String getAttribute() {
        return attribute;
    }
    public String getNewAttributeName() {
        return newAttributeName;
    }
    public String getFilterMethod() {
        return filterMethod;
    }
    public String getFilterClass() {
        return filterClass;
    }
    private String attribute;
    private String newAttributeName;
    private String filterMethod;
    private String filterClass;
}
