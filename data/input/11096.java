class EventNode extends ReplyNode {
    void constrain(Context ctx) {
        super.constrain(ctx.inEventSubcontext());
    }
}
