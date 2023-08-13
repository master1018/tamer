public class TestSerializedForm extends JavadocTester implements Serializable {
    private static final String BUG_ID = "4341304-4485668-4966728";
    private static final String[][] TEST = {
        {BUG_ID + FS + "serialized-form.html",
            "protected&nbsp;java.lang.Object&nbsp;readResolve()"},
        {BUG_ID + FS + "serialized-form.html",
            "protected&nbsp;java.lang.Object&nbsp;writeReplace()"},
        {BUG_ID + FS + "serialized-form.html",
            "protected&nbsp;java.lang.Object&nbsp;readObjectNoData()"},
        {BUG_ID + FS + "serialized-form.html",
            "See Also"},
    };
    private static final String[][] NEGATED_TEST = NO_TEST;
    private static final String[] ARGS = new String[] {
        "-d", BUG_ID, "-sourcepath", SRC_DIR,
        SRC_DIR + FS + "TestSerializedForm.java"
    };
    public final int SERIALIZABLE_CONSTANT = 1;
    public static void main(String[] args) {
        TestSerializedForm tester = new TestSerializedForm();
        int actualExitCode = run(tester, ARGS, TEST, NEGATED_TEST);
        tester.printSummary();
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
    private void readObject(ObjectInputStream s) {}
    private void writeObject(ObjectOutputStream s) {}
    protected Object readResolve(){return null;}
    protected Object writeReplace(){return null;}
    protected Object readObjectNoData() {
        return null;
    }
}
