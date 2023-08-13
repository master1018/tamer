class InheritAgent1100 extends InheritAgent1100Super {
}
class InheritAgent1100Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent1100Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent1100Super!");
    }
}
