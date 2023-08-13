final class CounterMode extends FeedbackCipher {
    private final byte[] counter;
    private final byte[] encryptedCounter;
    private int used;
    private byte[] counterSave = null;
    private byte[] encryptedCounterSave = null;
    private int usedSave = 0;
    CounterMode(SymmetricCipher embeddedCipher) {
        super(embeddedCipher);
        counter = new byte[blockSize];
        encryptedCounter = new byte[blockSize];
    }
    String getFeedback() {
        return "CTR";
    }
    void reset() {
        System.arraycopy(iv, 0, counter, 0, blockSize);
        used = blockSize;
    }
    void save() {
        if (counterSave == null) {
            counterSave = new byte[blockSize];
            encryptedCounterSave = new byte[blockSize];
        }
        System.arraycopy(counter, 0, counterSave, 0, blockSize);
        System.arraycopy(encryptedCounter, 0, encryptedCounterSave, 0,
            blockSize);
        usedSave = used;
    }
    void restore() {
        System.arraycopy(counterSave, 0, counter, 0, blockSize);
        System.arraycopy(encryptedCounterSave, 0, encryptedCounter, 0,
            blockSize);
        used = usedSave;
    }
    void init(boolean decrypting, String algorithm, byte[] key, byte[] iv)
            throws InvalidKeyException {
        if ((key == null) || (iv == null) || (iv.length != blockSize)) {
            throw new InvalidKeyException("Internal error");
        }
        this.iv = iv;
        reset();
        embeddedCipher.init(false, algorithm, key);
    }
    void encrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        crypt(in, inOff, len, out, outOff);
    }
    void decrypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        crypt(in, inOff, len, out, outOff);
    }
    private static void increment(byte[] b) {
        int n = b.length - 1;
        while ((n >= 0) && (++b[n] == 0)) {
            n--;
        }
    }
    private void crypt(byte[] in, int inOff, int len, byte[] out, int outOff) {
        while (len-- > 0) {
            if (used >= blockSize) {
                embeddedCipher.encryptBlock(counter, 0, encryptedCounter, 0);
                increment(counter);
                used = 0;
            }
            out[outOff++] = (byte)(in[inOff++] ^ encryptedCounter[used++]);
        }
    }
}
