public class TagletOutputImpl implements TagletOutput {
    private StringBuffer output;
    public TagletOutputImpl(String o) {
        setOutput(o);
    }
    public void setOutput (Object o) {
        output = new StringBuffer(o == null ? "" : (String) o);
    }
    public void appendOutput(TagletOutput o) {
        output.append(o.toString());
    }
    public boolean hasInheritDocTag() {
        return output.indexOf(InheritDocTaglet.INHERIT_DOC_INLINE_TAG) != -1;
    }
    public String toString() {
        return output.toString();
    }
    public boolean isEmpty() {
        return (toString().trim().isEmpty());
    }
}
