public class AccessConstants {
    public static void main(String[] args) {
        byte[] ref = new byte[4];
        ref[0] = ObjectStreamConstants.TC_BASE;
        ref[1] = ObjectStreamConstants.TC_NULL;
        ref[2] = ObjectStreamConstants.TC_REFERENCE;
        ref[3] = ObjectStreamConstants.TC_CLASSDESC;
        int version = ObjectStreamConstants.PROTOCOL_VERSION_1;
    }
}
