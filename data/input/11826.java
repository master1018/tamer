public class DescribedMX implements DescribedMXBean {
    private String name ;
    @SqeDescriptorKey("NO PARAMETER CONSTRUCTOR DescribedMX")
    public DescribedMX() {}
    @SqeDescriptorKey("ONE PARAMETER CONSTRUCTOR DescribedMX")
    @ConstructorProperties({"name", "unused"})
    public DescribedMX(@SqeDescriptorKey("CONSTRUCTOR PARAMETER name")String name,
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
