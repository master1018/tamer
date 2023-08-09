public class Pack200Props {
    public static void main(String... args) {
        verifyDefaults();
        File out = new File("test" + Utils.PACK_FILE_EXT);
        out.delete();
        verifySegmentLimit(out);
    }
    static void verifySegmentLimit(File outFile) {
        File sdkHome = Utils.JavaSDK;
        File testJar = new File(new File(sdkHome, "lib"), "tools.jar");
        System.out.println("using pack200: " + Utils.getPack200Cmd());
        List<String> cmdsList = new ArrayList<>();
        cmdsList.add(Utils.getPack200Cmd());
        cmdsList.add("--effort=1");
        cmdsList.add("--verbose");
        cmdsList.add("--no-gzip");
        cmdsList.add(outFile.getName());
        cmdsList.add(testJar.getAbsolutePath());
        List<String> outList = Utils.runExec(cmdsList);
        int count = 0;
        for (String line : outList) {
            System.out.println(line);
            if (line.matches(".*Transmitted.*files of.*input bytes in a segment of.*bytes")) {
                count++;
            }
        }
        if (count == 0) {
            throw new RuntimeException("no segments or no output ????");
        } else if (count > 1) {
            throw new RuntimeException("multiple segments detected, expected 1");
        }
    }
    private static void verifyDefaults() {
        Map<String, String> expectedDefaults = new HashMap<>();
        Packer p = Pack200.newPacker();
        expectedDefaults.put("com.sun.java.util.jar.pack.default.timezone",
                p.FALSE);
        expectedDefaults.put("com.sun.java.util.jar.pack.disable.native",
                p.FALSE);
        expectedDefaults.put("com.sun.java.util.jar.pack.verbose", "0");
        expectedDefaults.put(p.CLASS_ATTRIBUTE_PFX + "CompilationID", "RUH");
        expectedDefaults.put(p.CLASS_ATTRIBUTE_PFX + "SourceID", "RUH");
        expectedDefaults.put(p.CODE_ATTRIBUTE_PFX + "CharacterRangeTable",
                "NH[PHPOHIIH]");
        expectedDefaults.put(p.CODE_ATTRIBUTE_PFX + "CoverageTable",
                "NH[PHHII]");
        expectedDefaults.put(p.DEFLATE_HINT, p.KEEP);
        expectedDefaults.put(p.EFFORT, "5");
        expectedDefaults.put(p.KEEP_FILE_ORDER, p.TRUE);
        expectedDefaults.put(p.MODIFICATION_TIME, p.KEEP);
        expectedDefaults.put(p.SEGMENT_LIMIT, "-1");
        expectedDefaults.put(p.UNKNOWN_ATTRIBUTE, p.PASS);
        Map<String, String> props = p.properties();
        int errors = 0;
        for (String key : expectedDefaults.keySet()) {
            String def = expectedDefaults.get(key);
            String x = props.get(key);
            if (x == null) {
                System.out.println("Error: key not found:" + key);
                errors++;
            } else {
                if (!def.equals(x)) {
                    System.out.println("Error: key " + key
                            + "\n  value expected: " + def
                            + "\n  value obtained: " + x);
                    errors++;
                }
            }
        }
        if (errors > 0) {
            throw new RuntimeException(errors +
                    " error(s) encountered in default properties verification");
        }
    }
}
