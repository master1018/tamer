class InheritAgent1011 extends InheritAgent1011Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent1011!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent1011!");
    }
}
class InheritAgent1011Super {
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent1011Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
}
