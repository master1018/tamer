public class DisabledAlgorithmConstraints implements AlgorithmConstraints {
    public final static String PROPERTY_CERTPATH_DISABLED_ALGS =
            "jdk.certpath.disabledAlgorithms";
    public final static String PROPERTY_TLS_DISABLED_ALGS =
            "jdk.tls.disabledAlgorithms";
    private static Map<String, String[]> disabledAlgorithmsMap =
            Collections.synchronizedMap(new HashMap<String, String[]>());
    private static Map<String, KeySizeConstraints> keySizeConstraintsMap =
        Collections.synchronizedMap(new HashMap<String, KeySizeConstraints>());
    private String[] disabledAlgorithms;
    private KeySizeConstraints keySizeConstraints;
    public DisabledAlgorithmConstraints(String propertyName) {
        synchronized (disabledAlgorithmsMap) {
            if(!disabledAlgorithmsMap.containsKey(propertyName)) {
                loadDisabledAlgorithmsMap(propertyName);
            }
            disabledAlgorithms = disabledAlgorithmsMap.get(propertyName);
            keySizeConstraints = keySizeConstraintsMap.get(propertyName);
        }
    }
    @Override
    final public boolean permits(Set<CryptoPrimitive> primitives,
            String algorithm, AlgorithmParameters parameters) {
        if (algorithm == null || algorithm.length() == 0) {
            throw new IllegalArgumentException("No algorithm name specified");
        }
        if (primitives == null || primitives.isEmpty()) {
            throw new IllegalArgumentException(
                        "No cryptographic primitive specified");
        }
        Set<String> elements = null;
        for (String disabled : disabledAlgorithms) {
            if (disabled == null || disabled.isEmpty()) {
                continue;
            }
            if (disabled.equalsIgnoreCase(algorithm)) {
                return false;
            }
            if (elements == null) {
                elements = decomposes(algorithm);
            }
            for (String element : elements) {
                if (disabled.equalsIgnoreCase(element)) {
                    return false;
                }
            }
        }
        return true;
    }
    @Override
    final public boolean permits(Set<CryptoPrimitive> primitives, Key key) {
        return checkConstraints(primitives, "", key, null);
    }
    @Override
    final public boolean permits(Set<CryptoPrimitive> primitives,
            String algorithm, Key key, AlgorithmParameters parameters) {
        if (algorithm == null || algorithm.length() == 0) {
            throw new IllegalArgumentException("No algorithm name specified");
        }
        return checkConstraints(primitives, algorithm, key, parameters);
    }
    protected Set<String> decomposes(String algorithm) {
        if (algorithm == null || algorithm.length() == 0) {
            return new HashSet<String>();
        }
        Pattern transPattern = Pattern.compile("/");
        String[] transTockens = transPattern.split(algorithm);
        Set<String> elements = new HashSet<String>();
        for (String transTocken : transTockens) {
            if (transTocken == null || transTocken.length() == 0) {
                continue;
            }
            Pattern pattern =
                    Pattern.compile("with|and", Pattern.CASE_INSENSITIVE);
            String[] tokens = pattern.split(transTocken);
            for (String token : tokens) {
                if (token == null || token.length() == 0) {
                    continue;
                }
                elements.add(token);
            }
        }
        if (elements.contains("SHA1") && !elements.contains("SHA-1")) {
            elements.add("SHA-1");
        }
        if (elements.contains("SHA-1") && !elements.contains("SHA1")) {
            elements.add("SHA1");
        }
        if (elements.contains("SHA224") && !elements.contains("SHA-224")) {
            elements.add("SHA-224");
        }
        if (elements.contains("SHA-224") && !elements.contains("SHA224")) {
            elements.add("SHA224");
        }
        if (elements.contains("SHA256") && !elements.contains("SHA-256")) {
            elements.add("SHA-256");
        }
        if (elements.contains("SHA-256") && !elements.contains("SHA256")) {
            elements.add("SHA256");
        }
        if (elements.contains("SHA384") && !elements.contains("SHA-384")) {
            elements.add("SHA-384");
        }
        if (elements.contains("SHA-384") && !elements.contains("SHA384")) {
            elements.add("SHA384");
        }
        if (elements.contains("SHA512") && !elements.contains("SHA-512")) {
            elements.add("SHA-512");
        }
        if (elements.contains("SHA-512") && !elements.contains("SHA512")) {
            elements.add("SHA512");
        }
        return elements;
    }
    private boolean checkConstraints(Set<CryptoPrimitive> primitives,
            String algorithm, Key key, AlgorithmParameters parameters) {
        if (key == null) {
            throw new IllegalArgumentException("The key cannot be null");
        }
        if (algorithm != null && algorithm.length() != 0) {
            if (!permits(primitives, algorithm, parameters)) {
                return false;
            }
        }
        if (!permits(primitives, key.getAlgorithm(), null)) {
            return false;
        }
        if (keySizeConstraints.disables(key)) {
            return false;
        }
        return true;
    }
    private static void loadDisabledAlgorithmsMap(
            final String propertyName) {
        String property = AccessController.doPrivileged(
            new PrivilegedAction<String>() {
                public String run() {
                    return Security.getProperty(propertyName);
                }
            });
        String[] algorithmsInProperty = null;
        if (property != null && !property.isEmpty()) {
            if (property.charAt(0) == '"' &&
                    property.charAt(property.length() - 1) == '"') {
                property = property.substring(1, property.length() - 1);
            }
            algorithmsInProperty = property.split(",");
            for (int i = 0; i < algorithmsInProperty.length; i++) {
                algorithmsInProperty[i] = algorithmsInProperty[i].trim();
            }
        }
        if (algorithmsInProperty == null) {
            algorithmsInProperty = new String[0];
        }
        disabledAlgorithmsMap.put(propertyName, algorithmsInProperty);
        KeySizeConstraints keySizeConstraints =
            new KeySizeConstraints(algorithmsInProperty);
        keySizeConstraintsMap.put(propertyName, keySizeConstraints);
    }
    private static class KeySizeConstraints {
        private static final Pattern pattern = Pattern.compile(
                "(\\S+)\\s+keySize\\s*(<=|<|==|!=|>|>=)\\s*(\\d+)");
        private Map<String, Set<KeySizeConstraint>> constraintsMap =
            Collections.synchronizedMap(
                        new HashMap<String, Set<KeySizeConstraint>>());
        public KeySizeConstraints(String[] restrictions) {
            for (String restriction : restrictions) {
                if (restriction == null || restriction.isEmpty()) {
                    continue;
                }
                Matcher matcher = pattern.matcher(restriction);
                if (matcher.matches()) {
                    String algorithm = matcher.group(1);
                    KeySizeConstraint.Operator operator =
                             KeySizeConstraint.Operator.of(matcher.group(2));
                    int length = Integer.parseInt(matcher.group(3));
                    algorithm = algorithm.toLowerCase(Locale.ENGLISH);
                    synchronized (constraintsMap) {
                        if (!constraintsMap.containsKey(algorithm)) {
                            constraintsMap.put(algorithm,
                                new HashSet<KeySizeConstraint>());
                        }
                        Set<KeySizeConstraint> constraintSet =
                            constraintsMap.get(algorithm);
                        KeySizeConstraint constraint =
                            new KeySizeConstraint(operator, length);
                        constraintSet.add(constraint);
                    }
                }
            }
        }
        public boolean disables(Key key) {
            String algorithm = key.getAlgorithm().toLowerCase(Locale.ENGLISH);
            synchronized (constraintsMap) {
                if (constraintsMap.containsKey(algorithm)) {
                    Set<KeySizeConstraint> constraintSet =
                                        constraintsMap.get(algorithm);
                    for (KeySizeConstraint constraint : constraintSet) {
                        if (constraint.disables(key)) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }
    private static class KeySizeConstraint {
        static enum Operator {
            EQ,         
            NE,         
            LT,         
            LE,         
            GT,         
            GE;         
            static Operator of(String s) {
                switch (s) {
                    case "==":
                        return EQ;
                    case "!=":
                        return NE;
                    case "<":
                        return LT;
                    case "<=":
                        return LE;
                    case ">":
                        return GT;
                    case ">=":
                        return GE;
                }
                throw new IllegalArgumentException(
                        s + " is not a legal Operator");
            }
        }
        private int minSize;            
        private int maxSize;            
        private int prohibitedSize = -1;    
        public KeySizeConstraint(Operator operator, int length) {
            switch (operator) {
                case EQ:      
                    this.minSize = 0;
                    this.maxSize = Integer.MAX_VALUE;
                    prohibitedSize = length;
                    break;
                case NE:
                    this.minSize = length;
                    this.maxSize = length;
                    break;
                case LT:
                    this.minSize = length;
                    this.maxSize = Integer.MAX_VALUE;
                    break;
                case LE:
                    this.minSize = length + 1;
                    this.maxSize = Integer.MAX_VALUE;
                    break;
                case GT:
                    this.minSize = 0;
                    this.maxSize = length;
                    break;
                case GE:
                    this.minSize = 0;
                    this.maxSize = length > 1 ? (length - 1) : 0;
                    break;
                default:
                    this.minSize = Integer.MAX_VALUE;
                    this.maxSize = -1;
            }
        }
        public boolean disables(Key key) {
            int size = -1;
            if (key instanceof SecretKey) {
                SecretKey sk = (SecretKey)key;
                if (sk.getFormat().equals("RAW") && sk.getEncoded() != null) {
                    size = sk.getEncoded().length * 8;
                }
            }
            if (key instanceof RSAKey) {
                RSAKey pubk = (RSAKey)key;
                size = pubk.getModulus().bitLength();
            } else if (key instanceof ECKey) {
                ECKey pubk = (ECKey)key;
                size = pubk.getParams().getOrder().bitLength();
            } else if (key instanceof DSAKey) {
                DSAKey pubk = (DSAKey)key;
                size = pubk.getParams().getP().bitLength();
            } else if (key instanceof DHKey) {
                DHKey pubk = (DHKey)key;
                size = pubk.getParams().getP().bitLength();
            } 
            if (size == 0) {
                return true;    
            }
            if (size >= 0) {
                return ((size < minSize) || (size > maxSize) ||
                    (prohibitedSize == size));
            }
            return false;
        }
    }
}
