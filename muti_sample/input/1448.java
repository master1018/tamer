public class ICC_ProfileGray
extends ICC_Profile {
    static final long serialVersionUID = -1124721290732002649L;
    ICC_ProfileGray(long ID) {
        super(ID);
    }
    ICC_ProfileGray(ProfileDeferralInfo pdi) {
        super(pdi);
    }
    public float[] getMediaWhitePoint() {
        return super.getMediaWhitePoint();
    }
    public float getGamma() {
    float theGamma;
        theGamma = super.getGamma(ICC_Profile.icSigGrayTRCTag);
        return theGamma;
    }
    public short[] getTRC() {
    short[]    theTRC;
        theTRC = super.getTRC(ICC_Profile.icSigGrayTRCTag);
        return theTRC;
    }
}
