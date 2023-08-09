public class TestTagHolderMethod extends JavadocTester {
    private static final String BUG_ID = "4706525";
    public static final String[] ARGS = new String[] {
        "-docletpath", SRC_DIR, "-doclet", "TestTagHolderMethod", "-sourcepath",
                SRC_DIR, "pkg"};
    public static boolean start(RootDoc root) throws Exception {
        ClassDoc[] classes = root.classes();
        for (int i = 0; i < classes.length; i++) {
            checkHolders(classes[i].fields());
            checkHolders(classes[i].constructors());
            checkHolders(classes[i].methods());
            checkHolders(classes[i].innerClasses());
        }
        return true;
    }
    private static void checkHolders(Doc[] holders) throws Exception {
        for (int i = 0; i < holders.length; i++) {
            Doc holder = holders[i];
            Tag[] tags = holder.tags();
            for (int j = 0; j < tags.length; j++) {
                if (! tags[j].holder().name().equals(holder.name())) {
                    throw new Exception("The holder method does not return the correct Doc object.");
                } else {
                    System.out.println(tags[j].name() + " is held by " + holder.name());
                }
            }
        }
    }
    public static void main(String[] args) {
        run(new TestTagHolderMethod(), ARGS, new String[][]{}, new String[][]{});
    }
    public String getBugId() {
        return BUG_ID;
    }
    public String getBugName() {
        return getClass().getName();
    }
}
