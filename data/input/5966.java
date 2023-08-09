public class TransferableProxy implements Transferable {
    public TransferableProxy(Transferable t, boolean local) {
        transferable = t;
        isLocal = local;
    }
    public DataFlavor[] getTransferDataFlavors() {
        return transferable.getTransferDataFlavors();
    }
    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return transferable.isDataFlavorSupported(flavor);
    }
    public Object getTransferData(DataFlavor df)
        throws UnsupportedFlavorException, IOException
    {
        Object data = transferable.getTransferData(df);
        if (data != null && isLocal && df.isFlavorSerializedObjectType()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ClassLoaderObjectOutputStream oos =
                new ClassLoaderObjectOutputStream(baos);
            oos.writeObject(data);
            ByteArrayInputStream bais =
                new ByteArrayInputStream(baos.toByteArray());
            try {
                ClassLoaderObjectInputStream ois =
                    new ClassLoaderObjectInputStream(bais,
                                                     oos.getClassLoaderMap());
                data = ois.readObject();
            } catch (ClassNotFoundException cnfe) {
                throw (IOException)new IOException().initCause(cnfe);
            }
        }
        return data;
    }
    protected final Transferable transferable;
    protected final boolean isLocal;
}
class ClassLoaderObjectOutputStream extends ObjectOutputStream {
    private final Map<Set<String>, ClassLoader> map =
        new HashMap<Set<String>, ClassLoader>();
    public ClassLoaderObjectOutputStream(OutputStream os) throws IOException {
        super(os);
    }
    protected void annotateClass(final Class<?> cl) throws IOException {
        ClassLoader classLoader =
            (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return cl.getClassLoader();
                }
            });
        Set<String> s = new HashSet<String>(1);
        s.add(cl.getName());
        map.put(s, classLoader);
    }
    protected void annotateProxyClass(final Class<?> cl) throws IOException {
        ClassLoader classLoader =
            (ClassLoader)AccessController.doPrivileged(new PrivilegedAction() {
                public Object run() {
                    return cl.getClassLoader();
                }
            });
        Class[] interfaces = cl.getInterfaces();
        Set<String> s = new HashSet<String>(interfaces.length);
        for (int i = 0; i < interfaces.length; i++) {
            s.add(interfaces[i].getName());
        }
        map.put(s, classLoader);
    }
    public Map<Set<String>, ClassLoader> getClassLoaderMap() {
        return new HashMap(map);
    }
}
class ClassLoaderObjectInputStream extends ObjectInputStream {
    private final Map<Set<String>, ClassLoader> map;
    public ClassLoaderObjectInputStream(InputStream is,
                                        Map<Set<String>, ClassLoader> map)
      throws IOException {
        super(is);
        if (map == null) {
            throw new NullPointerException("Null map");
        }
        this.map = map;
    }
    protected Class<?> resolveClass(ObjectStreamClass classDesc)
      throws IOException, ClassNotFoundException {
        String className = classDesc.getName();
        Set<String> s = new HashSet<String>(1);
        s.add(className);
        ClassLoader classLoader = map.get(s);
        return Class.forName(className, false, classLoader);
    }
    protected Class<?> resolveProxyClass(String[] interfaces)
      throws IOException, ClassNotFoundException {
        Set<String> s = new HashSet<String>(interfaces.length);
        for (int i = 0; i < interfaces.length; i++) {
            s.add(interfaces[i]);
        }
        ClassLoader classLoader = map.get(s);
        ClassLoader nonPublicLoader = null;
        boolean hasNonPublicInterface = false;
        Class[] classObjs = new Class[interfaces.length];
        for (int i = 0; i < interfaces.length; i++) {
            Class cl = Class.forName(interfaces[i], false, classLoader);
            if ((cl.getModifiers() & Modifier.PUBLIC) == 0) {
                if (hasNonPublicInterface) {
                    if (nonPublicLoader != cl.getClassLoader()) {
                        throw new IllegalAccessError(
                            "conflicting non-public interface class loaders");
                    }
                } else {
                    nonPublicLoader = cl.getClassLoader();
                    hasNonPublicInterface = true;
                }
            }
            classObjs[i] = cl;
        }
        try {
            return Proxy.getProxyClass(hasNonPublicInterface ?
                                       nonPublicLoader : classLoader,
                                       classObjs);
        } catch (IllegalArgumentException e) {
            throw new ClassNotFoundException(null, e);
        }
    }
}
