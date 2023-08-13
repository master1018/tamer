public class AttributeTests {
    public static void main(String... args) throws Exception {
        test6982312();
        test6746111();
    }
    static void test6982312() throws IOException {
        String pack200Cmd = Utils.getPack200Cmd();
        File dynJar = new File(".", "dyn.jar");
        Utils.copyFile(new File(Utils.TEST_SRC_DIR, "dyn.jar"), dynJar);
        File testJar = new File(".", "test.jar");
        List<String> cmds = new ArrayList<String>();
        cmds.add(pack200Cmd);
        cmds.add("--repack");
        cmds.add(testJar.getAbsolutePath());
        cmds.add(dynJar.getAbsolutePath());
        Utils.runExec(cmds);
        Utils.doCompareBitWise(dynJar.getAbsoluteFile(), testJar.getAbsoluteFile());
        testJar.delete();
        dynJar.delete();
    }
    static void test6746111() throws Exception {
        String pack200Cmd = Utils.getPack200Cmd();
        File badAttrJar = new File(".", "badattr.jar");
        Utils.copyFile(new File(Utils.TEST_SRC_DIR, "badattr.jar"), badAttrJar);
        File testJar = new File(".", "test.jar");
        List<String> cmds = new ArrayList<String>();
        cmds.add(pack200Cmd);
        cmds.add("--repack");
        cmds.add("-v");
        cmds.add(testJar.getAbsolutePath());
        cmds.add(badAttrJar.getAbsolutePath());
        List<String> output = Utils.runExec(cmds);
        Utils.doCompareBitWise(badAttrJar.getAbsoluteFile(), testJar.getAbsoluteFile());
        String[] expectedStrings = {
            "WARNING: Passing class file uncompressed due to unrecognized" +
                    " attribute: Foo.class",
            "INFO: com.sun.java.util.jar.pack.Attribute$FormatException: " +
                    "class attribute \"XourceFile\":  is unknown attribute " +
                    "in class Foo",
            "INFO: com.sun.java.util.jar.pack.ClassReader$ClassFormatException: " +
                    "AnnotationDefault: attribute length cannot be zero, in Test.message()",
            "WARNING: Passing class file uncompressed due to unknown class format: Test.class"
        };
        List<String> notfoundList = new ArrayList<String>();
        notfoundList.addAll(Arrays.asList(expectedStrings));
        for (String x : output) {
            findString(x, notfoundList, expectedStrings);
        }
        if (!notfoundList.isEmpty()) {
            System.out.println("Not found:");
            for (String x : notfoundList) {
                System.out.println(x);
            }
            throw new Exception("Test fails: " + notfoundList.size() +
                    " expected strings not found");
        }
        testJar.delete();
        badAttrJar.delete();
    }
    private static void findString(String outputStr, List<String> notfoundList,
            String[] expectedStrings) {
        for (String y : expectedStrings) {
            if (outputStr.contains(y)) {
                notfoundList.remove(y);
                return;
            }
        }
    }
}
