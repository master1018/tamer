package repast.simphony.engine.watcher.query;

import repast.simphony.context.Context;

public class ASTAndExpression extends SimpleNode {

    public ASTAndExpression(int id) {
        super(id);
    }

    public ASTAndExpression(QueryParser p, int id) {
        super(p, id);
    }

    public IBooleanExpression buildExpression(Context context) {
        IBooleanExpression lhs = ((SimpleNode) this.children[0]).buildExpression(context);
        IBooleanExpression rhs = ((SimpleNode) this.children[1]).buildExpression(context);
        return new AndBooleanExpression(lhs, rhs);
    }
}
