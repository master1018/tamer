class ConstantSetNode extends AbstractNamedNode {
    protected static final Map<String, String> constantMap = new HashMap<String, String>();
    void prune() {
        List<Node> addons = new ArrayList<Node>();
        for (Iterator it = components.iterator(); it.hasNext(); ) {
            Node node = (Node)it.next();
        }
        if (!addons.isEmpty()) {
            components.addAll(addons);
        }
        super.prune();
    }
    void constrainComponent(Context ctx, Node node) {
        if (node instanceof ConstantNode) {
            node.constrain(ctx);
            constantMap.put(name + "_" + ((ConstantNode) node).getName(), node.comment());
        } else {
            error("Expected 'Constant', got: " + node);
        }
    }
    void document(PrintWriter writer) {
        writer.println("<h4><a name=\"" + context.whereC + "\">" + name +
                       " Constants</a></h4>");
        writer.println(comment());
        writer.println("<dd><table border=1 cellpadding=3 cellspacing=0 width=\"90%\" summary=\"\"><tr>");
        writer.println("<th width=\"20%\"><th width=\"5%\"><th width=\"65%\">");
        ConstantNode n;
        for (Iterator it = components.iterator(); it.hasNext();) {
            n = ((ConstantNode)it.next());
            writer.println("<a NAME=\"" + name + "_" + n.name + "\"></a>");
            n.document(writer);
        }
        writer.println("</table>");
    }
    void documentIndex(PrintWriter writer) {
        writer.print("<li><a href=\"#" + context.whereC + "\">");
        writer.println(name() + "</a> Constants");
    }
    void genJavaClassSpecifics(PrintWriter writer, int depth) {
    }
    void genJava(PrintWriter writer, int depth) {
        genJavaClass(writer, depth);
    }
    public static String getConstant(String key){
        String com = constantMap.get(key);
        if(com == null){
            return "";
        } else {
            return com;
        }
    }
}
