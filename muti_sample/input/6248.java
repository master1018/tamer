public class ManifestTestAgent {
    private static Instrumentation instrumentation;
    private ManifestTestAgent() {
    }
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("Hello from ManifestTestAgent!");
        System.out.println("isNativeMethodPrefixSupported()=" +
            inst.isNativeMethodPrefixSupported());
        System.out.println("isRedefineClassesSupported()=" +
            inst.isRedefineClassesSupported());
        System.out.println("isRetransformClassesSupported()=" +
            inst.isRetransformClassesSupported());
    }
}
