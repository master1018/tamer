public class BigHello {
    private final static int NLOOPS = 20000;
    private static Object garbage[];
    public static void main(String args[]) {
        long count = 0;
        System.out.println("Big Hello start");
        for(int i=1; i<=NLOOPS; i++) {
            count += i;
            garbage = new Object[i];
            garbage[0] = new Object();
        }
        System.out.println("Allocated " + count +
                           " array elements, and " + NLOOPS +
                           " arrays and Objects.");
        System.out.println("Big Hello end");
    }
}
