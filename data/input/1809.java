class NoCallStackClassLoader extends ClassLoader {
    public NoCallStackClassLoader(String className,
                                  byte[] byteCode,
                                  String[] referencedClassNames,
                                  ClassLoader referencedClassLoader,
                                  ProtectionDomain protectionDomain) {
        this(new String[] {className}, new byte[][] {byteCode},
             referencedClassNames, referencedClassLoader, protectionDomain);
    }
    public NoCallStackClassLoader(String[] classNames,
                                  byte[][] byteCodes,
                                  String[] referencedClassNames,
                                  ClassLoader referencedClassLoader,
                                  ProtectionDomain protectionDomain) {
        super(null);
        if (classNames == null || classNames.length == 0
            || byteCodes == null || classNames.length != byteCodes.length
            || referencedClassNames == null || protectionDomain == null)
            throw new IllegalArgumentException();
        for (int i = 0; i < classNames.length; i++) {
            if (classNames[i] == null || byteCodes[i] == null)
                throw new IllegalArgumentException();
        }
        for (int i = 0; i < referencedClassNames.length; i++) {
            if (referencedClassNames[i] == null)
                throw new IllegalArgumentException();
        }
        this.classNames = classNames;
        this.byteCodes = byteCodes;
        this.referencedClassNames = referencedClassNames;
        this.referencedClassLoader = referencedClassLoader;
        this.protectionDomain = protectionDomain;
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        for (int i = 0; i < classNames.length; i++) {
            if (name.equals(classNames[i])) {
                return defineClass(classNames[i], byteCodes[i], 0,
                                   byteCodes[i].length, protectionDomain);
            }
        }
        if (referencedClassLoader != null) {
            for (int i = 0; i < referencedClassNames.length; i++) {
                if (name.equals(referencedClassNames[i]))
                    return referencedClassLoader.loadClass(name);
            }
        }
        throw new ClassNotFoundException(name);
    }
    private final String[] classNames;
    private final byte[][] byteCodes;
    private final String[] referencedClassNames;
    private final ClassLoader referencedClassLoader;
    private final ProtectionDomain protectionDomain;
    public static byte[] stringToBytes(String s) {
        final int slen = s.length();
        byte[] bytes = new byte[slen];
        for (int i = 0; i < slen; i++)
            bytes[i] = (byte) s.charAt(i);
        return bytes;
    }
}
