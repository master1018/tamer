public abstract class LinkInfo {
    public ClassDoc classDoc;
    public ExecutableMemberDoc executableMemberDoc;
    public Type type;
    public boolean isVarArg = false;
    public boolean isTypeBound = false;
    public String label;
    public boolean isStrong = false;
    public boolean includeTypeInClassLinkLabel = true;
    public boolean includeTypeAsSepLink = false;
    public boolean excludeTypeBounds = false;
    public boolean excludeTypeParameterLinks = false;
    public boolean excludeTypeBoundsLinks = false;
    public boolean linkToSelf = true;
    public int displayLength = 0;
    public abstract int getContext();
    public abstract void setContext(int c);
    public abstract boolean isLinkable();
    public String getClassLinkLabel(Configuration configuration) {
        if (label != null && label.length() > 0) {
            return label;
        } else if (isLinkable()) {
            return classDoc.name();
        } else {
            return configuration.getClassName(classDoc);
        }
    }
}
