public class MonkeyFlipEvent extends MonkeyEvent {
    private static final byte[] FLIP_0 = {
        0x7f, 0x06,
        0x00, 0x00,
        (byte) 0xe0, 0x39,
        0x01, 0x00,
        0x05, 0x00,
        0x00, 0x00,
        0x01, 0x00,
        0x00, 0x00 };
    private static final byte[] FLIP_1 = {
        (byte) 0x85, 0x06,
        0x00, 0x00,
        (byte) 0x9f, (byte) 0xa5,
        0x0c, 0x00,
        0x05, 0x00,
        0x00, 0x00,
        0x00, 0x00,
        0x00, 0x00 };
    private final boolean mKeyboardOpen;
    public MonkeyFlipEvent(boolean keyboardOpen) {
        super(EVENT_TYPE_FLIP);
        mKeyboardOpen = keyboardOpen;
    }
    @Override
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (verbose > 0) {
            System.out.println(":Sending Flip keyboardOpen=" + mKeyboardOpen);
        }
        try {
            FileOutputStream f = new FileOutputStream("/dev/input/event0");
            f.write(mKeyboardOpen ? FLIP_0 : FLIP_1);
            f.close();
            return MonkeyEvent.INJECT_SUCCESS;
        } catch (IOException e) {
            System.out.println("Got IOException performing flip" + e);
            return MonkeyEvent.INJECT_FAIL;
        }
    }
}
