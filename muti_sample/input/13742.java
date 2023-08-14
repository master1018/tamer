class ParamTagImpl extends TagImpl implements ParamTag {
    private static Pattern typeParamRE = Pattern.compile("<([^<>]+)>");
    private final String parameterName;
    private final String parameterComment;
    private final boolean isTypeParameter;
    ParamTagImpl(DocImpl holder, String name, String text) {
        super(holder, name, text);
        String[] sa = divideAtWhite();
        Matcher m = typeParamRE.matcher(sa[0]);
        isTypeParameter = m.matches();
        parameterName = isTypeParameter ? m.group(1) : sa[0];
        parameterComment = sa[1];
    }
    public String parameterName() {
        return parameterName;
    }
    public String parameterComment() {
        return parameterComment;
    }
    public String kind() {
        return "@param";
    }
    public boolean isTypeParameter() {
        return isTypeParameter;
    }
    public String toString() {
        return name + ":" + text;
    }
    public Tag[] inlineTags() {
        return Comment.getInlineTags(holder, parameterComment);
    }
}
