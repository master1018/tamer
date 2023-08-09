public class ClientCodeWrapper {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public @interface Trusted { }
    public static ClientCodeWrapper instance(Context context) {
        ClientCodeWrapper instance = context.get(ClientCodeWrapper.class);
        if (instance == null)
            instance = new ClientCodeWrapper(context);
        return instance;
    }
    Map<Class<?>, Boolean> trustedClasses;
    protected ClientCodeWrapper(Context context) {
        trustedClasses = new HashMap<Class<?>, Boolean>();
    }
    public JavaFileManager wrap(JavaFileManager fm) {
        if (isTrusted(fm))
            return fm;
        return new WrappedJavaFileManager(fm);
    }
    public FileObject wrap(FileObject fo) {
        if (isTrusted(fo))
            return fo;
        return new WrappedFileObject(fo);
    }
    FileObject unwrap(FileObject fo) {
        if (fo instanceof WrappedFileObject)
            return ((WrappedFileObject) fo).clientFileObject;
        else
            return fo;
    }
    public JavaFileObject wrap(JavaFileObject fo) {
        if (isTrusted(fo))
            return fo;
        return new WrappedJavaFileObject(fo);
    }
    public Iterable<JavaFileObject> wrapJavaFileObjects(Iterable<? extends JavaFileObject> list) {
        List<JavaFileObject> wrapped = new ArrayList<JavaFileObject>();
        for (JavaFileObject fo : list)
            wrapped.add(wrap(fo));
        return Collections.unmodifiableList(wrapped);
    }
    JavaFileObject unwrap(JavaFileObject fo) {
        if (fo instanceof WrappedJavaFileObject)
            return ((JavaFileObject) ((WrappedJavaFileObject) fo).clientFileObject);
        else
            return fo;
    }
    <T> DiagnosticListener<T> wrap(DiagnosticListener<T> dl) {
        if (isTrusted(dl))
            return dl;
        return new WrappedDiagnosticListener<T>(dl);
    }
    TaskListener wrap(TaskListener tl) {
        if (isTrusted(tl))
            return tl;
        return new WrappedTaskListener(tl);
    }
    protected boolean isTrusted(Object o) {
        Class<?> c = o.getClass();
        Boolean trusted = trustedClasses.get(c);
        if (trusted == null) {
            trusted = c.getName().startsWith("com.sun.tools.javac.")
                    || c.isAnnotationPresent(Trusted.class);
            trustedClasses.put(c, trusted);
        }
        return trusted;
    }
    protected class WrappedJavaFileManager implements JavaFileManager {
        protected JavaFileManager clientJavaFileManager;
        WrappedJavaFileManager(JavaFileManager clientJavaFileManager) {
            clientJavaFileManager.getClass(); 
            this.clientJavaFileManager = clientJavaFileManager;
        }
        @Override
        public ClassLoader getClassLoader(Location location) {
            try {
                return clientJavaFileManager.getClassLoader(location);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse) throws IOException {
            try {
                return wrapJavaFileObjects(clientJavaFileManager.list(location, packageName, kinds, recurse));
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public String inferBinaryName(Location location, JavaFileObject file) {
            try {
                return clientJavaFileManager.inferBinaryName(location, unwrap(file));
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public boolean isSameFile(FileObject a, FileObject b) {
            try {
                return clientJavaFileManager.isSameFile(unwrap(a), unwrap(b));
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public boolean handleOption(String current, Iterator<String> remaining) {
            try {
                return clientJavaFileManager.handleOption(current, remaining);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public boolean hasLocation(Location location) {
            try {
                return clientJavaFileManager.hasLocation(location);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
            try {
                return wrap(clientJavaFileManager.getJavaFileForInput(location, className, kind));
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
            try {
                return wrap(clientJavaFileManager.getJavaFileForOutput(location, className, kind, unwrap(sibling)));
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
            try {
                return wrap(clientJavaFileManager.getFileForInput(location, packageName, relativeName));
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling) throws IOException {
            try {
                return wrap(clientJavaFileManager.getFileForOutput(location, packageName, relativeName, unwrap(sibling)));
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public void flush() throws IOException {
            try {
                clientJavaFileManager.flush();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public void close() throws IOException {
            try {
                clientJavaFileManager.close();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public int isSupportedOption(String option) {
            try {
                return clientJavaFileManager.isSupportedOption(option);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
    }
    protected class WrappedFileObject implements FileObject {
        protected FileObject clientFileObject;
        WrappedFileObject(FileObject clientFileObject) {
            clientFileObject.getClass(); 
            this.clientFileObject = clientFileObject;
        }
        @Override
        public URI toUri() {
            try {
                return clientFileObject.toUri();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public String getName() {
            try {
                return clientFileObject.getName();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public InputStream openInputStream() throws IOException {
            try {
                return clientFileObject.openInputStream();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public OutputStream openOutputStream() throws IOException {
            try {
                return clientFileObject.openOutputStream();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
            try {
                return clientFileObject.openReader(ignoreEncodingErrors);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
            try {
                return clientFileObject.getCharContent(ignoreEncodingErrors);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public Writer openWriter() throws IOException {
            try {
                return clientFileObject.openWriter();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public long getLastModified() {
            try {
                return clientFileObject.getLastModified();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public boolean delete() {
            try {
                return clientFileObject.delete();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
    }
    protected class WrappedJavaFileObject extends WrappedFileObject implements JavaFileObject {
        WrappedJavaFileObject(JavaFileObject clientJavaFileObject) {
            super(clientJavaFileObject);
        }
        @Override
        public Kind getKind() {
            try {
                return ((JavaFileObject)clientFileObject).getKind();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public boolean isNameCompatible(String simpleName, Kind kind) {
            try {
                return ((JavaFileObject)clientFileObject).isNameCompatible(simpleName, kind);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public NestingKind getNestingKind() {
            try {
                return ((JavaFileObject)clientFileObject).getNestingKind();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public Modifier getAccessLevel() {
            try {
                return ((JavaFileObject)clientFileObject).getAccessLevel();
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
    }
    protected class WrappedDiagnosticListener<T> implements DiagnosticListener<T> {
        protected DiagnosticListener<T> clientDiagnosticListener;
        WrappedDiagnosticListener(DiagnosticListener<T> clientDiagnosticListener) {
            clientDiagnosticListener.getClass(); 
            this.clientDiagnosticListener = clientDiagnosticListener;
        }
        @Override
        public void report(Diagnostic<? extends T> diagnostic) {
            try {
                clientDiagnosticListener.report(diagnostic);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
    }
    protected class WrappedTaskListener implements TaskListener {
        protected TaskListener clientTaskListener;
        WrappedTaskListener(TaskListener clientTaskListener) {
            clientTaskListener.getClass(); 
            this.clientTaskListener = clientTaskListener;
        }
        @Override
        public void started(TaskEvent ev) {
            try {
                clientTaskListener.started(ev);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
        @Override
        public void finished(TaskEvent ev) {
            try {
                clientTaskListener.finished(ev);
            } catch (ClientCodeException e) {
                throw e;
            } catch (RuntimeException e) {
                throw new ClientCodeException(e);
            } catch (Error e) {
                throw new ClientCodeException(e);
            }
        }
    }
}
