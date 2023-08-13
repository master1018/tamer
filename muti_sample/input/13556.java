class InheritAgent0011 extends InheritAgent0011Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0011!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent0011!");
    }
}
class InheritAgent0011Super {
}
