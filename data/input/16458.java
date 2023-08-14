public class LZWCompressor {
    int codeSize;
    int clearCode;
    int endOfInfo;
    int numBits;
    int limit;
    short prefix;
    BitFile bf;
    LZWStringTable lzss;
    boolean tiffFudge;
    public LZWCompressor(ImageOutputStream out, int codeSize, boolean TIFF)
        throws IOException
    {
        bf = new BitFile(out, !TIFF); 
        this.codeSize = codeSize;
        tiffFudge = TIFF;
        clearCode = 1 << codeSize;
        endOfInfo = clearCode + 1;
        numBits = codeSize + 1;
        limit = (1 << numBits) - 1;
        if (tiffFudge) {
            --limit;
        }
        prefix = (short)0xFFFF;
        lzss = new LZWStringTable();
        lzss.clearTable(codeSize);
        bf.writeBits(clearCode, numBits);
    }
    public void compress(byte[] buf, int offset, int length)
        throws IOException
    {
        int idx;
        byte c;
        short index;
        int maxOffset = offset + length;
        for (idx = offset; idx < maxOffset; ++idx) {
            c = buf[idx];
            if ((index = lzss.findCharString(prefix, c)) != -1) {
                prefix = index;
            } else {
                bf.writeBits(prefix, numBits);
                if (lzss.addCharString(prefix, c) > limit) {
                    if (numBits == 12) {
                        bf.writeBits(clearCode, numBits);
                        lzss.clearTable(codeSize);
                        numBits = codeSize + 1;
                    } else {
                        ++numBits;
                    }
                    limit = (1 << numBits) - 1;
                    if (tiffFudge) {
                        --limit;
                    }
                }
                prefix = (short)((short)c & 0xFF);
            }
        }
    }
    public void flush() throws IOException {
        if (prefix != -1) {
            bf.writeBits(prefix, numBits);
        }
        bf.writeBits(endOfInfo, numBits);
        bf.flush();
    }
    public void dump(PrintStream out) {
        lzss.dump(out);
    }
}
