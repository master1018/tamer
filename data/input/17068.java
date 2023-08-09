public class RedefineMethodAddInvokeAgent {
    private static Instrumentation instrumentation;
    private RedefineMethodAddInvokeAgent() {
    }
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Hello from RedefineMethodAddInvokeAgent!");
        System.out.println("isRedefineClassesSupported()=" +
            inst.isRedefineClassesSupported());
        instrumentation = inst;
    }
    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }
}
