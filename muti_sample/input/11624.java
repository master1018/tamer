class CommandNode extends AbstractCommandNode {
    void constrain(Context ctx) {
        if (components.size() == 3) {
            Node out   = components.get(0);
            Node reply = components.get(1);
            Node error = components.get(2);
            if (!(out instanceof OutNode)) {
                error("Expected 'Out' item, got: " + out);
            }
            if (!(reply instanceof ReplyNode)) {
                error("Expected 'Reply' item, got: " + reply);
            }
            if (!(error instanceof ErrorSetNode)) {
                error("Expected 'ErrorSet' item, got: " + error);
            }
        } else if (components.size() == 1) {
            Node evt = components.get(0);
            if (!(evt instanceof EventNode)) {
                error("Expected 'Event' item, got: " + evt);
            }
        } else {
            error("Command must have Out and Reply items or ErrorSet item");
        }
        super.constrain(ctx);
    }
    void genJavaClassSpecifics(PrintWriter writer, int depth) {
        indent(writer, depth);
        writer.println("static final int COMMAND = " +
                       nameNode.value() + ";");
    }
    void genJava(PrintWriter writer, int depth) {
        genJavaClass(writer, depth);
    }
}
