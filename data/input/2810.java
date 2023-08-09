public class TestCommandAPDU {
    private static final Random random = new Random();
    private static void test(int nc, int ne) throws Exception {
        System.out.println("nc = " + nc + ", ne = " + ne);
        byte[] data = new byte[nc];
        random.nextBytes(data);
        CommandAPDU apdu = new CommandAPDU((byte)0, (byte)0, (byte)0, (byte)0, data, ne);
        if (apdu.getNc() != nc) {
            throw new Exception("dataLength does not match");
        }
        if (Arrays.equals(data, apdu.getData()) == false) {
            throw new Exception("data does not match");
        }
        if (apdu.getNe() != ne) {
            throw new Exception("ne does not match");
        }
        byte[] apduBytes = apdu.getBytes();
        CommandAPDU apdu2 = new CommandAPDU(apduBytes);
        if (Arrays.equals(apduBytes, apdu2.getBytes()) == false) {
            throw new Exception("apduBytes do not match");
        }
        if (apdu2.getNc() != nc) {
            throw new Exception("dataLength does not match");
        }
        if (Arrays.equals(data, apdu2.getData()) == false) {
            throw new Exception("data does not match");
        }
        if (apdu2.getNe() != ne) {
            throw new Exception("ne does not match");
        }
    }
    public static void main(String[] args) throws Exception {
        int[] t = {0, 42, 0x81, 255, 256, 4242, 0x8181, 65535, 65536};
        for (int nc : t) {
            if (nc == 65536) {
                continue;
            }
            for (int ne : t) {
                test(nc, ne);
            }
        }
        System.out.println("OK");
    }
}
