public class GCMParameterSpecTest {
    private static byte[] bytes = new byte[] {
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
        0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f };
    private static int failed = 0;
    public static void main(String[] args) throws Exception {
        newGCMParameterSpecFail(-1, bytes);
        newGCMParameterSpecFail(128, null);
        newGCMParameterSpecPass(128, bytes);
        newGCMParameterSpecFail(-1, bytes, 2, 4);
        newGCMParameterSpecFail(128, null, 2, 4);
        newGCMParameterSpecFail(128, bytes, -2, 4);
        newGCMParameterSpecFail(128, bytes, 2, -4);
        newGCMParameterSpecFail(128, bytes, 2, 15);  
        newGCMParameterSpecPass(128, bytes, 2, 14);  
        newGCMParameterSpecPass(96, bytes, 4, 4);
        newGCMParameterSpecPass(96, bytes, 0, 0);
        try {
            new AEADBadTagException();
            new AEADBadTagException("Bad Tag Seen");
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }
        if (failed != 0) {
            throw new Exception("Test(s) failed");
        }
    }
    private static void newGCMParameterSpecPass(
            int tLen, byte[] src) {
        try {
            GCMParameterSpec gcmps = new GCMParameterSpec(tLen, src);
            if (gcmps.getTLen() != tLen) {
                throw new Exception("tLen's not equal");
            }
            if (!Arrays.equals(gcmps.getIV(), src)) {
                throw new Exception("IV's not equal");
            }
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }
    }
    private static void newGCMParameterSpecFail(
            int tLen, byte[] src) {
        try {
            new GCMParameterSpec(tLen, src);
            new Exception("Didn't Fail as Expected").printStackTrace();
            failed++;
        } catch (IllegalArgumentException e) {
        }
    }
    private static void newGCMParameterSpecPass(
            int tLen, byte[] src, int offset, int len) {
        try {
            GCMParameterSpec gcmps =
                new GCMParameterSpec(tLen, src, offset, len);
            if (gcmps.getTLen() != tLen) {
                throw new Exception("tLen's not equal");
            }
            if (!Arrays.equals(gcmps.getIV(),
                    Arrays.copyOfRange(src, offset, offset + len))) {
                System.out.println(offset + " " + len);
                System.out.println(Arrays.copyOfRange(src, offset, len)[0]);
                throw new Exception("IV's not equal");
            }
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }
    }
    private static void newGCMParameterSpecFail(
            int tLen, byte[] src, int offset, int len) {
        try {
            new GCMParameterSpec(tLen, src, offset, len);
            new Exception("Didn't Fail as Expected").printStackTrace();
            failed++;
        } catch (IllegalArgumentException e) {
        }
    }
}
