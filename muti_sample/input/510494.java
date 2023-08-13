class BerTlv {
    private int mTag = BER_UNKNOWN_TAG;
    private List<ComprehensionTlv> mCompTlvs = null;
    public static final int BER_UNKNOWN_TAG             = 0x00;
    public static final int BER_PROACTIVE_COMMAND_TAG   = 0xd0;
    public static final int BER_MENU_SELECTION_TAG      = 0xd3;
    public static final int BER_EVENT_DOWNLOAD_TAG      = 0xd6;
    private BerTlv(int tag, List<ComprehensionTlv> ctlvs) {
        mTag = tag;
        mCompTlvs = ctlvs;
    }
    public List<ComprehensionTlv> getComprehensionTlvs() {
        return mCompTlvs;
    }
    public int getTag() {
        return mTag;
    }
    public static BerTlv decode(byte[] data) throws ResultException {
        int curIndex = 0;
        int endIndex = data.length;
        int tag, length = 0;
        try {
            tag = data[curIndex++] & 0xff;
            if (tag == BER_PROACTIVE_COMMAND_TAG) {
                int temp = data[curIndex++] & 0xff;
                if (temp < 0x80) {
                    length = temp;
                } else if (temp == 0x81) {
                    temp = data[curIndex++] & 0xff;
                    if (temp < 0x80) {
                        throw new ResultException(
                                ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                    }
                    length = temp;
                } else {
                    throw new ResultException(
                            ResultCode.CMD_DATA_NOT_UNDERSTOOD);
                }
            } else {
                if (ComprehensionTlvTag.COMMAND_DETAILS.value() == (tag & ~0x80)) {
                    tag = BER_UNKNOWN_TAG;
                    curIndex = 0;
                }
            }
        } catch (IndexOutOfBoundsException e) {
            throw new ResultException(ResultCode.REQUIRED_VALUES_MISSING);
        } catch (ResultException e) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
        if (endIndex - curIndex < length) {
            throw new ResultException(ResultCode.CMD_DATA_NOT_UNDERSTOOD);
        }
        List<ComprehensionTlv> ctlvs = ComprehensionTlv.decodeMany(data,
                curIndex);
        return new BerTlv(tag, ctlvs);
    }
}
