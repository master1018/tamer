public class CpuSamplesTest {
    public static void main(String args[]) throws Exception {
        DemoRun hprof;
        hprof = new DemoRun("hprof", "cpu=samples");
        hprof.runit(args[0]);
        if (hprof.output_contains("ERROR")) {
            throw new RuntimeException("Test failed - ERROR seen in output");
        }
        System.out.println("Test passed - cleanly terminated");
    }
}
