class ThrowsTagImpl extends TagImpl implements ThrowsTag {
    private final String exceptionName;
    private final String exceptionComment;
    ThrowsTagImpl(DocImpl holder, String name, String text) {
        super(holder, name, text);
        String[] sa = divideAtWhite();
        exceptionName = sa[0];
        exceptionComment = sa[1];
    }
    public String exceptionName() {
        return exceptionName;
    }
    public String exceptionComment() {
        return exceptionComment;
    }
    public ClassDoc exception() {
        ClassDocImpl exceptionClass;
        if (!(holder instanceof ExecutableMemberDoc)) {
            exceptionClass = null;
        } else {
            ExecutableMemberDocImpl emd = (ExecutableMemberDocImpl)holder;
            ClassDocImpl con = (ClassDocImpl)emd.containingClass();
            exceptionClass = (ClassDocImpl)con.findClass(exceptionName);
        }
        return exceptionClass;
    }
    public Type exceptionType() {
        return exception();
    }
    public String kind() {
        return "@throws";
    }
    public Tag[] inlineTags() {
        return Comment.getInlineTags(holder, exceptionComment());
    }
}
