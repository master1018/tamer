public class PolicyUtils {
    private PolicyUtils() {}
    public static class URLLoader implements PrivilegedExceptionAction<InputStream> {
        public URL location;
        public URLLoader(URL location) {
            this.location = location;
        }
        public InputStream run() throws Exception {
            return location.openStream();
        }
    }
    public static class SystemKit implements PrivilegedAction<Properties> {
        public Properties run() {
            return System.getProperties();
        }
    }
    public static class SystemPropertyAccessor implements PrivilegedAction<String> {
        public String key;
        public SystemPropertyAccessor(String key) {
            this.key = key;
        }
        public PrivilegedAction<String> key(String key) {
            this.key = key;
            return this;
        }
        public String run() {
            return System.getProperty(key);
        }
    }
    public static class SecurityPropertyAccessor implements PrivilegedAction<String> {
        private String key;
        public SecurityPropertyAccessor(String key) {
            super();
            this.key = key;
        }
        public PrivilegedAction<String> key(String key) {
            this.key = key;
            return this;
        }
        public String run() {
            return Security.getProperty(key);
        }
    }
    public static class ProviderLoader<T> implements PrivilegedAction<T> {
        private String key;
        private Class<T> expectedType;
        public ProviderLoader(String key, Class<T> expected) {
            super();
            this.key = key;
            this.expectedType = expected;
        }
        public T run() {
            String klassName = Security.getProperty(key);
            if (klassName == null || klassName.length() == 0) {
                throw new SecurityException(Messages.getString("security.14C", 
                                            key));
            }
            try {
                Class<?> klass = Class.forName(klassName, true,
                        Thread.currentThread().getContextClassLoader());
                if (expectedType != null && klass.isAssignableFrom(expectedType)){
                    throw new SecurityException(Messages.getString("security.14D", 
                                              klassName, expectedType.getName()));
                }
                return (T)klass.newInstance();
            }
            catch (SecurityException se){
                throw se;
            }
            catch (Exception e) {
                SecurityException se = new SecurityException(
                        Messages.getString("security.14E", klassName)); 
                se.initCause(e);
                throw se;
            }
        }
    }
    public static class ExpansionFailedException extends Exception {
        private static final long serialVersionUID = 2869748055182612000L;
        public ExpansionFailedException(String message) {
            super(message);
        }
        public ExpansionFailedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
    public static String expand(String str, Properties properties)
            throws ExpansionFailedException {
        final String START_MARK = "${"; 
        final String END_MARK = "}"; 
        final int START_OFFSET = START_MARK.length();
        final int END_OFFSET = END_MARK.length();
        StringBuilder result = new StringBuilder(str);
        int start = result.indexOf(START_MARK);
        while (start >= 0) {
            int end = result.indexOf(END_MARK, start);
            if (end >= 0) {
                String key = result.substring(start + START_OFFSET, end);
                String value = properties.getProperty(key);
                if (value != null) {
                    result.replace(start, end + END_OFFSET, value);
                    start += value.length();
                } else {
                    throw new ExpansionFailedException(Messages.getString("security.14F", key)); 
                }
            }
            start = result.indexOf(START_MARK, start);
        }
        return result.toString();
    }
    public static String expandURL(String str, Properties properties)
            throws ExpansionFailedException {
        return expand(str, properties).replace(File.separatorChar, '/');
    }
    public static URL normalizeURL(URL codebase) {
        if (codebase != null && "file".equals(codebase.getProtocol())) { 
            try {
                if (codebase.getHost().length() == 0) {
                    String path = codebase.getFile();
                    if (path.length() == 0) {
                        path = "*";
                    }
                    return filePathToURI(new File(path)
                            .getAbsolutePath()).normalize().toURL();
                } else {
                    return codebase.toURI().normalize().toURL();
                }
            } catch (Exception e) {
            }
        }
        return codebase;
    }
    public static URI filePathToURI(String path) throws URISyntaxException {
        path = path.replace(File.separatorChar, '/');
        if (!path.startsWith("/")) { 
            return new URI("file", null, 
                    new StringBuilder(path.length() + 1).append('/')
                            .append(path).toString(), null, null);
        }
        return new URI("file", null, path, null, null); 
    }
    public static interface GeneralExpansionHandler {
        String resolve(String protocol, String data)
                throws ExpansionFailedException;
    }
    public static String expandGeneral(String str,
            GeneralExpansionHandler handler) throws ExpansionFailedException {
        final String START_MARK = "${{"; 
        final String END_MARK = "}}"; 
        final int START_OFFSET = START_MARK.length();
        final int END_OFFSET = END_MARK.length();
        StringBuilder result = new StringBuilder(str);
        int start = result.indexOf(START_MARK);
        while (start >= 0) {
            int end = result.indexOf(END_MARK, start);
            if (end >= 0) {
                String key = result.substring(start + START_OFFSET, end);
                int separator = key.indexOf(':');
                String protocol = (separator >= 0) ? key
                        .substring(0, separator) : key;
                String data = (separator >= 0) ? key.substring(separator + 1)
                        : null;
                String value = handler.resolve(protocol, data);
                result.replace(start, end + END_OFFSET, value);
                start += value.length();
            }
            start = result.indexOf(START_MARK, start);
        }
        return result.toString();
    }
    public static final String POLICY_ALLOW_DYNAMIC = "policy.allowSystemProperty"; 
    public static final String POLICY_EXPAND = "policy.expandProperties"; 
    public static final String TRUE = "true"; 
    public static final String FALSE = "false"; 
    public static boolean canExpandProperties() {
        return !Util.equalsIgnoreCase(FALSE,AccessController
                .doPrivileged(new SecurityPropertyAccessor(POLICY_EXPAND)));
    }
    public static URL[] getPolicyURLs(final Properties system,
            final String systemUrlKey, final String securityUrlPrefix) {
        final SecurityPropertyAccessor security = new SecurityPropertyAccessor(
                null);
        final List<URL> urls = new ArrayList<URL>();
        boolean dynamicOnly = false;
        URL dynamicURL = null;
        if (!Util.equalsIgnoreCase(FALSE, AccessController
                .doPrivileged(security.key(POLICY_ALLOW_DYNAMIC)))) {
            String location = system.getProperty(systemUrlKey);
            if (location != null) {
                if (location.startsWith("=")) { 
                    dynamicOnly = true;
                    location = location.substring(1);
                }
                try {
                    location = expandURL(location, system);
                    final File f = new File(location);
                    dynamicURL = AccessController
                            .doPrivileged(new PrivilegedExceptionAction<URL>() {
                                public URL run() throws Exception {
                                    if (f.exists()) {
                                        return f.toURI().toURL();
                                    } else {
                                        return null;
                                    }
                                }
                            });
                    if (dynamicURL == null) {
                        dynamicURL = new URL(location);
                    }
                }
                catch (Exception e) {
                }
            }
        }
        if (!dynamicOnly) {
            int i = 1;
            while (true) {
                String location = AccessController
                        .doPrivileged(security.key(new StringBuilder(
                                securityUrlPrefix).append(i++).toString()));
                if (location == null) {
                    break;
                }
                try {
                    location = expandURL(location, system);
                    URL anURL = new URL(location);
                    if (anURL != null) {
                        urls.add(anURL);
                    }
                }
                catch (Exception e) {
                }
            }
        }
        if (dynamicURL != null) {
            urls.add(dynamicURL);
        }
        return urls.toArray(new URL[urls.size()]);
    }
    public static PermissionCollection toPermissionCollection(
            Collection<Permission> perms) {
        Permissions pc = new Permissions();
        if (perms != null) {
            for (Iterator<Permission> iter = perms.iterator(); iter.hasNext();) {
                Permission element = iter.next();
                pc.add(element);
            }
        }
        return pc;
    }
    private static final Class[] NO_ARGS = {};
    private static final Class[] ONE_ARGS = { String.class };
    private static final Class[] TWO_ARGS = { String.class, String.class };
    public static Permission instantiatePermission(Class<?> targetType,
            String targetName, String targetActions) throws Exception {
        Class[][] argTypes = null;
        Object[][] args = null;
        if (targetActions != null) {
            argTypes = new Class[][] { TWO_ARGS, ONE_ARGS, NO_ARGS };
            args = new Object[][] { { targetName, targetActions },
                    { targetName }, {} };
        } else if (targetName != null) {
            argTypes = new Class[][] { ONE_ARGS, TWO_ARGS, NO_ARGS };
            args = new Object[][] { { targetName },
                    { targetName, targetActions }, {} };
        } else {
            argTypes = new Class[][] { NO_ARGS, ONE_ARGS, TWO_ARGS };
            args = new Object[][] { {}, { targetName },
                    { targetName, targetActions } };
        }
        for (int i = 0; i < argTypes.length; i++) {
            try {
                Constructor<?> ctor = targetType.getConstructor(argTypes[i]);
                return (Permission)ctor.newInstance(args[i]);
            }
            catch (NoSuchMethodException ignore) {}
        }
        throw new IllegalArgumentException(
                Messages.getString("security.150", targetType));
    }
    public static boolean matchSubset(Object[] what, Object[] where) {
        if (what == null) {
            return true;
        }
        for (int i = 0; i < what.length; i++) {
            if (what[i] != null) {
                if (where == null) {
                    return false;
                }
                boolean found = false;
                for (int j = 0; j < where.length; j++) {
                    if (what[i].equals(where[j])) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return false;
                }
            }
        }
        return true;
    }
}
