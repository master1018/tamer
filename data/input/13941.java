public class GetInterfaceTest {
    public static void main(String[] args) throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        if (engine == null) {
            System.out.println("Warning: No engine engine found; test vacuously passes.");
            return;
        }
        engine.eval("");
        Runnable runnable = ((Invocable)engine).getInterface(Runnable.class);
        if (runnable != null) {
            throw new RuntimeException("runnable is not null!");
        }
        engine.eval("function run() { println('this is run function'); }");
        runnable = ((Invocable)engine).getInterface(Runnable.class);
        runnable.run();
        engine.eval("function bar() { println('bar function'); }");
        Foo2 foo2 = ((Invocable)engine).getInterface(Foo2.class);
        if (foo2 != null) {
            throw new RuntimeException("foo2 is not null!");
        }
        engine.eval("function bar2() { println('bar2 function'); }");
        foo2 = ((Invocable)engine).getInterface(Foo2.class);
        foo2.bar();
        foo2.bar2();
    }
    interface Foo {
        public void bar();
    }
    interface Foo2 extends Foo {
        public void bar2();
    }
}
