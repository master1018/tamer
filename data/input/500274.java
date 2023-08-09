public class SHA1PRNG_SecureRandomImpl extends SecureRandomSpi implements
        Serializable, SHA1_Data {
    private static final long serialVersionUID = 283736797212159675L;
    private static final int[] END_FLAGS = { 0x80000000, 0x800000, 0x8000, 0x80 };
    private static final int[] RIGHT1 = { 0, 40, 48, 56 };
    private static final int[] RIGHT2 = { 0, 8, 16, 24 };
    private static final int[] LEFT = { 0, 24, 16, 8 };
    private static final int[] MASK = { 0xFFFFFFFF, 0x00FFFFFF, 0x0000FFFF,
            0x000000FF };
    private static final int HASHBYTES_TO_USE = 20;
    private static final int FRAME_LENGTH = 16;
    private static final int COUNTER_BASE = 0;
    private static final int HASHCOPY_OFFSET = 0;
    private static final int EXTRAFRAME_OFFSET = 5;
    private static final int FRAME_OFFSET = 21;
    private static final int MAX_BYTES = 48;
    private static final int UNDEFINED = 0;
    private static final int SET_SEED = 1;
    private static final int NEXT_BYTES = 2;
    private static SHA1PRNG_SecureRandomImpl myRandom;
    private transient int seed[];
    private transient long seedLength;
    private transient int copies[];
    private transient byte nextBytes[];
    private transient int nextBIndex;
    private transient long counter;
    private transient int state;
    public SHA1PRNG_SecureRandomImpl() {
        seed = new int[HASH_OFFSET + EXTRAFRAME_OFFSET];
        seed[HASH_OFFSET] = H0;
        seed[HASH_OFFSET + 1] = H1;
        seed[HASH_OFFSET + 2] = H2;
        seed[HASH_OFFSET + 3] = H3;
        seed[HASH_OFFSET + 4] = H4;
        seedLength = 0;
        copies = new int[2 * FRAME_LENGTH + EXTRAFRAME_OFFSET];
        nextBytes = new byte[DIGEST_LENGTH];
        nextBIndex = HASHBYTES_TO_USE;
        counter = COUNTER_BASE;
        state = UNDEFINED;
    }
    private void updateSeed(byte[] bytes) {
        SHA1Impl.updateHash(seed, bytes, 0, bytes.length - 1);
        seedLength += bytes.length;
    }
    protected void engineSetSeed(byte[] seed) {
        if (seed == null) {
            throw new NullPointerException(
                    Messages.getString("security.83", "seed")); 
        }
        if (state == NEXT_BYTES) { 
            System.arraycopy(copies, HASHCOPY_OFFSET, this.seed, HASH_OFFSET,
                    EXTRAFRAME_OFFSET);
        }
        state = SET_SEED;
        if (seed.length != 0) {
            updateSeed(seed);
        }
    }
    protected byte[] engineGenerateSeed(int numBytes) {
        byte[] myBytes; 
        if (numBytes < 0) {
            throw new NegativeArraySizeException(Messages.getString("security.171", numBytes)); 
        }
        if (numBytes == 0) {
            return new byte[0];
        }
        if (myRandom == null) {
            myRandom = new SHA1PRNG_SecureRandomImpl();
            myRandom.engineSetSeed(RandomBitsSupplier
                    .getRandomBits(DIGEST_LENGTH));
        }
        myBytes = new byte[numBytes];
        myRandom.engineNextBytes(myBytes);
        return myBytes;
    }
    protected void engineNextBytes(byte[] bytes) {
        int i, n;
        long bits; 
        int nextByteToReturn; 
        int lastWord; 
        final int extrabytes = 7;
        if (bytes == null) {
            throw new NullPointerException(
                    Messages.getString("security.83", "bytes")); 
        }
        lastWord = seed[BYTES_OFFSET] == 0 ? 0
                : (seed[BYTES_OFFSET] + extrabytes) >> 3 - 1;
        if (state == UNDEFINED) {
            updateSeed(RandomBitsSupplier.getRandomBits(DIGEST_LENGTH));
            nextBIndex = HASHBYTES_TO_USE;
        } else if (state == SET_SEED) {
            System.arraycopy(seed, HASH_OFFSET, copies, HASHCOPY_OFFSET,
                    EXTRAFRAME_OFFSET);
            for (i = lastWord + 3; i < FRAME_LENGTH + 2; i++) {
                seed[i] = 0;
            }
            bits = seedLength << 3 + 64; 
            if (seed[BYTES_OFFSET] < MAX_BYTES) {
                seed[14] = (int) (bits >>> 32);
                seed[15] = (int) (bits & 0xFFFFFFFF);
            } else {
                copies[EXTRAFRAME_OFFSET + 14] = (int) (bits >>> 32);
                copies[EXTRAFRAME_OFFSET + 15] = (int) (bits & 0xFFFFFFFF);
            }
            nextBIndex = HASHBYTES_TO_USE; 
        }
        state = NEXT_BYTES;
        if (bytes.length == 0) {
            return;
        }
        nextByteToReturn = 0;
        n = (HASHBYTES_TO_USE - nextBIndex) < (bytes.length - nextByteToReturn) ? HASHBYTES_TO_USE
                - nextBIndex
                : bytes.length - nextByteToReturn;
        if (n > 0) {
            System.arraycopy(nextBytes, nextBIndex, bytes, nextByteToReturn, n);
            nextBIndex += n;
            nextByteToReturn += n;
        }
        if (nextByteToReturn >= bytes.length) {
            return; 
        }
        n = seed[BYTES_OFFSET] & 0x03;
        for (;;) {
            if (n == 0) {
                seed[lastWord] = (int) (counter >>> 32);
                seed[lastWord + 1] = (int) (counter & 0xFFFFFFFF);
                seed[lastWord + 2] = END_FLAGS[0];
            } else {
                seed[lastWord] |= (int) ((counter >>> RIGHT1[n]) & MASK[n]);
                seed[lastWord + 1] = (int) ((counter >>> RIGHT2[n]) & 0xFFFFFFFF);
                seed[lastWord + 2] = (int) ((counter << LEFT[n]) | END_FLAGS[n]);
            }
            if (seed[BYTES_OFFSET] > MAX_BYTES) {
                copies[EXTRAFRAME_OFFSET] = seed[FRAME_LENGTH];
                copies[EXTRAFRAME_OFFSET + 1] = seed[FRAME_LENGTH + 1];
            }
            SHA1Impl.computeHash(seed);
            if (seed[BYTES_OFFSET] > MAX_BYTES) {
                System.arraycopy(seed, 0, copies, FRAME_OFFSET, FRAME_LENGTH);
                System.arraycopy(copies, EXTRAFRAME_OFFSET, seed, 0,
                        FRAME_LENGTH);
                SHA1Impl.computeHash(seed);
                System.arraycopy(copies, FRAME_OFFSET, seed, 0, FRAME_LENGTH);
            }
            counter++;
            int j = 0;
            for (i = 0; i < EXTRAFRAME_OFFSET; i++) {
                int k = seed[HASH_OFFSET + i];
                nextBytes[j] = (byte) (k >>> 24); 
                nextBytes[j + 1] = (byte) (k >>> 16); 
                nextBytes[j + 2] = (byte) (k >>> 8); 
                nextBytes[j + 3] = (byte) (k); 
                j += 4;
            }
            nextBIndex = 0;
            j = HASHBYTES_TO_USE < (bytes.length - nextByteToReturn) ? HASHBYTES_TO_USE
                    : bytes.length - nextByteToReturn;
            if (j > 0) {
                System.arraycopy(nextBytes, 0, bytes, nextByteToReturn, j);
                nextByteToReturn += j;
                nextBIndex += j;
            }
            if (nextByteToReturn >= bytes.length) {
                break;
            }
        }
    }
    private void writeObject(ObjectOutputStream oos) throws IOException {
        int[] intData = null;
        final int only_hash = EXTRAFRAME_OFFSET;
        final int hashes_and_frame = EXTRAFRAME_OFFSET * 2 + FRAME_LENGTH;
        final int hashes_and_frame_extra = EXTRAFRAME_OFFSET * 2 + FRAME_LENGTH
                * 2;
        oos.writeLong(seedLength);
        oos.writeLong(counter);
        oos.writeInt(state);
        oos.writeInt(seed[BYTES_OFFSET]);
        int nRemaining = (seed[BYTES_OFFSET] + 3) >> 2; 
        if (state != NEXT_BYTES) {
            intData = new int[only_hash + nRemaining];
            System.arraycopy(seed, 0, intData, 0, nRemaining);
            System.arraycopy(seed, HASH_OFFSET, intData, nRemaining,
                    EXTRAFRAME_OFFSET);
        } else {
            int offset = 0;
            if (seed[BYTES_OFFSET] < MAX_BYTES) { 
                intData = new int[hashes_and_frame + nRemaining];
            } else { 
                intData = new int[hashes_and_frame_extra + nRemaining];
                intData[offset] = seed[FRAME_LENGTH];
                intData[offset + 1] = seed[FRAME_LENGTH + 1];
                intData[offset + 2] = seed[FRAME_LENGTH + 14];
                intData[offset + 3] = seed[FRAME_LENGTH + 15];
                offset += 4;
            }
            System.arraycopy(seed, 0, intData, offset, FRAME_LENGTH);
            offset += FRAME_LENGTH;
            System.arraycopy(copies, FRAME_LENGTH + EXTRAFRAME_OFFSET, intData,
                    offset, nRemaining);
            offset += nRemaining;
            System.arraycopy(copies, 0, intData, offset, EXTRAFRAME_OFFSET);
            offset += EXTRAFRAME_OFFSET;
            System.arraycopy(seed, HASH_OFFSET, intData, offset,
                    EXTRAFRAME_OFFSET);
        }
        for (int i = 0; i < intData.length; i++) {
            oos.writeInt(intData[i]);
        }
        oos.writeInt(nextBIndex);
        oos.write(nextBytes, nextBIndex, HASHBYTES_TO_USE - nextBIndex);
    }
    private void readObject(ObjectInputStream ois) throws IOException,
            ClassNotFoundException {
        seed = new int[HASH_OFFSET + EXTRAFRAME_OFFSET];
        copies = new int[2 * FRAME_LENGTH + EXTRAFRAME_OFFSET];
        nextBytes = new byte[DIGEST_LENGTH];
        seedLength = ois.readLong();
        counter = ois.readLong();
        state = ois.readInt();
        seed[BYTES_OFFSET] = ois.readInt();
        int nRemaining = (seed[BYTES_OFFSET] + 3) >> 2; 
        if (state != NEXT_BYTES) {
            for (int i = 0; i < nRemaining; i++) {
                seed[i] = ois.readInt();
            }
            for (int i = 0; i < EXTRAFRAME_OFFSET; i++) {
                seed[HASH_OFFSET + i] = ois.readInt();
            }
        } else {
            if (seed[BYTES_OFFSET] >= MAX_BYTES) {
                seed[FRAME_LENGTH] = ois.readInt();
                seed[FRAME_LENGTH + 1] = ois.readInt();
                seed[FRAME_LENGTH + 14] = ois.readInt();
                seed[FRAME_LENGTH + 15] = ois.readInt();
            }
            for (int i = 0; i < FRAME_LENGTH; i++) {
                seed[i] = ois.readInt();
            }
            for (int i = 0; i < nRemaining; i++) {
                copies[FRAME_LENGTH + EXTRAFRAME_OFFSET + i] = ois.readInt();
            }
            for (int i = 0; i < EXTRAFRAME_OFFSET; i++) {
                copies[i] = ois.readInt();
            }
            for (int i = 0; i < EXTRAFRAME_OFFSET; i++) {
                seed[HASH_OFFSET + i] = ois.readInt();
            }
        }
        nextBIndex = ois.readInt();
        ois.read(nextBytes, nextBIndex, HASHBYTES_TO_USE - nextBIndex);
    }
}
