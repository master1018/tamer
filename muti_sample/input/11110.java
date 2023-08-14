class InheritAgent1001 extends InheritAgent1001Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent1001!");
    }
}
class InheritAgent1001Super {
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent1001Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
}
