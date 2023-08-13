final class Config {
    static final int ERR_HALT       = 1;
    static final int ERR_IGNORE_ALL = 2;
    static final int ERR_IGNORE_LIB = 3;
    private static final boolean staticAllowSingleThreadedModules;
    static {
        String p = "sun.security.pkcs11.allowSingleThreadedModules";
        String s = AccessController.doPrivileged(new GetPropertyAction(p));
        if ("false".equalsIgnoreCase(s)) {
            staticAllowSingleThreadedModules = false;
        } else {
            staticAllowSingleThreadedModules = true;
        }
    }
    private final static Map<String,Config> configMap =
                                        new HashMap<String,Config>();
    static Config getConfig(final String name, final InputStream stream) {
        Config config = configMap.get(name);
        if (config != null) {
            return config;
        }
        try {
            config = new Config(name, stream);
            configMap.put(name, config);
            return config;
        } catch (Exception e) {
            throw new ProviderException("Error parsing configuration", e);
        }
    }
    static Config removeConfig(String name) {
        return configMap.remove(name);
    }
    private final static boolean DEBUG = false;
    private static void debug(Object o) {
        if (DEBUG) {
            System.out.println(o);
        }
    }
    private Reader reader;
    private StreamTokenizer st;
    private Set<String> parsedKeywords;
    private String name;
    private String library;
    private String description;
    private int slotID = -1;
    private int slotListIndex = -1;
    private Set<Long> enabledMechanisms;
    private Set<Long> disabledMechanisms;
    private boolean showInfo = false;
    private TemplateManager templateManager;
    private int handleStartupErrors = ERR_HALT;
    private boolean keyStoreCompatibilityMode = true;
    private boolean explicitCancel = true;
    private int insertionCheckInterval = 2000;
    private boolean omitInitialize = false;
    private boolean allowSingleThreadedModules = true;
    private String functionList = "C_GetFunctionList";
    private boolean nssUseSecmod;
    private String nssLibraryDirectory;
    private String nssSecmodDirectory;
    private String nssModule;
    private Secmod.DbMode nssDbMode = Secmod.DbMode.READ_WRITE;
    private boolean nssNetscapeDbWorkaround = true;
    private String nssArgs;
    private boolean nssUseSecmodTrust = false;
    private Config(String filename, InputStream in) throws IOException {
        if (in == null) {
            if (filename.startsWith("--")) {
                String config = filename.substring(2).replace("\\n", "\n");
                reader = new StringReader(config);
            } else {
                in = new FileInputStream(expand(filename));
            }
        }
        if (reader == null) {
            reader = new BufferedReader(new InputStreamReader(in));
        }
        parsedKeywords = new HashSet<String>();
        st = new StreamTokenizer(reader);
        setupTokenizer();
        parse();
    }
    String getName() {
        return name;
    }
    String getLibrary() {
        return library;
    }
    String getDescription() {
        if (description != null) {
            return description;
        }
        return "SunPKCS11-" + name + " using library " + library;
    }
    int getSlotID() {
        return slotID;
    }
    int getSlotListIndex() {
        if ((slotID == -1) && (slotListIndex == -1)) {
            return 0;
        } else {
            return slotListIndex;
        }
    }
    boolean getShowInfo() {
        return (SunPKCS11.debug != null) || showInfo;
    }
    TemplateManager getTemplateManager() {
        if (templateManager == null) {
            templateManager = new TemplateManager();
        }
        return templateManager;
    }
    boolean isEnabled(long m) {
        if (enabledMechanisms != null) {
            return enabledMechanisms.contains(Long.valueOf(m));
        }
        if (disabledMechanisms != null) {
            return !disabledMechanisms.contains(Long.valueOf(m));
        }
        return true;
    }
    int getHandleStartupErrors() {
        return handleStartupErrors;
    }
    boolean getKeyStoreCompatibilityMode() {
        return keyStoreCompatibilityMode;
    }
    boolean getExplicitCancel() {
        return explicitCancel;
    }
    int getInsertionCheckInterval() {
        return insertionCheckInterval;
    }
    boolean getOmitInitialize() {
        return omitInitialize;
    }
    boolean getAllowSingleThreadedModules() {
        return staticAllowSingleThreadedModules && allowSingleThreadedModules;
    }
    String getFunctionList() {
        return functionList;
    }
    boolean getNssUseSecmod() {
        return nssUseSecmod;
    }
    String getNssLibraryDirectory() {
        return nssLibraryDirectory;
    }
    String getNssSecmodDirectory() {
        return nssSecmodDirectory;
    }
    String getNssModule() {
        return nssModule;
    }
    Secmod.DbMode getNssDbMode() {
        return nssDbMode;
    }
    public boolean getNssNetscapeDbWorkaround() {
        return nssUseSecmod && nssNetscapeDbWorkaround;
    }
    String getNssArgs() {
        return nssArgs;
    }
    boolean getNssUseSecmodTrust() {
        return nssUseSecmodTrust;
    }
    private static String expand(final String s) throws IOException {
        try {
            return PropertyExpander.expand(s);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
    private void setupTokenizer() {
        st.resetSyntax();
        st.wordChars('a', 'z');
        st.wordChars('A', 'Z');
        st.wordChars('0', '9');
        st.wordChars(':', ':');
        st.wordChars('.', '.');
        st.wordChars('_', '_');
        st.wordChars('-', '-');
        st.wordChars('/', '/');
        st.wordChars('\\', '\\');
        st.wordChars('$', '$');
        st.wordChars('{', '{'); 
        st.wordChars('}', '}');
        st.wordChars('*', '*');
        st.wordChars('+', '+');
        st.wordChars('~', '~');
        st.whitespaceChars(0, ' ');
        st.commentChar('#');
        st.eolIsSignificant(true);
        st.quoteChar('\"');
    }
    private ConfigurationException excToken(String msg) {
        return new ConfigurationException(msg + " " + st);
    }
    private ConfigurationException excLine(String msg) {
        return new ConfigurationException(msg + ", line " + st.lineno());
    }
    private void parse() throws IOException {
        while (true) {
            int token = nextToken();
            if (token == TT_EOF) {
                break;
            }
            if (token == TT_EOL) {
                continue;
            }
            if (token != TT_WORD) {
                throw excToken("Unexpected token:");
            }
            String word = st.sval;
            if (word.equals("name")) {
                name = parseStringEntry(word);
            } else if (word.equals("library")) {
                library = parseLibrary(word);
            } else if (word.equals("description")) {
                parseDescription(word);
            } else if (word.equals("slot")) {
                parseSlotID(word);
            } else if (word.equals("slotListIndex")) {
                parseSlotListIndex(word);
            } else if (word.equals("enabledMechanisms")) {
                parseEnabledMechanisms(word);
            } else if (word.equals("disabledMechanisms")) {
                parseDisabledMechanisms(word);
            } else if (word.equals("attributes")) {
                parseAttributes(word);
            } else if (word.equals("handleStartupErrors")) {
                parseHandleStartupErrors(word);
            } else if (word.endsWith("insertionCheckInterval")) {
                insertionCheckInterval = parseIntegerEntry(word);
                if (insertionCheckInterval < 100) {
                    throw excLine(word + " must be at least 100 ms");
                }
            } else if (word.equals("showInfo")) {
                showInfo = parseBooleanEntry(word);
            } else if (word.equals("keyStoreCompatibilityMode")) {
                keyStoreCompatibilityMode = parseBooleanEntry(word);
            } else if (word.equals("explicitCancel")) {
                explicitCancel = parseBooleanEntry(word);
            } else if (word.equals("omitInitialize")) {
                omitInitialize = parseBooleanEntry(word);
            } else if (word.equals("allowSingleThreadedModules")) {
                allowSingleThreadedModules = parseBooleanEntry(word);
            } else if (word.equals("functionList")) {
                functionList = parseStringEntry(word);
            } else if (word.equals("nssUseSecmod")) {
                nssUseSecmod = parseBooleanEntry(word);
            } else if (word.equals("nssLibraryDirectory")) {
                nssLibraryDirectory = parseLibrary(word);
                nssUseSecmod = true;
            } else if (word.equals("nssSecmodDirectory")) {
                nssSecmodDirectory = expand(parseStringEntry(word));
                nssUseSecmod = true;
            } else if (word.equals("nssModule")) {
                nssModule = parseStringEntry(word);
                nssUseSecmod = true;
            } else if (word.equals("nssDbMode")) {
                String mode = parseStringEntry(word);
                if (mode.equals("readWrite")) {
                    nssDbMode = Secmod.DbMode.READ_WRITE;
                } else if (mode.equals("readOnly")) {
                    nssDbMode = Secmod.DbMode.READ_ONLY;
                } else if (mode.equals("noDb")) {
                    nssDbMode = Secmod.DbMode.NO_DB;
                } else {
                    throw excToken("nssDbMode must be one of readWrite, readOnly, and noDb:");
                }
                nssUseSecmod = true;
            } else if (word.equals("nssNetscapeDbWorkaround")) {
                nssNetscapeDbWorkaround = parseBooleanEntry(word);
                nssUseSecmod = true;
            } else if (word.equals("nssArgs")) {
                parseNSSArgs(word);
            } else if (word.equals("nssUseSecmodTrust")) {
                nssUseSecmodTrust = parseBooleanEntry(word);
            } else {
                throw new ConfigurationException
                        ("Unknown keyword '" + word + "', line " + st.lineno());
            }
            parsedKeywords.add(word);
        }
        reader.close();
        reader = null;
        st = null;
        parsedKeywords = null;
        if (name == null) {
            throw new ConfigurationException("name must be specified");
        }
        if (nssUseSecmod == false) {
            if (library == null) {
                throw new ConfigurationException("library must be specified");
            }
        } else {
            if (library != null) {
                throw new ConfigurationException
                    ("library must not be specified in NSS mode");
            }
            if ((slotID != -1) || (slotListIndex != -1)) {
                throw new ConfigurationException
                    ("slot and slotListIndex must not be specified in NSS mode");
            }
            if (nssArgs != null) {
                throw new ConfigurationException
                    ("nssArgs must not be specified in NSS mode");
            }
            if (nssUseSecmodTrust != false) {
                throw new ConfigurationException("nssUseSecmodTrust is an "
                    + "internal option and must not be specified in NSS mode");
            }
        }
    }
    private int nextToken() throws IOException {
        int token = st.nextToken();
        debug(st);
        return token;
    }
    private void parseEquals() throws IOException {
        int token = nextToken();
        if (token != '=') {
            throw excToken("Expected '=', read");
        }
    }
    private void parseOpenBraces() throws IOException {
        while (true) {
            int token = nextToken();
            if (token == TT_EOL) {
                continue;
            }
            if ((token == TT_WORD) && st.sval.equals("{")) {
                return;
            }
            throw excToken("Expected '{', read");
        }
    }
    private boolean isCloseBraces(int token) {
        return (token == TT_WORD) && st.sval.equals("}");
    }
    private String parseWord() throws IOException {
        int token = nextToken();
        if (token != TT_WORD) {
            throw excToken("Unexpected value:");
        }
        return st.sval;
    }
    private String parseStringEntry(String keyword) throws IOException {
        checkDup(keyword);
        parseEquals();
        int token = nextToken();
        if (token != TT_WORD && token != '\"') {
            throw excToken("Unexpected value:");
        }
        String value = st.sval;
        debug(keyword + ": " + value);
        return value;
    }
    private boolean parseBooleanEntry(String keyword) throws IOException {
        checkDup(keyword);
        parseEquals();
        boolean value = parseBoolean();
        debug(keyword + ": " + value);
        return value;
    }
    private int parseIntegerEntry(String keyword) throws IOException {
        checkDup(keyword);
        parseEquals();
        int value = decodeNumber(parseWord());
        debug(keyword + ": " + value);
        return value;
    }
    private boolean parseBoolean() throws IOException {
        String val = parseWord();
        if (val.equals("true")) {
            return true;
        } else if (val.equals("false")) {
            return false;
        } else {
            throw excToken("Expected boolean value, read:");
        }
    }
    private String parseLine() throws IOException {
        String s = parseWord();
        while (true) {
            int token = nextToken();
            if ((token == TT_EOL) || (token == TT_EOF)) {
                break;
            }
            if (token != TT_WORD) {
                throw excToken("Unexpected value");
            }
            s = s + " " + st.sval;
        }
        return s;
    }
    private int decodeNumber(String str) throws IOException {
        try {
            if (str.startsWith("0x") || str.startsWith("0X")) {
                return Integer.parseInt(str.substring(2), 16);
            } else {
                return Integer.parseInt(str);
            }
        } catch (NumberFormatException e) {
            throw excToken("Expected number, read");
        }
    }
    private static boolean isNumber(String s) {
        if (s.length() == 0) {
            return false;
        }
        char ch = s.charAt(0);
        return ((ch >= '0') && (ch <= '9'));
    }
    private void parseComma() throws IOException {
        int token = nextToken();
        if (token != ',') {
            throw excToken("Expected ',', read");
        }
    }
    private static boolean isByteArray(String val) {
        return val.startsWith("0h");
    }
    private byte[] decodeByteArray(String str) throws IOException {
        if (str.startsWith("0h") == false) {
            throw excToken("Expected byte array value, read");
        }
        str = str.substring(2);
        try {
            return new BigInteger(str, 16).toByteArray();
        } catch (NumberFormatException e) {
            throw excToken("Expected byte array value, read");
        }
    }
    private void checkDup(String keyword) throws IOException {
        if (parsedKeywords.contains(keyword)) {
            throw excLine(keyword + " must only be specified once");
        }
    }
    private String parseLibrary(String keyword) throws IOException {
        checkDup(keyword);
        parseEquals();
        String lib = parseLine();
        lib = expand(lib);
        int i = lib.indexOf("/$ISA/");
        if (i != -1) {
            String osName = System.getProperty("os.name", "");
            String osArch = System.getProperty("os.arch", "");
            String prefix = lib.substring(0, i);
            String suffix = lib.substring(i + 5);
            if (osName.equals("SunOS") && osArch.equals("sparcv9")) {
                lib = prefix + "/sparcv9" + suffix;
            } else if (osName.equals("SunOS") && osArch.equals("amd64")) {
                lib = prefix + "/amd64" + suffix;
            } else {
                lib = prefix + suffix;
            }
        }
        debug(keyword + ": " + lib);
        if (!(new File(lib)).isAbsolute()) {
            throw new ConfigurationException(
                "Absolute path required for library value: " + lib);
        }
        return lib;
    }
    private void parseDescription(String keyword) throws IOException {
        checkDup(keyword);
        parseEquals();
        description = parseLine();
        debug("description: " + description);
    }
    private void parseSlotID(String keyword) throws IOException {
        if (slotID >= 0) {
            throw excLine("Duplicate slot definition");
        }
        if (slotListIndex >= 0) {
            throw excLine
                ("Only one of slot and slotListIndex must be specified");
        }
        parseEquals();
        String slotString = parseWord();
        slotID = decodeNumber(slotString);
        debug("slot: " + slotID);
    }
    private void parseSlotListIndex(String keyword) throws IOException {
        if (slotListIndex >= 0) {
            throw excLine("Duplicate slotListIndex definition");
        }
        if (slotID >= 0) {
            throw excLine
                ("Only one of slot and slotListIndex must be specified");
        }
        parseEquals();
        String slotString = parseWord();
        slotListIndex = decodeNumber(slotString);
        debug("slotListIndex: " + slotListIndex);
    }
    private void parseEnabledMechanisms(String keyword) throws IOException {
        enabledMechanisms = parseMechanisms(keyword);
    }
    private void parseDisabledMechanisms(String keyword) throws IOException {
        disabledMechanisms = parseMechanisms(keyword);
    }
    private Set<Long> parseMechanisms(String keyword) throws IOException {
        checkDup(keyword);
        Set<Long> mechs = new HashSet<Long>();
        parseEquals();
        parseOpenBraces();
        while (true) {
            int token = nextToken();
            if (isCloseBraces(token)) {
                break;
            }
            if (token == TT_EOL) {
                continue;
            }
            if (token != TT_WORD) {
                throw excToken("Expected mechanism, read");
            }
            long mech = parseMechanism(st.sval);
            mechs.add(Long.valueOf(mech));
        }
        if (DEBUG) {
            System.out.print("mechanisms: [");
            for (Long mech : mechs) {
                System.out.print(Functions.getMechanismName(mech));
                System.out.print(", ");
            }
            System.out.println("]");
        }
        return mechs;
    }
    private long parseMechanism(String mech) throws IOException {
        if (isNumber(mech)) {
            return decodeNumber(mech);
        } else {
            try {
                return Functions.getMechanismId(mech);
            } catch (IllegalArgumentException e) {
                throw excLine("Unknown mechanism: " + mech);
            }
        }
    }
    private void parseAttributes(String keyword) throws IOException {
        if (templateManager == null) {
            templateManager = new TemplateManager();
        }
        int token = nextToken();
        if (token == '=') {
            String s = parseWord();
            if (s.equals("compatibility") == false) {
                throw excLine("Expected 'compatibility', read " + s);
            }
            setCompatibilityAttributes();
            return;
        }
        if (token != '(') {
            throw excToken("Expected '(' or '=', read");
        }
        String op = parseOperation();
        parseComma();
        long objectClass = parseObjectClass();
        parseComma();
        long keyAlg = parseKeyAlgorithm();
        token = nextToken();
        if (token != ')') {
            throw excToken("Expected ')', read");
        }
        parseEquals();
        parseOpenBraces();
        List<CK_ATTRIBUTE> attributes = new ArrayList<CK_ATTRIBUTE>();
        while (true) {
            token = nextToken();
            if (isCloseBraces(token)) {
                break;
            }
            if (token == TT_EOL) {
                continue;
            }
            if (token != TT_WORD) {
                throw excToken("Expected mechanism, read");
            }
            String attributeName = st.sval;
            long attributeId = decodeAttributeName(attributeName);
            parseEquals();
            String attributeValue = parseWord();
            attributes.add(decodeAttributeValue(attributeId, attributeValue));
        }
        templateManager.addTemplate
                (op, objectClass, keyAlg, attributes.toArray(CK_A0));
    }
    private void setCompatibilityAttributes() {
        templateManager.addTemplate(O_ANY, CKO_SECRET_KEY, PCKK_ANY,
        new CK_ATTRIBUTE[] {
            TOKEN_FALSE,
            SENSITIVE_FALSE,
            EXTRACTABLE_TRUE,
            ENCRYPT_TRUE,
            DECRYPT_TRUE,
            WRAP_TRUE,
            UNWRAP_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_SECRET_KEY, CKK_GENERIC_SECRET,
        new CK_ATTRIBUTE[] {
            SIGN_TRUE,
            VERIFY_TRUE,
            ENCRYPT_NULL,
            DECRYPT_NULL,
            WRAP_NULL,
            UNWRAP_NULL,
            DERIVE_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PRIVATE_KEY, PCKK_ANY,
        new CK_ATTRIBUTE[] {
            TOKEN_FALSE,
            SENSITIVE_FALSE,
            EXTRACTABLE_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PUBLIC_KEY, PCKK_ANY,
        new CK_ATTRIBUTE[] {
            TOKEN_FALSE,
        });
        templateManager.addTemplate(O_ANY, CKO_PRIVATE_KEY, CKK_RSA,
        new CK_ATTRIBUTE[] {
            DECRYPT_TRUE,
            SIGN_TRUE,
            SIGN_RECOVER_TRUE,
            UNWRAP_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PUBLIC_KEY, CKK_RSA,
        new CK_ATTRIBUTE[] {
            ENCRYPT_TRUE,
            VERIFY_TRUE,
            VERIFY_RECOVER_TRUE,
            WRAP_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PRIVATE_KEY, CKK_DSA,
        new CK_ATTRIBUTE[] {
            SIGN_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PUBLIC_KEY, CKK_DSA,
        new CK_ATTRIBUTE[] {
            VERIFY_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PRIVATE_KEY, CKK_DH,
        new CK_ATTRIBUTE[] {
            DERIVE_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PRIVATE_KEY, CKK_EC,
        new CK_ATTRIBUTE[] {
            SIGN_TRUE,
            DERIVE_TRUE,
        });
        templateManager.addTemplate(O_ANY, CKO_PUBLIC_KEY, CKK_EC,
        new CK_ATTRIBUTE[] {
            VERIFY_TRUE,
        });
    }
    private final static CK_ATTRIBUTE[] CK_A0 = new CK_ATTRIBUTE[0];
    private String parseOperation() throws IOException {
        String op = parseWord();
        if (op.equals("*")) {
            return TemplateManager.O_ANY;
        } else if (op.equals("generate")) {
            return TemplateManager.O_GENERATE;
        } else if (op.equals("import")) {
            return TemplateManager.O_IMPORT;
        } else {
            throw excLine("Unknown operation " + op);
        }
    }
    private long parseObjectClass() throws IOException {
        String name = parseWord();
        try {
            return Functions.getObjectClassId(name);
        } catch (IllegalArgumentException e) {
            throw excLine("Unknown object class " + name);
        }
    }
    private long parseKeyAlgorithm() throws IOException {
        String name = parseWord();
        if (isNumber(name)) {
            return decodeNumber(name);
        } else {
            try {
                return Functions.getKeyId(name);
            } catch (IllegalArgumentException e) {
                throw excLine("Unknown key algorithm " + name);
            }
        }
    }
    private long decodeAttributeName(String name) throws IOException {
        if (isNumber(name)) {
            return decodeNumber(name);
        } else {
            try {
                return Functions.getAttributeId(name);
            } catch (IllegalArgumentException e) {
                throw excLine("Unknown attribute name " + name);
            }
        }
    }
    private CK_ATTRIBUTE decodeAttributeValue(long id, String value)
            throws IOException {
        if (value.equals("null")) {
            return new CK_ATTRIBUTE(id);
        } else if (value.equals("true")) {
            return new CK_ATTRIBUTE(id, true);
        } else if (value.equals("false")) {
            return new CK_ATTRIBUTE(id, false);
        } else if (isByteArray(value)) {
            return new CK_ATTRIBUTE(id, decodeByteArray(value));
        } else if (isNumber(value)) {
            return new CK_ATTRIBUTE(id, Integer.valueOf(decodeNumber(value)));
        } else {
            throw excLine("Unknown attribute value " + value);
        }
    }
    private void parseNSSArgs(String keyword) throws IOException {
        checkDup(keyword);
        parseEquals();
        int token = nextToken();
        if (token != '"') {
            throw excToken("Expected quoted string");
        }
        nssArgs = expand(st.sval);
        debug("nssArgs: " + nssArgs);
    }
    private void parseHandleStartupErrors(String keyword) throws IOException {
        checkDup(keyword);
        parseEquals();
        String val = parseWord();
        if (val.equals("ignoreAll")) {
            handleStartupErrors = ERR_IGNORE_ALL;
        } else if (val.equals("ignoreMissingLibrary")) {
            handleStartupErrors = ERR_IGNORE_LIB;
        } else if (val.equals("halt")) {
            handleStartupErrors = ERR_HALT;
        } else {
            throw excToken("Invalid value for handleStartupErrors:");
        }
        debug("handleStartupErrors: " + handleStartupErrors);
    }
}
class ConfigurationException extends IOException {
    ConfigurationException(String msg) {
        super(msg);
    }
}
