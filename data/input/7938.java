public class TestControl extends Utils {
    private static final int IOCTL_SMARTCARD_VENDOR_IFD_EXCHANGE = 0x42000000 + 1;
    public static void main(String[] args) throws Exception {
        CardTerminal terminal = getTerminal(args);
        Card card = terminal.connect("T=0");
        System.out.println("card: " + card);
        byte[] data = new byte[] {2};
        try {
            byte[] resp = card.transmitControlCommand(IOCTL_SMARTCARD_VENDOR_IFD_EXCHANGE, data);
            System.out.println("Firmware: " + toString(resp));
            throw new Exception();
        } catch (CardException e) {
            System.out.println("OK: " + e);
            e.printStackTrace(System.out);
        }
        try {
            card.transmitControlCommand(IOCTL_SMARTCARD_VENDOR_IFD_EXCHANGE, null);
        } catch (NullPointerException e) {
            System.out.println("OK: " + e);
        }
        card.disconnect(false);
        System.out.println("OK.");
    }
}
