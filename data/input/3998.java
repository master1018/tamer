class InheritAgent1101 extends InheritAgent1101Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent1101!");
    }
}
class InheritAgent1101Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent1101Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent1101Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
}
