public class GetCharsOverLength {
   public static void main (String argv[]) {
    StringBuffer sb = new StringBuffer("sample string buffer");
    char dst[] = new char[30];
    boolean failed = false;
    int a[][] = {
                  {0, 0, dst.length + 1},
                  {0, 0, dst.length + 2},
                  {0, 0, dst.length + 20},
                  {5, 5, dst.length + 1},
                  {5, 5, dst.length + 2},
                  {5, 5, dst.length + 20}
    };
    for (int i = 0; i < a.length; i++) {
        try {
            sb.getChars(a[i][0], a[i][1], dst, a[i][2]);
            throw new RuntimeException("Bounds test failed");
        } catch (IndexOutOfBoundsException iobe) {
        }
    }
  }
}
