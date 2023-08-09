public class AttributeModificationException extends NamingException {
    private ModificationItem[] unexecs = null;
    public AttributeModificationException(String explanation) {
        super(explanation);
    }
    public AttributeModificationException() {
        super();
    }
    public void setUnexecutedModifications(ModificationItem[] e) {
        unexecs = e;
    }
    public ModificationItem[] getUnexecutedModifications() {
        return unexecs;
    }
    public String toString() {
        String orig = super.toString();
        if (unexecs != null) {
            orig += ("First unexecuted modification: " +
                     unexecs[0].toString());
        }
        return orig;
    }
    private static final long serialVersionUID = 8060676069678710186L;
}
