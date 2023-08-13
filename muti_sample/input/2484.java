public final class XPathFilter2ParameterSpec implements TransformParameterSpec {
    private final List xPathList;
    public XPathFilter2ParameterSpec(List xPathList) {
        if (xPathList == null) {
            throw new NullPointerException("xPathList cannot be null");
        }
        List xPathListCopy = new ArrayList(xPathList);
        if (xPathListCopy.isEmpty()) {
            throw new IllegalArgumentException("xPathList cannot be empty");
        }
        int size = xPathListCopy.size();
        for (int i = 0; i < size; i++) {
            if (!(xPathListCopy.get(i) instanceof XPathType)) {
                throw new ClassCastException
                    ("xPathList["+i+"] is not a valid type");
            }
        }
        this.xPathList = Collections.unmodifiableList(xPathListCopy);
    }
    public List getXPathList() {
        return xPathList;
    }
}
