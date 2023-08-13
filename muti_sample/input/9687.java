public class Cipher {
    private static final Debug debug =
                        Debug.getInstance("jca", "Cipher");
    public static final int ENCRYPT_MODE = 1;
    public static final int DECRYPT_MODE = 2;
    public static final int WRAP_MODE = 3;
    public static final int UNWRAP_MODE = 4;
    public static final int PUBLIC_KEY = 1;
    public static final int PRIVATE_KEY = 2;
    public static final int SECRET_KEY = 3;
    private Provider provider;
    private CipherSpi spi;
    private String transformation;
    private CryptoPermission cryptoPerm;
    private ExemptionMechanism exmech;
    private boolean initialized = false;
    private int opmode = 0;
    private static final String KEY_USAGE_EXTENSION_OID = "2.5.29.15";
    private CipherSpi firstSpi;
    private Service firstService;
    private Iterator serviceIterator;
    private List transforms;
    private final Object lock;
    protected Cipher(CipherSpi cipherSpi,
                     Provider provider,
                     String transformation) {
        if (!JceSecurityManager.INSTANCE.isCallerTrusted()) {
            throw new NullPointerException();
        }
        this.spi = cipherSpi;
        this.provider = provider;
        this.transformation = transformation;
        this.cryptoPerm = CryptoAllPermission.INSTANCE;
        this.lock = null;
    }
    Cipher(CipherSpi cipherSpi, String transformation) {
        this.spi = cipherSpi;
        this.transformation = transformation;
        this.cryptoPerm = CryptoAllPermission.INSTANCE;
        this.lock = null;
    }
    private Cipher(CipherSpi firstSpi, Service firstService,
            Iterator serviceIterator, String transformation, List transforms) {
        this.firstSpi = firstSpi;
        this.firstService = firstService;
        this.serviceIterator = serviceIterator;
        this.transforms = transforms;
        this.transformation = transformation;
        this.lock = new Object();
    }
    private static String[] tokenizeTransformation(String transformation)
            throws NoSuchAlgorithmException {
        if (transformation == null) {
            throw new NoSuchAlgorithmException("No transformation given");
        }
        String[] parts = new String[3];
        int count = 0;
        StringTokenizer parser = new StringTokenizer(transformation, "/");
        try {
            while (parser.hasMoreTokens() && count < 3) {
                parts[count++] = parser.nextToken().trim();
            }
            if (count == 0 || count == 2 || parser.hasMoreTokens()) {
                throw new NoSuchAlgorithmException("Invalid transformation"
                                               + " format:" +
                                               transformation);
            }
        } catch (NoSuchElementException e) {
            throw new NoSuchAlgorithmException("Invalid transformation " +
                                           "format:" + transformation);
        }
        if ((parts[0] == null) || (parts[0].length() == 0)) {
            throw new NoSuchAlgorithmException("Invalid transformation:" +
                                   "algorithm not specified-"
                                   + transformation);
        }
        return parts;
    }
    private final static String ATTR_MODE = "SupportedModes";
    private final static String ATTR_PAD  = "SupportedPaddings";
    private final static int S_NO    = 0;       
    private final static int S_MAYBE = 1;       
    private final static int S_YES   = 2;       
    private static class Transform {
        final String transform;
        final String suffix;
        final String mode;
        final String pad;
        Transform(String alg, String suffix, String mode, String pad) {
            this.transform = alg + suffix;
            this.suffix = suffix.toUpperCase(Locale.ENGLISH);
            this.mode = mode;
            this.pad = pad;
        }
        void setModePadding(CipherSpi spi) throws NoSuchAlgorithmException,
                NoSuchPaddingException {
            if (mode != null) {
                spi.engineSetMode(mode);
            }
            if (pad != null) {
                spi.engineSetPadding(pad);
            }
        }
        int supportsModePadding(Service s) {
            int smode = supportsMode(s);
            if (smode == S_NO) {
                return smode;
            }
            int spad = supportsPadding(s);
            return Math.min(smode, spad);
        }
        int supportsMode(Service s) {
            return supports(s, ATTR_MODE, mode);
        }
        int supportsPadding(Service s) {
            return supports(s, ATTR_PAD, pad);
        }
        private static int supports(Service s, String attrName, String value) {
            if (value == null) {
                return S_YES;
            }
            String regexp = s.getAttribute(attrName);
            if (regexp == null) {
                return S_MAYBE;
            }
            return matches(regexp, value) ? S_YES : S_NO;
        }
        private final static Map patternCache =
            Collections.synchronizedMap(new HashMap());
        private static boolean matches(String regexp, String str) {
            Pattern pattern = (Pattern)patternCache.get(regexp);
            if (pattern == null) {
                pattern = Pattern.compile(regexp);
                patternCache.put(regexp, pattern);
            }
            return pattern.matcher(str.toUpperCase(Locale.ENGLISH)).matches();
        }
    }
    private static List getTransforms(String transformation)
            throws NoSuchAlgorithmException {
        String[] parts = tokenizeTransformation(transformation);
        String alg = parts[0];
        String mode = parts[1];
        String pad = parts[2];
        if ((mode != null) && (mode.length() == 0)) {
            mode = null;
        }
        if ((pad != null) && (pad.length() == 0)) {
            pad = null;
        }
        if ((mode == null) && (pad == null)) {
            Transform tr = new Transform(alg, "", null, null);
            return Collections.singletonList(tr);
        } else { 
            List list = new ArrayList(4);
            list.add(new Transform(alg, "/" + mode + "/" + pad, null, null));
            list.add(new Transform(alg, "/" + mode, null, pad));
            list.add(new Transform(alg, "
            list.add(new Transform(alg, "", mode, pad));
            return list;
        }
    }
    private static Transform getTransform(Service s, List transforms) {
        String alg = s.getAlgorithm().toUpperCase(Locale.ENGLISH);
        for (Iterator t = transforms.iterator(); t.hasNext(); ) {
            Transform tr = (Transform)t.next();
            if (alg.endsWith(tr.suffix)) {
                return tr;
            }
        }
        return null;
    }
    public static final Cipher getInstance(String transformation)
            throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        List transforms = getTransforms(transformation);
        List cipherServices = new ArrayList(transforms.size());
        for (Iterator t = transforms.iterator(); t.hasNext(); ) {
            Transform transform = (Transform)t.next();
            cipherServices.add(new ServiceId("Cipher", transform.transform));
        }
        List services = GetInstance.getServices(cipherServices);
        Iterator t = services.iterator();
        Exception failure = null;
        while (t.hasNext()) {
            Service s = (Service)t.next();
            if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                continue;
            }
            Transform tr = getTransform(s, transforms);
            if (tr == null) {
                continue;
            }
            int canuse = tr.supportsModePadding(s);
            if (canuse == S_NO) {
                continue;
            }
            if (canuse == S_YES) {
                return new Cipher(null, s, t, transformation, transforms);
            } else { 
                try {
                    CipherSpi spi = (CipherSpi)s.newInstance(null);
                    tr.setModePadding(spi);
                    return new Cipher(spi, s, t, transformation, transforms);
                } catch (Exception e) {
                    failure = e;
                }
            }
        }
        throw new NoSuchAlgorithmException
            ("Cannot find any provider supporting " + transformation, failure);
    }
    public static final Cipher getInstance(String transformation,
                                           String provider)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            NoSuchPaddingException
    {
        if ((provider == null) || (provider.length() == 0)) {
            throw new IllegalArgumentException("Missing provider");
        }
        Provider p = Security.getProvider(provider);
        if (p == null) {
            throw new NoSuchProviderException("No such provider: " +
                                              provider);
        }
        return getInstance(transformation, p);
    }
    public static final Cipher getInstance(String transformation,
                                           Provider provider)
            throws NoSuchAlgorithmException, NoSuchPaddingException
    {
        if (provider == null) {
            throw new IllegalArgumentException("Missing provider");
        }
        Exception failure = null;
        List transforms = getTransforms(transformation);
        boolean providerChecked = false;
        String paddingError = null;
        for (Iterator t = transforms.iterator(); t.hasNext();) {
            Transform tr = (Transform)t.next();
            Service s = provider.getService("Cipher", tr.transform);
            if (s == null) {
                continue;
            }
            if (providerChecked == false) {
                Exception ve = JceSecurity.getVerificationResult(provider);
                if (ve != null) {
                    String msg = "JCE cannot authenticate the provider "
                        + provider.getName();
                    throw new SecurityException(msg, ve);
                }
                providerChecked = true;
            }
            if (tr.supportsMode(s) == S_NO) {
                continue;
            }
            if (tr.supportsPadding(s) == S_NO) {
                paddingError = tr.pad;
                continue;
            }
            try {
                CipherSpi spi = (CipherSpi)s.newInstance(null);
                tr.setModePadding(spi);
                Cipher cipher = new Cipher(spi, transformation);
                cipher.provider = s.getProvider();
                cipher.initCryptoPermission();
                return cipher;
            } catch (Exception e) {
                failure = e;
            }
        }
        if (failure instanceof NoSuchPaddingException) {
            throw (NoSuchPaddingException)failure;
        }
        if (paddingError != null) {
            throw new NoSuchPaddingException
                ("Padding not supported: " + paddingError);
        }
        throw new NoSuchAlgorithmException
                ("No such algorithm: " + transformation, failure);
    }
    private void initCryptoPermission() throws NoSuchAlgorithmException {
        if (JceSecurity.isRestricted() == false) {
            cryptoPerm = CryptoAllPermission.INSTANCE;
            exmech = null;
            return;
        }
        cryptoPerm = getConfiguredPermission(transformation);
        String exmechName = cryptoPerm.getExemptionMechanism();
        if (exmechName != null) {
            exmech = ExemptionMechanism.getInstance(exmechName);
        }
    }
    private static int warnCount = 10;
    void chooseFirstProvider() {
        if (spi != null) {
            return;
        }
        synchronized (lock) {
            if (spi != null) {
                return;
            }
            if (debug != null) {
                int w = --warnCount;
                if (w >= 0) {
                    debug.println("Cipher.init() not first method "
                        + "called, disabling delayed provider selection");
                    if (w == 0) {
                        debug.println("Further warnings of this type will "
                            + "be suppressed");
                    }
                    new Exception("Call trace").printStackTrace();
                }
            }
            Exception lastException = null;
            while ((firstService != null) || serviceIterator.hasNext()) {
                Service s;
                CipherSpi thisSpi;
                if (firstService != null) {
                    s = firstService;
                    thisSpi = firstSpi;
                    firstService = null;
                    firstSpi = null;
                } else {
                    s = (Service)serviceIterator.next();
                    thisSpi = null;
                }
                if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                    continue;
                }
                Transform tr = getTransform(s, transforms);
                if (tr == null) {
                    continue;
                }
                if (tr.supportsModePadding(s) == S_NO) {
                    continue;
                }
                try {
                    if (thisSpi == null) {
                        Object obj = s.newInstance(null);
                        if (obj instanceof CipherSpi == false) {
                            continue;
                        }
                        thisSpi = (CipherSpi)obj;
                    }
                    tr.setModePadding(thisSpi);
                    initCryptoPermission();
                    spi = thisSpi;
                    provider = s.getProvider();
                    firstService = null;
                    serviceIterator = null;
                    transforms = null;
                    return;
                } catch (Exception e) {
                    lastException = e;
                }
            }
            ProviderException e = new ProviderException
                    ("Could not construct CipherSpi instance");
            if (lastException != null) {
                e.initCause(lastException);
            }
            throw e;
        }
    }
    private final static int I_KEY       = 1;
    private final static int I_PARAMSPEC = 2;
    private final static int I_PARAMS    = 3;
    private final static int I_CERT      = 4;
    private void implInit(CipherSpi thisSpi, int type, int opmode, Key key,
            AlgorithmParameterSpec paramSpec, AlgorithmParameters params,
            SecureRandom random) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        switch (type) {
        case I_KEY:
            checkCryptoPerm(thisSpi, key);
            thisSpi.engineInit(opmode, key, random);
            break;
        case I_PARAMSPEC:
            checkCryptoPerm(thisSpi, key, paramSpec);
            thisSpi.engineInit(opmode, key, paramSpec, random);
            break;
        case I_PARAMS:
            checkCryptoPerm(thisSpi, key, params);
            thisSpi.engineInit(opmode, key, params, random);
            break;
        case I_CERT:
            checkCryptoPerm(thisSpi, key);
            thisSpi.engineInit(opmode, key, random);
            break;
        default:
            throw new AssertionError("Internal Cipher error: " + type);
        }
    }
    private void chooseProvider(int initType, int opmode, Key key,
            AlgorithmParameterSpec paramSpec,
            AlgorithmParameters params, SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        synchronized (lock) {
            if (spi != null) {
                implInit(spi, initType, opmode, key, paramSpec, params, random);
                return;
            }
            Exception lastException = null;
            while ((firstService != null) || serviceIterator.hasNext()) {
                Service s;
                CipherSpi thisSpi;
                if (firstService != null) {
                    s = firstService;
                    thisSpi = firstSpi;
                    firstService = null;
                    firstSpi = null;
                } else {
                    s = (Service)serviceIterator.next();
                    thisSpi = null;
                }
                if (s.supportsParameter(key) == false) {
                    continue;
                }
                if (JceSecurity.canUseProvider(s.getProvider()) == false) {
                    continue;
                }
                Transform tr = getTransform(s, transforms);
                if (tr == null) {
                    continue;
                }
                if (tr.supportsModePadding(s) == S_NO) {
                    continue;
                }
                try {
                    if (thisSpi == null) {
                        thisSpi = (CipherSpi)s.newInstance(null);
                    }
                    tr.setModePadding(thisSpi);
                    initCryptoPermission();
                    implInit(thisSpi, initType, opmode, key, paramSpec,
                                                        params, random);
                    provider = s.getProvider();
                    this.spi = thisSpi;
                    firstService = null;
                    serviceIterator = null;
                    transforms = null;
                    return;
                } catch (Exception e) {
                    if (lastException == null) {
                        lastException = e;
                    }
                }
            }
            if (lastException instanceof InvalidKeyException) {
                throw (InvalidKeyException)lastException;
            }
            if (lastException instanceof InvalidAlgorithmParameterException) {
                throw (InvalidAlgorithmParameterException)lastException;
            }
            if (lastException instanceof RuntimeException) {
                throw (RuntimeException)lastException;
            }
            String kName = (key != null) ? key.getClass().getName() : "(null)";
            throw new InvalidKeyException
                ("No installed provider supports this key: "
                + kName, lastException);
        }
    }
    public final Provider getProvider() {
        chooseFirstProvider();
        return this.provider;
    }
    public final String getAlgorithm() {
        return this.transformation;
    }
    public final int getBlockSize() {
        chooseFirstProvider();
        return spi.engineGetBlockSize();
    }
    public final int getOutputSize(int inputLen) {
        if (!initialized && !(this instanceof NullCipher)) {
            throw new IllegalStateException("Cipher not initialized");
        }
        if (inputLen < 0) {
            throw new IllegalArgumentException("Input size must be equal " +
                                               "to or greater than zero");
        }
        chooseFirstProvider();
        return spi.engineGetOutputSize(inputLen);
    }
    public final byte[] getIV() {
        chooseFirstProvider();
        return spi.engineGetIV();
    }
    public final AlgorithmParameters getParameters() {
        chooseFirstProvider();
        return spi.engineGetParameters();
    }
    public final ExemptionMechanism getExemptionMechanism() {
        chooseFirstProvider();
        return exmech;
    }
    private void checkCryptoPerm(CipherSpi checkSpi, Key key)
            throws InvalidKeyException {
        if (cryptoPerm == CryptoAllPermission.INSTANCE) {
            return;
        }
        AlgorithmParameterSpec params;
        try {
            params = getAlgorithmParameterSpec(checkSpi.engineGetParameters());
        } catch (InvalidParameterSpecException ipse) {
            throw new InvalidKeyException
                ("Unsupported default algorithm parameters");
        }
        if (!passCryptoPermCheck(checkSpi, key, params)) {
            throw new InvalidKeyException(
                "Illegal key size or default parameters");
        }
    }
    private void checkCryptoPerm(CipherSpi checkSpi, Key key,
            AlgorithmParameterSpec params) throws InvalidKeyException,
            InvalidAlgorithmParameterException {
        if (cryptoPerm == CryptoAllPermission.INSTANCE) {
            return;
        }
        if (!passCryptoPermCheck(checkSpi, key, null)) {
            throw new InvalidKeyException("Illegal key size");
        }
        if ((params != null) && (!passCryptoPermCheck(checkSpi, key, params))) {
            throw new InvalidAlgorithmParameterException("Illegal parameters");
        }
    }
    private void checkCryptoPerm(CipherSpi checkSpi, Key key,
            AlgorithmParameters params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        if (cryptoPerm == CryptoAllPermission.INSTANCE) {
            return;
        }
        AlgorithmParameterSpec pSpec;
        try {
            pSpec = getAlgorithmParameterSpec(params);
        } catch (InvalidParameterSpecException ipse) {
            throw new InvalidAlgorithmParameterException
                ("Failed to retrieve algorithm parameter specification");
        }
        checkCryptoPerm(checkSpi, key, pSpec);
    }
    private boolean passCryptoPermCheck(CipherSpi checkSpi, Key key,
                                        AlgorithmParameterSpec params)
            throws InvalidKeyException {
        String em = cryptoPerm.getExemptionMechanism();
        int keySize = checkSpi.engineGetKeySize(key);
        String algComponent;
        int index = transformation.indexOf('/');
        if (index != -1) {
            algComponent = transformation.substring(0, index);
        } else {
            algComponent = transformation;
        }
        CryptoPermission checkPerm =
            new CryptoPermission(algComponent, keySize, params, em);
        if (!cryptoPerm.implies(checkPerm)) {
            if (debug != null) {
                debug.println("Crypto Permission check failed");
                debug.println("granted: " + cryptoPerm);
                debug.println("requesting: " + checkPerm);
            }
            return false;
        }
        if (exmech == null) {
            return true;
        }
        try {
            if (!exmech.isCryptoAllowed(key)) {
                if (debug != null) {
                    debug.println(exmech.getName() + " isn't enforced");
                }
                return false;
            }
        } catch (ExemptionMechanismException eme) {
            if (debug != null) {
                debug.println("Cannot determine whether "+
                              exmech.getName() + " has been enforced");
                eme.printStackTrace();
            }
            return false;
        }
        return true;
    }
    private static void checkOpmode(int opmode) {
        if ((opmode < ENCRYPT_MODE) || (opmode > UNWRAP_MODE)) {
            throw new InvalidParameterException("Invalid operation mode");
        }
    }
    public final void init(int opmode, Key key) throws InvalidKeyException {
        init(opmode, key, JceSecurity.RANDOM);
    }
    public final void init(int opmode, Key key, SecureRandom random)
            throws InvalidKeyException
    {
        initialized = false;
        checkOpmode(opmode);
        if (spi != null) {
            checkCryptoPerm(spi, key);
            spi.engineInit(opmode, key, random);
        } else {
            try {
                chooseProvider(I_KEY, opmode, key, null, null, random);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException(e);
            }
        }
        initialized = true;
        this.opmode = opmode;
    }
    public final void init(int opmode, Key key, AlgorithmParameterSpec params)
            throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        init(opmode, key, params, JceSecurity.RANDOM);
    }
    public final void init(int opmode, Key key, AlgorithmParameterSpec params,
                           SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        initialized = false;
        checkOpmode(opmode);
        if (spi != null) {
            checkCryptoPerm(spi, key, params);
            spi.engineInit(opmode, key, params, random);
        } else {
            chooseProvider(I_PARAMSPEC, opmode, key, params, null, random);
        }
        initialized = true;
        this.opmode = opmode;
    }
    public final void init(int opmode, Key key, AlgorithmParameters params)
            throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        init(opmode, key, params, JceSecurity.RANDOM);
    }
    public final void init(int opmode, Key key, AlgorithmParameters params,
                           SecureRandom random)
            throws InvalidKeyException, InvalidAlgorithmParameterException
    {
        initialized = false;
        checkOpmode(opmode);
        if (spi != null) {
            checkCryptoPerm(spi, key, params);
            spi.engineInit(opmode, key, params, random);
        } else {
            chooseProvider(I_PARAMS, opmode, key, null, params, random);
        }
        initialized = true;
        this.opmode = opmode;
    }
    public final void init(int opmode, Certificate certificate)
            throws InvalidKeyException
    {
        init(opmode, certificate, JceSecurity.RANDOM);
    }
    public final void init(int opmode, Certificate certificate,
                           SecureRandom random)
            throws InvalidKeyException
    {
        initialized = false;
        checkOpmode(opmode);
        if (certificate instanceof java.security.cert.X509Certificate) {
            X509Certificate cert = (X509Certificate)certificate;
            Set critSet = cert.getCriticalExtensionOIDs();
            if (critSet != null && !critSet.isEmpty()
                && critSet.contains(KEY_USAGE_EXTENSION_OID)) {
                boolean[] keyUsageInfo = cert.getKeyUsage();
                if ((keyUsageInfo != null) &&
                    (((opmode == Cipher.ENCRYPT_MODE) &&
                      (keyUsageInfo.length > 3) &&
                      (keyUsageInfo[3] == false)) ||
                     ((opmode == Cipher.WRAP_MODE) &&
                      (keyUsageInfo.length > 2) &&
                      (keyUsageInfo[2] == false)))) {
                    throw new InvalidKeyException("Wrong key usage");
                }
            }
        }
        PublicKey publicKey =
            (certificate==null? null:certificate.getPublicKey());
        if (spi != null) {
            checkCryptoPerm(spi, publicKey);
            spi.engineInit(opmode, publicKey, random);
        } else {
            try {
                chooseProvider(I_CERT, opmode, publicKey, null, null, random);
            } catch (InvalidAlgorithmParameterException e) {
                throw new InvalidKeyException(e);
            }
        }
        initialized = true;
        this.opmode = opmode;
    }
    private void checkCipherState() {
        if (!(this instanceof NullCipher)) {
            if (!initialized) {
                throw new IllegalStateException("Cipher not initialized");
            }
            if ((opmode != Cipher.ENCRYPT_MODE) &&
                (opmode != Cipher.DECRYPT_MODE)) {
                throw new IllegalStateException("Cipher not initialized " +
                                                "for encryption/decryption");
            }
        }
    }
    public final byte[] update(byte[] input) {
        checkCipherState();
        if (input == null) {
            throw new IllegalArgumentException("Null input buffer");
        }
        chooseFirstProvider();
        if (input.length == 0) {
            return null;
        }
        return spi.engineUpdate(input, 0, input.length);
    }
    public final byte[] update(byte[] input, int inputOffset, int inputLen) {
        checkCipherState();
        if (input == null || inputOffset < 0
            || inputLen > (input.length - inputOffset) || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        if (inputLen == 0) {
            return null;
        }
        return spi.engineUpdate(input, inputOffset, inputLen);
    }
    public final int update(byte[] input, int inputOffset, int inputLen,
                            byte[] output)
            throws ShortBufferException {
        checkCipherState();
        if (input == null || inputOffset < 0
            || inputLen > (input.length - inputOffset) || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        if (inputLen == 0) {
            return 0;
        }
        return spi.engineUpdate(input, inputOffset, inputLen,
                                      output, 0);
    }
    public final int update(byte[] input, int inputOffset, int inputLen,
                            byte[] output, int outputOffset)
            throws ShortBufferException {
        checkCipherState();
        if (input == null || inputOffset < 0
            || inputLen > (input.length - inputOffset) || inputLen < 0
            || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        if (inputLen == 0) {
            return 0;
        }
        return spi.engineUpdate(input, inputOffset, inputLen,
                                      output, outputOffset);
    }
    public final int update(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException {
        checkCipherState();
        if ((input == null) || (output == null)) {
            throw new IllegalArgumentException("Buffers must not be null");
        }
        if (input == output) {
            throw new IllegalArgumentException("Input and output buffers must "
                + "not be the same object, consider using buffer.duplicate()");
        }
        if (output.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        chooseFirstProvider();
        return spi.engineUpdate(input, output);
    }
    public final byte[] doFinal()
            throws IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        chooseFirstProvider();
        return spi.engineDoFinal(null, 0, 0);
    }
    public final int doFinal(byte[] output, int outputOffset)
            throws IllegalBlockSizeException, ShortBufferException,
               BadPaddingException {
        checkCipherState();
        if ((output == null) || (outputOffset < 0)) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        return spi.engineDoFinal(null, 0, 0, output, outputOffset);
    }
    public final byte[] doFinal(byte[] input)
            throws IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        if (input == null) {
            throw new IllegalArgumentException("Null input buffer");
        }
        chooseFirstProvider();
        return spi.engineDoFinal(input, 0, input.length);
    }
    public final byte[] doFinal(byte[] input, int inputOffset, int inputLen)
            throws IllegalBlockSizeException, BadPaddingException {
        checkCipherState();
        if (input == null || inputOffset < 0
            || inputLen > (input.length - inputOffset) || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        return spi.engineDoFinal(input, inputOffset, inputLen);
    }
    public final int doFinal(byte[] input, int inputOffset, int inputLen,
                             byte[] output)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
        checkCipherState();
        if (input == null || inputOffset < 0
            || inputLen > (input.length - inputOffset) || inputLen < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        return spi.engineDoFinal(input, inputOffset, inputLen,
                                       output, 0);
    }
    public final int doFinal(byte[] input, int inputOffset, int inputLen,
                             byte[] output, int outputOffset)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
        checkCipherState();
        if (input == null || inputOffset < 0
            || inputLen > (input.length - inputOffset) || inputLen < 0
            || outputOffset < 0) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        return spi.engineDoFinal(input, inputOffset, inputLen,
                                       output, outputOffset);
    }
    public final int doFinal(ByteBuffer input, ByteBuffer output)
            throws ShortBufferException, IllegalBlockSizeException,
            BadPaddingException {
        checkCipherState();
        if ((input == null) || (output == null)) {
            throw new IllegalArgumentException("Buffers must not be null");
        }
        if (input == output) {
            throw new IllegalArgumentException("Input and output buffers must "
                + "not be the same object, consider using buffer.duplicate()");
        }
        if (output.isReadOnly()) {
            throw new ReadOnlyBufferException();
        }
        chooseFirstProvider();
        return spi.engineDoFinal(input, output);
    }
    public final byte[] wrap(Key key)
            throws IllegalBlockSizeException, InvalidKeyException {
        if (!(this instanceof NullCipher)) {
            if (!initialized) {
                throw new IllegalStateException("Cipher not initialized");
            }
            if (opmode != Cipher.WRAP_MODE) {
                throw new IllegalStateException("Cipher not initialized " +
                                                "for wrapping keys");
            }
        }
        chooseFirstProvider();
        return spi.engineWrap(key);
    }
    public final Key unwrap(byte[] wrappedKey,
                            String wrappedKeyAlgorithm,
                            int wrappedKeyType)
            throws InvalidKeyException, NoSuchAlgorithmException {
        if (!(this instanceof NullCipher)) {
            if (!initialized) {
                throw new IllegalStateException("Cipher not initialized");
            }
            if (opmode != Cipher.UNWRAP_MODE) {
                throw new IllegalStateException("Cipher not initialized " +
                                                "for unwrapping keys");
            }
        }
        if ((wrappedKeyType != SECRET_KEY) &&
            (wrappedKeyType != PRIVATE_KEY) &&
            (wrappedKeyType != PUBLIC_KEY)) {
            throw new InvalidParameterException("Invalid key type");
        }
        chooseFirstProvider();
        return spi.engineUnwrap(wrappedKey,
                                      wrappedKeyAlgorithm,
                                      wrappedKeyType);
    }
    private AlgorithmParameterSpec getAlgorithmParameterSpec(
                                      AlgorithmParameters params)
            throws InvalidParameterSpecException {
        if (params == null) {
            return null;
        }
        String alg = params.getAlgorithm().toUpperCase(Locale.ENGLISH);
        if (alg.equalsIgnoreCase("RC2")) {
            return params.getParameterSpec(RC2ParameterSpec.class);
        }
        if (alg.equalsIgnoreCase("RC5")) {
            return params.getParameterSpec(RC5ParameterSpec.class);
        }
        if (alg.startsWith("PBE")) {
            return params.getParameterSpec(PBEParameterSpec.class);
        }
        if (alg.startsWith("DES")) {
            return params.getParameterSpec(IvParameterSpec.class);
        }
        return null;
    }
    private static CryptoPermission getConfiguredPermission(
            String transformation) throws NullPointerException,
            NoSuchAlgorithmException {
        if (transformation == null) throw new NullPointerException();
        String[] parts = tokenizeTransformation(transformation);
        return JceSecurityManager.INSTANCE.getCryptoPermission(parts[0]);
    }
    public static final int getMaxAllowedKeyLength(String transformation)
            throws NoSuchAlgorithmException {
        CryptoPermission cp = getConfiguredPermission(transformation);
        return cp.getMaxKeySize();
    }
    public static final AlgorithmParameterSpec getMaxAllowedParameterSpec(
            String transformation) throws NoSuchAlgorithmException {
        CryptoPermission cp = getConfiguredPermission(transformation);
        return cp.getAlgorithmParameterSpec();
    }
    public final void updateAAD(byte[] src) {
        if (src == null) {
            throw new IllegalArgumentException("src buffer is null");
        }
        updateAAD(src, 0, src.length);
    }
    public final void updateAAD(byte[] src, int offset, int len) {
        checkCipherState();
        if ((src == null) || (offset < 0) || (len < 0)
                || ((len + offset) > src.length)) {
            throw new IllegalArgumentException("Bad arguments");
        }
        chooseFirstProvider();
        if (len == 0) {
            return;
        }
        spi.engineUpdateAAD(src, offset, len);
    }
    public final void updateAAD(ByteBuffer src) {
        checkCipherState();
        if (src == null) {
            throw new IllegalArgumentException("src ByteBuffer is null");
        }
        chooseFirstProvider();
        if (src.remaining() == 0) {
            return;
        }
        spi.engineUpdateAAD(src);
    }
}
