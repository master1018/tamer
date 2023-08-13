public class RedefineMethodAddInvokeApp {
    public static void main(String args[]) throws Exception {
        System.out.println("Hello from RedefineMethodAddInvokeApp!");
        new RedefineMethodAddInvokeApp().doTest();
        System.exit(0);
    }
    private void doTest() throws Exception {
        RedefineMethodAddInvokeTarget target =
            new RedefineMethodAddInvokeTarget();
        System.out.println("RedefineMethodAddInvokeApp: invoking myMethod()");
        target.test(0);  
        do_redefine(1);
        System.out.println("RedefineMethodAddInvokeApp: invoking myMethod1()");
        target.test(1);  
        do_redefine(2);
        System.out.println("RedefineMethodAddInvokeApp: invoking myMethod2()");
        target.test(2);  
    }
    private static void do_redefine(int counter) throws Exception {
        File f = new File("RedefineMethodAddInvokeTarget_" + counter +
            ".class");
        System.out.println("Reading test class from " + f);
        InputStream redefineStream = new FileInputStream(f);
        byte[] redefineBuffer = NamedBuffer.loadBufferFromStream(redefineStream);
        ClassDefinition redefineParamBlock = new ClassDefinition(
            RedefineMethodAddInvokeTarget.class, redefineBuffer);
        RedefineMethodAddInvokeAgent.getInstrumentation().redefineClasses(
            new ClassDefinition[] {redefineParamBlock});
    }
}
