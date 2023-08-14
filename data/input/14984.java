public class GCMAPI {
    private static byte[] bytes = new byte[] {
        0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17,
        0x18, 0x19, 0x1a, 0x1b, 0x1c, 0x1d, 0x1e, 0x1f };
    private static int failed = 0;
    private static Cipher c;
    public static void main(String[] args) throws Exception {
        c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"));
        updateAADFail((byte[]) null);
        updateAADPass(bytes);
        updateAADFail(null, 2, 4);
        updateAADFail(bytes, -2, 4);
        updateAADFail(bytes, 2, -4);
        updateAADFail(bytes, 2, 15);  
        updateAADPass(bytes, 2, 14);  
        updateAADPass(bytes, 4, 4);
        updateAADPass(bytes, 0, 0);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        updateAADFail((ByteBuffer) null);
        updateAADPass(bb);
        if (failed != 0) {
            throw new Exception("Test(s) failed");
        }
    }
    private static void updateAADPass(byte[] src) {
        try {
            c.updateAAD(src);
        } catch (UnsupportedOperationException e) {
        }catch (Exception e) {
            e.printStackTrace();
            failed++;
        }
    }
    private static void updateAADFail(byte[] src) {
        try {
            c.updateAAD(src);
            new Exception("Didn't Fail as Expected").printStackTrace();
            failed++;
        } catch (IllegalArgumentException e) {
        }
    }
    private static void updateAADPass(byte[] src, int offset, int len) {
        try {
            c.updateAAD(src, offset, len);
        } catch (UnsupportedOperationException e) {
        } catch (Exception e) {
            e.printStackTrace();
            failed++;
        }
    }
    private static void updateAADFail(byte[] src, int offset, int len) {
        try {
            c.updateAAD(src, offset, len);
            new Exception("Didn't Fail as Expected").printStackTrace();
            failed++;
        } catch (IllegalArgumentException e) {
        }
    }
    private static void updateAADPass(ByteBuffer src) {
        try {
            c.updateAAD(src);
        } catch (UnsupportedOperationException e) {
        }catch (Exception e) {
            e.printStackTrace();
            failed++;
        }
    }
    private static void updateAADFail(ByteBuffer src) {
        try {
            c.updateAAD(src);
            new Exception("Didn't Fail as Expected").printStackTrace();
            failed++;
        } catch (IllegalArgumentException e) {
        }
    }
}
