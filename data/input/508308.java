abstract class ResponseData {
    public abstract void format(ByteArrayOutputStream buf);
}
class SelectItemResponseData extends ResponseData {
    private int id;
    public SelectItemResponseData(int id) {
        super();
        this.id = id;
    }
    @Override
    public void format(ByteArrayOutputStream buf) {
        int tag = 0x80 | ComprehensionTlvTag.ITEM_ID.value();
        buf.write(tag); 
        buf.write(1); 
        buf.write(id); 
    }
}
class GetInkeyInputResponseData extends ResponseData {
    private boolean mIsUcs2;
    private boolean mIsPacked;
    private boolean mIsYesNo;
    private boolean mYesNoResponse;
    public String mInData;
    protected static final byte GET_INKEY_YES = 0x01;
    protected static final byte GET_INKEY_NO = 0x00;
    public GetInkeyInputResponseData(String inData, boolean ucs2, boolean packed) {
        super();
        this.mIsUcs2 = ucs2;
        this.mIsPacked = packed;
        this.mInData = inData;
        this.mIsYesNo = false;
    }
    public GetInkeyInputResponseData(boolean yesNoResponse) {
        super();
        this.mIsUcs2 = false;
        this.mIsPacked = false;
        this.mInData = "";
        this.mIsYesNo = true;
        this.mYesNoResponse = yesNoResponse;
    }
    @Override
    public void format(ByteArrayOutputStream buf) {
        if (buf == null) {
            return;
        }
        int tag = 0x80 | ComprehensionTlvTag.TEXT_STRING.value();
        buf.write(tag); 
        byte[] data;
        if (mIsYesNo) {
            data = new byte[1];
            data[0] = mYesNoResponse ? GET_INKEY_YES : GET_INKEY_NO;
        } else if (mInData != null && mInData.length() > 0) {
            try {
                if (mIsUcs2) {
                    data = mInData.getBytes("UTF-16");
                } else if (mIsPacked) {
                    int size = mInData.length();
                    byte[] tempData = GsmAlphabet
                            .stringToGsm7BitPacked(mInData);
                    data = new byte[size];
                    System.arraycopy(tempData, 1, data, 0, size);
                } else {
                    data = GsmAlphabet.stringToGsm8BitPacked(mInData);
                }
            } catch (UnsupportedEncodingException e) {
                data = new byte[0];
            } catch (EncodeException e) {
                data = new byte[0];
            }
        } else {
            data = new byte[0];
        }
        buf.write(data.length + 1);
        if (mIsUcs2) {
            buf.write(0x08); 
        } else if (mIsPacked) {
            buf.write(0x00); 
        } else {
            buf.write(0x04); 
        }
        for (byte b : data) {
            buf.write(b);
        }
    }
}
