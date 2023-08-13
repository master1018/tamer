public class ArgumentSanity {
    public static void main(String[]args) throws Exception {
        byte[] data = { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
                        0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f };
        byte[] out = new byte[16];
        MessageDigest dig = null;
        try {
            dig = MessageDigest.getInstance("md5");
            try {
                dig.update(null, 5, 20);
            } catch (IllegalArgumentException e) {
                System.err.println(e);
            }
            try {
                dig.update(data, 5, 20);
            } catch (IllegalArgumentException e) {
                System.err.println(e);
            }
            try {
                dig.digest(null, 5, 20);
            } catch (IllegalArgumentException e) {
                System.err.println(e);
            }
            try {
                dig.digest(out, 5, 20);
            } catch (IllegalArgumentException e) {
                System.err.println(e);
            }
            System.out.println("Test succeeded");
        } catch (Exception e) {
            throw new Exception("Test failed: Wrong exception thrown");
        }
    }
}
