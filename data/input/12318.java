public class RetransformApp {
    public static void main(String args[]) throws Exception {
        (new RetransformApp()).run(args, System.out);
    }
    int foo(int x) {
        return x * x;
    }
    public void run(String args[], PrintStream out) throws Exception {
        out.println("start");
        for (int i = 0; i < 4; i++) {
            if (foo(3) != 9) {
                throw new Exception("ERROR: unexpected application behavior");
            }
        }
        out.println("undo");
        RetransformAgent.undo();
        for (int i = 0; i < 4; i++) {
            if (foo(3) != 9) {
                throw new Exception("ERROR: unexpected application behavior");
            }
        }
        out.println("end");
        if (RetransformAgent.succeeded()) {
            out.println("Instrumentation succeeded.");
        } else {
            throw new Exception("ERROR: Instrumentation failed.");
        }
    }
}
