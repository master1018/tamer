public class NullUndefinedVarTest {
        public static void main(String[] args) throws Exception {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine jsengine = Helper.getJsEngine(manager);
            if (jsengine == null) {
                System.out.println("Warning: No js engine found; test vacuously passes.");
                return;
            }
            jsengine.eval("var n = null; " +
                          "if (n !== null) throw 'expecting null';" +
                          "var u = undefined; " +
                          "if (u !== undefined) throw 'undefined expected';");
        }
}
