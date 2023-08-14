public class MBeanServerFileAccessController
    extends MBeanServerAccessController {
    static final String READONLY = "readonly";
    static final String READWRITE = "readwrite";
    static final String CREATE = "create";
    static final String UNREGISTER = "unregister";
    private enum AccessType {READ, WRITE, CREATE, UNREGISTER};
    private static class Access {
        final boolean write;
        final String[] createPatterns;
        private boolean unregister;
        Access(boolean write, boolean unregister, List<String> createPatternList) {
            this.write = write;
            int npats = (createPatternList == null) ? 0 : createPatternList.size();
            if (npats == 0)
                this.createPatterns = NO_STRINGS;
            else
                this.createPatterns = createPatternList.toArray(new String[npats]);
            this.unregister = unregister;
        }
        private final String[] NO_STRINGS = new String[0];
    }
    public MBeanServerFileAccessController(String accessFileName)
        throws IOException {
        super();
        this.accessFileName = accessFileName;
        Properties props = propertiesFromFile(accessFileName);
        parseProperties(props);
    }
    public MBeanServerFileAccessController(String accessFileName,
                                           MBeanServer mbs)
        throws IOException {
        this(accessFileName);
        setMBeanServer(mbs);
    }
    public MBeanServerFileAccessController(Properties accessFileProps)
        throws IOException {
        super();
        if (accessFileProps == null)
            throw new IllegalArgumentException("Null properties");
        originalProps = accessFileProps;
        parseProperties(accessFileProps);
    }
    public MBeanServerFileAccessController(Properties accessFileProps,
                                           MBeanServer mbs)
        throws IOException {
        this(accessFileProps);
        setMBeanServer(mbs);
    }
    @Override
    public void checkRead() {
        checkAccess(AccessType.READ, null);
    }
    @Override
    public void checkWrite() {
        checkAccess(AccessType.WRITE, null);
    }
    @Override
    public void checkCreate(String className) {
        checkAccess(AccessType.CREATE, className);
    }
    @Override
    public void checkUnregister(ObjectName name) {
        checkAccess(AccessType.UNREGISTER, null);
    }
    public synchronized void refresh() throws IOException {
        Properties props;
        if (accessFileName == null)
            props = (Properties) originalProps;
        else
            props = propertiesFromFile(accessFileName);
        parseProperties(props);
    }
    private static Properties propertiesFromFile(String fname)
        throws IOException {
        FileInputStream fin = new FileInputStream(fname);
        try {
            Properties p = new Properties();
            p.load(fin);
            return p;
        } finally {
            fin.close();
        }
    }
    private synchronized void checkAccess(AccessType requiredAccess, String arg) {
        final AccessControlContext acc = AccessController.getContext();
        final Subject s =
            AccessController.doPrivileged(new PrivilegedAction<Subject>() {
                    public Subject run() {
                        return Subject.getSubject(acc);
                    }
                });
        if (s == null) return; 
        final Set principals = s.getPrincipals();
        String newPropertyValue = null;
        for (Iterator i = principals.iterator(); i.hasNext(); ) {
            final Principal p = (Principal) i.next();
            Access access = accessMap.get(p.getName());
            if (access != null) {
                boolean ok;
                switch (requiredAccess) {
                    case READ:
                        ok = true;  
                        break;
                    case WRITE:
                        ok = access.write;
                        break;
                    case UNREGISTER:
                        ok = access.unregister;
                        if (!ok && access.write)
                            newPropertyValue = "unregister";
                        break;
                    case CREATE:
                        ok = checkCreateAccess(access, arg);
                        if (!ok && access.write)
                            newPropertyValue = "create " + arg;
                        break;
                    default:
                        throw new AssertionError();
                }
                if (ok)
                    return;
            }
        }
        SecurityException se = new SecurityException("Access denied! Invalid " +
                "access level for requested MBeanServer operation.");
        if (newPropertyValue != null) {
            SecurityException se2 = new SecurityException("Access property " +
                    "for this identity should be similar to: " + READWRITE +
                    " " + newPropertyValue);
            se.initCause(se2);
        }
        throw se;
    }
    private static boolean checkCreateAccess(Access access, String className) {
        for (String classNamePattern : access.createPatterns) {
            if (classNameMatch(classNamePattern, className))
                return true;
        }
        return false;
    }
    private static boolean classNameMatch(String pattern, String className) {
        StringBuilder sb = new StringBuilder();
        StringTokenizer stok = new StringTokenizer(pattern, "*", true);
        while (stok.hasMoreTokens()) {
            String tok = stok.nextToken();
            if (tok.equals("*"))
                sb.append("[^.]*");
            else
                sb.append(Pattern.quote(tok));
        }
        return className.matches(sb.toString());
    }
    private void parseProperties(Properties props) {
        this.accessMap = new HashMap<String, Access>();
        for (Map.Entry<Object, Object> entry : props.entrySet()) {
            String identity = (String) entry.getKey();
            String accessString = (String) entry.getValue();
            Access access = Parser.parseAccess(identity, accessString);
            accessMap.put(identity, access);
        }
    }
    private static class Parser {
        private final static int EOS = -1;  
        static {
            assert !Character.isWhitespace(EOS);
        }
        private final String identity;  
        private final String s;  
        private final int len;   
        private int i;
        private int c;
        private Parser(String identity, String s) {
            this.identity = identity;
            this.s = s;
            this.len = s.length();
            this.i = 0;
            if (i < len)
                this.c = s.codePointAt(i);
            else
                this.c = EOS;
        }
        static Access parseAccess(String identity, String s) {
            return new Parser(identity, s).parseAccess();
        }
        private Access parseAccess() {
            skipSpace();
            String type = parseWord();
            Access access;
            if (type.equals(READONLY))
                access = new Access(false, false, null);
            else if (type.equals(READWRITE))
                access = parseReadWrite();
            else {
                throw syntax("Expected " + READONLY + " or " + READWRITE +
                        ": " + type);
            }
            if (c != EOS)
                throw syntax("Extra text at end of line");
            return access;
        }
        private Access parseReadWrite() {
            List<String> createClasses = new ArrayList<String>();
            boolean unregister = false;
            while (true) {
                skipSpace();
                if (c == EOS)
                    break;
                String type = parseWord();
                if (type.equals(UNREGISTER))
                    unregister = true;
                else if (type.equals(CREATE))
                    parseCreate(createClasses);
                else
                    throw syntax("Unrecognized keyword " + type);
            }
            return new Access(true, unregister, createClasses);
        }
        private void parseCreate(List<String> createClasses) {
            while (true) {
                skipSpace();
                createClasses.add(parseClassName());
                skipSpace();
                if (c == ',')
                    next();
                else
                    break;
            }
        }
        private String parseClassName() {
            final int start = i;
            boolean dotOK = false;
            while (true) {
                if (c == '.') {
                    if (!dotOK)
                        throw syntax("Bad . in class name");
                    dotOK = false;
                } else if (c == '*' || Character.isJavaIdentifierPart(c))
                    dotOK = true;
                else
                    break;
                next();
            }
            String className = s.substring(start, i);
            if (!dotOK)
                throw syntax("Bad class name " + className);
            return className;
        }
        private void next() {
            if (c != EOS) {
                i += Character.charCount(c);
                if (i < len)
                    c = s.codePointAt(i);
                else
                    c = EOS;
            }
        }
        private void skipSpace() {
            while (Character.isWhitespace(c))
                next();
        }
        private String parseWord() {
            skipSpace();
            if (c == EOS)
                throw syntax("Expected word at end of line");
            final int start = i;
            while (c != EOS && !Character.isWhitespace(c))
                next();
            String word = s.substring(start, i);
            skipSpace();
            return word;
        }
        private IllegalArgumentException syntax(String msg) {
            return new IllegalArgumentException(
                    msg + " [" + identity + " " + s + "]");
        }
    }
    private Map<String, Access> accessMap;
    private Properties originalProps;
    private String accessFileName;
}
