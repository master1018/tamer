public class Described implements DescribedMBean {
    private String name ;
    @SqeDescriptorKey("NO PARAMETER CONSTRUCTOR Described")
    public Described() {}
    @SqeDescriptorKey("ONE PARAMETER CONSTRUCTOR Described")
    @ConstructorProperties({"name", "unused"})
    public Described(@SqeDescriptorKey("CONSTRUCTOR PARAMETER name")String name,
            @SqeDescriptorKey("CONSTRUCTOR PARAMETER unused")String unused) {
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
