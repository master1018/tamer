class CodeContext extends Context {
    Label breakLabel;
    Label contLabel;
    CodeContext(Context ctx, Node node) {
        super(ctx, node);
        switch (node.op) {
          case DO:
          case WHILE:
          case FOR:
          case FINALLY:
          case SYNCHRONIZED:
            this.breakLabel = new Label();
            this.contLabel = new Label();
            break;
          case SWITCH:
          case TRY:
          case INLINEMETHOD:
          case INLINENEWINSTANCE:
            this.breakLabel = new Label();
            break;
          default:
            if ((node instanceof Statement) && (((Statement)node).labels != null)) {
                this.breakLabel = new Label();
            }
        }
    }
}
