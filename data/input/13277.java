public final class MemoryClassLoader extends ClassLoader {
    private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    private final MemoryFileManager manager = new MemoryFileManager(this.compiler);
    public Class<?> compile(String name, String content) {
        compile(new Source(name, content));
        try {
            return findClass(name);
        }
        catch (ClassNotFoundException exception) {
            throw new Error(exception);
        }
    }
    public void compile(Source... sources) {
        List<Source> list = new ArrayList<Source>();
        if (sources != null) {
            for (Source source : sources) {
                if (source != null) {
                    list.add(source);
                }
            }
        }
        synchronized (this.manager) {
            this.compiler.getTask(null, this.manager, null, null, null, list).call();
        }
    }
    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        synchronized (this.manager) {
            Output mc = this.manager.map.remove(name);
            if (mc != null) {
                byte[] array = mc.toByteArray();
                return defineClass(name, array, 0, array.length);
            }
        }
        return super.findClass(name);
    }
    private static final class MemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
        private final Map<String, Output> map = new HashMap<String, Output>();
        MemoryFileManager(JavaCompiler compiler) {
            super(compiler.getStandardFileManager(null, null, null));
        }
        @Override
        public Output getJavaFileForOutput(Location location, String name, Kind kind, FileObject source) {
            Output mc = this.map.get(name);
            if (mc == null) {
                mc = new Output(name);
                this.map.put(name, mc);
            }
            return mc;
        }
    }
    private static class MemoryFileObject extends SimpleJavaFileObject {
        MemoryFileObject(String name, Kind kind) {
            super(toURI(name, kind.extension), kind);
        }
        private static URI toURI(String name, String extension) {
            try {
                return new URI("mfm:
            }
            catch (URISyntaxException exception) {
                throw new Error(exception);
            }
        }
    }
    private static final class Output extends MemoryFileObject {
        private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Output(String name) {
            super(name, Kind.CLASS);
        }
        byte[] toByteArray() {
            return this.baos.toByteArray();
        }
        @Override
        public ByteArrayOutputStream openOutputStream() {
            this.baos.reset();
            return this.baos;
        }
    }
    public static final class Source extends MemoryFileObject {
        private final String content;
        Source(String name, String content) {
            super(name, Kind.SOURCE);
            this.content = content;
        }
        @Override
        public CharSequence getCharContent(boolean ignore) {
            return this.content;
        }
    }
}
