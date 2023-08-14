public final class TerminalFactory {
    private final static String PROP_NAME =
                        "javax.smartcardio.TerminalFactory.DefaultType";
    private final static String defaultType;
    private final static TerminalFactory defaultFactory;
    static {
        String type = AccessController.doPrivileged
                            (new GetPropertyAction(PROP_NAME, "PC/SC")).trim();
        TerminalFactory factory = null;
        try {
            factory = TerminalFactory.getInstance(type, null);
        } catch (Exception e) {
        }
        if (factory == null) {
            try {
                type = "PC/SC";
                Provider sun = Security.getProvider("SunPCSC");
                if (sun == null) {
                    Class clazz = Class.forName("sun.security.smartcardio.SunPCSC");
                    sun = (Provider)clazz.newInstance();
                }
                factory = TerminalFactory.getInstance(type, null, sun);
            } catch (Exception e) {
            }
        }
        if (factory == null) {
            type = "None";
            factory = new TerminalFactory
                        (NoneFactorySpi.INSTANCE, NoneProvider.INSTANCE, "None");
        }
        defaultType = type;
        defaultFactory = factory;
    }
    private static final class NoneProvider extends Provider {
        final static Provider INSTANCE = new NoneProvider();
        private NoneProvider() {
            super("None", 1.0d, "none");
        }
    }
    private static final class NoneFactorySpi extends TerminalFactorySpi {
        final static TerminalFactorySpi INSTANCE = new NoneFactorySpi();
        private NoneFactorySpi() {
        }
        protected CardTerminals engineTerminals() {
            return NoneCardTerminals.INSTANCE;
        }
    }
    private static final class NoneCardTerminals extends CardTerminals {
        final static CardTerminals INSTANCE = new NoneCardTerminals();
        private NoneCardTerminals() {
        }
        public List<CardTerminal> list(State state) throws CardException {
            if (state == null) {
                throw new NullPointerException();
            }
            return Collections.emptyList();
        }
        public boolean waitForChange(long timeout) throws CardException {
            throw new IllegalStateException("no terminals");
        }
    }
    private final TerminalFactorySpi spi;
    private final Provider provider;
    private final String type;
    private TerminalFactory(TerminalFactorySpi spi, Provider provider, String type) {
        this.spi = spi;
        this.provider = provider;
        this.type = type;
    }
    public static String getDefaultType() {
        return defaultType;
    }
    public static TerminalFactory getDefault() {
        return defaultFactory;
    }
    public static TerminalFactory getInstance(String type, Object params)
            throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("TerminalFactory",
            TerminalFactorySpi.class, type, params);
        return new TerminalFactory((TerminalFactorySpi)instance.impl,
            instance.provider, type);
    }
    public static TerminalFactory getInstance(String type, Object params,
            String provider) throws NoSuchAlgorithmException, NoSuchProviderException {
        Instance instance = GetInstance.getInstance("TerminalFactory",
            TerminalFactorySpi.class, type, params, provider);
        return new TerminalFactory((TerminalFactorySpi)instance.impl,
            instance.provider, type);
    }
    public static TerminalFactory getInstance(String type, Object params,
            Provider provider) throws NoSuchAlgorithmException {
        Instance instance = GetInstance.getInstance("TerminalFactory",
            TerminalFactorySpi.class, type, params, provider);
        return new TerminalFactory((TerminalFactorySpi)instance.impl,
            instance.provider, type);
    }
    public Provider getProvider() {
        return provider;
    }
    public String getType() {
        return type;
    }
    public CardTerminals terminals() {
        return spi.engineTerminals();
    }
    public String toString() {
        return "TerminalFactory for type " + type + " from provider "
            + provider.getName();
    }
}
