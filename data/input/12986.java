public class RhinoExceptionTest {
    private static final String ERROR_MSG = "error from JavaScript";
    public static void main(String[] args) throws Exception {
        ScriptEngineManager m = new ScriptEngineManager();
        ScriptEngine engine = Helper.getJsEngine(m);
        if (engine == null) {
            System.out.println("Warning: No js engine found; test vacuously passes.");
            return;
        }
        engine.put("msg", ERROR_MSG);
        try {
            engine.eval("throw new Error(msg);");
        } catch (ScriptException exp) {
            if (exp.getMessage().indexOf(ERROR_MSG) == -1) {
                throw exp;
            }
        }
        try {
            engine.eval("throw (msg);");
        } catch (ScriptException exp) {
            if (exp.getMessage().indexOf(ERROR_MSG) == -1) {
                throw exp;
            }
        }
        try {
            CompiledScript scr = ((Compilable)engine).compile("throw new Error(msg);");
            scr.eval();
        } catch (ScriptException exp) {
            if (exp.getMessage().indexOf(ERROR_MSG) == -1) {
                throw exp;
            }
        }
        try {
            CompiledScript scr = ((Compilable)engine).compile("throw msg;");
            scr.eval();
        } catch (ScriptException exp) {
            if (exp.getMessage().indexOf(ERROR_MSG) == -1) {
                throw exp;
            }
        }
    }
}
