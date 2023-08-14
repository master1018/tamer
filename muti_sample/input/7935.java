public class Test6700047 {
    static byte[] dummy = new byte[256];
    public static void main(String[] args) {
        for (int i = 0; i < 100000; i++) {
            intToLeftPaddedAsciiBytes();
        }
    }
    public static int intToLeftPaddedAsciiBytes() {
        int offset = 40;
        int q;
        int r;
        int         i   = 100;
        int result = 1;
        while (offset > 0) {
            q = (i * 52429);
            r = i;
            offset--;
            i = q;
            if (i == 0) {
                break;
            }
        }
        if (offset > 0) {
            for(int j = 0; j < offset; j++) {
                result++;
                dummy[i] = 0;
            }
        }
        return result;
    }
}
