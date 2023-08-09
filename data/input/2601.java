public class ExpandAndEncode {
    private static String tests[] = {
        "${env.test1}/test.jar",
        "file:${env.test2}/test.jar",
        "file:/foo/${env.test3}",
        "file:/foo/${env.test4}"
    };
    private static String answers[] = {
        "http:
        "file:/my%20home/test.jar",
        "file:/foo/bar%23foobar",
        "file:/foo/goofy:bar%23foobar"
    };
    private static boolean checkAnswer(String result, int i) {
        if (result.equals(answers[i])) {
            return true;
        } else {
            System.out.println("Test#" + i + ": expected " + answers[i]);
            System.out.println("Test#" + i + ": got " + result);
            return false;
        }
    }
    public static void main(String[] args) throws Exception {
        boolean status = true;
        System.setProperty("env.test1", "http:
        System.setProperty("env.test2", File.separator + "my home");
        System.setProperty("env.test3", "bar#foobar");
        System.setProperty("env.test4", "goofy:bar#foobar");
        for (int i=0; i < tests.length; i++) {
            String result = PropertyExpander.expand(tests[i], true);
            status &= checkAnswer(result, i);
        }
        if (status) {
            System.out.println("PASSED");
        } else {
            throw new Exception("FAILED");
        }
    }
}
