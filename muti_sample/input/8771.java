public class NullScriptEngine implements ScriptEngineAbstraction {
    public boolean initialize(String jsHelperText) {
        return true;
    }
    public void execute(Diagram d, String code) {
    }
}
