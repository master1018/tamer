public class IccFileNotFound extends IccException {
    IccFileNotFound() {
    }
    IccFileNotFound(String s) {
        super(s);
    }
    IccFileNotFound(int ef) {
        super("ICC EF Not Found 0x" + Integer.toHexString(ef));
    }
}
