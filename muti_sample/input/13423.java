public class OptionsTest {
    public static void main(String args[]) throws Exception {
        DemoRun hprof;
        List<String> options = new LinkedList<String>();
        options.add("cpu=samples,depth=0");
        options.add("cpu=times,depth=0");
        options.add("cpu=old,depth=0");
        options.add("depth=0");
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
