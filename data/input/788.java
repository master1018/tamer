public class Test6959129 {
  public static void main(String[] args) {
    long start  = System.currentTimeMillis();
    int min = Integer.MAX_VALUE-30000;
    int max = Integer.MAX_VALUE;
    long maxmoves = 0;
    try {
      maxmoves = maxMoves(min, max);
    } catch (AssertionError e) {
      System.out.println("Passed");
      System.exit(95);
    }
    System.out.println("maxMove:" + maxmoves);
    System.out.println("FAILED");
    System.exit(97);
  }
  public static long hailstoneLengthImp(long n) {
    long moves = 0;
    while (n != 1) {
      assert n > 1;
      if (isEven(n)) {
        n = n / 2;
      } else {
        n = 3 * n + 1;
      }
      ++moves;
    }
    return moves;
  }
  private static boolean isEven(long n) {
    return n % 2 == 0;
  }
  public static long maxMoves(int min, int max) {
    long maxmoves = 0;
    for (int n = min; n <= max; n++) {
      if ((n & 1023) == 0) System.out.println(n);
      long moves = hailstoneLengthImp(n);
      if (moves > maxmoves) {
        maxmoves = moves;
      }
    }
    return maxmoves;
  }
}
