public class ExitFinalizersAndJIT {
    public static void main(String[] args) throws Exception {
        System.runFinalizersOnExit(true);
    }
}
