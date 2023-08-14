class InheritAgent0110 extends InheritAgent0110Super {
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent0110!");
    }
}
class InheritAgent0110Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0110Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
}
