public class JNITest {
    public JNITest() {
    }
    public int test(int intArg, double doubleArg, String stringArg) {
        int[] intArray = { 42, 53, 65, 127 };
        return part1(intArg, doubleArg, stringArg, intArray);
    }
    private native int part1(int intArg, double doubleArg, String stringArg,
        int[] arrayArg);
    private int part2(double doubleArg, int fromArray, String stringArg) {
        int result;
        System.out.println(stringArg + " : " + (float) doubleArg + " : " +
            fromArray);
        result = part3(stringArg);
        return result + 6;
    }
    private static native int part3(String stringArg);
}
