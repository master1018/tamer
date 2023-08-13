public class HatHeapDump1Test {
    public static void main(String args[]) throws Exception {
        HatRun run;
        run = new HatRun("heap=dump", "");
        run.runit(args[0]);
        if (run.output_contains("ERROR")) {
            throw new RuntimeException("Test failed - ERROR seen in output");
        }
        System.out.println("Test passed - cleanly terminated");
    }
}
