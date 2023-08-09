class Unload2Targ {
    static void foo() {
        System.err.println("Unload2Targ ran");
    }
    static void classFinalize() {
        UnloadEventTarg.unloading2();
    }
}
