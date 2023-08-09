class Terminator {
    private static SignalHandler handler = null;
    static void setup() {
        if (handler != null) return;
        SignalHandler sh = new SignalHandler() {
            public void handle(Signal sig) {
                Shutdown.exit(sig.getNumber() + 0200);
            }
        };
        handler = sh;
        try {
            Signal.handle(new Signal("HUP"), sh);
            Signal.handle(new Signal("INT"), sh);
            Signal.handle(new Signal("TERM"), sh);
        } catch (IllegalArgumentException e) {
        }
    }
    static void teardown() {
    }
}
