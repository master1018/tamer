public final class SunPCSC extends Provider {
    private static final long serialVersionUID = 6168388284028876579L;
    public SunPCSC() {
        super("SunPCSC", 1.7d, "Sun PC/SC provider");
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                put("TerminalFactory.PC/SC", "sun.security.smartcardio.SunPCSC$Factory");
                return null;
            }
        });
    }
    public static final class Factory extends TerminalFactorySpi {
        public Factory(Object obj) throws PCSCException {
            if (obj != null) {
                throw new IllegalArgumentException
                    ("SunPCSC factory does not use parameters");
            }
            PCSC.checkAvailable();
            PCSCTerminals.initContext();
        }
        protected CardTerminals engineTerminals() {
            return new PCSCTerminals();
        }
    }
}
