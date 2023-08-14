public class TestChannel extends Utils {
    static final byte[] c1 = parse("00 A4 04 00 07 A0 00 00 00 62 81 01 00");
    static final byte[] r1 = parse("07:a0:00:00:00:62:81:01:04:01:00:00:24:05:00:0b:04:b0:55:90:00");
    static final byte[] openChannel = parse("00 70 00 00 01");
    static final byte[] closeChannel = new byte[] {0x01, 0x70, (byte)0x80, 0};
    public static void main(String[] args) throws Exception {
        CardTerminal terminal = getTerminal(args);
        Card card = terminal.connect("T=0");
        System.out.println("card: " + card);
        CardChannel basicChannel = card.getBasicChannel();
        try {
            basicChannel.transmit(new CommandAPDU(openChannel));
        } catch (IllegalArgumentException e) {
            System.out.println("OK: " + e);
        }
        try {
            basicChannel.transmit(new CommandAPDU(closeChannel));
        } catch (IllegalArgumentException e) {
            System.out.println("OK: " + e);
        }
        byte[] atr = card.getATR().getBytes();
        System.out.println("atr: " + toString(atr));
        boolean supportsChannels = false;
        for (int i = 0; i < atr.length; i++) {
            if (atr[i] == 0x73) {
                supportsChannels = true;
                break;
            }
        }
        if (supportsChannels == false) {
            System.out.println("Card does not support logical channels, skipping...");
        } else {
            CardChannel channel = card.openLogicalChannel();
            System.out.println("channel: " + channel);
            channel.close();
        }
        card.disconnect(false);
        System.out.println("OK.");
    }
}
