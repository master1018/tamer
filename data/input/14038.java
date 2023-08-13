public class TestPresent {
    private static class Timer {
        private long time = System.currentTimeMillis();
        long update() {
            long t = System.currentTimeMillis();
            long diff = t - time;
            time = t;
            return diff;
        }
        long print() {
            long t = update();
            System.out.println("Elapsed time: " + t + " ms.");
            return t;
        }
    }
    private static boolean isFalse(boolean b) throws Exception {
        if (b) {
            throw new Exception("not false");
        }
        return b;
    }
    private static boolean isTrue(boolean b) throws Exception {
        if (!b) {
            throw new Exception("not true");
        }
        return b;
    }
    public static void main(String[] args) throws Exception {
        TerminalFactory factory = TerminalFactory.getInstance("PC/SC", null);
        System.out.println(factory);
        List<CardTerminal> terminals = factory.terminals().list();
        System.out.println("Terminals: " + terminals);
        if (terminals.isEmpty()) {
            throw new Exception("No card terminals available");
        }
        CardTerminal terminal = terminals.get(0);
        while (terminal.isCardPresent()) {
            System.out.println("*** Remove card!");
            Thread.sleep(1000);
        }
        Timer timer = new Timer();
        System.out.println("Testing waitForCardAbsent() with card already absent...");
        isTrue(terminal.waitForCardAbsent(10));
        timer.print();
        isTrue(terminal.waitForCardAbsent(100));
        timer.print();
        isTrue(terminal.waitForCardAbsent(10000));
        timer.print();
        isTrue(terminal.waitForCardAbsent(0));
        timer.print();
        System.out.println("Testing waitForCardPresent() timeout...");
        isFalse(terminal.waitForCardPresent(10));
        timer.print();
        isFalse(terminal.waitForCardPresent(100));
        timer.print();
        isFalse(terminal.waitForCardPresent(1000));
        timer.print();
        isFalse(terminal.isCardPresent());
        isFalse(terminal.isCardPresent());
        System.out.println("*** Insert card!");
        isTrue(terminal.waitForCardPresent(0));
        timer.print();
        isTrue(terminal.isCardPresent());
        isTrue(terminal.isCardPresent());
        System.out.println("Testing waitForCardPresent() with card already present...");
        isTrue(terminal.waitForCardPresent(0));
        timer.print();
        isTrue(terminal.waitForCardPresent(10000));
        timer.print();
        isTrue(terminal.waitForCardPresent(100));
        timer.print();
        isTrue(terminal.waitForCardPresent(10));
        timer.print();
        System.out.println("Testing waitForCardAbsent() timeout...");
        isFalse(terminal.waitForCardAbsent(1000));
        timer.print();
        isFalse(terminal.waitForCardAbsent(100));
        timer.print();
        isFalse(terminal.waitForCardAbsent(10));
        timer.print();
        System.out.println("*** Remove card!");
        isTrue(terminal.waitForCardAbsent(0));
        timer.print();
        isFalse(terminal.isCardPresent());
        isFalse(terminal.isCardPresent());
        System.out.println("OK.");
    }
}
