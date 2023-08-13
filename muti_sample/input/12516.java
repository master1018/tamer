public class ByteToCharUnicode extends ByteToCharConverter {
    static final char BYTE_ORDER_MARK = (char) 0xfeff;
    static final char REVERSED_MARK = (char) 0xfffe;
    static final int AUTO = 0;
    static final int BIG = 1;
    static final int LITTLE = 2;
    int originalByteOrder;      
    int byteOrder;              
    boolean usesMark;           
    public ByteToCharUnicode() {
        originalByteOrder = byteOrder = AUTO;
        usesMark = true;
    }
    protected ByteToCharUnicode(int bo, boolean m) {
        originalByteOrder = byteOrder = bo;
        usesMark = m;
    }
    public String getCharacterEncoding() {
        switch (originalByteOrder) {
        case BIG:
            return usesMark ? "UnicodeBig" : "UnicodeBigUnmarked";
        case LITTLE:
            return usesMark ? "UnicodeLittle" : "UnicodeLittleUnmarked";
        default:
            return "Unicode";
        }
    }
    boolean started = false;
    int leftOverByte;
    boolean leftOver = false;
    public int convert(byte[] in, int inOff, int inEnd,
                       char[] out, int outOff, int outEnd)
        throws ConversionBufferFullException, MalformedInputException
    {
        byteOff = inOff;
        charOff = outOff;
        if (inOff >= inEnd)
            return 0;
        int b1, b2;
        int bc = 0;
        int inI = inOff, outI = outOff;
        if (leftOver) {
            b1 = leftOverByte & 0xff;
            leftOver = false;
        }
        else {
            b1 = in[inI++] & 0xff;
        }
        bc = 1;
        if (usesMark && !started) {     
            if (inI < inEnd) {
                b2 = in[inI++] & 0xff;
                bc = 2;
                char c = (char) ((b1 << 8) | b2);
                int bo = AUTO;
                if (c == BYTE_ORDER_MARK)
                    bo = BIG;
                else if (c == REVERSED_MARK)
                    bo = LITTLE;
                if (byteOrder == AUTO) {
                    if (bo == AUTO) {
                        badInputLength = bc;
                        throw new
                            MalformedInputException("Missing byte-order mark");
                    }
                    byteOrder = bo;
                    if (inI < inEnd) {
                        b1 = in[inI++] & 0xff;
                        bc = 1;
                    }
                }
                else if (bo == AUTO) {
                    inI--;
                    bc = 1;
                }
                else if (byteOrder == bo) {
                    if (inI < inEnd) {
                        b1 = in[inI++] & 0xff;
                        bc = 1;
                    }
                }
                else {
                    badInputLength = bc;
                    throw new
                        MalformedInputException("Incorrect byte-order mark");
                }
                started = true;
            }
        }
        while (inI < inEnd) {
            b2 = in[inI++] & 0xff;
            bc = 2;
            char c;
            if (byteOrder == BIG)
                c = (char) ((b1 << 8) | b2);
            else
                c = (char) ((b2 << 8) | b1);
            if (c == REVERSED_MARK)
                throw new
                    MalformedInputException("Reversed byte-order mark");
            if (outI >= outEnd)
                throw new ConversionBufferFullException();
            out[outI++] = c;
            byteOff = inI;
            charOff = outI;
            if (inI < inEnd) {
                b1 = in[inI++] & 0xff;
                bc = 1;
            }
        }
        if (bc == 1) {
            leftOverByte = b1;
            byteOff = inI;
            leftOver = true;
        }
        return outI - outOff;
    }
    public void reset() {
        leftOver = false;
        byteOff = charOff = 0;
        started = false;
        byteOrder = originalByteOrder;
    }
    public int flush(char buf[], int off, int len)
        throws MalformedInputException
    {
        if (leftOver) {
            reset();
            throw new MalformedInputException();
        }
        byteOff = charOff = 0;
        return 0;
    }
}
