public class IsModifiableClassApp {
    public static void main(String args[]) throws Exception {
        (new IsModifiableClassApp()).run(args, System.out);
    }
    public void run(String args[], PrintStream out) throws Exception {
        if (!IsModifiableClassAgent.completed) {
            throw new Exception("ERROR: IsModifiableClassAgent did not complete.");
        }
        if (IsModifiableClassAgent.fail) {
            throw new Exception("ERROR: IsModifiableClass failed.");
        } else {
            out.println("IsModifiableClass succeeded.");
        }
    }
}
