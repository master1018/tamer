public class InvalidArgs {
    public static void main(String[] args) throws Exception {
        byte[] in = new byte[8];
        try {
            IvParameterSpec iv = new IvParameterSpec(in, 0, -2);
            throw new Exception("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
        try {
            IvParameterSpec iv = new IvParameterSpec(in, -2, in.length);
            throw new Exception("expected ArrayIndexOutOfBoundsException");
        } catch (ArrayIndexOutOfBoundsException aiobe) {}
    }
}
