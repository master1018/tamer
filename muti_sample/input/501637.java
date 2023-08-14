public class DxUtil {
    private static boolean isDalvik = false;
    static {
    }
    public static void checkVerifyException(Throwable t) {
        if (t instanceof VerifyError || t instanceof ClassNotFoundException || t instanceof LinkageError) {
                if (t instanceof VerifyError) {
                    if (((VerifyError)t).getMessage().contains("Main_")) {
                        System.out.print("verify failed on Main_");
                    }
                }
        } else {
            throw new RuntimeException("Verify error expected", t);
        }
    }
}
