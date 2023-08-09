public class DummyScriptEngine extends AbstractScriptEngine {
    public Object eval(String str, ScriptContext ctx) {
        return eval(new StringReader(str), ctx);
    }
    public Object eval(Reader reader, ScriptContext ctx) {
        System.out.println("eval done!");
        return null;
    }
    public ScriptEngineFactory getFactory() {
        return new DummyScriptEngineFactory();
    }
    public Bindings createBindings() {
        return new SimpleBindings();
    }
}
