abstract class AbstractSimpleTypeNode extends AbstractTypeNode {
    void constrain(Context ctx) {
        context = ctx;
        nameNode.constrain(ctx);
        if (components.size() != 0) {
            error("Extraneous content: " + components.get(0));
        }
    }
}
