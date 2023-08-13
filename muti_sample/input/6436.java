public class TestExclusive extends Utils {
    static volatile boolean exclusive;
    static volatile boolean otherOK;
    public static void main(String[] args) throws Exception {
        CardTerminal terminal = getTerminal(args);
        Card card = terminal.connect("T=0");
        System.out.println("card: " + card);
        Thread thread = new Thread(new OtherThread(card));
        thread.setDaemon(true);
        thread.start();
        card.beginExclusive();
        exclusive = true;
        Thread.sleep(1000);
        System.out.println("=1=resuming...");
        CardChannel channel = card.getBasicChannel();
        System.out.println("=1=Transmitting...");
        transmitTestCommand(channel);
        System.out.println("=1=OK");
        try {
            card.beginExclusive();
        } catch (CardException e) {
            System.out.println("=1=OK: " + e);
        }
        card.endExclusive();
        try {
            card.endExclusive();
        } catch (IllegalStateException e) {
            System.out.println("=1=OK: " + e);
        }
        exclusive = false;
        Thread.sleep(1000);
        card.disconnect(false);
        if (otherOK == false) {
            throw new Exception("Secondary thread failed");
        }
        System.out.println("=1=OK.");
    }
    private static class OtherThread implements Runnable {
        private final Card card;
        OtherThread(Card card) {
            this.card = card;
        }
        public void run() {
            try {
                while (exclusive == false) {
                    Thread.sleep(100);
                }
                System.out.println("=2=trying endexclusive...");
                try {
                    card.endExclusive();
                } catch (IllegalStateException e) {
                    System.out.println("=2=OK: " + e);
                }
                System.out.println("=2=trying beginexclusive...");
                try {
                    card.beginExclusive();
                } catch (CardException e) {
                    System.out.println("=2=OK: " + e);
                }
                System.out.println("=2=trying to transmit...");
                CardChannel channel = card.getBasicChannel();
                try {
                    channel.transmit(new CommandAPDU(C1));
                } catch (CardException e) {
                    System.out.println("=2=OK: " + e);
                }
                while (exclusive) {
                    Thread.sleep(100);
                }
                System.out.println("=2=transmitting...");
                transmitTestCommand(channel);
                System.out.println("=2=OK...");
                System.out.println("=2=setting ok");
                otherOK = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
