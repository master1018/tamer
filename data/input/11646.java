public class NullKey {
    public static void main(String[] args) throws Exception {
        try {
            DESKeySpec desSpec = new DESKeySpec(null);
            throw new Exception("expected NullPointerException");
        } catch (NullPointerException npe) {}
        try {
            DESKeySpec desSpec = new DESKeySpec(null, 0);
            throw new Exception("expected NullPointerException");
        } catch (NullPointerException npe) {}
        try {
            boolean parityAdjusted = DESKeySpec.isParityAdjusted(null, 0);
            throw new Exception("expected InvalidKeyException");
        } catch (InvalidKeyException ike) {}
        try {
            boolean weak = DESKeySpec.isWeak(null, 0);
            throw new Exception("expected InvalidKeyException");
        } catch (InvalidKeyException ike) {}
    }
}
