public final class SHA extends DigestBase {
    private final int[] W;
    private final int[] state;
    public SHA() {
        super("SHA-1", 20, 64);
        state = new int[5];
        W = new int[80];
        implReset();
    }
    private SHA(SHA base) {
        super(base);
        this.state = base.state.clone();
        this.W = new int[80];
    }
    public Object clone() {
        return new SHA(this);
    }
    void implReset() {
        state[0] = 0x67452301;
        state[1] = 0xefcdab89;
        state[2] = 0x98badcfe;
        state[3] = 0x10325476;
        state[4] = 0xc3d2e1f0;
    }
    void implDigest(byte[] out, int ofs) {
        long bitsProcessed = bytesProcessed << 3;
        int index = (int)bytesProcessed & 0x3f;
        int padLen = (index < 56) ? (56 - index) : (120 - index);
        engineUpdate(padding, 0, padLen);
        i2bBig4((int)(bitsProcessed >>> 32), buffer, 56);
        i2bBig4((int)bitsProcessed, buffer, 60);
        implCompress(buffer, 0);
        i2bBig(state, 0, out, ofs, 20);
    }
    private final static int round1_kt = 0x5a827999;
    private final static int round2_kt = 0x6ed9eba1;
    private final static int round3_kt = 0x8f1bbcdc;
    private final static int round4_kt = 0xca62c1d6;
    void implCompress(byte[] buf, int ofs) {
        b2iBig64(buf, ofs, W);
        for (int t = 16; t <= 79; t++) {
            int temp = W[t-3] ^ W[t-8] ^ W[t-14] ^ W[t-16];
            W[t] = (temp << 1) | (temp >>> 31);
        }
        int a = state[0];
        int b = state[1];
        int c = state[2];
        int d = state[3];
        int e = state[4];
        for (int i = 0; i < 20; i++) {
            int temp = ((a<<5) | (a>>>(32-5))) +
                ((b&c)|((~b)&d))+ e + W[i] + round1_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
        for (int i = 20; i < 40; i++) {
            int temp = ((a<<5) | (a>>>(32-5))) +
                (b ^ c ^ d) + e + W[i] + round2_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
        for (int i = 40; i < 60; i++) {
            int temp = ((a<<5) | (a>>>(32-5))) +
                ((b&c)|(b&d)|(c&d)) + e + W[i] + round3_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
        for (int i = 60; i < 80; i++) {
            int temp = ((a<<5) | (a>>>(32-5))) +
                (b ^ c ^ d) + e + W[i] + round4_kt;
            e = d;
            d = c;
            c = ((b<<30) | (b>>>(32-30)));
            b = a;
            a = temp;
        }
        state[0] += a;
        state[1] += b;
        state[2] += c;
        state[3] += d;
        state[4] += e;
    }
}
