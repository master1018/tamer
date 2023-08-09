public class DummyScriptEngineFactory implements ScriptEngineFactory {
    public String getEngineName() {
        return "dummy";
    }
    public String getEngineVersion() {
        return "-1.0";
    }
    public List<String> getExtensions() {
        return extensions;
    }
    public String getLanguageName() {
        return "dummy";
    }
    public String getLanguageVersion() {
        return "-1.0";
    }
    public String getMethodCallSyntax(String obj, String m, String... args) {
        StringBuffer buf = new StringBuffer();
        buf.append("call " + m + " ");
        buf.append(" on " + obj + " with ");
        for (int i = 0; i < args.length; i++) {
            buf.append(args[i] + ", ");
        }
        buf.append(";");
        return buf.toString();
    }
    public List<String> getMimeTypes() {
        return mimeTypes;
    }
    public List<String> getNames() {
        return names;
    }
    public String getOutputStatement(String str) {
        return "output " + str;
    }
    public String getParameter(String key) {
        if (key.equals(ScriptEngine.ENGINE)) {
            return getEngineName();
        } else if (key.equals(ScriptEngine.ENGINE_VERSION)) {
            return getEngineVersion();
        } else if (key.equals(ScriptEngine.NAME)) {
            return getEngineName();
        } else if (key.equals(ScriptEngine.LANGUAGE)) {
            return getLanguageName();
        } else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) {
            return getLanguageVersion();
        } else {
            return null;
        }
    }
    public String getProgram(String... statements) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < statements.length; i++) {
            buf.append(statements[i]);
        }
        return buf.toString();
    }
    public ScriptEngine getScriptEngine() {
        return new DummyScriptEngine();
    }
    private static List<String> names;
    private static List<String> extensions;
    private static List<String> mimeTypes;
    static {
        names = new ArrayList<String>(1);
        names.add("dummy");
        names = Collections.unmodifiableList(names);
        extensions = names;
        mimeTypes = new ArrayList<String>(0);
        mimeTypes = Collections.unmodifiableList(mimeTypes);
    }
}
