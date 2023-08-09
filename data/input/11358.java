class BooleanTypeNode extends AbstractSimpleTypeNode {
    String docType() {
        return "boolean";
    }
    public void genJavaWrite(PrintWriter writer, int depth,
                             String writeLabel) {
        genJavaDebugWrite(writer, depth, writeLabel);
        indent(writer, depth);
        writer.println("ps.writeBoolean(" + writeLabel + ");");
    }
    String javaRead() {
        return "ps.readBoolean()";
    }
}
