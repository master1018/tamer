public final class MD5 extends DigestBase {
    private final int[] state;
    private final int[] x;
    private static final int S11 = 7;
    private static final int S12 = 12;
    private static final int S13 = 17;
    private static final int S14 = 22;
    private static final int S21 = 5;
    private static final int S22 = 9;
    private static final int S23 = 14;
    private static final int S24 = 20;
    private static final int S31 = 4;
    private static final int S32 = 11;
    private static final int S33 = 16;
    private static final int S34 = 23;
    private static final int S41 = 6;
    private static final int S42 = 10;
    private static final int S43 = 15;
    private static final int S44 = 21;
    public MD5() {
        super("MD5", 16, 64);
        state = new int[4];
        x = new int[16];
        implReset();
    }
    private MD5(MD5 base) {
        super(base);
        this.state = base.state.clone();
        this.x = new int[16];
    }
    public Object clone() {
        return new MD5(this);
    }
    void implReset() {
        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;
    }
    void implDigest(byte[] out, int ofs) {
        long bitsProcessed = bytesProcessed << 3;
        int index = (int)bytesProcessed & 0x3f;
        int padLen = (index < 56) ? (56 - index) : (120 - index);
        engineUpdate(padding, 0, padLen);
        i2bLittle4((int)bitsProcessed, buffer, 56);
        i2bLittle4((int)(bitsProcessed >>> 32), buffer, 60);
        implCompress(buffer, 0);
        i2bLittle(state, 0, out, ofs, 16);
    }
    private static int FF(int a, int b, int c, int d, int x, int s, int ac) {
        a += ((b & c) | ((~b) & d)) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }
    private static int GG(int a, int b, int c, int d, int x, int s, int ac) {
        a += ((b & d) | (c & (~d))) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }
    private static int HH(int a, int b, int c, int d, int x, int s, int ac) {
        a += ((b ^ c) ^ d) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }
    private static int II(int a, int b, int c, int d, int x, int s, int ac) {
        a += (c ^ (b | (~d))) + x + ac;
        return ((a << s) | (a >>> (32 - s))) + b;
    }
    void implCompress(byte[] buf, int ofs) {
        b2iLittle64(buf, ofs, x);
        int a = state[0];
        int b = state[1];
        int c = state[2];
        int d = state[3];
        a = FF ( a, b, c, d, x[ 0], S11, 0xd76aa478); 
        d = FF ( d, a, b, c, x[ 1], S12, 0xe8c7b756); 
        c = FF ( c, d, a, b, x[ 2], S13, 0x242070db); 
        b = FF ( b, c, d, a, x[ 3], S14, 0xc1bdceee); 
        a = FF ( a, b, c, d, x[ 4], S11, 0xf57c0faf); 
        d = FF ( d, a, b, c, x[ 5], S12, 0x4787c62a); 
        c = FF ( c, d, a, b, x[ 6], S13, 0xa8304613); 
        b = FF ( b, c, d, a, x[ 7], S14, 0xfd469501); 
        a = FF ( a, b, c, d, x[ 8], S11, 0x698098d8); 
        d = FF ( d, a, b, c, x[ 9], S12, 0x8b44f7af); 
        c = FF ( c, d, a, b, x[10], S13, 0xffff5bb1); 
        b = FF ( b, c, d, a, x[11], S14, 0x895cd7be); 
        a = FF ( a, b, c, d, x[12], S11, 0x6b901122); 
        d = FF ( d, a, b, c, x[13], S12, 0xfd987193); 
        c = FF ( c, d, a, b, x[14], S13, 0xa679438e); 
        b = FF ( b, c, d, a, x[15], S14, 0x49b40821); 
        a = GG ( a, b, c, d, x[ 1], S21, 0xf61e2562); 
        d = GG ( d, a, b, c, x[ 6], S22, 0xc040b340); 
        c = GG ( c, d, a, b, x[11], S23, 0x265e5a51); 
        b = GG ( b, c, d, a, x[ 0], S24, 0xe9b6c7aa); 
        a = GG ( a, b, c, d, x[ 5], S21, 0xd62f105d); 
        d = GG ( d, a, b, c, x[10], S22,  0x2441453); 
        c = GG ( c, d, a, b, x[15], S23, 0xd8a1e681); 
        b = GG ( b, c, d, a, x[ 4], S24, 0xe7d3fbc8); 
        a = GG ( a, b, c, d, x[ 9], S21, 0x21e1cde6); 
        d = GG ( d, a, b, c, x[14], S22, 0xc33707d6); 
        c = GG ( c, d, a, b, x[ 3], S23, 0xf4d50d87); 
        b = GG ( b, c, d, a, x[ 8], S24, 0x455a14ed); 
        a = GG ( a, b, c, d, x[13], S21, 0xa9e3e905); 
        d = GG ( d, a, b, c, x[ 2], S22, 0xfcefa3f8); 
        c = GG ( c, d, a, b, x[ 7], S23, 0x676f02d9); 
        b = GG ( b, c, d, a, x[12], S24, 0x8d2a4c8a); 
        a = HH ( a, b, c, d, x[ 5], S31, 0xfffa3942); 
        d = HH ( d, a, b, c, x[ 8], S32, 0x8771f681); 
        c = HH ( c, d, a, b, x[11], S33, 0x6d9d6122); 
        b = HH ( b, c, d, a, x[14], S34, 0xfde5380c); 
        a = HH ( a, b, c, d, x[ 1], S31, 0xa4beea44); 
        d = HH ( d, a, b, c, x[ 4], S32, 0x4bdecfa9); 
        c = HH ( c, d, a, b, x[ 7], S33, 0xf6bb4b60); 
        b = HH ( b, c, d, a, x[10], S34, 0xbebfbc70); 
        a = HH ( a, b, c, d, x[13], S31, 0x289b7ec6); 
        d = HH ( d, a, b, c, x[ 0], S32, 0xeaa127fa); 
        c = HH ( c, d, a, b, x[ 3], S33, 0xd4ef3085); 
        b = HH ( b, c, d, a, x[ 6], S34,  0x4881d05); 
        a = HH ( a, b, c, d, x[ 9], S31, 0xd9d4d039); 
        d = HH ( d, a, b, c, x[12], S32, 0xe6db99e5); 
        c = HH ( c, d, a, b, x[15], S33, 0x1fa27cf8); 
        b = HH ( b, c, d, a, x[ 2], S34, 0xc4ac5665); 
        a = II ( a, b, c, d, x[ 0], S41, 0xf4292244); 
        d = II ( d, a, b, c, x[ 7], S42, 0x432aff97); 
        c = II ( c, d, a, b, x[14], S43, 0xab9423a7); 
        b = II ( b, c, d, a, x[ 5], S44, 0xfc93a039); 
        a = II ( a, b, c, d, x[12], S41, 0x655b59c3); 
        d = II ( d, a, b, c, x[ 3], S42, 0x8f0ccc92); 
        c = II ( c, d, a, b, x[10], S43, 0xffeff47d); 
        b = II ( b, c, d, a, x[ 1], S44, 0x85845dd1); 
        a = II ( a, b, c, d, x[ 8], S41, 0x6fa87e4f); 
        d = II ( d, a, b, c, x[15], S42, 0xfe2ce6e0); 
        c = II ( c, d, a, b, x[ 6], S43, 0xa3014314); 
        b = II ( b, c, d, a, x[13], S44, 0x4e0811a1); 
        a = II ( a, b, c, d, x[ 4], S41, 0xf7537e82); 
        d = II ( d, a, b, c, x[11], S42, 0xbd3af235); 
        c = II ( c, d, a, b, x[ 2], S43, 0x2ad7d2bb); 
        b = II ( b, c, d, a, x[ 9], S44, 0xeb86d391); 
        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
    }
}
