public class CDROutputStream_1_2 extends CDROutputStream_1_1
{
    protected boolean primitiveAcrossFragmentedChunk = false;
    protected boolean specialChunk = false;
    private boolean headerPadding;
    protected void handleSpecialChunkBegin(int requiredSize)
    {
        if (inBlock && requiredSize + bbwi.position() > bbwi.buflen) {
            int oldSize = bbwi.position();
            bbwi.position(blockSizeIndex - 4);
            writeLongWithoutAlign((oldSize - blockSizeIndex) + requiredSize);
            bbwi.position(oldSize);
            specialChunk = true;
        }
    }
    protected void handleSpecialChunkEnd()
    {
        if (inBlock && specialChunk) {
            inBlock = false;
            blockSizeIndex = -1;
            blockSizePosition = -1;
            start_block();
            specialChunk = false;
        }
    }
    private void checkPrimitiveAcrossFragmentedChunk()
    {
        if (primitiveAcrossFragmentedChunk) {
            primitiveAcrossFragmentedChunk = false;
            inBlock = false;
            blockSizeIndex = -1;
            blockSizePosition = -1;
            start_block();
        }
    }
    public void write_octet(byte x) {
        super.write_octet(x);
        checkPrimitiveAcrossFragmentedChunk();
    }
    public void write_short(short x) {
        super.write_short(x);
        checkPrimitiveAcrossFragmentedChunk();
    }
    public void write_long(int x) {
        super.write_long(x);
        checkPrimitiveAcrossFragmentedChunk();
    }
    public void write_longlong(long x) {
        super.write_longlong(x);
        checkPrimitiveAcrossFragmentedChunk();
    }
    void setHeaderPadding(boolean headerPadding) {
        this.headerPadding = headerPadding;
    }
    protected void alignAndReserve(int align, int n) {
        if (headerPadding == true) {
            headerPadding = false;
            alignOnBoundary(ORBConstants.GIOP_12_MSG_BODY_ALIGNMENT);
        }
        bbwi.position(bbwi.position() + computeAlignment(align));
        if (bbwi.position() + n  > bbwi.buflen)
            grow(align, n);
    }
    protected void grow(int align, int n) {
        int oldSize = bbwi.position();
        boolean handleChunk = (inBlock && !specialChunk);
        if (handleChunk) {
            int oldIndex = bbwi.position();
            bbwi.position(blockSizeIndex - 4);
            writeLongWithoutAlign((oldIndex - blockSizeIndex) + n);
            bbwi.position(oldIndex);
        }
        bbwi.needed = n;
        bufferManagerWrite.overflow(bbwi);
        if (bbwi.fragmented) {
            bbwi.fragmented = false;
            fragmentOffset += (oldSize - bbwi.position());
            if (handleChunk)
                primitiveAcrossFragmentedChunk = true;
        }
    }
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_2;
    }
    public void write_wchar(char x)
    {
        CodeSetConversion.CTBConverter converter = getWCharConverter();
        converter.convert(x);
        handleSpecialChunkBegin(1 + converter.getNumBytes());
        write_octet((byte)converter.getNumBytes());
        byte[] result = converter.getBytes();
        internalWriteOctetArray(result, 0, converter.getNumBytes());
        handleSpecialChunkEnd();
    }
    public void write_wchar_array(char[] value, int offset, int length)
    {
        if (value == null) {
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        }
        CodeSetConversion.CTBConverter converter = getWCharConverter();
        int totalNumBytes = 0;
        int maxLength = (int)Math.ceil(converter.getMaxBytesPerChar() * length);
        byte[] buffer = new byte[maxLength + length];
        for (int i = 0; i < length; i++) {
            converter.convert(value[offset + i]);
            buffer[totalNumBytes++] = (byte)converter.getNumBytes();
            System.arraycopy(converter.getBytes(), 0,
                             buffer, totalNumBytes,
                             converter.getNumBytes());
            totalNumBytes += converter.getNumBytes();
        }
        handleSpecialChunkBegin(totalNumBytes);
        internalWriteOctetArray(buffer, 0, totalNumBytes);
        handleSpecialChunkEnd();
    }
    public void write_wstring(String value) {
        if (value == null) {
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        }
        if (value.length() == 0) {
            write_long(0);
            return;
        }
        CodeSetConversion.CTBConverter converter = getWCharConverter();
        converter.convert(value);
        handleSpecialChunkBegin(computeAlignment(4) + 4 + converter.getNumBytes());
        write_long(converter.getNumBytes());
        internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());
        handleSpecialChunkEnd();
    }
}
