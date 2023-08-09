public class JavapFileManager extends JavacFileManager {
    private JavapFileManager(Context context, Charset charset) {
        super(context, true, charset);
        setIgnoreSymbolFile(true);
    }
    public static JavapFileManager create(final DiagnosticListener<? super JavaFileObject> dl, PrintWriter log) {
        Context javac_context = new Context();
        if (dl != null)
            javac_context.put(DiagnosticListener.class, dl);
        javac_context.put(com.sun.tools.javac.util.Log.outKey, log);
        return new JavapFileManager(javac_context, null);
    }
    void setIgnoreSymbolFile(boolean b) {
        ignoreSymbolFile = b;
    }
}
