public class ExtremeShiftingTests {
    public static void main(String... args) {
        try {
            ONE.shiftLeft(Integer.MIN_VALUE);
            throw new RuntimeException("Should not reach here.");
        } catch (ArithmeticException ae) {
            ; 
        }
        try {
            ONE.shiftRight(Integer.MIN_VALUE);
            throw new RuntimeException("Should not reach here.");
        } catch (ArithmeticException ae) {
            ; 
        }
    }
}
