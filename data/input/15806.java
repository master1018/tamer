public class ImplementationCacheTest {
    static class SourceFile extends SimpleJavaFileObject {
        final String source = "interface I { void m(); }\n" +
                              "class A implements I { public void m() {} }\n" +
                              "class B extends A { }\n";
        public SourceFile() {
            super(URI.create("test.java"), Kind.SOURCE);
        }
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return source;
        }
    }
    public static void main(String[] args) throws IOException {
        List<? extends JavaFileObject> files = Arrays.asList(new SourceFile());
        JavaCompiler tool = ToolProvider.getSystemJavaCompiler();
        JavacTask ct = (JavacTask)tool.getTask(null, null, null, null, null, files);
        Context ctx = new Context();
        JavacFileManager.preRegister(ctx);
        checkImplementationCache(ct.analyze(), Types.instance(ctx));
    }
    static void checkImplementationCache(Iterable<? extends Element> elements, Types types) {
        if (types == null) {
            throw new AssertionError("problems initializing Types");
        }
        Symbol a = null;
        Symbol b = null;
        Symbol i = null;
        for (Element e : elements) {
            if (e.getSimpleName().contentEquals("A")) {
                a = (Symbol)e;
            } else if (e.getSimpleName().contentEquals("B")) {
                b = (Symbol)e;
            } else if (e.getSimpleName().contentEquals("I")) {
                i = (Symbol)e;
            }
        }
        if (a == null || b == null || i == null) {
            throw new AssertionError("missing class");
        }
        MethodSymbol I_m = null;
        for (Symbol sym : i.members().getElements()) {
            if (sym.name.contentEquals("m")) {
                I_m = (MethodSymbol)sym;
            }
        }
        if (I_m == null) {
            throw new AssertionError("missing method m() in scope of interface I");
        }
        Symbol impl = I_m.implementation((TypeSymbol)b, types, true);
        if (impl == null || impl.owner != a) {
            throw new AssertionError("wrong implementation for m() in B");
        }
        b.members().enter(I_m.clone(b));
        Symbol newImpl = I_m.implementation((TypeSymbol)b, types, true);
        if (newImpl == impl) {
            throw new AssertionError("stale implementation for m() in B");
        }
        if (newImpl == null || newImpl.owner != b) {
            throw new AssertionError("wrong implementation for m() in B");
        }
    }
}
