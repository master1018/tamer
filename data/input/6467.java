class InheritAgent0111 extends InheritAgent0111Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0111!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
    public static void premain (String agentArgs, Instrumentation instArg) {
        System.out.println("Hello from Double-Arg InheritAgent0111!");
    }
}
class InheritAgent0111Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0111Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
}
