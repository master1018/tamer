public class MinstExample {
    private static int called = 0;
    private static void foobar() {
        called++;
    }
    public static void main(String[] args) {
        System.out.println("MinstExample started");
        for(int i=0; i<200; i++) foobar();
        System.out.println("MinstExample ended");
    }
}
