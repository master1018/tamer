public class Test6985295 {
    public static void main(String[] args) {
        int min = Integer.MAX_VALUE-50000;
        int max = Integer.MAX_VALUE;
        System.out.println("max = " + max);
        long counter = 0;
        int i;
        for(i = min; i <= max; i++) {
            counter++;
            if (counter > 1000000) {
              System.out.println("Passed");
              System.exit(95);
            }
        }
        System.out.println("iteration went " + counter + " times (" + i + ")");
        System.out.println("FAILED");
        System.exit(97);
    }
}
