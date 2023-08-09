public class FailsWhenJvmtiVersionDiffers {
    public static void main(String args[]) throws Exception {
        DemoRun demo;
        demo = new DemoRun("versionCheck", ""  );
        demo.runit(args[0]);
        if (demo.output_contains("ERROR")) {
            System.out.println(
             "NOTE: The jmvti.h file doesn't match the JVMTI in the VM.\n"
            +"      This may or may not be a serious issue.\n"
            +"      Check the jtr file for details.\n"
            +"      Call your local serviceability representative for help."
            );
            throw new RuntimeException("Test failed - ERROR seen in output");
        }
        System.out.println("Test passed - cleanly terminated");
    }
}
