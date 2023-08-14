public class SHA384Digest
    extends LongDigest
{
    private static final int    DIGEST_LENGTH = 48;
    public SHA384Digest()
    {
    }
    public SHA384Digest(SHA384Digest t)
    {
        super(t);
    }
    public String getAlgorithmName()
    {
        return "SHA-384";
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
        reset();
        return DIGEST_LENGTH;
    }
    public void reset()
    {
        super.reset();
        H1 = 0xcbbb9d5dc1059ed8l;
        H2 = 0x629a292a367cd507l;
        H3 = 0x9159015a3070dd17l;
        H4 = 0x152fecd8f70e5939l;
        H5 = 0x67332667ffc00b31l;
        H6 = 0x8eb44a8768581511l;
        H7 = 0xdb0c2e0d64f98fa7l;
        H8 = 0x47b5481dbefa4fa4l;
    }
}
