public class MethodOriginator implements Originator {
    private final MethodDoc mMethod;
    private final String mComment;
    private final ClassDoc mClass;
    private String knownFailure = null;
    private String brokenTest = null;
    private String toBeFixed = null;
    MethodOriginator(MethodDoc testMethod, ClassDoc clazz, String comment) {
        mMethod = testMethod;
        mComment = comment;
        mClass = clazz;
        AnnotationDesc[] annots = testMethod.annotations();
        for (AnnotationDesc annot : annots) {
            if (annot.annotationType().qualifiedName().equals(
                    "dalvik.annotation.KnownFailure")) {
                knownFailure = "<b>@KnownFailure:</b>"
                        + (String)annot.elementValues()[0].value().value();
            } else if (annot.annotationType().qualifiedName().equals(
                    "dalvik.annotation.BrokenTest")) {
                brokenTest = "<b>@BrokenTest:</b>"
                        + (String)annot.elementValues()[0].value().value();
            } else if (annot.annotationType().qualifiedName().equals(
                    "dalvik.annotation.ToBeFixed")) {
                String info = "N/A";
                if (annot.elementValues().length > 0) {
                    info = (String)annot.elementValues()[0].value().value();
                }
                toBeFixed = "<b>@ToBeFixed:</b>" + info;
            }
        }
    }
    public String asString() {
        return (mComment != null ? "comment:" + mComment + " - " : "")
                + mClass.qualifiedName() + ": <b>" + mMethod.name() + "</b>"
                + mMethod.signature()
                + (brokenTest != null ? " [" + brokenTest + "]" : "")
                + (toBeFixed != null ? " [" + toBeFixed + "]" : "")
                + (knownFailure != null ? " [" + knownFailure + "]" : "");
    }
    public boolean isDisabled() {
        return mMethod.name().startsWith("_test");
    }
    public String getBrokenTest() {
        return brokenTest;
    }
    public String getToBeFixed() {
        return toBeFixed;
    }
    public String getKnownFailure() {
        return knownFailure;
    }
}
