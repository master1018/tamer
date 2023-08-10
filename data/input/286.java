public class MetaParamImpl implements MetaParam {
    private String name;
    private OutType outType = new OutTypeImpl();
    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public OutType getOutType() {
        return this.outType;
    }
    @Override
    public void setName(String toSet) {
        this.name = toSet;
    }
}
