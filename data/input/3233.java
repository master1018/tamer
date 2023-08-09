abstract class AbstractSimpleNode extends Node {
    AbstractSimpleNode() {
        kind = "-simple-";
        components = new ArrayList<Node>();
    }
    void document(PrintWriter writer) {
        writer.print(toString());
    }
}
