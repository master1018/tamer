class CantResolveArgs {
    void m() {
        new Runnable() {
            { unknown(); }
            public void run() { }
        };
    }
}
