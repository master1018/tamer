public class StackMapTableTest {
    public static void main(String args[]) throws Exception {
        DemoRun hprof;
        List<String> options = new LinkedList<String>();
        options.add("cpu=samples");
        options.add("cpu=times");
        options.add("heap=sites");
        options.add("");
        for(String option: options) {
            hprof = new DemoRun("hprof", option);
            hprof.runit(args[0]);
            if (hprof.output_contains("ERROR")) {
                throw new RuntimeException("Test failed with " + option
                                           + " - ERROR seen in output");
            }
        }
        System.out.println("Test passed - cleanly terminated");
    }
}
