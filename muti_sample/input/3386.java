public class Test7048332 {
  static int capacity = 2;
  static int first = 1;
  static int last = 2;
  static int test(int i1, int i2, int i3, int i4, int i5, int i6) {
    final int result;
    if (last >= first) {
      result = last - first;
    } else {
      result = last - first + capacity;
    }
    return result;
  }
  public static void main(String [] args) {
    for (int i = 0; i < 11000; i++) {
      last = (i & 1) << 1; 
      int k = test(1, 2, 3, 4, 5, 6);
      if (k != 1) {
        System.out.println("FAILED: " + k + " != 1");
        System.exit(97);
      }
    }
  }
}
