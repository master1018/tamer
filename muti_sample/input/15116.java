public class JavaScriptScopeTest {
        public static void main(String[] args) throws Exception {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine jsengine = Helper.getJsEngine(manager);
            if (jsengine == null) {
                System.out.println("Warning: No js engine found; test vacuously passes.");
                return;
            }
            jsengine.eval("var v = 'hello';");
            Bindings b = jsengine.createBindings();
            if (b.keySet().size() != 0) {
                throw new RuntimeException("no variables expected in new scope");
            }
            jsengine.put("fromJava", "hello world");
            jsengine.eval(" if (fromJava != 'hello world') throw 'unexpected'");
            if (! jsengine.get("v").equals("hello")) {
                throw new RuntimeException("unexpected value of 'v'");
            }
            if (! jsengine.get("fromJava").equals("hello world")) {
                throw new RuntimeException("unexpected value of 'fromJava'");
            }
        }
}
