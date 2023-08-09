public class Test6930043 {
    int[] a;
    int idx;
    public int loop_back(int i, int i_0_) {
        int i_1_ = 0;
        int[] is = a;
        if (is == null) return 0;
        for (int i_2_ = i; i_2_ >= i_0_; i_2_--)
            i_1_ += is[idx = i_2_];
        return i_1_;
    }
    public int loop_forw(int start, int end) {
        int result = 0;
        int[] is = a;
        if (is == null) return 0;
        for (int index = start; index < end; index++)
            result += is[index];
        return result;
    }
    public static void main(String[] strings) {
        Test6930043 var_Test6930043 = new Test6930043();
        var_Test6930043.a = new int[1000000];
        var_Test6930043.loop_forw(10, 999990);
        var_Test6930043.loop_forw(10, 999990);
        for (int i = 0; i < 3; i++) {
            try {
                if (var_Test6930043.loop_forw(-1, 999990) != 0) throw new InternalError();
            } catch (ArrayIndexOutOfBoundsException e) { }
        }
        var_Test6930043.loop_back(999990, 10);
        var_Test6930043.loop_back(999990, 10);
        for (int i = 0; i < 3; i++) {
            try {
                if (var_Test6930043.loop_back(999990, -1) != 0) throw new InternalError();
            } catch (ArrayIndexOutOfBoundsException e) { }
        }
    }
}
