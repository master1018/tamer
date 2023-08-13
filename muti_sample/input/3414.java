public class NegativeNonce {
    public static void main(String[] args) throws Exception {
        for (int i=0; i<100; i++) {
            if (Nonce.value() < 0) {
                throw new Exception("Nonce value is negative. Wrong!");
            }
        }
    }
}
