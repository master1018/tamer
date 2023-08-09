class ValueTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "value";
    }
    String javaType() {
        return "ValueImpl";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeValue(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readValue()";
    }
}
