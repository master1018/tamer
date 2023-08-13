public abstract class ScriptEngineFactoryBase implements ScriptEngineFactory {
    public String getName() {
        return (String)getParameter(ScriptEngine.NAME);
    }
    public String getEngineName() {
        return (String)getParameter(ScriptEngine.ENGINE);
    }
    public String getEngineVersion() {
        return (String)getParameter(ScriptEngine.ENGINE_VERSION);
    }
    public String getLanguageName() {
        return (String)getParameter(ScriptEngine.LANGUAGE);
    }
    public String getLanguageVersion() {
        return (String)getParameter(ScriptEngine.LANGUAGE_VERSION);
    }
}
