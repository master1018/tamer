public class Test7024475 {
    static int i;
    static int x1;
    static int[] bucket_B;
    static void test(Test7024475 test, int i, int c0, int j, int c1) {
        for (;;) {
            if (c1 > c0) {
                if (c0 > 253) {
                    throw new InternalError("c0 = " + c0);
                }
                int index = c0 * 256 + c1;
                if (index == -1) return;
                i = bucket_B[index];
                if (1 < j - i && test != null)
                    x1 = 0;
                j = i;
                c1--;
            } else {
                c0--;
                if (j <= 0)
                    break;
                c1 = 255;
            }
        }
    }
    public static void main(String args[]) {
        Test7024475 t = new Test7024475();
        bucket_B = new int[256*256];
        for (int i = 1; i < 256*256; i++) {
            bucket_B[i] = 1;
        }
        for (int n = 0; n < 100000; n++) {
            test(t, 2, 85, 1, 134);
        }
    }
}
