public abstract class KeySelector {
    public static class Purpose {
        private final String name;
        private Purpose(String name)    { this.name = name; }
        public String toString()        { return name; }
        public static final Purpose SIGN = new Purpose("sign");
        public static final Purpose VERIFY = new Purpose("verify");
        public static final Purpose ENCRYPT = new Purpose("encrypt");
        public static final Purpose DECRYPT = new Purpose("decrypt");
    }
    protected KeySelector() {}
    public abstract KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
        AlgorithmMethod method, XMLCryptoContext context)
        throws KeySelectorException;
    public static KeySelector singletonKeySelector(Key key) {
        return new SingletonKeySelector(key);
    }
    private static class SingletonKeySelector extends KeySelector {
        private final Key key;
        SingletonKeySelector(Key key) {
            if (key == null) {
                throw new NullPointerException();
            }
            this.key = key;
        }
        public KeySelectorResult select(KeyInfo keyInfo, Purpose purpose,
            AlgorithmMethod method, XMLCryptoContext context)
            throws KeySelectorException {
            return new KeySelectorResult() {
                public Key getKey() {
                    return key;
                }
            };
        }
    }
}
