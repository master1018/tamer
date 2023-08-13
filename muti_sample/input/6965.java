public class TestConnectAgain extends Utils {
    public static void main(String[] args) throws Exception {
        CardTerminal terminal = getTerminal(args);
        Card card = terminal.connect("T=0");
        CardChannel channel = card.getBasicChannel();
        transmitTestCommand(channel);
        Card card2 = terminal.connect("*");
        if (card != card2) {
            throw new Exception("Different card object");
        }
        card2 = terminal.connect("T=0");
        if (card != card2) {
            throw new Exception("Different card object");
        }
        System.out.println("Remove card!");
        terminal.waitForCardAbsent(0);
        try {
            transmitTestCommand(channel);
            throw new Exception();
        } catch (CardException e) {
            System.out.println("OK: " + e);
        }
        System.out.println("Insert card!");
        terminal.waitForCardPresent(0);
        try {
            transmitTestCommand(channel);
            throw new Exception();
        } catch (IllegalStateException e) {
            System.out.println("OK: " + e);
        }
        card = terminal.connect("*");
        if (card == card2) {
            throw new Exception("Old card object");
        }
        try {
            transmitTestCommand(channel);
            throw new Exception();
        } catch (IllegalStateException e) {
            System.out.println("OK: " + e);
        }
        channel = card.getBasicChannel();
        transmitTestCommand(channel);
        card2 = terminal.connect("*");
        if (card != card2) {
            throw new Exception("Different card object");
        }
        card.disconnect(false);
        System.out.println("OK.");
    }
}
