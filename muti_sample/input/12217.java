class Unload1Targ {
    static void foo() {
        System.err.println("Unload1Targ ran");
    }
    static void classFinalize() {
        UnloadEventTarg.unloading1();
    }
}
