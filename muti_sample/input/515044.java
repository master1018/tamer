public class ClassOriginator implements Originator {
    private final ClassDoc mClassDoc;
    private final String mComment;
    ClassOriginator(ClassDoc classDoc, String comment) {
        mClassDoc = classDoc;
        mComment = comment;
    }
    public String asString() {
        return (mComment != null ? mComment + " - " : "") + " -class- "
                + mClassDoc.qualifiedName();
    }
    public boolean isDisabled() {
        return false;
    }
    public String getToBeFixed() {
        return null;
    }
    public String getBrokenTest() {
        return null;
    }
    public String getKnownFailure() {
        return null;
    }
}
