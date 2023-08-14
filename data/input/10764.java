class CheckContext extends Context {
    public Vset vsBreak = Vset.DEAD_END;
    public Vset vsContinue = Vset.DEAD_END;
    public Vset vsTryExit = Vset.DEAD_END;
    CheckContext(Context ctx, Statement stat) {
        super(ctx, stat);
    }
}
