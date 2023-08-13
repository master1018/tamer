public class TestConnect extends Utils {
    public static void main(String[] args) throws Exception {
        TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null, "SunPCSC");
        System.out.println(factory);
        List<CardTerminal> terminals = factory.terminals().list();
        System.out.println("Terminals: " + terminals);
        if (terminals.isEmpty()) {
            throw new Exception("No card terminals available");
        }
        CardTerminal terminal = terminals.get(0);
        if (terminal.isCardPresent() == false) {
            System.out.println("*** Insert card");
            if (terminal.waitForCardPresent(20 * 1000) == false) {
                throw new Exception("no card available");
            }
        }
        System.out.println("card present: " + terminal.isCardPresent());
        Card card = terminal.connect("*");
        System.out.println("card: " + card);
        if (card.getProtocol().equals("T=0") == false) {
            throw new Exception("Not T=0 protocol");
        }
        transmit(card);
        card.disconnect(false);
        try {
            transmit(card);
            throw new Exception("transmitted to disconnected card");
        } catch (IllegalStateException e) {
            System.out.println("OK: " + e);
        }
        try {
            card = terminal.connect("T=Foo");
            System.out.println(card);
            throw new Exception("connected via T=Foo");
        } catch (IllegalArgumentException e) {
            System.out.println("OK: " + e);
        }
        card = terminal.connect("T=0");
        System.out.println(card);
        if (card.getProtocol().equals("T=0") == false) {
            throw new Exception("Not T=0 protocol");
        }
        transmit(card);
        card.disconnect(true);
        card = terminal.connect("*");
        System.out.println("card: " + card);
        if (card.getProtocol().equals("T=0") == false) {
            throw new Exception("Not T=0 protocol");
        }
        transmit(card);
        card.disconnect(true);
        card.disconnect(true);
        System.out.println("OK.");
    }
    private static void transmit(Card card) throws Exception {
        CardChannel channel = card.getBasicChannel();
        System.out.println("Transmitting...");
        transmitTestCommand(channel);
    }
}
