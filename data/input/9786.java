public class Cause {
    public static void main(String[] args) throws Exception {
        Exception e = new Exception();
        AssertionError ae = new AssertionError(e);
        if (ae.getCause() != e)
            throw new Exception("Cause not set.");
        ae = new AssertionError("gosh it's late");
        if (ae.getCause() != null)
            throw new Exception("Cause set erroneously: " + ae.getCause());
    }
}
