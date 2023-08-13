public class CDROutputStream_1_1 extends CDROutputStream_1_0
{
    protected int fragmentOffset = 0;
    protected void alignAndReserve(int align, int n) {
        int alignment = computeAlignment(align);
        if (bbwi.position() + n + alignment > bbwi.buflen) {
            grow(align, n);
            alignment = computeAlignment(align);
        }
        bbwi.position(bbwi.position() + alignment);
    }
    protected void grow(int align, int n) {
        int oldSize = bbwi.position();
        super.grow(align, n);
        if (bbwi.fragmented) {
            bbwi.fragmented = false;
            fragmentOffset += (oldSize - bbwi.position());
        }
    }
    public int get_offset() {
        return bbwi.position() + fragmentOffset;
    }
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_1;
    }
    public void write_wchar(char x)
    {
        CodeSetConversion.CTBConverter converter = getWCharConverter();
        converter.convert(x);
        if (converter.getNumBytes() != 2)
            throw wrapper.badGiop11Ctb(CompletionStatus.COMPLETED_MAYBE);
        alignAndReserve(converter.getAlignment(),
                        converter.getNumBytes());
        parent.write_octet_array(converter.getBytes(),
                                 0,
                                 converter.getNumBytes());
    }
    public void write_wstring(String value)
    {
        if (value == null) {
            throw wrapper.nullParam(CompletionStatus.COMPLETED_MAYBE);
        }
        int len = value.length() + 1;
        write_long(len);
        CodeSetConversion.CTBConverter converter = getWCharConverter();
        converter.convert(value);
        internalWriteOctetArray(converter.getBytes(), 0, converter.getNumBytes());
        write_short((short)0);
    }
}
