public class HeapViewerTest {
    public static void main(String args[]) throws Exception {
        DemoRun demo;
        demo = new DemoRun("heapViewer", ""  );
        demo.runit(args[0]);
        if (demo.output_contains("ERROR")) {
            throw new RuntimeException("Test failed - ERROR seen in output");
        }
        System.out.println("Test passed - cleanly terminated");
    }
}
