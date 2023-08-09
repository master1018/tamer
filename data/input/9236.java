public class UnDescribed implements UnDescribedMBean {
    private String name ;
    public UnDescribed() {}
    @ConstructorProperties({"name", "unused"})
    public UnDescribed(String name, String unused) {
        this.name = name ;
    }
    public String getStringProp() {
        return this.name;
    }
    public void setStringProp(String name) {
        this.name = name;
    }
    public void doNothing() {}
    public void doNothingParam(String param) {}
}
