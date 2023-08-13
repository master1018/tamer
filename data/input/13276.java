public class JSDB extends Tool {
    public static void main(String[] args) {
        JSDB jsdb = new JSDB();
        jsdb.start(args);
        jsdb.stop();
    }
    public void run() {
        JSJavaScriptEngine engine = new JSJavaScriptEngine() {
                private ObjectReader objReader = new ObjectReader();
                private JSJavaFactory factory = new JSJavaFactoryImpl();
                public ObjectReader getObjectReader() {
                    return objReader;
                }
                public JSJavaFactory getJSJavaFactory() {
                    return factory;
                }
            };
        engine.startConsole();
    }
}
