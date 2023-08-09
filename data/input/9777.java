public class FinExit {
    boolean finalized = false;
    public void finalize() {
        if (finalized) {
            System.out.println("2");
        } else {
            finalized = true;
            System.out.println("1");
            System.exit(0);
        }
    }
    public static void main(String[] args) throws Exception {
        System.runFinalizersOnExit(true);
        Object o = new FinExit();
        System.exit(0);
    }
}
