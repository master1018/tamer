public class ICC_ProfileGray extends ICC_Profile {
    private static final long serialVersionUID = -1124721290732002649L;
    ICC_ProfileGray(long profileHandle) {
        super(profileHandle);
    }
    public short[] getTRC() {
        return super.getTRC(icSigGrayTRCTag);
    }
    @Override
    public float[] getMediaWhitePoint() {
        return super.getMediaWhitePoint();
    }
    public float getGamma() {
        return super.getGamma(icSigGrayTRCTag);
    }
}
