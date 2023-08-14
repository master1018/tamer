public class MacThread extends TestThread {
    MacThread(String[] names) {
        super(names);
    }
    @Override
    public void test() throws Exception {
        int size = 256;
        byte[] src1 = new byte[size];
        byte[] src2 = new byte[size];
        byte[] src3 = new byte[size];
        int i;
        for (i = 0; i < size; i++) {
            src1[i] = (byte)i;
            src2[i] = (byte)i;
            src3[i] = (byte)(size - i - 1);
        }
        Mac m = Mac.getInstance(algName);
        byte[] b = {(byte)0, (byte)0, (byte)0, (byte)0, (byte)0};
        SecretKeySpec sks = new SecretKeySpec(b, "SHA1");
        m.init(sks);
        byte[] res = m.doFinal(src1);
        String sign1 = new String(res);
        m.init(sks);
        res = m.doFinal(src2);
        String sign2 = new String(res);
        m.init(sks);
        res = m.doFinal(src3);
        String sign3 = new String(res);
        if (sign1.compareTo(sign2) != 0 || sign1.compareTo(sign3) == 0 ||
                sign2.compareTo(sign3) == 0) {
            throw new Exception ("Signature is not correct for algorithm " + algName);
        }
    }
}
