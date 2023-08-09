public class NativeOrder {
    public static void main(String[] args) throws Exception {
        ByteOrder bo = ByteOrder.nativeOrder();
        System.err.println(bo);
        String arch = System.getProperty("os.arch");
        if (((arch.equals("i386") && (bo != ByteOrder.LITTLE_ENDIAN))) ||
            ((arch.equals("sparc") && (bo != ByteOrder.BIG_ENDIAN)))) {
            throw new Exception("Wrong byte order");
        }
    }
}
