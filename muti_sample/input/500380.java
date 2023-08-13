public class SHA512Digest
    extends LongDigest
{
    private static final int    DIGEST_LENGTH = 64;
    public SHA512Digest()
    {
    }
    public SHA512Digest(SHA512Digest t)
    {
        super(t);
    }
    public String getAlgorithmName()
    {
        return "SHA-512";
    }
    public int getDigestSize()
    {
        return DIGEST_LENGTH;
    }
    public int doFinal(
        byte[]  out,
        int     outOff)
    {
        finish();
        unpackWord(H1, out, outOff);
        unpackWord(H2, out, outOff + 8);
        unpackWord(H3, out, outOff + 16);
        unpackWord(H4, out, outOff + 24);
        unpackWord(H5, out, outOff + 32);
        unpackWord(H6, out, outOff + 40);
        unpackWord(H7, out, outOff + 48);
        unpackWord(H8, out, outOff + 56);
        reset();
        return DIGEST_LENGTH;
    }
    public void reset()
    {
        super.reset();
        H1 = 0x6a09e667f3bcc908L;
        H2 = 0xbb67ae8584caa73bL;
        H3 = 0x3c6ef372fe94f82bL;
        H4 = 0xa54ff53a5f1d36f1L;
        H5 = 0x510e527fade682d1L;
        H6 = 0x9b05688c2b3e6c1fL;
        H7 = 0x1f83d9abfb41bd6bL;
        H8 = 0x5be0cd19137e2179L;
    }
}
