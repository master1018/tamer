public class AddressUtil {
    public static int getDirectBufferAddress(Buffer buf) {
        if (!(buf instanceof DirectBuffer)) {
            return 0;
        }
        return ((DirectBuffer) buf).getEffectiveAddress().toInt();
    }
}
