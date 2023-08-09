public class NullIV {
    public static void main(String[] args) throws Exception {
        try {
            IvParameterSpec ivParams = new IvParameterSpec(null);
            throw new Exception("expected NullPointerException");
        } catch (NullPointerException npe) {}
        try {
            IvParameterSpec ivParams = new IvParameterSpec(null, 0, 0);
            throw new Exception("expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {}
    }
}
