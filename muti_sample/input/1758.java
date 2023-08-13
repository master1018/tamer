public final class ExcC14NParameterSpec implements C14NMethodParameterSpec {
    private List preList;
    public static final String DEFAULT = "#default";
    public ExcC14NParameterSpec() {
        preList = Collections.EMPTY_LIST;
    }
    public ExcC14NParameterSpec(List prefixList) {
        if (prefixList == null) {
            throw new NullPointerException("prefixList cannot be null");
        }
        this.preList = new ArrayList(prefixList);
        for (int i = 0, size = preList.size(); i < size; i++) {
            if (!(preList.get(i) instanceof String)) {
                throw new ClassCastException("not a String");
            }
        }
        preList = Collections.unmodifiableList(preList);
    }
    public List getPrefixList() {
        return preList;
    }
}
