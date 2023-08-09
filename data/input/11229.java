public class CDRInputStream_1_1 extends CDRInputStream_1_0
{
    protected int fragmentOffset = 0;
    public GIOPVersion getGIOPVersion() {
        return GIOPVersion.V1_1;
    }
    public CDRInputStreamBase dup() {
        CDRInputStreamBase result = super.dup();
        ((CDRInputStream_1_1)result).fragmentOffset = this.fragmentOffset;
        return result;
    }
    protected int get_offset() {
        return bbwi.position() + fragmentOffset;
    }
    protected void alignAndCheck(int align, int n) {
        checkBlockLength(align, n);
        int alignment = computeAlignment(bbwi.position(), align);
        if (bbwi.position() + n + alignment  > bbwi.buflen) {
            if (bbwi.position() + alignment == bbwi.buflen)
            {
                bbwi.position(bbwi.position() + alignment);
            }
            grow(align, n);
            alignment = computeAlignment(bbwi.position(), align);
        }
        bbwi.position(bbwi.position() + alignment);
    }
    protected void grow(int align, int n) {
        bbwi.needed = n;
        int oldSize = bbwi.position();
        bbwi = bufferManagerRead.underflow(bbwi);
        if (bbwi.fragmented) {
            fragmentOffset += (oldSize - bbwi.position());
            markAndResetHandler.fragmentationOccured(bbwi);
            bbwi.fragmented = false;
        }
    }
    private class FragmentableStreamMemento extends StreamMemento
    {
        private int fragmentOffset_;
        public FragmentableStreamMemento()
        {
            super();
            fragmentOffset_ = fragmentOffset;
        }
    }
    public java.lang.Object createStreamMemento() {
        return new FragmentableStreamMemento();
    }
    public void restoreInternalState(java.lang.Object streamMemento)
    {
        super.restoreInternalState(streamMemento);
        fragmentOffset
            = ((FragmentableStreamMemento)streamMemento).fragmentOffset_;
    }
    public char read_wchar() {
        alignAndCheck(2, 2);
        char[] result = getConvertedChars(2, getWCharConverter());
        if (getWCharConverter().getNumChars() > 1)
            throw wrapper.btcResultMoreThanOneChar() ;
        return result[0];
    }
    public String read_wstring() {
        int len = read_long();
        if (len == 0)
            return new String("");
        checkForNegativeLength(len);
        len = len - 1;
        char[] result = getConvertedChars(len * 2, getWCharConverter());
        read_short();
        return new String(result, 0, getWCharConverter().getNumChars());
    }
}
