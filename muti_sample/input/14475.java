public class PluggableContextTest {
    public static void main(String[] args) throws Exception {
        ScriptEngineManager m = new ScriptEngineManager();
        ScriptContext ctx = new MyContext();
        ctx.setAttribute("x", "hello", MyContext.APP_SCOPE);
        ScriptEngine e = Helper.getJsEngine(m);
        if (e == null) {
            System.out.println("Warning: No js engine found; test vacuously passes.");
            return;
        }
        e.eval("x", ctx);
    }
}
