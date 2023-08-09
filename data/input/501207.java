public class ICC_ProfileRGB extends ICC_Profile {
    private static final long serialVersionUID = 8505067385152579334L;
    ICC_ProfileRGB(long profileHandle) {
        super(profileHandle);
    }
    public static final int REDCOMPONENT = 0;
    public static final int GREENCOMPONENT = 1;
    public static final int BLUECOMPONENT = 2;
    private static final String UNKNOWN_COMPONENT_MSG = Messages
            .getString("awt.15E"); 
    @Override
    public short[] getTRC(int component) {
        switch (component) {
            case REDCOMPONENT:
                return super.getTRC(icSigRedTRCTag);
            case GREENCOMPONENT:
                return super.getTRC(icSigGreenTRCTag);
            case BLUECOMPONENT:
                return super.getTRC(icSigBlueTRCTag);
            default:
        }
        throw new IllegalArgumentException(UNKNOWN_COMPONENT_MSG);
    }
    @Override
    public float getGamma(int component) {
        switch (component) {
            case REDCOMPONENT:
                return super.getGamma(icSigRedTRCTag);
            case GREENCOMPONENT:
                return super.getGamma(icSigGreenTRCTag);
            case BLUECOMPONENT:
                return super.getGamma(icSigBlueTRCTag);
            default:
        }
        throw new IllegalArgumentException(UNKNOWN_COMPONENT_MSG);
    }
    public float[][] getMatrix() {
        float [][] m = new float[3][3]; 
        float[] redXYZ = getXYZValue(icSigRedColorantTag);
        float[] greenXYZ = getXYZValue(icSigGreenColorantTag);
        float[] blueXYZ = getXYZValue(icSigBlueColorantTag);
        m[0][0] = redXYZ[0];
        m[1][0] = redXYZ[1];
        m[2][0] = redXYZ[2];
        m[0][1] = greenXYZ[0];
        m[1][1] = greenXYZ[1];
        m[2][1] = greenXYZ[2];
        m[0][2] = blueXYZ[0];
        m[1][2] = blueXYZ[1];
        m[2][2] = blueXYZ[2];
        return m;
    }
    @Override
    public float[] getMediaWhitePoint() {
        return super.getMediaWhitePoint();
    }
}
