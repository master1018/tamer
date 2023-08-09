public class ICC_ProfileHelper {
    public static int getIntFromByteArray(byte[] byteArray, int idx) {
        return (byteArray[idx] & 0xFF)|
               ((byteArray[idx+1] & 0xFF) << 8) |
               ((byteArray[idx+2] & 0xFF) << 16)|
               ((byteArray[idx+3] & 0xFF) << 24);
    }
    public static int getBigEndianFromByteArray(byte[] byteArray, int idx) {
        return ((byteArray[idx] & 0xFF) << 24)   |
               ((byteArray[idx+1] & 0xFF) << 16) |
               ((byteArray[idx+2] & 0xFF) << 8)  |
               ( byteArray[idx+3] & 0xFF);
    }
    public static short getShortFromByteArray(byte[] byteArray, int idx) {
        return (short) ((byteArray[idx] & 0xFF) |
                       ((byteArray[idx+1] & 0xFF) << 8));
    }
    public static int getRenderingIntent(ICC_Profile profile) {
        return getIntFromByteArray(
                profile.getData(ICC_Profile.icSigHead), 
                ICC_Profile.icHdrRenderingIntent
            );
    }
}
