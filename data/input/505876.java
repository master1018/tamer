public class SHAOutputStream extends OutputStream implements Cloneable {
    private static final int K0_19 = 0x5a827999;
    private static final int K20_39 = 0x6ed9eba1;
    private static final int K40_59 = 0x8f1bbcdc;
    private static final int K60_79 = 0xca62c1d6;
    private static final int H0 = 0x67452301;
    private static final int H1 = 0xefcdab89;
    private static final int H2 = 0x98badcfe;
    private static final int H3 = 0x10325476;
    private static final int H4 = 0xc3d2e1f0;
    private static final int HConstantsSize = 5;
    private static final int HashSizeInBytes = 20;
    private static final int BlockSizeInBytes = 16 * 4;
    private static final int WArraySize = 80;
    private int[] HConstants;
    private int[] WArray;
    private byte[] MArray;
    private long bytesProcessed;
    private int bytesToProcess;
    private byte[] oneByte = new byte[1];
    public SHAOutputStream() {
        super();
        initialize();
        reset();
    }
    public SHAOutputStream(byte[] state) {
        this();
        if (state.length < HashSizeInBytes) {
            throw new IllegalArgumentException();
        }
        for (int i = 0; i < 4; i++) {
            HConstants[i] = 0;
            for (int j = 0; j < 4; j++) {
                HConstants[i] += (state[4 * i + j] & 0xFF) << 8 * (3 - j);
            }
        }
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        SHAOutputStream result = (SHAOutputStream) super.clone();
        result.HConstants = this.HConstants.clone();
        result.WArray = this.WArray.clone();
        result.MArray = this.MArray.clone();
        result.oneByte = this.oneByte.clone();
        return result;
    }
    private void copyToInternalBuffer(byte[] buffer, int off, int len) {
        int index;
        index = off;
        for (int i = bytesToProcess; i < bytesToProcess + len; i++) {
            MArray[i] = buffer[index];
            index++;
        }
        bytesToProcess = bytesToProcess + len;
    }
    public int[] getHash() {
        this.padBuffer();
        this.processBuffer();
        int[] result = HConstants.clone();
        reset();
        return result;
    }
    public byte[] getHashAsBytes() {
        byte[] hash = new byte[HashSizeInBytes];
        this.padBuffer();
        this.processBuffer();
        for (int i = 0; i < (HashSizeInBytes / 4); ++i) {
            hash[i * 4] = (byte) (HConstants[i] >>> 24 & 0xff);
            hash[i * 4 + 1] = (byte) (HConstants[i] >>> 16 & 0xff);
            hash[i * 4 + 2] = (byte) (HConstants[i] >>> 8 & 0xff);
            hash[i * 4 + 3] = (byte) (HConstants[i] & 0xff);
        }
        reset();
        return hash;
    }
    public byte[] getHashAsBytes(boolean pad) {
        byte[] hash = new byte[HashSizeInBytes];
        if (pad) {
            this.padBuffer();
            this.processBuffer();
        }
        for (int i = 0; i < (HashSizeInBytes / 4); i++) {
            hash[i * 4] = (byte) (HConstants[i] >>> 24 & 0xff);
            hash[i * 4 + 1] = (byte) (HConstants[i] >>> 16 & 0xff);
            hash[i * 4 + 2] = (byte) (HConstants[i] >>> 8 & 0xff);
            hash[i * 4 + 3] = (byte) (HConstants[i] & 0xff);
        }
        reset();
        return hash;
    }
    private void initialize() {
        HConstants = new int[HConstantsSize];
        MArray = new byte[BlockSizeInBytes];
        WArray = new int[WArraySize];
    }
    private void padBuffer() {
        long lengthInBits;
        MArray[bytesToProcess] = (byte) 0x80;
        for (int i = bytesToProcess + 1; i < BlockSizeInBytes; i++) {
            MArray[i] = (byte) 0;
        }
        lengthInBits = (bytesToProcess + bytesProcessed) * 8;
        if ((bytesToProcess + 9) > BlockSizeInBytes) {
            this.processBuffer();
            for (int i = 0; i < BlockSizeInBytes; i++) {
                MArray[i] = (byte) 0;
            }
        }
        for (int i = 1; i < 9; i++) {
            MArray[BlockSizeInBytes - i] = (byte) (lengthInBits & 0xff);
            lengthInBits = lengthInBits >>> 8;
        }
    }
    private void processBuffer() {
        int A; 
        int B; 
        int C; 
        int D; 
        int E; 
        int temp; 
        int t; 
        for (t = 0; t <= 15; t++) { 
            WArray[t] = (MArray[4 * t] & 0xff) << 24
                    | ((MArray[4 * t + 1] & 0xff) << 16)
                    | ((MArray[4 * t + 2] & 0xff) << 8)
                    | (MArray[4 * t + 3] & 0xff);
        }
        for (t = 16; t <= 79; t++) { 
            temp = (WArray[t - 3] ^ WArray[t - 8] ^ WArray[t - 14] ^ WArray[t - 16]);
            temp = (temp << 1) | (temp >>> (32 - 1)); 
            WArray[t] = temp;
        }
        A = HConstants[0];
        B = HConstants[1];
        C = HConstants[2];
        D = HConstants[3];
        E = HConstants[4];
        for (t = 0; t <= 19; t++) {
            temp = (A << 5) | (A >>> (32 - 5)); 
            temp = temp + E + WArray[t] + K0_19;
            temp = temp + ((B & C) | (~B & D));
            E = D;
            D = C;
            C = (B << 30) | (B >>> (32 - 30)); 
            B = A;
            A = temp;
        }
        for (t = 20; t <= 39; t++) {
            temp = (A << 5) | (A >>> (32 - 5)); 
            temp = temp + E + WArray[t] + K20_39;
            temp = temp + (B ^ C ^ D);
            E = D;
            D = C;
            C = (B << 30) | (B >>> (32 - 30)); 
            B = A;
            A = temp;
        }
        for (t = 40; t <= 59; t++) {
            temp = (A << 5) | (A >>> (32 - 5)); 
            temp = temp + E + WArray[t] + K40_59;
            temp = temp + ((B & C) | (B & D) | (C & D));
            E = D;
            D = C;
            C = (B << 30) | (B >>> (32 - 30)); 
            B = A;
            A = temp;
        }
        for (t = 60; t <= 79; t++) {
            temp = (A << 5) | (A >>> (32 - 5)); 
            temp = temp + E + WArray[t] + K60_79;
            temp = temp + (B ^ C ^ D);
            E = D;
            D = C;
            C = (B << 30) | (B >>> (32 - 30)); 
            B = A;
            A = temp;
        }
        HConstants[0] = HConstants[0] + A;
        HConstants[1] = HConstants[1] + B;
        HConstants[2] = HConstants[2] + C;
        HConstants[3] = HConstants[3] + D;
        HConstants[4] = HConstants[4] + E;
        bytesProcessed = bytesProcessed + BlockSizeInBytes;
        bytesToProcess = 0; 
    }
    public void reset() {
        HConstants[0] = H0;
        HConstants[1] = H1;
        HConstants[2] = H2;
        HConstants[3] = H3;
        HConstants[4] = H4;
        bytesProcessed = 0;
        bytesToProcess = 0;
    }
    @Override
    public String toString() {
        return this.getClass().getName() + ':' + toStringBlock(getHashAsBytes());
    }
    private static String toStringBlock(byte[] block) {
        return toStringBlock(block, 0, block.length);
    }
    private static String toStringBlock(byte[] block, int off, int len) {
        String hexdigits = "0123456789ABCDEF";
        StringBuilder buf = new StringBuilder();
        buf.append('[');
        for (int i = off; i < off + len; ++i) {
            buf.append(hexdigits.charAt((block[i] >>> 4) & 0xf));
            buf.append(hexdigits.charAt(block[i] & 0xf));
        }
        buf.append(']');
        return buf.toString();
    }
    @Override
    public void write(byte[] buffer, int off, int len) {
        int spaceLeft;
        int start;
        int bytesLeft;
        spaceLeft = BlockSizeInBytes - bytesToProcess;
        if (len < spaceLeft) { 
            this.copyToInternalBuffer(buffer, off, len);
            return;
        }
        this.copyToInternalBuffer(buffer, off, spaceLeft);
        bytesLeft = len - spaceLeft;
        this.processBuffer();
        start = off + spaceLeft;
        while (bytesLeft >= BlockSizeInBytes) {
            this.copyToInternalBuffer(buffer, start, BlockSizeInBytes);
            bytesLeft = bytesLeft - BlockSizeInBytes;
            this.processBuffer();
            start = start + BlockSizeInBytes;
        }
        if (bytesLeft > 0) {
            this.copyToInternalBuffer(buffer, start, bytesLeft);
        }
    }
    @Override
    public void write(int b) {
        oneByte[0] = (byte) b;
        write(oneByte, 0, 1);
    }
}
