public class Test6968348 {
    static Unsafe unsafe;
    static final long[] buffer = new long[4096];
    static int array_long_base_offset;
    public static void main(String[] args) throws Exception {
        Class c = Test6968348.class.getClassLoader().loadClass("sun.misc.Unsafe");
        Field f = c.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        unsafe = (Unsafe)f.get(c);
        array_long_base_offset = unsafe.arrayBaseOffset(long[].class);
        for (int n = 0; n < 100000; n++) {
            test();
        }
    }
    public static void test() {
        for (long i = array_long_base_offset; i < 4096; i += 8) {
            unsafe.putLong(buffer, i, Long.reverseBytes(i));
        }
    }
}
