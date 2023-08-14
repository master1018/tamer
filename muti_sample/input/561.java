class InheritAgent0101 extends InheritAgent0101Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0101!");
    }
}
class InheritAgent0101Super {
    public static void premain (String agentArgs) {
        System.out.println("Hello from Single-Arg InheritAgent0101Super!");
        throw new Error("ERROR: THIS AGENT SHOULD NOT HAVE BEEN CALLED.");
    }
}
